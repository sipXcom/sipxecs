/**
 *
 *
 * Copyright (c) 2015 sipXcom, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxcallback;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.sipfoundry.commons.log4j.SipFoundryLayout;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class SipXcallbackServer {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");

    protected abstract CallbackCallHandler getCallbackCallHandler();
    private int m_eventSocketPort;

    public void runServer() {
        LOG.info("Starting SipXcallback listening on port " + m_eventSocketPort);
        try {
            ServerSocket serverSocket = new ServerSocket(m_eventSocketPort);
            LOG.info("SipXcallback::run Starting SipXcallback thread with client " + serverSocket);
            for (;;) {
                Socket client = serverSocket.accept();
                CallbackCallHandler sipxCalback = getCallbackCallHandler();
                sipxCalback.setClient(client);
                Thread thread = new Thread(sipxCalback);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("FAILED TO START Callback server" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setEventSocketPort(int port) {
        m_eventSocketPort = port;
    }

    /**
     * Main entry point for sipXcallback
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            initSystemProperties();
            ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
                "classpath:/org/sipfoundry/sipxcallback/system.beans.xml",
                "classpath:/org/sipfoundry/sipxcallback/imdb.beans.xml",
            });
            SipXcallbackServer socket = (SipXcallbackServer) context.getBean("sipxCallbackServer");
            socket.runServer();
        } catch (BeansException ex) {
            System.out.println("FAILED TO CREATE SPRING CONTAINER" + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void initSystemProperties() {
        String path = System.getProperty("conf.dir");
        // Configure log4j
        PropertyConfigurator.configureAndWatch(path+"/sipxcallback/log4j.properties", 
                SipFoundryLayout.LOG4J_MONITOR_FILE_DELAY);
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
