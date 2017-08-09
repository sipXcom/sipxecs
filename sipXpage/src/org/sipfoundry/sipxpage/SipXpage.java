/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxpage;

import java.net.InetSocketAddress;
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
import org.sipfoundry.sipxpage.Configuration.PageGroupConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

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
   private ApplicationContext context ;
   private MongoTemplate m_nodeDb;
   
   
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

      try {
          // Load nodeDb from Context
          m_nodeDb = (MongoTemplate) context.getBean("nodeDb");
          LOG.info("MongoDB connection object loaded. " + m_nodeDb);
          // Clear old busy states written by this server. They must be from an old instance
          clearBusyStatesFromServer();
      } catch (Exception e)
      {
          LOG.fatal("Cannot create MongoDB NodeDB connection", e) ;
          System.err.println(e.getMessage());
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
         
         pager.setContext(new ClassPathXmlApplicationContext(new String[] {
                 "classpath*:/sipxplugin.beans.xml"
             }));       
         
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
          setUserBusy(user, true);
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
                setUserBusy(p.getInbound().getRequestUri().getUser(), false);
                // End that page
                p.end() ;
                break ;
            }
         }
      }
      return true ;
   }
   
   private void setContext(ApplicationContext context)
   {
	   this.context = context;
   }
   
   private boolean isUserBusy(String user)
   {
	   // TODO Ask MongoDB if user state is busy
	   
	   
	   
	   return false;
   }
   
   private void setUserBusy(String user, boolean busy)
   {
	   // TODO Write to MongoDB, store state with user, own IP, busy state and time stamp for TTL
       if(busy)
       {
    	   //TODO Add entry to MongoDB
       }
       else
       {
    	   //TODO Erase entry from MongoDB
       }
   }
   
   private void clearBusyStatesFromServer()
   {
	   // TODO Remove all states from MongoDb with own IP (config.ipAddress)
	   LOG.info("Clear busy states in node DB from server with IP: " + config.ipAddress);
   }
}
