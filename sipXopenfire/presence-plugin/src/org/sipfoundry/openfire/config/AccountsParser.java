/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.openfire.config;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.spi.MUCPersistenceManager;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;
import org.sipfoundry.openfire.plugin.presence.SipXBookmarkManager;
import org.sipfoundry.openfire.plugin.presence.SipXOpenfirePlugin;
import org.sipfoundry.openfire.plugin.presence.SipXOpenfirePluginException;
import org.sipfoundry.openfire.plugin.presence.UserAccount;
import org.xml.sax.InputSource;
import org.xmpp.packet.JID;

public class AccountsParser {

    public static final String XMPP_INFO = "xmpp-account-info";
    public static final String USER = "user";
    public static final String GROUP = "group";
    public static final String CHAT_ROOM = "chat-room";
    public static String userTag = String.format("%s/%s", XMPP_INFO, USER);
    public static String groupTag = String.format("%s/%s", XMPP_INFO, GROUP);
    public static String chatRoomTag = String.format("%s/%s", XMPP_INFO, CHAT_ROOM);
    public static String groupMemberTag = String.format("%s/%s", groupTag, USER);

    private static String currentTag = null;
    private static Digester digester;
    private final String accountDbFileName;
    private long lastModified;
    private final File accountDbFile;
    private final File watchFile;
    private static Logger logger = Logger.getLogger(AccountsParser.class);
    private XmppAccountInfo previousXmppAccountInfo = null;

    private static Timer timer = new Timer();

    static {
        Logger logger = Logger.getLogger(Digester.class);
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        logger.setLevel(Level.OFF);
        logger = Logger.getLogger("org.apache.commons.beanutils");
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        logger.setLevel(Level.OFF);
    }

    class Scanner extends TimerTask {

        @Override
        public void run() {
            if (watchFile.lastModified() != AccountsParser.this.lastModified) {
                logger.info("XMPP account configuration changes detected - reparsing file");
                AccountsParser.this.lastModified = watchFile.lastModified();
                parseAccounts();
            }
        }
    }

    public void parseAccounts() {
        String fileUrl = "file://" + accountDbFileName;
        XmppAccountInfo newAccountInfo = AccountsParser.parse(fileUrl);
        logger.debug("Pruning unwanted users");
        pruneUnwantedXmppUsers( newAccountInfo.getXmppUserAccountNames() );
        logger.debug("Done pruning unwanted users");
        logger.debug("Pruning unwanted groups");
        pruneUnwantedXmppGroups( newAccountInfo.getXmppGroupNames() ); // prune groups before applying deltas - see XX-7886
        logger.debug("Done pruning unwanted groups");
        logger.debug("Enforcing config deltas");
        // unfortunately, previous account info is always null on startup; this will trigger a
        // synchronization on each startup
        enforceConfigurationDeltas(newAccountInfo, previousXmppAccountInfo);
        logger.debug("Done enforcing config deltas");
        logger.debug("Pruning unwanted chatrooms");
        pruneUnwantedXmppChatrooms( newAccountInfo );
        logger.debug("Dome pruning unwanted chatrooms");
        logger.debug("Pruning unwanted chatroom services");
        pruneUnwantedXmppChatRoomServices( newAccountInfo.getXmppChatRooms() );
        logger.debug("Done pruning unwanted chatroom services");
        // Make sure that all user accounts can create multi-user chatrooms
        SipXOpenfirePlugin plugin = SipXOpenfirePlugin.getInstance();
        logger.debug("Setting allowed for chat services");
        plugin.setAllowedUsersForChatServices(plugin.getUserAccounts());
        logger.debug("Done setting allowed for chat services");
        previousXmppAccountInfo = newAccountInfo;
    }

    private static void enforceConfigurationDeltas(XmppAccountInfo newAccountInfo,
            XmppAccountInfo previousXmppAccountInfo) {
        setElementsChangeStatusBasedOnPreviousConfiguration(newAccountInfo, previousXmppAccountInfo);
        for (XmppConfigurationElement element : newAccountInfo.getAllElements()) {
            if (element.getStatus() != XmppAccountStatus.UNCHANGED) {
                try {
                    element.update();
                } catch (Exception e) {
                    logger.error("setElementsChangeStatusBasedOnPreviousConfiguration caught ", e);
                }
            }
        }
    }

    private static void setElementsChangeStatusBasedOnPreviousConfiguration(XmppAccountInfo newXmppAccountInfo,
            XmppAccountInfo previousXmppAccountInfo) {
        if (previousXmppAccountInfo == null) {
            // no previous config, everything looks new to us.
            for (XmppConfigurationElement element : newXmppAccountInfo.getAllElements()) {
                element.setStatus(XmppAccountStatus.NEW);
            }
        } else {
            // process users
            setElementsChangeStatusBasedOnPreviousConfiguration(newXmppAccountInfo.getXmppUserAccountMap(),
                    previousXmppAccountInfo.getXmppUserAccountMap());
            // process groups
            setElementsChangeStatusBasedOnPreviousConfiguration(newXmppAccountInfo.getXmppGroupMap(),
                    previousXmppAccountInfo.getXmppGroupMap());
            // process chatrooms
            setElementsChangeStatusBasedOnPreviousConfiguration(newXmppAccountInfo.getXmppChatRoomMap(),
                    previousXmppAccountInfo.getXmppChatRoomMap());
        }
        if (logger.isEnabledFor(Level.INFO)) {
            for (XmppConfigurationElement element : newXmppAccountInfo.getAllElements()) {
                logger.info(element.toString());
            }

        }
    }

    private static void setElementsChangeStatusBasedOnPreviousConfiguration(
            Map<String, ? extends XmppConfigurationElement> newXmppAccountMap,
            Map<String, ? extends XmppConfigurationElement> previousXmppAccountMap) {
        for (String elementName : newXmppAccountMap.keySet()) {
            // check if element taken from new account map used to exist in the previous one
            XmppConfigurationElement elementFromNewConfig = newXmppAccountMap.get(elementName);
            XmppConfigurationElement elementFromPreviousConfig = previousXmppAccountMap.get(elementName);
            if (elementFromPreviousConfig == null) {
                // element not found in previous configuration, it must be new
                elementFromNewConfig.setStatus(XmppAccountStatus.NEW);

            } else {
                // the element is not new, check if it has changed
                if (elementFromNewConfig.equals(elementFromPreviousConfig) == true) {
                    elementFromNewConfig.setStatus(XmppAccountStatus.UNCHANGED);
                } else {
                    elementFromNewConfig.setStatus(XmppAccountStatus.MODIFIED);
                }
            }
        }
    }

    private static void pruneUnwantedXmppUsers(Set<String> xmppUserAccountNamesMasterList) {
        SipXOpenfirePlugin plugin = SipXOpenfirePlugin.getInstance();
        // recall user accounts currently configured in openfire
        Collection<UserAccount> userAccountsInOpenfire = plugin.getUserAccounts();

        // remove all those accounts currently configured in openfire that
        // do not show up on the provided master list
        for (UserAccount userAccountInOpenfire : userAccountsInOpenfire) {
            if (xmppUserAccountNamesMasterList.contains(userAccountInOpenfire.getXmppUserName()) == false) {
                if (!userAccountInOpenfire.getXmppUserName().equals("admin")) {
                    try {
                        // remove user from all the groups it used to belong to - this forces an
                        // immediate update of the groups in IM clients
                        String jidAsString = XmppAccountInfo.appendDomain(userAccountInOpenfire.getXmppUserName());
                        JID jid = new JID(jidAsString);
                        Collection<Group> groups = GroupManager.getInstance().getGroups(jid);
                        for (Group group : groups) {
                            logger.debug("pruneUnwantedXmppUsers removing "
                                    + userAccountInOpenfire.getXmppUserName() + " from group " + group.getName());
                            group.getMembers().remove(jid);
                        }
                        logger.info("Pruning Unwanted Xmpp User " + userAccountInOpenfire.getXmppUserName());
                        plugin.destroyUser(XmppAccountInfo.appendDomain(userAccountInOpenfire.getXmppUserName()));
                    } catch (Exception e) {
                        logger.error("pruneUnwantedXmppUsers caught ", e);
                    }
                }
            }
        }
    }

    private static void pruneUnwantedXmppGroups(Set<String> xmppGroupNamesMasterList) {
        SipXOpenfirePlugin plugin = SipXOpenfirePlugin.getInstance();
        // recall groups currently configured in openfire
        Collection<Group> groupsInOpenfire = plugin.getGroups();

        // remove all those groups currently configured in openfire that
        // do not show up on the provided master list
        for (Group groupInOpenfire : groupsInOpenfire) {
            if (xmppGroupNamesMasterList.contains(groupInOpenfire.getName()) == false) {
                try {
                    plugin.deleteGroup(groupInOpenfire.getName());
                    logger.info("Pruning Unwanted Xmpp group " + groupInOpenfire.getName());
                } catch (Exception e) {
                    logger.error("pruneUnwantedXmppGroups caught ", e);
                }
            }
        }
    }

    private static void pruneUnwantedXmppChatrooms(XmppAccountInfo newAccountInfo) {
        SipXOpenfirePlugin plugin = SipXOpenfirePlugin.getInstance();
        // recall chatrooms currently configured in openfire
        Collection<MUCRoom> chatRoomsInOpenfire = plugin.getMUCRooms();

        // remove all those groups currently configured in openfire that
        // do not show up on the provided master list
        for (MUCRoom mucRoomInOpenfire : chatRoomsInOpenfire) {
            String domain = mucRoomInOpenfire.getMUCService().getServiceDomain().split("\\.")[0];
            if (newAccountInfo.getXmppChatRoom(domain, mucRoomInOpenfire.getName()) == null) {
                // openfire chatroom not found in the account info - remove it if not ad hoc
                if (!mucRoomInOpenfire.wasSavedToDB()) {
                    /*
                     * Anything not in our database is deemed adhoc - enforce logging so that even
                     * ad hoc rooms get logged.
                     */
                    logger.debug("Adhoc chat room detected - enable logging");
                    mucRoomInOpenfire.setLogEnabled(true);
                } else {
                    logger.info("Pruning Unwanted Xmpp chatroom " + domain + ":" + mucRoomInOpenfire.getName());
                    mucRoomInOpenfire.destroyRoom(null, "not a managed chat");
                    MUCPersistenceManager.deleteFromDB(mucRoomInOpenfire);
                    // when IM room is deleted, delete bookmark as well if necessary
                    if (SipXBookmarkManager.isInitialized()) {
                        SipXBookmarkManager manager = SipXBookmarkManager.getInstance();
                        if (manager.getMUCBookmarkID(mucRoomInOpenfire.getName()) != null)  {
                            manager.deleteMUCBookmark(mucRoomInOpenfire.getName());
                        }
                    }
                }
            }
        }
    }

    private static void pruneUnwantedXmppChatRoomServices(Collection<XmppChatRoom> configuredXmppChatRooms) {
        /*
         * Restrict the chat services to those contained configured chatrooms
         */
        HashSet<String> allowedDomains = new HashSet<String>();
        for (XmppChatRoom configuredChatRoom : configuredXmppChatRooms) {
            allowedDomains.add(configuredChatRoom.getSubdomain());
        }
        SipXOpenfirePlugin plugin = SipXOpenfirePlugin.getInstance();
        try {
            plugin.pruneChatServices(allowedDomains);
        } catch (Exception e) {
            logger.error("pruneUnwantedXmppChatRoomServices caught ", e);
        }
    }

    public AccountsParser(String accountDbFileName, String watchFileName) {
        try {
            digester = new Digester();
            addRules(digester);
        } catch (Exception ex) {
            throw new SipXOpenfirePluginException(ex);
        }

        this.watchFile = new File(watchFileName);
        
        this.accountDbFileName = accountDbFileName;
        this.accountDbFile = new File(accountDbFileName);
        if (!accountDbFile.exists()) {
            throw new SipXOpenfirePluginException("Account db file not found : " + accountDbFileName);
        }

    }

    public void startScanner() {
        Scanner scanner = new Scanner();
        timer.schedule(scanner, 0, 10000);
    }

    public static void stopScanner() {
        timer.cancel();
    }

    private static void addCallMethod(String elementName, String methodName) {
        digester.addCallMethod(String.format("%s/%s", currentTag, elementName), methodName, 0);
    }

    /*
     * Add the digester rules.
     *
     * @param digester
     */
    private static void addRules(Digester digester) throws Exception {
        AccountsParser.digester = digester;
        digester.setUseContextClassLoader(true);
        digester.addObjectCreate(XMPP_INFO, XmppAccountInfo.class.getName());

        digester.addObjectCreate(userTag, XmppUserAccount.class.getName());
        digester.addSetNext(userTag, "addAccount");

        digester.addObjectCreate(groupTag, XmppGroup.class.getName());
        digester.addSetNext(groupTag, "addGroup");

        digester.addObjectCreate(groupMemberTag, XmppGroupMember.class.getName());
        digester.addSetNext(groupMemberTag, "addMember");

        digester.addObjectCreate(chatRoomTag, XmppChatRoom.class.getName());
        digester.addSetNext(chatRoomTag, "addChatRoom");

        currentTag = userTag;
        addCallMethod("password", "setPassword");
        addCallMethod("user-name", "setUserName");
        addCallMethod("sip-user-name", "setSipUserName");
        addCallMethod("email", "setEmail");
        addCallMethod("display-name", "setDisplayName");
        addCallMethod("on-the-phone-message", "setOnThePhoneMessage");
        addCallMethod("conference-extension", "setConferenceExtension");
        addCallMethod("advertise-on-call-status", "setAdvertiseOnCallPreference");
        addCallMethod("show-on-call-details", "setShowOnCallDetailsPreference");
        currentTag = groupTag;
        addCallMethod("group-name", "setGroupName");
        addCallMethod("description", "setDescription");
        addCallMethod("administrator", "setAdministrator");
        currentTag = groupMemberTag;
        addCallMethod("user-name", "setUserName");
        currentTag = chatRoomTag;
        addCallMethod("subdomain", "setSubdomain");
        addCallMethod("conference-extension", "setConferenceExtension");
        addCallMethod("conference-reach-info", "setConferenceReachabilityInfo");
        addCallMethod("room-name", "setRoomName");
        addCallMethod("description", "setDescription");
        addCallMethod("password", "setPassword");
        addCallMethod("room-owner", "setOwner");
        addCallMethod("moderated", "setModerated");
        addCallMethod("log-room-conversations", "setLogRoomConversations");
        addCallMethod("is-public-room", "setIsPublicRoom");
        addCallMethod("is-members-only", "setMembersOnly");
        addCallMethod("is-persistent", "setPersistent");

    }

    public static XmppAccountInfo parse(String url) {
        // Create a Digester instance
        try {
            InputSource inputSource = new InputSource(url);
            digester.parse(inputSource);
            XmppAccountInfo accountInfo = (XmppAccountInfo) digester.getRoot();

            parseMongoUsers(accountInfo);

            return accountInfo;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SipXOpenfirePluginException(ex);
        }

    }

    private static void parseMongoUsers(XmppAccountInfo accountInfo) {
        List<User> users = UnfortunateLackOfSpringSupportFactory.getValidUsers().getUsersWithImEnabled();
        for (User user : users) {
            XmppUserAccount account = new XmppUserAccount();
            account.setPassword(user.getPintoken());
            account.setUserName(user.getJid());
            account.setSipUserName(user.getUserName());
            account.setEmail(StringUtils.defaultIfEmpty(user.getEmailAddress(), StringUtils.EMPTY));
            account.setDisplayName(StringUtils.defaultIfEmpty(user.getImDisplayName(), StringUtils.EMPTY));
            account.setAdvertiseOnCallPreference(BooleanUtils.toStringTrueFalse(user.isAdvertiseOnCallStatus()));
            account.setOnThePhoneMessage(StringUtils.defaultIfEmpty(user.getOnthePhoneMessage(), StringUtils.EMPTY));

            accountInfo.addAccount(account);
        }
    }
}
