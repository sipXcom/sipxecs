/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipcallwatcher;

import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.SecureAccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.SipListener;

import org.apache.log4j.Appender;
import org.sipfoundry.commons.jainsip.AbstractSipStackBean;
import org.sipfoundry.commons.jainsip.ListeningPointAddress;
import org.sipfoundry.openfire.plugin.presence.SipXOpenfirePlugin;

public class SipStackBean extends AbstractSipStackBean implements AccountManager {
    
    Collection<ListeningPointAddress> lpaSet = new HashSet<ListeningPointAddress>();
    private Subscriber subscriber;
    private ListeningPointAddressImpl tcpListeningPointAddress;
    
    public SipStackBean() {
        super();
        
    }
    
    public Subscriber getSubscriber() {      
        this.subscriber.setProvider(tcpListeningPointAddress.getSipProvider());
        return subscriber;
        
    }
    
   
    @Override
    public Collection<ListeningPointAddress> getListeningPointAddresses() {   
        this.tcpListeningPointAddress = new ListeningPointAddressImpl("tcp");
        lpaSet.add(this.tcpListeningPointAddress);
        return lpaSet;
    }

    @Override
    public String getLogLevel() {
        return CallWatcher.getConfig().getLogLevel();
    }

    @Override
    public SipListener getSipListener(AbstractSipStackBean abstactSipStackBean) {
         if ( this.subscriber != null ) return this.subscriber ;
         else {
             this.subscriber = new Subscriber(this);
          
             return this.subscriber;
         }
    }
    
    

    @Override
    public Appender getStackAppender() {
        return SipXOpenfirePlugin.getLogAppender();
    }

    @Override
    public String getStackName() {
        return "sipxcallwatcher";
    }

    @Override
    public AccountManager getPlainTextPasswordAccountManager() {      
        return this;
    }

    /**
     * Special users are not in the hashed password database.
     */
    @Override
    public SecureAccountManager getHashedPasswordAccountManager() {
        return null;
    }

    @Override
    public UserCredentials getCredentials(ClientTransaction ctx, String authRealm) {
        return new UserCredentials() {
            public String getPassword() {
                return CallWatcher.getConfig().getPassword();
            }

            public String getSipDomain() {
                return CallWatcher.getConfig().getProxyDomain();
            }

            public String getUserName() {
                return CallWatcher.getConfig().getUserName();
            }
        };
    }

    @Override
    public Properties getExtraStackProperties() {
        Properties extraProperties = new Properties();
        /* Enable DELIVER_UNSOLICITED_NOTIFY which will prevent nested acquisition
         * of Server and Client Transaction semaphores that can lead to 
         * initialization problems when system is loaded.  Enabling this 
         * option is harmless in our case because we know the other end is a
         * well-behaved Subscribe client (sipXrls)
         */
        extraProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true"); 
        return extraProperties;
    }

}
