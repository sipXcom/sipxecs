/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxpage;

import gov.nist.javax.sip.address.SipUri;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import javax.sdp.SessionDescription;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;

/**
 * A PageGroup is the object that, given an inbound call,
 * generates all the outbound calls to the phones and manages
 * the lifetimes of the calls and the RTP forking.
 *
 * @author Woof!
 *
 */
public class PageGroup implements LegListener
{
   static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxpage");
   LegSipListener legSipListener = null ;
   PacketSocket rtpSocket = null ;
   PacketSocket tossSocket = null ;
   String ipAddress ;
   int rtpPort ;
   int tossPort ;
   RtpFork rtpFork = null ;
   Vector<String> destinations ;
   Vector<Leg> outbounds ;
   Leg inbound = null ;
   InetSocketAddress inboundRtp = null ;
   URL beep ;
   String user = "";
   int maximumDuration = 60000;

   PageGroup(LegSipListener legSipListener, String ipAddress, int rtpPort) throws SocketException
   {
      this.legSipListener = legSipListener ;
      this.ipAddress = ipAddress ;
      this.rtpPort = rtpPort ;
      this.tossPort = rtpPort+2 ;

      rtpSocket = new PacketSocket(rtpPort);
      tossSocket = new PacketSocket(tossPort);
      rtpFork = new RtpFork(rtpSocket, 20, this) ;

      resetDestinations() ;

      outbounds = new Vector<Leg>() ;
   }

   public void resetDestinations()
   {
      destinations = new Vector<String>() ;
   }

   /**
    * Add a phone to be paged to the group.
    *
    * @param device The SIP URL of the phone.
    */
   public void addDestination(String device)
   {
      destinations.add(device) ;
   }

   /**
    * Set the beep to be used when this group is paged.
    *
    * @param beep A file: URL (or just a rooted pathname)
    * of an audio file to be used for the beep.
    */
   public void setBeep(String beep)
   {
      try
      {
         if (beep.startsWith("/"))
         {
            beep = "file:///" + beep ; // Add file protocol
         }
         this.beep = new URL(beep) ;
      } catch (MalformedURLException e)
      {
         LOG.warn("PageGroup::setBeep failed", e) ;
      }
   }

   /**
    * Set the timeout to be used when this group is paged.
    *
   * @param timeout The page timeout in mS
    */
   public void setMaximumDuration(int timeout)
   {
       this.maximumDuration = timeout;
   }
   
   /**
    * Set the user for this group.
    *
   * @param user The page group user
    */
   public void setUser(String user)
   {
       this.user = user;
   }

   /**
    *
    * @return true if this PageGroup is currently involved
    * in a page.
    */
   public boolean isBusy()
   {
	   return isUserBusy(user);
   }

   /**
    *
    * @return the inbound call this page is using
    */
   public Leg getInbound()
   {
      return inbound ;
   }

   /**
    * Trigger a page of all the destinations.
    *
    * @param inbound  The inbound Leg (audio from here goes to all destinations)
    * @param inboundRtp The destination to send RTP packets so the inbound caller can hear.
    * @param alertInfoKey The magic key needed for Polycom Auto-Answer
    * @return
    */
   public boolean page(Leg inbound, InetSocketAddress inboundRtp, String alertInfoKey)
   {	
      if (isBusy() == false)
      {
         LOG.debug("PageGroup::page starting") ;
         this.inbound = inbound ;
         this.inboundRtp = inboundRtp ;
         setUserBusy(user, true, maximumDuration);
         
         // Spin up the RTP forker
         rtpFork.start() ;
         try
         {
            // Get the originator for the Page.
            InboundLeg origLeg = (InboundLeg) inbound;
            String pageOriginatorAddress;
            pageOriginatorAddress = origLeg.getAddress();

            // Answer the inbound call.
            inbound.acceptCall(rtpPort) ;

            // Start the timers
            if (maximumDuration > 0)
            {
               // Start the maximumDuration timer if it is greater than 0
               Timers.addTimer("maximum_duration", maximumDuration, this) ; // End this page after this many mS
            }
            Timers.addTimer("beep_start", 1000, this) ; // Start the beep after this much time

            // Place a call to each destination
            for (String destination : destinations)
            {
               // Compare the originator with the destination.  Only place an outbound call
               // if they aren't the same.  We don't make a call to the same destination that
               // is initiating the page.
               if (destination.compareToIgnoreCase(pageOriginatorAddress) != 0)
               {
                  Leg outbound = placeCall(inbound.getDisplayName(), origLeg.getCallId(), destination, alertInfoKey) ;
                  if (outbound != null)
                  {
                     // Keep track of them!
                     outbounds.add(outbound) ;
                  }
               }
               else
               {
                   LOG.info(String.format("Skipping %s as it is the page originator.", pageOriginatorAddress));
               }
            }
            return true ;
         } catch (Throwable t)
         {
            LOG.warn("PageGroup::page", t) ;
            end() ;
         }
      }
      LOG.debug("PageGroup::page failed") ;
      return false ;
   }

   /**
    * End this page, by clearing up all timers, outbound calls, the inbound call,
    * and the RTP forker.
    *
    * synchronized so multiple calls are serialized.  Only the first should
    * be needed.
    */
   public synchronized void end()
   {
      // Remove all timers associated with me
      Timers.removeTimer(this) ;

      // Hangup on all outbound calls.
      for(Leg outbound : outbounds)
      {
         try
         {
            outbound.destroyLeg() ;
         } catch (Exception e)
         {
            LOG.error("PageGroup::end outbound", e) ;
         }
      }
      outbounds.removeAllElements() ;

      // Stop the RTP processing.
      rtpFork.stop() ;
      rtpFork.removeAllDestinations() ;

      // Hangup on the inbound call
      if (inbound != null)
      {
         try
         {
            inbound.destroyLeg() ;
         } catch (Exception e)
         {

            LOG.error("PageGroup::end inbound", e) ;
         }
         inbound = null ;
      }
      setUserBusy(user, false, 0);
   }

   /**
    * Place an outbound call
    *
    * @param fromName  The name to display as "from"
    * @param fromCallId  The call Id from the page originator call.
    * @param destination The user@host to call
    * @param alertInfoKey The magic key needed for Polycom Auto-Answer
    * @return
    */
   Leg placeCall(String fromName, String fromCallId, String destination, String alertInfoKey)
   {
      LOG.debug(String.format("PageGroup::placeCall(%s, %s)", fromName, destination));
      OutboundLeg oLeg = null ;
      try {
         oLeg = new OutboundLeg(legSipListener, this) ;
         SipURI toAddress = new SipUri();
         String dogs[] = destination.split("@") ;
         String user = dogs[0] ;
         String host = dogs[1] ;
         toAddress.setUser(user);
         toAddress.setHost(host);

         SessionDescription sdp = legSipListener.buildSdp(new InetSocketAddress(ipAddress, tossPort), false) ;

         oLeg.createLeg(toAddress, "Page from "+fromName, fromCallId, sdp, alertInfoKey);

      } catch (Throwable t) {
         LOG.warn(String.format("PageGroup::placeCall Problem calling %s", destination), t) ;
         try
         {
            oLeg.destroyLeg() ;
         } catch (Exception e1){}
         oLeg = null ;
      }

      return oLeg ;
   }

   /**
    * The event handler for this group's timers, RtpFork, and calls.
    */
   public boolean onEvent(LegEvent event)
   {
      LOG.debug("PageGroup::onEvent got event "+ event.getDescription()) ;

      if (event.getDescription().equals("timer: status=fired name=maximum_duration"))
      {
         // Maximum duration timer fired, end the page.
         end() ;
      }
      else if (event.getDescription().equals("timer: status=fired name=beep_start"))
      {
         if (beep != null)
         {
            // Start the beep (if any)
            // Add the inbound caller to the RTP mix so he can hear the beep
            rtpFork.addDestination(inboundRtp) ;
            rtpFork.startLocalAudio(beep) ;
         }
      }
      else if (event.getDescription().equals("localAudio end"))
      {
         // Now that the beep is done, remove the inbound caller to the RTP mix
         // So he does not hear himself
         rtpFork.removeDestination(inboundRtp) ;
      }
      else if (event.getDescription().startsWith("dialog bye"))
      {
         // Someone hung up.
         Leg leg = event.getLeg() ;
         if (leg == inbound)
         {
            // Inbound call ended.  End the page.
            end() ;
         }
         else
         {
            // Outbound call ended.  Stop sending rtp to it
            rtpFork.removeDestination(leg.getRemoteRtpAddress()) ;
         }
      }
      else if (event.getDescription().equals("sdp"))
      {
         // SDP changed, keep rtpFork informed.
         rtpFork.removeDestination(event.getLeg().getPreviousRtpAddress()) ;
         rtpFork.addDestination(event.getLeg().getRemoteRtpAddress()) ;
      }


      return true ;
   }
   
   private DBCollection getDbCollection()
   {
	   DB imDb = UnfortunateLackOfSpringSupportFactory.getImdb();
	   LOG.debug("PageGroup::MongoDebug::getDbCollection::imDb " + imDb);
	   if(imDb != null)
	   {
		   return imDb.getCollection(SipXpage.MONGO_COLLECTION_PAGING);
	   } else
	   {
		   return null;
	   }
   }
   
   private boolean isUserBusy(String user)
   {
	   DBCollection pagingCollection = getDbCollection();
	   BasicDBObject query = new BasicDBObject();
	   query.append(SipXpage.MONGO_PAGING_USER, user);
	   DBCursor cursor = pagingCollection.find(query);
	   
	   LOG.debug("PageGroup::MongoDebug::isUserBusy::cursor " + cursor);
	   if(cursor.hasNext())
	   {
		   BasicDBObject dbo = (BasicDBObject)cursor.next();
		   boolean busyState = ((BasicDBObject)dbo).getBoolean(SipXpage.MONGO_BUSY, false);
		   LOG.debug("PageGroup::MongoDebug::isUserBusy::busyState " + busyState);
		   return busyState;
	   } else 
	   {
		   LOG.debug("PageGroup::MongoDebug::isUserBusy: No cursor, busy is false");
		   return false;
	   }
   }
   
   private void setUserBusy(String user, boolean busy, int timeout)
   {
	   DBCollection pagingCollection = getDbCollection();
	   BasicDBObject query = new BasicDBObject();
	   LOG.debug("PageGroup::MongoDebug::setUserBusy::user " + user);
	   LOG.debug("PageGroup::MongoDebug::setUserBusy::busy " + busy);
	   WriteResult result = null;
	   if(busy)
       {
		   query.append(SipXpage.MONGO_IP, ipAddress);
		   query.append(SipXpage.MONGO_PAGING_USER, user);
		   query.append(SipXpage.MONGO_BUSY, busy);
		   query.append(SipXpage.MONGO_EXPIRE_TIME, new Date(System.currentTimeMillis() + timeout));
		   result = pagingCollection.insert(query);
       }
       else
       {
		   query.append(SipXpage.MONGO_PAGING_USER, user);
		   result = pagingCollection.remove(query);
       }
	   if(result != null)
	   {
		   LOG.debug("PageGroup::MongoDebug::setUserBusy::result " + result.toString());
	   } else
	   {
		   LOG.debug("PageGroup::MongoDebug::setUserBusy: No result found");
	   }
   }
}
