/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.voicemail.mailbox;

import static org.sipfoundry.commons.mongo.MongoConstants.DAYS_TO_KEEP_VM;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.springframework.beans.factory.annotation.Required;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class VoicemailCleanup {

    private static final int DISABLE_VOICEMAIL_CLEANUP = 0;
    private static final Log LOG = LogFactory.getLog(VoicemailCleanup.class);
    private MailboxManager m_mailboxManager;
    private ValidUsers m_validUsers;

    public void run() {
        LOG.warn("Starting Voicemail cleanup");
        DBCursor cursor = m_validUsers.getUsers();
        Iterator<DBObject> objects = cursor.iterator();
        while (objects.hasNext()) {
            DBObject users = objects.next();
            String userName = ValidUsers.getStringValue(users, UID);
            Integer daysToKeepVM = ValidUsers.getIntegerValue(users, DAYS_TO_KEEP_VM);
            if (daysToKeepVM != null && daysToKeepVM != DISABLE_VOICEMAIL_CLEANUP) {
                m_mailboxManager.cleanupMailbox(userName, daysToKeepVM);
            }
        }
        cursor.close();
        LOG.warn("Finished Voicemail cleanup");
    }

    @Required
    public void setMailboxManager(MailboxManager mailboxManager) {
        m_mailboxManager = mailboxManager;
    }

    @Required
    public void setValidUsers(ValidUsers validUsers) {
        m_validUsers = validUsers;
    }
}
