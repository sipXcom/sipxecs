/*
 *
 *
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxivr;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.sipfoundry.commons.log4j.SipFoundryLayout;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class SipXivrServer {
    private int m_eventSocketPort;
    private String m_logLevel;
    private String m_logFile;

    protected abstract SipXivr getSipxIvrHandler();

    public void init() {
        // Configure log4j
        Properties props = new Properties();
        props.setProperty("log4j.rootLogger", "warn, file");
        props.setProperty("log4j.logger.org.sipfoundry.sipxivr",
                SipFoundryLayout.mapSipFoundry2log4j(m_logLevel).toString());
        //props.setProperty("log4j.logger.org.springframework", "debug");
        // props.setProperty("log4j.logger.org.mortbay", "debug");
        props.setProperty("log4j.appender.file", "org.sipfoundry.commons.log4j.SipFoundryAppender");
        props.setProperty("log4j.appender.file.File", m_logFile);
        props.setProperty("log4j.appender.file.layout", "org.sipfoundry.commons.log4j.SipFoundryLayout");
        props.setProperty("log4j.appender.file.layout.facility", "sipXivr");
        PropertyConfigurator.configure(props);
    }

    public void runServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(m_eventSocketPort);
            for (;;) {
                Socket client = serverSocket.accept();
                SipXivr sipxIvr = getSipxIvrHandler();
                sipxIvr.setClient(client);
                Thread thread = new Thread(sipxIvr);
                thread.start();
            }
        } catch (IOException ex) {
            System.out.println("FAILED TO START IVR SERVER" + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public void setEventSocketPort(int port) {
        m_eventSocketPort = port;
    }

    public void setLogLevel(String logLevel) {
        m_logLevel = logLevel;
    }

    public void setLogFile(String logFile) {
        m_logFile = logFile;
    }

    /**
     * Main entry point for sipXivr
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            initSystemProperties();
            ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
                "classpath:/org/sipfoundry/sipxivr/system.beans.xml",
                "classpath:/org/sipfoundry/sipxivr/imdb.beans.xml",
                "classpath:/org/sipfoundry/sipxivr/email/email.beans.xml",
                "classpath:/org/sipfoundry/attendant/attendant.beans.xml",
                "classpath:/org/sipfoundry/bridge/bridge.beans.xml",
                "classpath:/org/sipfoundry/faxrx/fax.beans.xml",
                "classpath:/org/sipfoundry/moh/moh.beans.xml",
                "classpath:/org/sipfoundry/voicemail/voicemail.beans.xml",
                "classpath:/org/sipfoundry/sipxivr/rest/rest.beans.xml",
                "classpath:/org/sipfoundry/voicemail/mailbox/mailbox.beans.xml",
                "classpath*:/sipxivrplugin.beans.xml"
            });
            SipXivrServer socket = (SipXivrServer) context.getBean("sipxIvrServer");
            socket.runServer();
        } catch (BeansException ex) {
            System.out.println("FAILED TO CREATE SPRING CONTAINER" + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void initSystemProperties() {
        String path = System.getProperty("conf.dir");
        if (path == null) {
            System.err.println("Cannot get System Property conf.dir!  Check jvm argument -Dconf.dir=") ;
            System.exit(1);
        }
        
        // Setup SSL properties so we can talk to HTTPS servers
        String keyStore = System.getProperty("javax.net.ssl.keyStore");
        if (keyStore == null) {
            // Take an educated guess as to where it should be
            keyStore = path+"/ssl/ssl.keystore";
            System.setProperty("javax.net.ssl.keyStore", keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", "changeit"); // Real security!
        }
        String trustStore = System.getProperty("javax.net.ssl.trustStore");
        if (trustStore == null) {
            // Take an educated guess as to where it should be
            trustStore = path+"/ssl/authorities.jks";
            System.setProperty("javax.net.ssl.trustStore", trustStore);
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
            System.setProperty("javax.net.ssl.trustStorePassword", "changeit"); // Real security!
        }
    }
}
