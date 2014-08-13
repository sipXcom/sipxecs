package org.sipfoundry.openfire.plugin.job.group;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupAlreadyExistsException;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.sipfoundry.openfire.sync.job.Job;
import org.xmpp.packet.JID;

public class GroupUpdateJob implements Job {
    private static Logger logger = Logger.getLogger(GroupUpdateJob.class);

    private static final long serialVersionUID = 1L;

    private final String groupName;
    private final String oldGroupName;
    private final boolean isImGroup;
    private final String description;
    private final JID imbot;

    public GroupUpdateJob(String groupName, String oldGroupName, boolean imGroup, String description, JID imbot) {
        this.groupName = groupName;
        this.oldGroupName = oldGroupName;
        this.isImGroup = imGroup;
        this.description = description;
        this.imbot = imbot;
    }

    @Override
    public void process() {
        logger.debug("start processing " + toString());

        // not an imgroup but in cache: delete it
        if (!isImGroup) {
            GroupShared.removeGroup(oldGroupName);
            logger.debug("end processing " + toString());
            return;
        }

        // imgroup and not in cache: create it
        if (isImGroup && StringUtils.isBlank(groupName)) {
            createGroup(groupName);
            logger.debug("end processing " + toString());
            return;
        }

        // imgroup and in cache: update it
        if (isImGroup && !StringUtils.isBlank(groupName)) {
            // new name is not the same as the one from cache, delete old group,
            // create new
            // one
            if (!StringUtils.equalsIgnoreCase(oldGroupName, groupName)) {
                // group name changed: delete old one, create new with new name
                if (imbot != null) {
                    try {
                        logger.debug("delete mybuddy from group " + oldGroupName);
                        Group group = GroupManager.getInstance().getGroup(oldGroupName);
                        group.getMembers().remove(imbot);
                    } catch (GroupNotFoundException e) {
                        logger.error("Group not found trying to remove mybuddy from " + groupName);
                    }
                }
                GroupShared.removeGroup(oldGroupName);
                createGroup(groupName);
                if (imbot != null) {
                    try {
                        logger.debug("add mybuddy in new group " + groupName);
                        Group group = GroupManager.getInstance().getGroup(groupName);
                        group.getMembers().add(imbot);
                    } catch (GroupNotFoundException e) {
                        logger.error("Group not found trying to add mybuddy in " + groupName);
                    }
                }
                logger.debug("end processing " + toString());
                return;
            }

            // same group name: update existing one
            try {
                Group ofGroup = GroupManager.getInstance().getGroup(groupName);
                ofGroup.setDescription(description);
            } catch (GroupNotFoundException e) {
                logger.error("Group not found trying to set new description " + groupName);
            }
        }

        logger.debug("end processing " + toString());
    }

    private static void createGroup(String groupName) {
        logger.debug("update group " + groupName);
        try {
            GroupManager.getInstance().createGroup(groupName);
        } catch (GroupAlreadyExistsException e1) {
            logger.error(String.format("Update group: Group %s already exists", groupName));
        }
    }

    @Override
    public String toString() {
        return "GroupUpdateJob [groupName=" + groupName + ", oldGroupName=" + oldGroupName + ", isImGroup="
            + isImGroup + ", description=" + description + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
        result = prime * result + (isImGroup ? 1231 : 1237);
        result = prime * result + ((oldGroupName == null) ? 0 : oldGroupName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GroupUpdateJob other = (GroupUpdateJob) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (groupName == null) {
            if (other.groupName != null) {
                return false;
            }
        } else if (!groupName.equals(other.groupName)) {
            return false;
        }
        if (isImGroup != other.isImGroup) {
            return false;
        }
        if (oldGroupName == null) {
            if (other.oldGroupName != null) {
                return false;
            }
        } else if (!oldGroupName.equals(other.oldGroupName)) {
            return false;
        }
        return true;
    }
}
