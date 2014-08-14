package org.sipfoundry.openfire.plugin.job;

import static org.sipfoundry.commons.mongo.MongoConstants.DESCR;
import static org.sipfoundry.commons.mongo.MongoConstants.EMAIL;
import static org.sipfoundry.commons.mongo.MongoConstants.GROUPS;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_DISPLAY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ENABLED;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_GROUP;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ID;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.jivesoftware.openfire.XMPPServer;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.commons.userdb.UserGroup;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;
import org.sipfoundry.openfire.plugin.job.group.GroupDeleteJob;
import org.sipfoundry.openfire.plugin.job.group.GroupUpdateJob;
import org.sipfoundry.openfire.plugin.job.user.UserDeleteJob;
import org.sipfoundry.openfire.plugin.job.user.UserUpdateJob;
import org.sipfoundry.openfire.plugin.job.vcard.VcardUpdateJob;
import org.sipfoundry.openfire.provider.CacheHolder;
import org.sipfoundry.openfire.sync.MongoOperation;
import org.sipfoundry.openfire.sync.job.AbstractJobFactory;
import org.sipfoundry.openfire.sync.job.Job;
import org.xmpp.packet.JID;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class JobFactory extends AbstractJobFactory {
    private static Logger logger = Logger.getLogger(JobFactory.class);
    private static final String COLLECTION_NAME = "entity";

    public Job createJob(MongoOperation op, DBObject dbObj, Object id) {
        Job job = null;

        if (id != null) {
            if (id instanceof String) {
                job = createJob(op, dbObj, (String) id);
            } else if (id instanceof Long) {
                job = createJob(op, dbObj, (Long) id);
            } else if (id instanceof ObjectId) {
                // we assume all id's that are Mongo ObjectId
                // are avatar operations
                job = createJob(op, dbObj, (ObjectId) id);
            } else {
                logger.warn(String.format("Unknown id type: %s", id.getClass().getName()));
            }
        } else {
            logger.warn("Id was null.");
        }

        return job;
    }

    private static Job createJob(MongoOperation op, DBObject dbObj, String id) {
        Job job = null;

        if (id.startsWith("User")) {
            job = buildUserJob(op, dbObj, id);
        } else if (id.startsWith("Group")) {
            job = buildGroupJob(op, dbObj, id);
        }

        return job;
    }

    private static Job createJob(MongoOperation op, DBObject dbObj, ObjectId id) {
        logger.debug("id: " + id.toString());
        logger.debug("op: " + op);
        Job vcardUpdateJob = null;

        switch (op) {
        case INSERT:
            if (dbObj.containsField("filename")) {
                String filename = dbObj.get("filename").toString();
                if (filename.startsWith("avatar_")) {
                    String userName = StringUtils.substringBetween(filename, "avatar_", ".");
                    logger.debug("avatar insert for user: " + userName);
                    String imid = null;
                    User user = UnfortunateLackOfSpringSupportFactory.getValidUsers().getUser(userName);
                    if (user != null) {
                        imid = user.getJid();
                    }
                    vcardUpdateJob = imid != null ? new VcardUpdateJob(imid, dbObj) : null;
                }
            }
            break;
        }
        return vcardUpdateJob;
    }

    private static Job buildUserJob(MongoOperation op, DBObject dbObj, String id) {
        logger.debug("User job obj: " + dbObj);

        Job userJob = null;
        // get the im name from the update object; if it's not there (it has not been updated),
        // use the cached value
        String userImName = (String) dbObj.get(IM_ID);
        if (userImName == null) {
            userImName = CacheHolder.getUserName(id);
        }
        if (userImName == null) {
            userImName = lookupImId(id);
        }

        List<String> groupNames = getGroupNames(dbObj);
        if (!StringUtils.isBlank(userImName)) {
            switch (op) {
            case INSERT:
                if (!(Boolean) dbObj.get(IM_ENABLED) == Boolean.TRUE) {
                    break;
                }
                //$FALL-THROUGH$ - only if IM is enabled, do an update for this user
            case UPDATE:
                String oldImName = CacheHolder.getUserName(id) == null ? userImName : CacheHolder.getUserName(id);
                Boolean imUser = (Boolean) dbObj.get(IM_ENABLED);
                // avoid querying the actual value; the value wasn't updated, but the user is
                // cached, therefore it must have IM enabled
                if (imUser == null) {
                    imUser = true;
                }
                String displayName = (String) dbObj.get(IM_DISPLAY_NAME);
                String email = (String) dbObj.get(EMAIL);
                String uid = (String) dbObj.get(UID);
                userJob = new UserUpdateJob(userImName, oldImName, imUser, displayName, email, uid, groupNames);
                break;
            case DELETE:
                userJob = new UserDeleteJob(userImName);
                break;
            default:
                logger.warn(String.format("Unsupported user operation %s. Ignoring.", op));
                break;
            }
        } else {
            logger.warn("Missing user name for update/delete operation");
        }

        return userJob;
    }

    private static Job buildGroupJob(MongoOperation op, DBObject dbObj, String id) {
        logger.debug("Group job obj: " + dbObj);
        logger.debug("Group job id: " + id);

        Job groupJob = null;
        String groupName = (String) dbObj.get(UID);

        if (!StringUtils.isBlank(groupName)) {
            switch (op) {
            case UPDATE:
                String oldGroupName = CacheHolder.getGroupName(id);
                // if it wasn't cached, it's not in use - no need to update
                if (oldGroupName != null) {
                    String imGroupStr = (String) dbObj.get(IM_GROUP);
                    boolean imGroup;
                    if (imGroupStr != null) {
                        imGroup = "1".equals(imGroupStr);
                    } else {
                        // avoid querying the actual value; the value wasn't updated, but the
                        // group is cached, therefore it must have IM enabled
                        imGroup = true;
                    }
                    String description = (String) dbObj.get(DESCR);
                    UserGroup group = UnfortunateLackOfSpringSupportFactory.getValidUsers().getImGroup(groupName);
                    if (group.isImbotEnabled()) {
                        String xmppDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
                        String imBotName = UnfortunateLackOfSpringSupportFactory.getValidUsers().getImBotName();
                        JID imBotJid = new JID(imBotName, xmppDomain, null, false);
                        groupJob = new GroupUpdateJob(groupName, oldGroupName, imGroup, description, imBotJid);
                    } else {
                        groupJob = new GroupUpdateJob(groupName, oldGroupName, imGroup, description, null);
                    }
                } else {
                    logger.debug(String.format("Skipping update of group %s. Not in cache.", groupName));
                }
                break;
            case DELETE:
                groupJob = new GroupDeleteJob(groupName);
                break;
            default:
                logger.warn(String.format("Unsupported group operation %s. Ignoring.", op));
                break;
            }
        } else {
            String imGroupStr = (String) dbObj.get(IM_GROUP);
            boolean imGroup = false;
            if (imGroupStr != null) {
                imGroup = "1".equals(imGroupStr);
            }
            if (imGroup) {
                groupName = lookupGroupName(id);
                logger.debug("New group with im enabled, loading name " + groupName + " using id " + id);
                if (!StringUtils.isBlank(groupName)) {
                    groupJob = new GroupUpdateJob(groupName, groupName, imGroup, "", null);
                }
            } else {
                logger.warn("Missing group name for update/delete operation");
            }
        }

        return groupJob;
    }

    private static List<String> getGroupNames(DBObject userObject) {
        List<String> actualGroups = new ArrayList<String>();
        List<Object> groupList = (BasicDBList) userObject.get(GROUPS);

        if (groupList == null) {
            return null;
        }

        for (int i = 0; i < groupList.size(); i++) {
            actualGroups.add((String) groupList.get(i));
        }

        return actualGroups;
    }

    private static String lookupImId(String uid) {
        DBObject query = new BasicDBObject("_id", uid);
        DBObject fields = new BasicDBObject("imid", 1);
        DBObject user = getCollection().findOne(query, fields);
        return (String) user.get("imid");
    }

    private static String lookupGroupName(String groupId) {
        DBObject query = new BasicDBObject("_id", groupId);
        DBObject fields = new BasicDBObject("uid", 1);
        DBObject user = getCollection().findOne(query, fields);
        return (String) user.get("uid");
    }

    private static DBCollection getCollection() {
        DB db = UnfortunateLackOfSpringSupportFactory.getImdb();

        return db.getCollection(COLLECTION_NAME);
    }
}
