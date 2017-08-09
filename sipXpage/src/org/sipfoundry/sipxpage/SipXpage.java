/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxpage;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.sipfoundry.commons.log4j.SipFoundryLayout;
import org.sipfoundry.commons.siprouter.ProxyRouter;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;
import org.sipfoundry.sipxpage.Configuration.PageGroupConfig;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SipXpage implements LegListener
{
   static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxpage");

   private SipStack sipStack;
   private ListeningPoint udpListeningPoint;
   private ListeningPoint tcpListeningPoint;
   private SipProvider sipProvider;
   private LegSipListener legSipListener;

   /**
    * pageGroups holds the each PageGroup object that has been configured
    * user2Group maps sip user names from the inbound call to the PageGroup
    * config holds the configuration for all
    */
   private Configuration config ;
   private Vector<PageGroup> pageGroups = new Vector<PageGroup>();
   private HashMap<String, PageGroup>user2Group = new HashMap<String, PageGroup>() ;
   
   // MongoDB constants for paging
   private final String MONGO_IP = "server_ip";
   private final String MONGO_BUSY = "busy_state";
   private final String MONGO_PAGING_USER = "paging_user";
   private final String MONGO_COLLECTION_PAGING = "paging";
   private final String MONGO_EXPIRE_TIME = "expire_time";
   /**
    * Initialize everything.
    *
    * Load the configuration, start the stack
    */
   private void init()
   {
      // Load the configuration
      config = new Configuration() ;

      // Configure log4j
      String path = System.getProperty("conf.dir");
      PropertyConfigurator.configureAndWatch(path+"/sipxpage/log4j.properties", 
              SipFoundryLayout.LOG4J_MONITOR_FILE_DELAY);
      
      // Start the SIP stack
      SipFactory sipFactory = null;
      sipFactory = SipFactory.getInstance();
      sipFactory.setPathName("gov.nist");
      Properties properties = new Properties();

      properties.setProperty("javax.sip.STACK_NAME", "sipXpage");

      String logDir = System.getProperty("log.dir");
      properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
              logDir + "/sipxpage_debug.log");
      properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
              logDir + "/sipxpage_server.log");

      // Drop the client connection after we are done with the transaction.
      properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
            "false");
      // Set to 0 (or NONE) in your production code for max speed.
      // You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for debug + traces.
      // Your code will limp at 32 but it is best for debugging.
      properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", config.traceLevel);

      properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT", "true");
      properties.setProperty("gov.nist.javax.sip.LOG_FACTORY",
    		  "org.sipfoundry.commons.log4j.SipFoundryLogRecordFactory");


      // String proxy = config.ipAddress + ":" + "5060/TCP" ;
      // properties.setProperty("javax.sip.OUTBOUND_PROXY", proxy);

      properties.setProperty("javax.sip.ROUTER_PATH", ProxyRouter.class.getName());

	  if(getDbCollection() != null)
	  {
		  createIndexForPaging();
          // Clear old busy states written by this server. They must be from an old instance
          clearBusyStatesFromServer();
	  } else
	  {
          LOG.fatal("Cannot create MongoDB connection") ;
          System.exit(1); 
	  }

	  try {
         // Create SipStack object
         sipStack = sipFactory.createSipStack(properties);
         System.out.println("createSipStack " + sipStack);

      } catch (PeerUnavailableException e) {
         // could not find
         // gov.nist.jain.protocol.ip.sip.SipStackImpl
         // in the classpath
         LOG.fatal("Cannot create sipStack", e) ;
         System.err.println(e.getMessage());
         System.exit(1);
      }

      try {
         udpListeningPoint = sipStack.createListeningPoint(config.ipAddress, config.udpSipPort, "udp");
         tcpListeningPoint = sipStack.createListeningPoint(config.ipAddress, config.tcpSipPort, "tcp");
         sipProvider = sipStack.createSipProvider(udpListeningPoint);
         sipProvider.addListeningPoint(tcpListeningPoint) ;

         legSipListener= new LegSipListener() ;
         legSipListener.init(sipFactory, sipProvider, this);
      } catch (Exception e) {
         LOG.fatal("Cannot create ListeningPoint", e) ;
         System.err.println(e.getMessage());
         System.exit(1);
      }

      // Start the timers
      Timers.start(100) ;

      // Create the Page Groups
      int rtpPort = config.startingRtpPort ;
      String pageGroupDescription = "unknown" ;
      String user = "unknown" ;
      try
      {
         for (PageGroupConfig pc : config.pageGroups)
         {
            user = pc.user ;
            pageGroupDescription = pc.description ;
            PageGroup p ;
            LOG.debug(String.format("Page Group %s (%s)", user, pageGroupDescription)) ;
            p = new PageGroup(legSipListener, udpListeningPoint.getIPAddress(), rtpPort) ;
            if (pc.beep == null)
            {
               throw new Exception("beep for Page Group "+user+" is missing.") ;
            }
            LOG.debug(String.format("Page Group %s adding beep %s",
                  user, pc.beep)) ;
            p.setBeep(pc.beep) ;
            LOG.debug(String.format("Page Group %s adding timeout %d",
                  user, pc.maximumDuration));
            p.setMaximumDuration(pc.maximumDuration);
            user2Group.put(user, p) ;
            for (String dest : pc.urls.split(","))
            {
               LOG.debug(String.format("Page Group %s adding destination %s",
                     user, dest)) ;
               p.addDestination(dest) ;
            }
            pageGroups.add(p) ;
            rtpPort += 4 ;
         }

      } catch (Exception e)
      {
         LOG.fatal(String.format("Cannot create PageGroup  %s (%s)",
               user, pageGroupDescription), e) ;
         e.printStackTrace() ;
         System.exit(1);
      }



      LOG.info(String.format("sipXpage listening on %s:%04d/UDP %04d/TCP",
         config.ipAddress, config.udpSipPort, config.tcpSipPort)) ;
   }

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      try
      {
         SipXpage pager = new SipXpage() ;
         pager.init() ;
      } catch (Exception e)
      {
         LOG.fatal(e) ;
         e.printStackTrace() ;
         System.exit(1) ;
      }
      catch (Throwable t)
      {
         LOG.fatal(t) ;
         t.printStackTrace() ;
         System.exit(1) ;
      }

   }

   /**
    * When an incoming call arrives, find the appropriate PageGroup
    * (based on the user name that was called), and page that group.
    *
    * If the user name isn't found, or that group is currently involved
    * in a page already, reject the call.
    *
    * @param event The event that describes the incoming call.
    */
   private void page(LegEvent event)
   {
      Leg leg = event.getLeg() ;
      InetSocketAddress sdpAddress = event.getSdpAddress() ;
      String alertInfoKey = leg.getRequestUri().getParameter("Alert-info") ;
      String user = leg.getRequestUri().getUser() ;
      PageGroup pageGroup = null ;


      pageGroup = user2Group.get(user) ;
      LOG.info("SipXpage::page user="+user) ;

      if (pageGroup == null)
      {
         LOG.info("Page group "+user+" not provisioned.") ;
         try
         {
            event.getLeg().destroyLeg() ;
         } catch (Exception e)
         {
            LOG.error("SipXpage::page", e) ;
         }
         return ;
      }
      
      if (isUserBusy(user) == true || pageGroup.isBusy() == true ||
          pageGroup.page(leg, sdpAddress, alertInfoKey) == false)
      {
         // Already have an inbound call for that page group.  Return busy here response.
         try
         {
            event.getLeg().destroyLeg() ;
         } catch (Exception e)
         {
            LOG.error("SipXpage::page", e) ;
         }
      } else
      {
          setUserBusy(user, true, pageGroup.maximumDuration);
      }
   }

   /**
    * As this is the LegListener that gets invoked for incoming calls,
    * dispatch the invite and bye events.
    */
   public boolean onEvent(LegEvent event)
   {
      LOG.debug("SipXpage.onEvent got event "+ event.getDescription()) ;
      if (event.getDescription() == "invite")
      {
         page(event) ;
      }
      else if (event.getDescription().startsWith("dialog bye"))
      {
         // Find the pagegroup that goes with the inbound leg
         // that just hung up.
         for (PageGroup p : pageGroups)
         {
            if (p.getInbound() == event.getLeg())
            {
                setUserBusy(p.getInbound().getRequestUri().getUser(), false, 0);
                // End that page
                p.end() ;
                break ;
            }
         }
      }
      return true ;
   }
   
   private boolean isUserBusy(String user)
   {
	   DBCollection pagingCollection = getDbCollection();
	   BasicDBObject query = new BasicDBObject();
	   query.append(MONGO_PAGING_USER, user);
	   DBObject dbo = pagingCollection.findOne(query);
	   
	   if(dbo != null)
	   {
		   return ((BasicDBObject)dbo).getBoolean(MONGO_BUSY, false);
	   } else 
	   {
		   return false;
	   }
   }
   
   private void setUserBusy(String user, boolean busy, int timeout)
   {
	   DBCollection pagingCollection = getDbCollection();
	   BasicDBObject query = new BasicDBObject();
	   if(busy)
       {
		   query.append(MONGO_IP, config.ipAddress);
		   query.append(MONGO_PAGING_USER, user);
		   query.append(MONGO_BUSY, busy);
		   query.append(MONGO_EXPIRE_TIME, new Date(System.currentTimeMillis() + timeout));
		   pagingCollection.insert(query);
       }
       else
       {
		   query.append(MONGO_IP, config.ipAddress);
		   query.append(MONGO_PAGING_USER, user);
		   pagingCollection.remove(query);
       }
   }
   
   private void clearBusyStatesFromServer()
   {
	   if(config != null)
	   {
	       LOG.info("Clear busy states in node DB from server with IP: " + config.ipAddress);
		   DBCollection pagingCollection = getDbCollection();
		   BasicDBObject query = new BasicDBObject();
		   query.append(MONGO_IP, config.ipAddress);
		   pagingCollection.remove(query);
	   } else
	   {
		   LOG.fatal("Could not clear busy states from server. IP is unknown.");   
	   }
   }
   
   private DBCollection getDbCollection()
   {
	   DB imDb = UnfortunateLackOfSpringSupportFactory.getImdb();
	   if(imDb != null)
	   {
		   return imDb.getCollection(MONGO_COLLECTION_PAGING);
	   } else
	   {
		   return null;
	   }
   }
   
   private void createIndexForPaging()
   {
	   DBCollection pagingCollection = getDbCollection();
	   if(pagingCollection != null)
	   {
	       DBCollection indexesCollection = UnfortunateLackOfSpringSupportFactory.getImdb().getCollection("system.indexes");

	       // Add index for state TTL
	       BasicDBObject dateField = new BasicDBObject().append(MONGO_EXPIRE_TIME, 1);
	       BasicDBObject deleteObj = new BasicDBObject().append("expireAfterSeconds", 1);
	   
	       BasicDBObject query = new BasicDBObject();
	       query.append("key", dateField);
	       query.append("ns", "imdb" + "." + MONGO_COLLECTION_PAGING);
	       query.append("expireAfterSeconds", 1);
	   
	       // Check if index already exist. If not create it
	       DBCursor cursor = indexesCollection.find(query).limit(1);
	       if(!cursor.hasNext())
	       {
	    	   // TTL could exist with different expire time. Drop to ensure correct time
	    	   pagingCollection.dropIndex(dateField);
	    	   pagingCollection.createIndex(dateField, deleteObj);
	       }
	   }
   }
}
