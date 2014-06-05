package org.sipfoundry.openfire.plugin.job.vcard;

import java.io.StringReader;
import java.net.UnknownHostException;
import java.security.MessageDigest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.XMPPServerInfo;
import org.jivesoftware.openfire.handler.PresenceUpdateHandler;
import org.jivesoftware.openfire.provider.VCardProvider;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.sipfoundry.commons.mongo.MongoFactory;
import org.sipfoundry.commons.userdb.profile.UserProfileServiceImpl;
import org.sipfoundry.openfire.plugin.job.Job;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.xmpp.packet.Presence;

import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class VcardUpdateJob implements Job {
    private static Logger logger = Logger.getLogger(VcardUpdateJob.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected final String userImName;
    protected final String oldMd5;
    protected final String newMd5;
    protected static UserProfileServiceImpl userProfileService;

    static {
        Mongo mongo;
        userProfileService = new UserProfileServiceImpl();
        try {
            mongo = MongoFactory.fromConnectionFile();
            MongoTemplate profilesDb = new MongoTemplate(mongo, "profiles");
            userProfileService.setProfilesDb(profilesDb);
        } catch (UnknownHostException e) {
            logger.error("Error instantiating mongo");
        }
    }

    public VcardUpdateJob(String userImName, DBObject dbObj) {
        this.userImName = userImName;
        this.oldMd5 = userProfileService.getAvatarDBFileMD5(userImName);
        this.newMd5 = dbObj == null ? null : (dbObj.get("md5") != null ? dbObj.get("md5").toString() : null);
    }

    @Override
    public void process() {
        try {
            if (newMd5 != null && newMd5.equals(oldMd5)) {
                logger.debug("Nothing changed!");
                return;
            }
            VCardProvider provider = VCardManager.getProvider();
            Element vcard = provider.loadVCard(userImName);

            logger.debug("update vcard!");
            VCardManager.getInstance().setVCard(userImName, vcard);

            updateAvatar(userImName, vcard);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            for (StackTraceElement el : e.getStackTrace()) {
                logger.error(el.toString());
            }
        }
    }

    protected static void updateAvatar(String username, Element vCard) throws Exception {
        if (vCard.element("PHOTO") == null) {
            return;
        }
        Element binValElement = vCard.element("PHOTO").element("BINVAL");
        if (binValElement == null) {
            return;
        }
        String avatarStr = binValElement.getText();
        XMPPServer server = XMPPServer.getInstance();
        XMPPServerInfo info = server.getServerInfo();
        String aor = username + "@" + info.getXMPPDomain();
        String itemId = getItemId(avatarStr.getBytes());
        Presence presenceAvatar = createPresenceAvatar(aor, itemId);
        PresenceUpdateHandler puh = server.getPresenceUpdateHandler();
        logger.debug("processing " + presenceAvatar.toXML());
        puh.process(presenceAvatar);
    }

    private static String getItemId(byte[] avatarBytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(avatarBytes);
        byte[] digest = md.digest();
        return byteArrayToString(digest);
    }

    private static String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }

    private static Presence createPresenceAvatar(String aor, String itemId) throws Exception {
        StringBuilder builder = new StringBuilder("<presence>");
        builder.append("<priority>1</priority>");
        builder.append("<c xmlns='http://jabber.org/protocol/caps' node='http://pidgin.im/' hash='sha-1' ver='I22W7CegORwdbnu0ZiQwGpxr0Go='/>");
        builder.append("<x xmlns='vcard-temp:x:update'>");
        builder.append("<photo>");
        builder.append(itemId);
        builder.append("</photo></x></presence>");

        String xmlstr = builder.toString();
        SAXReader sreader = new SAXReader();

        Document avatarDoc = sreader.read(new StringReader(xmlstr));
        Element rootElement = avatarDoc.getRootElement();

        Presence avatar = new Presence(rootElement);

        avatar.setFrom(aor);

        XMPPServer.getInstance();
        UserManager um = XMPPServer.getInstance().getUserManager();
        User me = um.getUser(aor);

        PresenceManager pm = XMPPServer.getInstance().getPresenceManager();
        Presence mypresence = pm.getPresence(me);

        avatar.setType(mypresence.getType());
        avatar.setShow(mypresence.getShow());
        avatar.setStatus(mypresence.getStatus());
        avatar.setID(aor + "_presenceAvatar");
        return avatar;
    }

    @Override
    public String toString() {
        return "VcardUpdateJob [userImName=" + userImName + ", oldMd5=" + oldMd5 + ", newMd5=" + newMd5 + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((newMd5 == null) ? 0 : newMd5.hashCode());
        result = prime * result + ((oldMd5 == null) ? 0 : oldMd5.hashCode());
        result = prime * result + ((userImName == null) ? 0 : userImName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VcardUpdateJob other = (VcardUpdateJob) obj;
        if (newMd5 == null) {
            if (other.newMd5 != null)
                return false;
        } else if (!newMd5.equals(other.newMd5))
            return false;
        if (oldMd5 == null) {
            if (other.oldMd5 != null)
                return false;
        } else if (!oldMd5.equals(other.oldMd5))
            return false;
        if (userImName == null) {
            if (other.userImName != null)
                return false;
        } else if (!userImName.equals(other.userImName))
            return false;
        return true;
    }

}
