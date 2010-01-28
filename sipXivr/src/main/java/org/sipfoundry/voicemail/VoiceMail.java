/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.voicemail;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.sipfoundry.callpilot.CpRetrieve;
import org.sipfoundry.commons.freeswitch.DisconnectException;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocketInterface;
import org.sipfoundry.commons.freeswitch.Hangup;
import org.sipfoundry.commons.freeswitch.Localization;
import org.sipfoundry.commons.freeswitch.Play;
import org.sipfoundry.commons.freeswitch.PromptList;
import org.sipfoundry.commons.freeswitch.Record;
import org.sipfoundry.commons.freeswitch.Sleep;
import org.sipfoundry.commons.freeswitch.Transfer;
import org.sipfoundry.commons.userdb.DistributionList;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.commons.userdb.ValidUsersXML;
import org.sipfoundry.sipxivr.PhonePresence;
import org.sipfoundry.sipxivr.IvrChoice;
import org.sipfoundry.sipxivr.IvrConfiguration;
import org.sipfoundry.sipxivr.Mailbox;
import org.sipfoundry.sipxivr.Menu;
import org.sipfoundry.sipxivr.PersonalAttendant;


public class VoiceMail {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");

    // Global store for VoiceMail resource bundles keyed by locale
    private static final String RESOURCE_NAME = "org.sipfoundry.voicemail.VoiceMail";
    private static final String CPUI_RESOURCE_NAME = "org.sipfoundry.callpilot.CallPilot";
    private static HashMap<Locale, ResourceBundle> s_resourcesByLocale = new HashMap<Locale, ResourceBundle>();
    private static HashMap<Locale, ResourceBundle> s_cpui_resourcesByLocale = new HashMap<Locale, ResourceBundle>();
    
    private org.sipfoundry.sipxivr.IvrConfiguration m_ivrConfig;
    private Localization m_locStd;
    private Localization m_locCpui;
    private FreeSwitchEventSocketInterface m_fses;
    private ResourceBundle m_vmBundle;
    private Configuration m_config;
    private ValidUsersXML m_validUsers;
    private HashMap<String, DistributionList> m_sysDistLists;

    private Hashtable<String, String> m_parameters;  // The parameters from the sip URI
    
    private String m_action; // "deposit" or "retrieve"

    private Mailbox m_mailbox ;
    
    enum NextAction {
        repeat, exit, nextAttendant;
    }

    /**
     * Create a VoiceMail object.
     * 
     * @param ivrConfig top level configuration stuff
     * @param fses The FreeSwitchEventSocket with the call already answered
     * @param parameters The parameters from the sip URI (to determine locale and which action
     *        to run)
     */
    public VoiceMail(IvrConfiguration ivrConfig, FreeSwitchEventSocketInterface fses,
            Hashtable<String, String> parameters) {
        this.m_ivrConfig = ivrConfig;
        this.m_fses = fses;
        this.m_parameters = parameters;
        
        // Look for "locale" parameter
        String localeString = m_parameters.get("locale");
        if (localeString == null) {
            // Okay, try "lang" instead
            localeString = m_parameters.get("lang");
        }
        
        m_locStd = new Localization(RESOURCE_NAME, localeString, 
                   s_resourcesByLocale, m_ivrConfig, m_fses);
        m_locCpui = new Localization(CPUI_RESOURCE_NAME, localeString, 
                    s_cpui_resourcesByLocale, m_ivrConfig, m_fses);
    }

    /**
     * Load all the needed configuration.
     * 
     * The attendantBundle with the resources is located based on locale, as is the TextToPrompts
     * class. The attendant configuration files are loaded (if they changed since last time), and
     * the ValidUsers (also if they changed).
     * 
     */
    void loadConfig() {

        // Load the Voice Mail configuration
        m_config = Configuration.update(true);

        // Update the valid users list
        try {
            m_validUsers = ValidUsersXML.update(LOG, true);
        } catch (Exception e) {
            System.exit(1); // If you can't trust validUsers, who can you trust?        
        }
    }

    /**
     * Given an extension, convert to a full URL in this domain
     * 
     * @param extension
     * @return the Url
     */
    public String extensionToUrl(String extension) {
        return "sip:" + extension + "@" + m_ivrConfig.getSipxchangeDomainName();
    }
    
    /**
     * Run Voice Mail for each mailbox until there is nothing left to do.
     *
     * Keep running the next returned mailbox until there are none left, then exit.
     * 
     */
    public void run() throws Throwable {
        if (m_vmBundle == null) {
            loadConfig();
        }

        // Wait a bit so audio doesn't start too fast
        Sleep s = new Sleep(m_fses, 1000);
        s.go();
        
        // if division header origCalledNumber corresponds to a mailbox
        // then use it
        String mailboxString = m_parameters.get("origCalledNumber");
        if(mailboxString != null) {
            // validate
            if(m_validUsers.getUser(mailboxString) == null) {
                mailboxString = null;
            }
        }        
        
        if(mailboxString == null) {
            mailboxString = m_parameters.get("mailbox");
        }
        
        if (mailboxString == null) {
            // Use the From: user as the mailbox
            mailboxString = m_fses.getFromUser();
        }
        m_action = m_parameters.get("action");

        while(mailboxString != null) {
            LOG.info("Starting voicemail for mailbox \""+mailboxString+"\" action=\""+m_action);
            mailboxString = voicemail(mailboxString);
        }
        LOG.info("Ending voicemail");
    }

    public boolean usingCpUi(User user) {
        return (user.getVoicemailTui().compareTo("cpui") == 0);
    }

    private Localization userLoc(User user) {
        Localization usrLoc;
        if (usingCpUi(user)) {
            usrLoc = m_locCpui;
        } else {
            usrLoc = m_locStd;
        }
        return usrLoc;
    }

    /**
     * Do the VoiceMail action.
     * 
     * @throws Throwable indicating an error or hangup condition.
     */
    String voicemail(String mailboxString) {
               
        User user = m_validUsers.getUser(mailboxString);
        Localization usrLoc = userLoc(user);
         
        m_mailbox = null;
        if (user != null) {
           m_mailbox = new Mailbox(user);   
           if (user.getLocale() == null) {
               user.setLocale(usrLoc.getLocale()); // Set the locale for this user to be that passed with the call
           }
        }
        
        if (m_action.equals("deposit")) {
            if (m_mailbox == null) {
                // That extension is not valid.
                LOG.info("Extension "+mailboxString+" is not valid.");
                usrLoc.play("invalid_extension", "");
                goodbye();
                return null ;
            }

            if (!user.hasVoicemail()) {
                LOG.info("Mailbox "+m_mailbox.getUser().getUserName()+" does not have voicemail permission.");
                // That extension is not valid.
                usrLoc.play("invalid_extension", "");
                goodbye();
                return null ;
            }
            // Create the mailbox if it isn't there
            Mailbox.createDirsIfNeeded(m_mailbox);
 
            boolean isOnThePhone = false;
            String reason = m_parameters.get("call-forward-reason");
            if(reason != null) {
                isOnThePhone = reason.equals("user-busy");
            } else {
                /*
                // no diversion header present. check if user on the phone via openfire.
                PhonePresence phonePresence;
                try {
                    phonePresence = new PhonePresence();
                    isOnThePhone = phonePresence.isUserOnThePhone(user.getUserName());
                } catch (Exception e) {

                } 
                */                  
            }                
            
            String result = new Deposit(this).depositVoicemail(isOnThePhone);
            if (result == null) {
                return null ;
            }
            m_action = result;
        }
        
        if (m_action.equals("retrieve")) {
            if(usingCpUi(user)) {
                return new CpRetrieve(this).retrieveVoiceMail();
            } else {
                return new Retrieve(this).retrieveVoiceMail();
            }
        }
        
        return null;
    }

    /**
     * Select a distribution list from the list of lists.  Boy is this confuzing!
     * @return A list of users on the distribution list, null on error
     */
    Vector<User> selectDistributionList() {
        String validDigits = "123456789";
        Distributions d = null;
        
        // See if the new way to get distribution lists is being used.
        User user = m_mailbox.getUser();
        HashMap<String, DistributionList> dlists = user.getDistributionLists();
        if (dlists == null) {
            // Use the old way (distributionListsFile in the user's mailbox directory)
            DistributionsReader dr = new DistributionsReader();
            d = dr.readObject(m_mailbox.getDistributionListsFile()) ;
            if (d != null) {
                validDigits = d.getIndices();
            }
        } else {
            // Delete the old distributions.xml file so it isn't accidentally used later
            m_mailbox.getDistributionListsFile().delete();
        }
        

        Localization usrLoc = userLoc(user);

        for(;;) {
            // "Please select the distribution list.  Press * to cancel."
            PromptList pl = usrLoc.getPromptList("deposit_select_distribution");
            Menu menu = new VmMenu(this);
            IvrChoice choice = menu.collectDigit(pl, validDigits);
    
            if (!menu.isOkay()) {
                return null;
            }
            String digit = choice.getDigits();
            LOG.info("Mailbox "+user.getUserName()+" selectDistributionList ("+digit+")");
            
            Collection<String> userNames = null;
            if (dlists != null) {
                DistributionList list = dlists.get(digit);
                if (list != null) {
                    userNames = list.getList(m_sysDistLists);
                }
            } else if (d != null) {
                userNames = d.getList(digit);
            }
                
            if (userNames != null) {
                Vector<User> users = new Vector<User>();
                for (String userName : userNames) {
                    User u = m_validUsers.getUser(userName);
                    if (u != null && u.hasVoicemail()) {
                        users.add(u);
                    }
                }
                
                return users;
            }

            // "The list you have selected is not valid"
            usrLoc.play("deposit_distribution_notvalid", "");
        }
    }
    

    /**
     * Get the operator URL defined for this user.
     * Uses the PersonalAttendant's operator if defined, else the systems.
     * @param pa
     * @return
     */
    String getOperator(PersonalAttendant pa) {
        String transferUrl;
        // Try the Personal Attendant's definition of operator
        transferUrl = pa.getOperator();
        if (transferUrl == null) {
            // Try the system's definition of operator
            transferUrl = m_ivrConfig.getOperatorAddr();
        }
        return transferUrl;
    }

    /**
     * Record a message into a file named wavName
     * 
     * @param wavName
     * @return The Recording object
     */
    public Record recordMessage(String wavName) {
        // Flush any typed ahead digits
        Localization usrLoc = userLoc(m_mailbox.getUser());
        m_fses.trimDtmfQueue("") ;
        LOG.info(String.format("Recording message (%s)", wavName));
        Record rec = new Record(m_fses, usrLoc.getPromptList("beep"));
        rec.setRecordFile(wavName) ;
        rec.setRecordTime(300); 
        rec.setDigitMask("0123456789*#i"); // Any digit can stop the recording
        rec.go();
        return rec;
    }
    
 
    /**
     * Transfer to the operator
     */
    public void operator() {
        transfer(getOperator(m_mailbox.getPersonalAttendant()));
    }
    
    /**
     * Play the "please hold" prompt and then transfer.
     * 
     * @param uri
     */
    public void transfer(String uri) {
        Localization usrLoc = userLoc(m_mailbox.getUser());
        usrLoc.play("please_hold", "");
        Transfer xfer = new Transfer(m_fses, uri);
        xfer.go();
        throw new DisconnectException();
    }

    /**
     * Say good bye and hangup.
     * 
     */
    public void goodbye() {
        LOG.info("good bye");
        // Thank you.  Goodbye.
        Localization usrLoc = userLoc(m_mailbox.getUser());
        usrLoc.play("goodbye", "");
        new Hangup(m_fses).go();
    }

    /**
     * Perform the configured "failure" behavior, which can be either just hangup
     * or transfer to a destination after playing a prompt.
     * 
     */
    public void failure() {
        LOG.info("Input failure");

        if (m_config.isTransferOnFailure()) {
            new Play(m_fses, m_config.getTransferPrompt()).go();
            
            String dest = m_config.getTransferURL();
            if (!dest.toLowerCase().contains("sip:")) {
                LOG.error("transferUrl should be a sip: URL.  Assuming extension");
                dest = extensionToUrl(dest) ;
            }

            LOG.info("Transfer on falure to " + dest);
            new Transfer(m_fses, dest).go();
        }

        goodbye() ;
    }

    public ResourceBundle getAttendantBundle() {
        return m_vmBundle;
    }

    public void setAttendantBundle(ResourceBundle attendantBundle) {
        m_vmBundle = attendantBundle;
    }

    public Configuration getConfig() {
        return m_config;
    }

    public void setConfig(Configuration config) {
        m_config = config;
    }

    public ValidUsersXML getValidUsers() {
        return m_validUsers;
    }

    public void setValidUsers(ValidUsersXML validUsers) {
        m_validUsers = validUsers;
    }
    
    public Localization getLoc() {
        Localization usrLoc = userLoc(m_mailbox.getUser());
        return usrLoc;
    }
    
    public HashMap<String, DistributionList> getSysDistList() {
        return m_sysDistLists;
    }
    
    public Mailbox getMailbox() {
        return m_mailbox;
    }

    public void setMailbox(Mailbox newMailbox) {
        m_mailbox = newMailbox;
    }
    
    public boolean isDeposit() { 
        return m_action.equals("deposit");
    }
        
    public void playError(String errPrompt, String ...vars) {
        Localization usrLoc = userLoc(m_mailbox.getUser());
        usrLoc.play("error_beep", "");
        m_fses.trimDtmfQueue("");
        usrLoc.play(errPrompt, "0123456789*#", vars);
    }
    
}
