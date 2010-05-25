/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.openfire.plugin.presence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginClassLoader;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupAlreadyExistsException;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.openfire.muc.MUCRole.Affiliation;
import org.jivesoftware.openfire.spi.PresenceManagerImpl;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.NotFoundException;
import org.sipfoundry.commons.log4j.SipFoundryAppender;
import org.sipfoundry.commons.log4j.SipFoundryLayout;
import org.sipfoundry.openfire.config.AccountsParser;
import org.sipfoundry.openfire.config.ConfigurationParser;
import org.sipfoundry.openfire.config.XmppAccountInfo;
import org.sipfoundry.openfire.config.XmppChatRoom;
import org.sipfoundry.openfire.config.XmppGroup;
import org.sipfoundry.openfire.config.XmppGroupMember;
import org.sipfoundry.openfire.config.XmppS2sInfo;
import org.sipfoundry.openfire.config.WatcherConfig;
import org.sipfoundry.openfire.config.XmppUserAccount;
import org.sipfoundry.sipcallwatcher.CallWatcher;
import org.sipfoundry.sipcallwatcher.ResourceStateChangeListener;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import org.jivesoftware.openfire.muc.HistoryStrategy;
import org.jivesoftware.openfire.muc.cluster.UpdateHistoryStrategy;


public class SipXOpenfirePlugin implements Plugin, Component {

    private MultiUserChatManager multiUserChatManager;
    private GroupManager groupManager;
    private UserManager userManager;
    private PresenceManager presenceManager;
    private PluginManager pluginManager;
    private ComponentManager componentManager;
    private String hostname;
    private Map<String, Presence> probedPresence;
    private JID componentJID;
    private XMPPServer server;
    private Localizer localizer;
    private boolean isInitialized = false;
    private List<AbstractMessagePacketInterceptor> abstractMessagePacketInterceptors = new ArrayList<AbstractMessagePacketInterceptor>();

    private static String DEFAULT_MUC_SERVICE = "conference";
    
    private static String configurationPath = "/etc/sipxpbx";

    private static WatcherConfig watcherConfig;

    private static String logFile;

    private static SipFoundryAppender logAppender;

    public static final String SIP_UID = "sipUid";

    public static final String SIP_PWD = "sipPwd";

    public static final String ON_THE_PHONE_MESSAGE = "onThePhoneMessage";

    public static final String CONFERENCE_EXTENSION = "conferenceExtension";

    private static final String PLUGIN_PATH = "sipx-openfire-presence";

    private static Logger log = Logger.getLogger(SipXOpenfirePlugin.class);

    private static SipXOpenfirePlugin instance;

    private Map<String, String> sipIdToXmppIdMap = new HashMap<String, String>();

    private Map<String, ConferenceInformation> roomNameToConferenceInfoMap = new HashMap<String, ConferenceInformation>();

    private Map<String, XmppUserPreferences> xmppUserPreferencesMap = new HashMap<String, XmppUserPreferences>(); // key is username part of JID

    private AccountsParser accountsParser;
    
    private ImLogger imLogger;

    public class ConferenceInformation {
        private String extension;
        private String name;
        private String pin;
        private String reachabilityInfo;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getPin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ConferenceInformation(String name, String extension, String pin, String reachabilityInfo) {
            this.name = name;
            this.extension = extension;
            this.pin = pin;
            this.reachabilityInfo = reachabilityInfo;
        }

        public String getReachabilityInfo() {
            return reachabilityInfo;
        }

        public void setReachabilityInfo( String reachabilityInfo ) {
            this.reachabilityInfo = reachabilityInfo;
        }
    }

    public class XmppUserPreferences {
        private boolean advertiseOnCallStatus;
        private boolean showOnCallDetails;
        
        public XmppUserPreferences(boolean advertiseOnCallStatus, boolean showOnCallDetails)
        {
            this.advertiseOnCallStatus = advertiseOnCallStatus;
            this.showOnCallDetails = showOnCallDetails;
        }
        public boolean getAdvertiseOnCallStatus()
        {
            return advertiseOnCallStatus;
        }
        public void setAdvertiseOnCallStatus( boolean advertiseOnCallStatus )
        {
            this.advertiseOnCallStatus = advertiseOnCallStatus;
        }
        public boolean getShowOnCallDetails()
        {
            return showOnCallDetails;
        }
        public void setShowOnCallDetails( boolean showOnCallDetails )
        {
            this.showOnCallDetails = showOnCallDetails;
        }
    }
    
    static void parseConfigurationFile() {
        String configurationFile = configurationPath + "/sipxopenfire.xml";

        if (!new File(configurationFile).exists()) {
            String msg = String.format("Configuration %s file not found -- exiting",
                    configurationFile);
            System.err.println(msg);
            throw new SipXOpenfirePluginException(msg);
        }
        ConfigurationParser parser = new ConfigurationParser();
        watcherConfig = parser.parse("file://" + configurationFile);
        logFile = watcherConfig.getLogDirectory() + "/sipxopenfire.log";

    }

    static void initializeLogging() throws SipXOpenfirePluginException {
        try {
            String javaClassPaths = System.getProperty("java.class.path");
            String openfireHome = System.getProperty("openfire.home");

            // Library libhostname.so does not get automatically added to the classpath
            // probably because the file extension does not match the *.jar pattern
            StringBuilder sb = new StringBuilder(javaClassPaths).append(":" + openfireHome
                + "/plugins/" + SipXOpenfirePlugin.PLUGIN_PATH
                + "/lib/libhostname.so");
            System.setProperty("java.class.path", sb.toString());

            // Configure log4j
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "warn, file");
            props.setProperty("log4j.logger.org.sipfoundry.openfire",
                    SipFoundryLayout.mapSipFoundry2log4j(watcherConfig.getLogLevel()).toString());
            props.setProperty("log4j.appender.file", SipFoundryAppender.class.getName());
            props.setProperty("log4j.appender.file.File", logFile);
            props.setProperty("log4j.appender.file.layout", SipFoundryLayout.class.getName());
            props.setProperty("log4j.appender.file.layout.facility", "JAVA");
            String log4jProps = configurationPath + "/log4j.properties";
            if (new File(log4jProps).exists()) {
                Properties fileProps = new Properties();
                fileProps.load(new FileInputStream(log4jProps));
                String level = fileProps
                        .getProperty("log4j.logger.org.sipfoundry.openfire");
                if (level != null) {
                    props.setProperty("log4j.logger.org.sipfoundry.openfire",level);
                }
            }
            PropertyConfigurator.configure(props);

        } catch (Exception ex) {
            throw new SipXOpenfirePluginException(ex);
        }
    }

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        SipXOpenfirePlugin.instance = this;

        InputStream in = getClass().getResourceAsStream("/config.properties");
        Properties properties = new Properties();
        componentManager = ComponentManagerFactory.getComponentManager();

        try {
            properties.load(in);
        } catch (IOException ex) {
            componentManager.getLog().error(ex);
        }

        try {
            if (new File("/tmp/sipx.properties").exists()) {
                System.getProperties()
                        .load(new FileInputStream(new File("/tmp/sipx.properties")));
            }
        } catch (Exception ex) {
            componentManager.getLog().error(ex);
            throw new SipXOpenfirePluginException("Error reading config file ", ex);
        }

        pluginManager = manager;
        ClassLoader classLoader = pluginManager.getPluginClassloader(this);
        classLoader.setPackageAssertionStatus("org.sipfoundry", true);
        Thread.currentThread().setContextClassLoader(classLoader);
        configurationPath = System.getProperty("conf.dir", "/etc/sipxpbx");
        this.localizer = instantiateLocalizer();
        
        parseConfigurationFile();
        initializeLogging();   
        log.info(">>>>>>>>STARTING " + SipXOpenfirePlugin.class + "<<<<<<<<");
        server = XMPPServer.getInstance();

        userManager = server.getUserManager();
        presenceManager = server.getPresenceManager();

        hostname = server.getServerInfo().getXMPPDomain();
        log.info("HostName = " + hostname);

        probedPresence = new ConcurrentHashMap<String, Presence>();

        groupManager = GroupManager.getInstance();

        multiUserChatManager = server.getMultiUserChatManager();

        /*
         * Create default multi-user chat service (see XX-6913) and remove others
         */
        createChatRoomService(DEFAULT_MUC_SERVICE);
        Collection<String> defaultSubdomain = new ArrayList<String>();
        defaultSubdomain.add(DEFAULT_MUC_SERVICE);
        try{
            pruneChatServices(defaultSubdomain);
        }
        catch( Exception ex ){
            log.error("initializePlugin caught exception while pruning chat services list ", ex );
        }
        
        /*
         * Update the server to server (s2s) settings
         */
        try{
            // Get the object instance that stores the server to server
            // generated by the sipXconfig side.
            XmppS2sInfo xmppS2sInfo = watcherConfig.getS2sInfo();

            xmppS2sInfo.updateS2sSettings();
        }
        catch( Exception ex ){
            log.error("initializePlugin caught exception while updating s2s settings ", ex );
        }
        
        /*
         * Load up the database.
         */
        log.info("hostname " + hostname);

        String accountConfigurationFile = configurationPath + "/xmpp-account-info.xml";
        if (!new File(accountConfigurationFile).exists()) {
            System.err.println("User account file not found");
            throw new SipXOpenfirePluginException("Cannot find user accounts file");
        } else {
            this.accountsParser = new AccountsParser(accountConfigurationFile);
            this.accountsParser.parseAccounts();
            this.accountsParser.startScanner(); 
        }

        // config and instantiate and the presence unifier used to gather all presence info
        PresenceUnifier.setPlugin(this);
        PresenceUnifier.getInstance();
       
        // add packet interceptors found in extras directory.
        if( this.getClass().getClassLoader() instanceof PluginClassLoader ){

            String extrasDirNameForClassLoader = System.getProperty("openfire.home");
            if (extrasDirNameForClassLoader != null) {
                extrasDirNameForClassLoader += "/extras/sipXecs";
                log.info("extras directory is " + extrasDirNameForClassLoader );
                /* PluginClassLoader automatically adds /lib to the supplied directory
                   so pass it a directory that does not already have it */
                PluginClassLoader pluginClassLoader = (PluginClassLoader)this.getClass().getClassLoader();
                pluginClassLoader.addDirectory(new File(extrasDirNameForClassLoader), false);
                
                String extrasDirName = extrasDirNameForClassLoader + "/lib";
                File extrasDir = new File(extrasDirName);
                loadDefault();
                loadExtras(pluginClassLoader, extrasDir);
            }
            else{
                log.error("Could not determine the extras directory name " + System.getProperties());
            }
        }
        
        if(watcherConfig.isImMessageLoggingEnabled()){
            imLogger = new ImLogger(watcherConfig.getImMessageLoggingDirectory());
            InterceptorManager.getInstance().addInterceptor(imLogger);
        }
        
        log.info("plugin initializaton completed");
        log.info("DONE");

        CallWatcher.setWatcherConfig(watcherConfig);

        /*
         * Everything else is ready.  Let the SIP side of the show begin...
         */
        try {
            CallWatcher.init();
            log.info("completed init");
            ResourceStateChangeListener resourceStateChangeListener = new ResourceStateChangeListenerImpl(
                    this);
            CallWatcher.getSubscriber().setResourceStateChangeListener(
                    resourceStateChangeListener);
        } catch (Exception e) {
            log.error("Error initializing CallWatcher", e);
            throw new SipXOpenfirePluginException("Init error", e);
        }
        
        isInitialized = true;
    }
    
    void loadDefault() {
        DefaultMessagePacketInterceptor originalInterceptor = new DefaultMessagePacketInterceptor();
        originalInterceptor.start(this);
        abstractMessagePacketInterceptors.add(originalInterceptor);
    }
    
    void loadExtras(ClassLoader classLoader, File extrasDir){
        // inspect all jars in the extras dir
        File[] jars = extrasDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        if (jars != null) {
            for (int i = 0; i < jars.length; i++) {
                // find all classes that are packet interceptors
                try{
                    log.info("loadExtras considering jar: " + jars[i].getName() );
                    JarFile jarFile = new JarFile(jars[i]);
                    // Get the manifest and its attributes
                    Manifest manifest = jarFile.getManifest(); 
                    Attributes attribs = manifest.getMainAttributes();
                    String messageInterceptorClassName = attribs.getValue("MessageInterceptorClass");
                    if (messageInterceptorClassName != null){
                        log.info("Found an extra MessageInterceptorClass " + messageInterceptorClassName );

                        Class packetInterceptorClass = Class.forName(messageInterceptorClassName, true, classLoader);
                        AbstractMessagePacketInterceptor abstractMessagePacketInterceptor = (AbstractMessagePacketInterceptor)packetInterceptorClass.newInstance();
                        abstractMessagePacketInterceptor.start(this);
                        InterceptorManager.getInstance().addInterceptor(abstractMessagePacketInterceptor);
                        abstractMessagePacketInterceptors.add(abstractMessagePacketInterceptor);
                    }
                }
                catch(Throwable e){
                    log.error("loadExtras caught exception:", e);
                }
            }
        }
    }            
    
    public boolean isInitialized()
    {
        return isInitialized;
    }

    public void destroyPlugin() {
        log.debug("DestroyPlugin");
        isInitialized = false;
        if( accountsParser != null ){
            accountsParser.stopScanner();
        }
        for(AbstractMessagePacketInterceptor abstractMessagePacketInterceptor : abstractMessagePacketInterceptors){
            InterceptorManager.getInstance().removeInterceptor(abstractMessagePacketInterceptor);
        }
	abstractMessagePacketInterceptors.clear();
        CallWatcher.destroy();
        
        multiUserChatManager = null;
        groupManager = null;
        userManager = null;
        presenceManager = null;
        pluginManager = null;
        componentManager = null;
        server = null;
        localizer = null;
        log = null;
        watcherConfig = null;
        logAppender = null;
        SipXOpenfirePlugin.instance = null;
        accountsParser = null;
    }

    public String getName() {
        return pluginManager.getName(this);
    }

    public String getDescription() {
        return pluginManager.getDescription(this);
    }

    public void initialize(JID jid, ComponentManager componentManager) {
    }

    public void start() {
    }

    public void shutdown() {
    }

    public void processPacket(Packet packet) {
        // Check that we are getting an answer to a presence probe
        if (packet instanceof Presence) {
            Presence presence = (Presence) packet;
            if (presence.isAvailable() || presence.getType() == Presence.Type.unavailable
                    || presence.getType() == Presence.Type.error) {
                // Store answer of presence probes
                probedPresence.put(presence.getFrom().toString(), presence);
            }
        }
    }

    // Experimental - to be debugged and tested
    public Presence getForeignUserPresence(String sender, String jid)
            throws UserNotFoundException {

        log.debug("getPresence : sender = " + sender + " jid = " + jid);

        if (jid == null) {
            throw new UserNotFoundException("Target JID not found in request");
        }

        JID targetJID = new JID(jid);
        // Check that the sender is not requesting information of a remote server entity
        if (targetJID.getDomain() == null || XMPPServer.getInstance().isRemote(targetJID)) {
            throw new UserNotFoundException("Domain does not matches local server domain");
        }
        if (!hostname.equals(targetJID.getDomain())) {
            // Sender is requesting information about component presence, so we send a
            // presence probe to the component.
            presenceManager.probePresence(componentJID, targetJID);

            // Wait 30 seconds until we get the probe presence result
            int count = 0;
            Presence presence = probedPresence.get(jid);
            while (presence == null) {
                if (count > 300) {
                    // After 30 seconds, timeout
                    throw new UserNotFoundException(
                            "Request for component presence has timed-out.");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // don't care!
                }
                presence = probedPresence.get(jid);

                count++;
            }
            // Clean-up probe presence result
            probedPresence.remove(jid);
            // Return component presence
            return presence;
        }
        if (targetJID.getNode() == null
                || !UserManager.getInstance().isRegisteredUser(targetJID.getNode())) {
            // Sender is requesting presence information of an anonymous user
            throw new UserNotFoundException("Username is null");
        }
        User user = userManager.getUser(targetJID.getNode());
        log.debug("user = " + user.getName());
        log.debug("isNameVisible " + user.isNameVisible());
        return ((PresenceManagerImpl) presenceManager).getPresence(user);

    }

    public void setPresenceState(String jid, UnifiedPresence.XmppPresence xmppPresence)
            throws UserNotFoundException {
        log.debug("setPresenceState : " + jid + " XmppPresence = " + xmppPresence);
        if (jid == null) {
            throw new UserNotFoundException("Target JID not found in request");
        }
        JID targetJID = new JID(jid);
        // Check that the sender is not requesting information of a remote server entity
        if (targetJID.getDomain() == null || XMPPServer.getInstance().isRemote(targetJID)) {
            throw new UserNotFoundException("Domain does not matches local server domain");
        }
        User user = userManager.getUser(targetJID.getNode());
        Presence presence = presenceManager.getPresence(user);
        if (presence == null) {
            log.debug("User is OFFLINE  -- cannot set presence state");
            return;
        }

        presence.setShow(xmppPresence.asPresenceShowEnum());
        user.getRoster().broadcastPresence(presence);

    }

    public void setPresenceStatus(String jid, String presenceMessage)
            throws UserNotFoundException {

        log.debug("setPresenceStatus jid = " + jid + " presenceMessage = " + presenceMessage);
        if (jid == null) {
            throw new UserNotFoundException("Target JID not found in request");
        }
        JID targetJID = new JID(jid);
        // Check that the sender is not requesting information of a remote server entity
        if (targetJID.getDomain() == null || XMPPServer.getInstance().isRemote(targetJID)) {
            throw new UserNotFoundException("Domain does not matches local server domain");
        }
        User user = userManager.getUser(targetJID.getNode());

        Presence presence = presenceManager.getPresence(user);

        if (presence == null) {
            log.debug("User is OFFLINE  -- cannot set presence state");
            return;
        }
        presence.setStatus(presenceMessage);

        user.getRoster().broadcastPresence(presence);
    }

    public boolean userHasAccount(String jid) {
        log.debug("userHasAccount : " + jid);
        try {
            if (jid == null) {
                throw new NullPointerException("Target JID not found in request");
            }
            JID targetJID = new JID(jid);
            // Check that the sender is not requesting information of a remote server entity
            if (targetJID.getDomain() == null || XMPPServer.getInstance().isRemote(targetJID)) {
                return false;
            }
            User user = userManager.getUser(targetJID.getNode());
            if (user == null) {
                return false;
            }
            return true;
        } catch (UserNotFoundException ex) {
            return false;
        }
    }

    public void update( XmppUserAccount userAccount ) throws Exception {
        log.debug("update UserAccount " + userAccount.getUserName());

        try {
            User user = userManager.getUser(userAccount.getUserName());
            // user already exists - update its properties
            user.setPassword(userAccount.getPassword());
            user.setName(userAccount.getDisplayName());
            user.setEmail(userAccount.getEmail());
            
            // user has changed, found out all the groups that the 
            // user is a part of and remove and re-add it so that changes 
            // made to user attributes such a display name get updated immediately.
            Collection<Group>  groups = groupManager.getGroups( user );
            for( Group group : groups )
            {
                log.debug("update UserAccount resetting " + userAccount.getUserName() + " in group " + group.getName());
                String jidAsString = XmppAccountInfo.appendDomain(userAccount.getUserName());
                JID jid = new JID(jidAsString);
                group.getMembers().remove(jid);
                group.getMembers().add(jid);
            }
        } catch (UserNotFoundException e) {
            try {
                // user does not exist create it.
                userManager.createUser(userAccount.getUserName(),
                                       userAccount.getPassword(),
                                       userAccount.getDisplayName(), 
                                       userAccount.getEmail());
            } catch (UserAlreadyExistsException ex) {
                throw new SipXOpenfirePluginException(ex);
            }
        } catch (Exception ex) {
            throw new SipXOpenfirePluginException(ex);
        }
        this.xmppUserPreferencesMap.put(userAccount.getUserName(),
                new XmppUserPreferences(userAccount.getAdvertiseOnCallPreference(),
                                        userAccount.getShowOnCallDetailsPreference()));

        String jid = XmppAccountInfo.appendDomain(userAccount.getUserName());
        String sipUserName = userAccount.getSipUserName();
        log.debug("setSipId " + jid + " sipUserName " + sipUserName);
        setSipId(jid, sipUserName);
        setOnThePhoneMessage(sipUserName, userAccount.getOnThePhoneMessage());
        setAllowedUserForChatServices(jid); // Make sure that user can create multi-user chatrooms
    }

    public void destroyUser(String jid) throws UserNotFoundException {
        try {
            log.info("destroyUser " + jid);

            JID targetJID = new JID(jid);

            User user = userManager.getUser(targetJID.getNode());
            if (user == null) {
                log.info("User not found " + jid);
                return;
            }
            userManager.deleteUser(user);
            // Remove user from all groups where he is a member.
            groupManager.deleteUser(user);
        } catch (UserNotFoundException ex) {
            log.debug("Could not find user " + jid);
        }
    }

    // gets a the sip username (without the SIP domain part) corresponding to the supplied XMPP display name
    // @throws a UserNotFoundException if the user is not found
    public String getSipIdFromXmppDisplayName(String displayName) throws UserNotFoundException {
        String sipUser = null;
        try{
            Collection<User> matchingUsers;
            Set<String> matchingCriteria = new HashSet<String>();
            matchingCriteria.add("Name");
            matchingUsers = userManager.findUsers(matchingCriteria, displayName);
            if( matchingUsers.size() == 1 ){
                User matchingUser = matchingUsers.iterator().next();
                sipUser = getSipId( matchingUser.getUsername() + "@" + getXmppDomain() );
            }
            if( sipUser == null ){
                throw new UserNotFoundException("cannot find user for display name " + displayName );
            }
        }
        catch( Exception ex ){
            throw new UserNotFoundException(ex + ": cannot find user for display name " + displayName );
        }
        return sipUser;
    }

    // gets a the sip username (without the SIP domain part) corresponding to the supplied JID 
    public String getSipId(String jid) throws UserNotFoundException {
        User user = userManager.getUser(jid);
        return user.getProperties().get(SIP_UID);
    }

    // associates a jid with a sip username (without the SIP domain part) 
    public void setSipId(String jid, String sipUserName) throws UserNotFoundException {
        User user = userManager.getUser(jid);
        user.getProperties().put(SIP_UID, sipUserName);
        user.getProperties().put(ON_THE_PHONE_MESSAGE, "I am on the phone");
        this.sipIdToXmppIdMap.put(sipUserName, jid);
    }

    public String getSipPassword(String userName) throws UserNotFoundException {
        User user = userManager.getUser(userName);
        return user.getProperties().get(SIP_PWD);
    }

    public void setSipPassword(String userName, String sipPassword) throws UserNotFoundException {
        User user = userManager.getUser(userName);
        user.getProperties().put(SIP_PWD, sipPassword);
    }

    public void update( XmppGroup group ) throws Exception {
        log.debug("update Group " + group.getGroupName());
        
        boolean isAllAdminGroup = false;
        String adminJid = null;
        JID adminJID = null;
        if (group.getAdministrator() == null) {
            isAllAdminGroup = true;
        } else {
            adminJid = XmppAccountInfo.appendDomain(group.getAdministrator());
            adminJID = new JID(adminJid);

        }

        // check if group already exists in openfire
        try{
            Group openfireGroup = getGroupByName(group.getGroupName());

            // enforce description in case it changed
            openfireGroup.setDescription(group.getDescription());
            
            // group already exists, make sure that it contains the correct set of members.
            // This is achieved in two operations:
            //  1- Add all members that are currently not in the group but are found in the group from configuration file
            //  2- Remove all members that are currently in the group but not found in the group from configuration file
            log.info("create Group: " + group.getGroupName() + " already exists - enforce members list");
            Collection<String> currentGroupMembers = new HashSet<String>();
            for( JID jid : openfireGroup.getMembers() ){
                currentGroupMembers.add(jid.toBareJID());
            }
            
            Collection<String> desiredGroupMembers = new HashSet<String>();
            Collection<String> desiredGroupMembersBackup = new HashSet<String>();
            for( XmppGroupMember member :group.getMembers() ){
                desiredGroupMembers.add(member.getJid());
                desiredGroupMembersBackup.add(member.getJid());
            }

            //  1- Add all members that are currently not in the group but are found in the group from configuration file
            desiredGroupMembers.removeAll(currentGroupMembers);
            log.info("Need to add the following members to group '" + group.getGroupName() + "': " + desiredGroupMembers);
            for( String jid : desiredGroupMembers){
                addUserToGroup(jid, group.getGroupName(), isAllAdminGroup);
            }

            //  2- Remove all members that are currently in the group but not found in the group from configuration file
            currentGroupMembers.removeAll(desiredGroupMembersBackup);
            log.info("Need to remove the following members to group '" + group.getGroupName() + "': " + currentGroupMembers);
            for( String jid : currentGroupMembers){
                removeUserFromGroup(jid, group.getGroupName());
            }
            
        }
        catch( GroupNotFoundException ex ){
            log.info("Group: " + group.getGroupName() + " does not exist - create it");
            Group openfireGroup = groupManager.createGroup(group.getGroupName());
            if (group.getDescription() != null) {
                openfireGroup.setDescription(group.getDescription());
            }

            if (adminJID != null && adminJID.getDomain().equals(this.getXmppDomain())
                    && this.isValidUser(adminJID)) {
                openfireGroup.getAdmins().add(adminJID);
            }
            
            // configure group as 'shared' among group members
            // (magic recipe taken from 'AddGroup.java file of openfire project)
            openfireGroup.getProperties().put("sharedRoster.showInRoster", "onlyGroup");
            openfireGroup.getProperties().put("sharedRoster.displayName", group.getGroupName());

            // add members to the group
            for (XmppGroupMember member : group.getMembers()) {
                String userJid = XmppAccountInfo.appendDomain(member.getJid());
                addUserToGroup(userJid, group.getGroupName(), isAllAdminGroup);
            }
        }
    }

    public void addUserToGroup(String jidAsString, String groupName, boolean isAdmin) throws GroupNotFoundException{
        JID userJID = new JID(jidAsString);
        addUserToGroup(userJID, groupName, isAdmin);
    }

    public void addUserToGroup(JID jid, String groupName, boolean isAdmin) throws GroupNotFoundException {
        if (isValidUser(jid)) {
            log.debug("addUserToGroup " + jid + " GroupName " + groupName);
            Group group = groupManager.getGroup(groupName, true);
            if (group.getAdmins().contains(jid)) {
                log.debug("Admins already has " + jid);
                group.getMembers().add(jid);
            } else {
                if (isAdmin) {
                    if (jid.getDomain().equals(this.getXmppDomain())) {
                        group.getAdmins().add(jid);
                    }
                }
                group.getMembers().add(jid);
            }
        }
        else{
            log.debug("addUserToGroup " + jid + " GroupName " + groupName + " failed because user is invalid");
        }

    }

    public void removeUserFromGroup(String userJid, String groupName)
            throws GroupNotFoundException {
        JID jid = new JID(userJid);
        Group group = groupManager.getGroup(groupName, true);
        group.getMembers().remove(jid);
        group.getAdmins().remove(jid);
    }

    public void deleteGroup(String groupName) throws GroupNotFoundException {
        Group group = groupManager.getGroup(groupName);
        groupManager.deleteGroup(group);

    }

    public void setOnThePhoneMessage(String sipUserName, String onThePhoneMessage)
            throws UserNotFoundException {
        String userName = this.sipIdToXmppIdMap.get(sipUserName);
        if (userName == null) {
            throw new UserNotFoundException("SIP User " + sipUserName + " not found");
        }
        User user = userManager.getUser(userName);
        if (!onThePhoneMessage.equals("")) {
            user.getProperties().put(ON_THE_PHONE_MESSAGE, onThePhoneMessage);
        } else {
            user.getProperties().remove(ON_THE_PHONE_MESSAGE);
        }
    }

    public String getOnThePhoneMessage(String jid) throws UserNotFoundException {

        User user = userManager.getUser(jid);
        return user.getProperties().get(ON_THE_PHONE_MESSAGE);

    }

    public void setConferenceExtension(String sipUserName, String conferenceExtension)
            throws UserNotFoundException {
        String userName = this.sipIdToXmppIdMap.get(sipUserName);
        if (userName == null) {
            throw new UserNotFoundException("SIP User " + sipUserName + " not found");
        }
        User user = userManager.getUser(userName);

        user.getProperties().put(CONFERENCE_EXTENSION, conferenceExtension);

    }

    public boolean groupExists(String groupName) {
        try {
            Group group = groupManager.getGroup(groupName);
            return true;
        } catch (GroupNotFoundException ex) {
            return false;
        }
    }

    public String getPresenceStatus(String jid) throws UserNotFoundException {
        log.debug("getPresenceStatus " + jid);
        JID targetJID = new JID(jid);
        User user = userManager.getUser(targetJID.getNode());
        Presence presence = presenceManager.getPresence(user);
        if (presence == null) {
            log.debug("User is offline");
            return "";
        }
        return presence.getStatus();
    }

    public void stopServer() throws Exception {
        server.stop();
    }

    public void startServer() throws Exception {
        server.start();
    }

    // returns the JID given a sip user part.
    public String getXmppId(String sipUserPart) {
        return this.sipIdToXmppIdMap.get(sipUserPart);
    }

    // returns the node part of the JID given a sip user part.
    public String getXmppNode(String sipUserPart) throws UserNotFoundException {
        String jidAsString = this.sipIdToXmppIdMap.get(sipUserPart);
        if( jidAsString == null ){
            throw new UserNotFoundException("cannot map SIP User part " + sipUserPart + " to XMPP node" );
        }
        JID jid = new JID(jidAsString);
        return jid.getNode();
    }

    // returns the XMPP display name for given jid supplied as text.
    // returns null if not found
    public String getXmppDisplayName(String xmppUserPart) throws UserNotFoundException{
        return userManager.getUser(xmppUserPart).getName();
    }

    /**
     * @return the server
     */
    public XMPPServer getServer() {
        return server;
    }

    public String getXmppDomain() {
        return this.server.getServerInfo().getXMPPDomain();
    }

    public String getSipDomain() {
        // in current implementation, SIP domain is the same as XMPP domain 
        return this.server.getServerInfo().getXMPPDomain();
    }

    public boolean isUserInGroup(String jid, String groupName) throws GroupNotFoundException {
        Group group = groupManager.getGroup(groupName, true);
        return group.isUser(jid);
    }

    public void destroyMultiUserChatService(String subdomain) {
        try {
            this.multiUserChatManager.removeMultiUserChatService(subdomain);

        } catch (NotFoundException nfe) {
            log.debug("destroyMultiUserChatService not found " + subdomain);
        }
    }
        
    public void update( XmppChatRoom xmppChatRoom ) throws Exception {
        log.info(String.format("update ChatRoom %s\n %s\n %s\n %s",
                xmppChatRoom.getSubdomain(), xmppChatRoom.getRoomName(), xmppChatRoom
                        .getDescription(), xmppChatRoom.getConferenceExtension()));
        if ( xmppChatRoom.getSubdomain() == null || xmppChatRoom.getRoomName() == null ) {
            log.error("Null Subdomain or RoomName specified.");
            return;
        }

        String subdomain                     = xmppChatRoom.getSubdomain();
        String ownerJid                      = xmppChatRoom.getOwner(); 
        String roomName                      = xmppChatRoom.getRoomName();
        boolean makeRoomModerated            = xmppChatRoom.isModerated(); 
        boolean makeRoomMembersOnly          = xmppChatRoom.isMembersOnly();
        boolean allowOccupantsToInviteOthers = false; 
        boolean isPublicRoom                 = xmppChatRoom.isPublicRoom();
        boolean logRoomConversations         = xmppChatRoom.isLogRoomConversations(); 
        boolean isPersistent                 = xmppChatRoom.isPersistent();
        String password                      = xmppChatRoom.getPassword();
        String description                   = xmppChatRoom.getDescription();
        String conferenceExtension           = xmppChatRoom.getConferenceExtension(); 
        String conferenceName                = xmppChatRoom.getConferenceName();
        String conferenceReachabilityInfo    = xmppChatRoom.getConferenceReachabilityInfo();
        
        MultiUserChatService mucService = createChatRoomService(subdomain);
        MUCRoom mucRoom = mucService.getChatRoom(roomName, new JID(ownerJid)); //creates room if it does not exist

        mucRoom.unlock(mucRoom.getRole());

        if (mucRoom.isPersistent() != isPersistent) {
            mucRoom.setPersistent(isPersistent);
        }

        //add new owner and remove all others.
        //Note: cannot remove all first then add as this throws ConflictException
        if (!mucRoom.getOwners().contains(ownerJid)) {
            mucRoom.addOwner(ownerJid, mucRoom.getRole());
        }
        for( String formerOwner : mucRoom.getOwners() ){
            if(!formerOwner.equals(ownerJid)){
                mucRoom.addNone(formerOwner, mucRoom.getRole());
            }
        }

        for (JID admins : XMPPServer.getInstance().getAdmins()) {
            if (!mucRoom.getOwners().contains(admins.toBareJID())) {
                mucRoom.addOwner(ownerJid, mucRoom.getRole());
            }
        }
        
        if (!mucRoom.canAnyoneDiscoverJID()) {
            mucRoom.setCanAnyoneDiscoverJID(true);
        }

        if (!mucRoom.canChangeNickname()) {
            mucRoom.setChangeNickname(true);
        }

        if (mucRoom.isModerated() != makeRoomModerated) {
            mucRoom.setModerated(makeRoomModerated);
        }
        if (mucRoom.isMembersOnly() != makeRoomMembersOnly) {
            mucRoom.setMembersOnly(makeRoomMembersOnly);
        }

        if (!mucRoom.isRegistrationEnabled()) {
            mucRoom.setRegistrationEnabled(true);
        }

        if (mucRoom.isPublicRoom() != isPublicRoom) {
            mucRoom.setPublicRoom(isPublicRoom);
        }

        if (mucRoom.canOccupantsInvite() != allowOccupantsToInviteOthers) {
            mucRoom.setCanOccupantsInvite(allowOccupantsToInviteOthers);

        }

        if (mucRoom.getDescription() != null && description == null
                || mucRoom.getDescription() == null && description != null
                || mucRoom.getDescription() != description
                || !mucRoom.getDescription().equals(description)) {
            mucRoom.setDescription(description);

        }

        /*
         * Check if password changed and set password if changed.
         */

        if (mucRoom.getPassword() != null && password == null || mucRoom.getPassword() == null
                && password != null || mucRoom.getPassword() != password
                || !mucRoom.getPassword().equals(password)) {
            mucRoom.setPassword(password);

        }

        if (!mucRoom.canOccupantsChangeSubject()) {

            mucRoom.setCanOccupantsChangeSubject(true);
        }

        if (!mucRoom.canChangeNickname()) {
            mucRoom.setChangeNickname(true);
        }

        if (mucRoom.isLogEnabled() != logRoomConversations) {
            mucRoom.setLogEnabled(logRoomConversations);
        }

        if (!mucRoom.wasSavedToDB()) {
            mucRoom.saveToDB();
        }

        /* The conference extension is the voice conf bridge extension */
        this.roomNameToConferenceInfoMap.put(subdomain + "." + roomName,
                new ConferenceInformation(conferenceName, conferenceExtension, password, conferenceReachabilityInfo));

    }

    public MultiUserChatService createChatRoomService(String subdomain) 
    {
        MultiUserChatService mucService = XMPPServer.getInstance().getMultiUserChatManager()
                .getMultiUserChatService(subdomain);
        if (mucService == null) {
            try{
                mucService = XMPPServer.getInstance().getMultiUserChatManager()
                        .createMultiUserChatService(subdomain, "default MUC service", false);
                Collection<JID> admins = XMPPServer.getInstance().getAdmins();
                JID admin = admins.iterator().next();
                mucService.addSysadmin(admin.toBareJID());
                mucService.setLogConversationsTimeout(60);
                mucService.setLogConversationBatchSize(100);
                mucService.setRoomCreationRestricted(true);
                HistoryStrategy historyStrategy = new HistoryStrategy(null);
                historyStrategy.setType(HistoryStrategy.Type.none);
                new UpdateHistoryStrategy(subdomain, historyStrategy).run();
                mucService.enableService(true, true);
            }
            catch( Exception ex ){
                log.error("createChatRoomService caught " + ex );
            }
        }
        return mucService;
    }
    
    /**
     * Delete a chat room from the domain.
     * 
     * @param domain
     * @param roomName
     */
    public void removeChatRoom(String domain, String roomName) {
        MultiUserChatService mucService = this.multiUserChatManager
                .getMultiUserChatService(domain);
        log.debug("removeChatRoom domain = " + domain + " roomName = " + roomName);
        mucService.removeChatRoom(roomName);
        this.roomNameToConferenceInfoMap.remove(domain + "." + roomName);
    }

    /**
     * Returns a chat room by name
     * 
     * @param domain
     * @param roomName
     * @return reference to multi=user chat room if found, otherwise returns null
     */
    public MUCRoom getChatRoom(String domain, String roomName) throws NotFoundException {
        MultiUserChatService mucService = this.multiUserChatManager
                .getMultiUserChatService(domain);
        if (mucService == null) {
            throw new NotFoundException("Service not found for domain " + domain);
        }
        MUCRoom mucRoom = mucService.getChatRoom(roomName);
        return mucRoom;
    }

    /**
     * Get all the members of a chat room.
     * 
     * @param domain
     * @param roomName
     * @return
     */
    public Collection<String> getMembers(String domain, String roomName) throws NotFoundException {
        MultiUserChatService mucService = this.multiUserChatManager
                .getMultiUserChatService(domain);
        if (mucService == null) {
            throw new NotFoundException("Service not found for domain " + domain);
        }
        MUCRoom mucRoom = mucService.getChatRoom(roomName);
        if (mucRoom == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        return mucRoom.getMembers();
    }

    public Map<String, String> getMucRoomAttributes(String domain, String roomName)
            throws NotFoundException {
        MultiUserChatService mucService = this.multiUserChatManager
                .getMultiUserChatService(domain);
        MUCRoom mucRoom = mucService.getChatRoom(roomName);
        if (mucRoom == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        Map<String, String> retval = new HashMap<String, String>();
        retval.put("isModerated", "" + mucRoom.isModerated());
        retval.put("isLogEnabled", "" + mucRoom.isLogEnabled());
        retval.put("isMembersOnly", "" + mucRoom.isMembersOnly());
        retval.put("isPublicRoom", "" + mucRoom.isPublicRoom());
        retval.put("isLoginRestrictedToNickName", "" + mucRoom.isLoginRestrictedToNickname());
        retval.put("isLocked", "" + mucRoom.isLocked());
        retval.put("isRegistrationEnabled", "" + mucRoom.isRegistrationEnabled());
        retval.put("isPasswordProtected", "" + mucRoom.isPasswordProtected());
        retval.put("canAnyoneDiscoverJID", "" + mucRoom.canAnyoneDiscoverJID());
        retval.put("canChangeNickName", "" + mucRoom.canChangeNickname());
        retval.put("canOccupantsInvite", "" + mucRoom.canOccupantsInvite());
        retval.put("canOccupantsChangeSubject", "" + mucRoom.canOccupantsChangeSubject());
        return retval;
    }

    public void setMucRoomAttributes(String domain, String roomName, Map newAttributes)
            throws NotFoundException {
        MultiUserChatService mucService = this.multiUserChatManager
                .getMultiUserChatService(domain);
        MUCRoom mucRoom = mucService.getChatRoom(roomName);
        if (mucRoom == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        Map<String, String> attribs = (Map<String, String>) newAttributes;
        boolean isModerated = Boolean.parseBoolean(attribs.get("isModerated"));
        mucRoom.setModerated(isModerated);
        boolean isLogEnabled = Boolean.parseBoolean(attribs.get("isLogEnabled"));
        mucRoom.setLogEnabled(isLogEnabled);
        boolean isMembersOnly = Boolean.parseBoolean(attribs.get("isMembersOnly"));
        mucRoom.setMembersOnly(isMembersOnly);
        boolean isPublicRoom = Boolean.parseBoolean(attribs.get("isPublicRoom"));
        mucRoom.setPublicRoom(isPublicRoom);
        boolean isLoginRestrictedToNickName = Boolean.parseBoolean(attribs
                .get("isLoginRestrictedToNickName"));
        mucRoom.setLoginRestrictedToNickname(isLoginRestrictedToNickName);
        boolean isRegistrationEnabled = Boolean
                .parseBoolean(attribs.get("isRegistrationEnabled"));
        mucRoom.setRegistrationEnabled(isRegistrationEnabled);
        boolean canAnyoneDiscoverJID = Boolean.parseBoolean(attribs.get("canAnyoneDiscoverJID"));
        mucRoom.setCanAnyoneDiscoverJID(canAnyoneDiscoverJID);
        boolean canChangeNickName = Boolean.parseBoolean(attribs.get("canChangeNickName"));
        mucRoom.setChangeNickname(canChangeNickName);
        boolean canOccupantsInvite = Boolean.parseBoolean(attribs.get("canOccupantsInvite"));
        mucRoom.setCanOccupantsInvite(canOccupantsInvite);
        boolean canOccupantsChangeSubject = Boolean.parseBoolean(attribs
                .get("canOccupantsChangeSubject"));
        mucRoom.setCanOccupantsChangeSubject(canOccupantsChangeSubject);
    }

    public String getConferenceName(String domain, String roomName) throws NotFoundException {
        MultiUserChatService mucService = this.multiUserChatManager
                .getMultiUserChatService(domain);
        MUCRoom mucRoom = mucService.getChatRoom(roomName);
        if (mucRoom == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        ConferenceInformation confInfo = this.roomNameToConferenceInfoMap.get(domain + "."
                + roomName);
        ;
        if (confInfo == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        return confInfo.name;
    }

    public String getConferenceExtension(String domain, String roomName) throws NotFoundException {
        MultiUserChatService mucService = this.multiUserChatManager
                .getMultiUserChatService(domain);
        MUCRoom mucRoom = mucService.getChatRoom(roomName);
        if (mucRoom == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        ConferenceInformation confInfo = this.roomNameToConferenceInfoMap.get(domain + "."
                + roomName);
        ;
        if (confInfo == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        return confInfo.extension;
    }
    
    public boolean shouldDisplayUserOnThePhoneStatus(String xmppUserName) throws NotFoundException {
        XmppUserPreferences prefs = this.xmppUserPreferencesMap.get(xmppUserName);
        ;
        if (prefs == null) {
            throw new NotFoundException("User not found " + xmppUserName);
        }
        return prefs.getAdvertiseOnCallStatus();
    }

    public boolean shouldDisplayCallDetails(String xmppUserName) throws NotFoundException {
        XmppUserPreferences prefs = this.xmppUserPreferencesMap.get(xmppUserName);
        if (prefs == null) {
            throw new NotFoundException("User not found " + xmppUserName);
        }
        return prefs.getShowOnCallDetails();
    }

    public String getConferencePin(String domain, String roomName) throws NotFoundException {
        MultiUserChatService mucService = this.multiUserChatManager
                .getMultiUserChatService(domain);
        MUCRoom mucRoom = mucService.getChatRoom(roomName);
        if (mucRoom == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        ConferenceInformation confInfo = this.roomNameToConferenceInfoMap.get(domain + "."
                + roomName);
        ;
        if (confInfo == null) {
            throw new NotFoundException("Room not found " + domain + " roomName " + roomName);
        }
        return confInfo.pin;
    }

    public HashSet<UserAccount> getUserAccounts() {
        HashSet<UserAccount> userAccounts = new HashSet<UserAccount>();
        for (User user : this.userManager.getUsers()) {
            UserAccount userAccount = new UserAccount();
            String sipUserId = user.getProperties().get(SIP_UID);
            userAccount.setXmppUserName(user.getUsername());
            userAccount.setSipUserName(sipUserId);
            userAccounts.add(userAccount);
        }
        return userAccounts;
    }

    public static SipXOpenfirePlugin getInstance() {
        return SipXOpenfirePlugin.instance;
    }

    public Collection<Group> getGroups() {
        return this.groupManager.getGroups();
    }
    
    public Group getGroupByName(String groupName) throws GroupNotFoundException{
        return this.groupManager.getGroup(groupName);
    }
   
    public Collection<MUCRoom> getMUCRooms() {
        HashSet<MUCRoom> retval = new HashSet<MUCRoom>();
        for (MultiUserChatService mucService : this.multiUserChatManager
                .getMultiUserChatServices()) {
            List<MUCRoom> chatRooms = mucService.getChatRooms();
            retval.addAll(chatRooms);
        }
        return retval;
    }

    public void pruneChatServices(Collection<String> subdomains) throws Exception {

        HashSet<MultiUserChatService> pruneSet = new HashSet<MultiUserChatService>();
        pruneSet.addAll(this.multiUserChatManager.getMultiUserChatServices());

        for (MultiUserChatService service : pruneSet) {
            String subdomain = service.getServiceDomain().split("\\.")[0];
            if (!subdomains.contains(subdomain) && 
                !subdomain.equals(DEFAULT_MUC_SERVICE)) {
                log.info("Pruning Unwanted Xmpp chatroom service " + subdomain );                
                this.multiUserChatManager.removeMultiUserChatService(subdomain);
            }
        }
    }
    
    public void setAllowedUsersForChatServices(Collection<UserAccount> accounts){
        HashSet<MultiUserChatService> chatServices = new HashSet<MultiUserChatService>();
        chatServices.addAll(this.multiUserChatManager.getMultiUserChatServices());

        for (MultiUserChatService service : chatServices) {          
            // start from scratch - clear out set of users allowed to create
            Collection<String> usersCurrentlyAllowedToCreate = service.getUsersAllowedToCreate();
            for( String user : usersCurrentlyAllowedToCreate){          
                service.removeUserAllowedToCreate(user);
            }

            // add in all the users who have accounts on the system
            for( UserAccount user : accounts){
                String userJID =  user.getXmppUserName() + "@" + getXmppDomain();         
                service.addUserAllowedToCreate(userJID);
            }           
        }
    }

    public void setAllowedUserForChatServices(String jid){
        HashSet<MultiUserChatService> chatServices = new HashSet<MultiUserChatService>();
        chatServices.addAll(this.multiUserChatManager.getMultiUserChatServices());

        for (MultiUserChatService service : chatServices) {            
            // start from scratch - clear out set of users allowed to create           
            service.addUserAllowedToCreate(jid);
        }           
    }

    public void kickOccupant(String subdomain, String roomName, String password,
            String memberJid, String reason) throws NotAllowedException {
        MUCRoom mucRoom = this.multiUserChatManager.getMultiUserChatService(subdomain)
                .getChatRoom(roomName);
        String roomPassword = mucRoom.getPassword();
        if (password == null && roomPassword != null) {
            throw new NotAllowedException("Password mismatch");
        }
        if (mucRoom.getPassword() != null && !mucRoom.getPassword().equals(password)) {
            throw new NotAllowedException("Password mismatch");
        }
        String actorJid = mucRoom.getOwners().iterator().next();
        JID memberJID = new JID(memberJid);
        JID actorJID = new JID(actorJid);
        mucRoom.kickOccupant(memberJID, actorJID, reason);

    }

    public void inviteOccupant(String subdomain, String roomName, String memberJid,
            String password, String reason) throws Exception {
        MultiUserChatService service = this.multiUserChatManager
                .getMultiUserChatService(subdomain);
        if (service == null) {
            throw new NotFoundException("MUC service not found for " + subdomain);
        }
        MUCRoom mucRoom = service.getChatRoom(roomName);
        if (mucRoom == null) {
            throw new NotFoundException("Room not found for " + subdomain + " roomName "
                    + roomName);
        }
        String roomPassword = mucRoom.getPassword();
        if (password == null && roomPassword != null) {
            throw new NotAllowedException("Password mismatch");
        }
        if (mucRoom.getPassword() != null && !mucRoom.getPassword().equals(password)) {
            throw new NotAllowedException("Password mismatch");
        }
        String ownerJid = mucRoom.getOwners().iterator().next();
        if (!mucRoom.getOccupants().contains(new JID(ownerJid))) {
            throw new NotAllowedException("Owner is not in te room -- cannot invite");
        }

        List<MUCRole> roles = mucRoom.getOccupantsByBareJID(ownerJid);
        JID memberJID = new JID(memberJid);
        for (MUCRole role : roles) {
            if (role.getAffiliation() == Affiliation.owner) {
                mucRoom.sendInvitation(memberJID, reason, role, null);
                break;
            }
        }

    }

    public PacketRouter getPacketRouter() {
        return this.getServer().getPacketRouter();
    }

    public WatcherConfig getSipXopenfireConfig() {
        return watcherConfig;
    }

    /**
     * @param logAppender the logAppender to set
     */
    public static void setLogAppender(SipFoundryAppender logAppender) {
        SipXOpenfirePlugin.logAppender = logAppender;
    }

    /**
     * @return the logAppender
     */
    public static SipFoundryAppender getLogAppender() {
        return logAppender;
    }

    public boolean isValidUser(JID userJid) {

        /*
         * A valid user in another domain - return OK for this.
         */
        if (!userJid.getDomain().equals(this.getXmppDomain()))
            return true;
        try {
            String username = userJid.getNode();
            this.userManager.getUser(username);
        } catch (UserNotFoundException e) {
            return false;
        }
        return true;
    }

    private Localizer instantiateLocalizer(){
        String localizationFile = System.getProperty("localization.file", "/etc/sipxpbx/openfire/sipxopenfire-prompts.properties");
        Properties props = new Properties();
        try
        {
            props.load(new FileInputStream(localizationFile));
        } 
        catch (FileNotFoundException e){
            log.warn("Localizer cannot find localization file '" + localizationFile + "'");
        } catch (IOException e)
        {
            log.error("Localizer caught " + e );
        }
        return new Localizer( props );
    }
    
    public Localizer getLocalizer(){
        return this.localizer;
    }
}

