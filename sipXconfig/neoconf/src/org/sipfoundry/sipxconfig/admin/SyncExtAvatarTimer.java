/**
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
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
package org.sipfoundry.sipxconfig.admin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.commons.userdb.profile.AvatarUploadException;
import org.sipfoundry.commons.userdb.profile.UserProfile;
import org.sipfoundry.commons.userdb.profile.UserProfileService;
import org.springframework.beans.factory.annotation.Required;

public class SyncExtAvatarTimer {
    public static final Log LOG = LogFactory.getLog(SyncExtAvatarTimer.class);
    private UserProfileService m_userProfileService;
    private ExecutorService m_executor = Executors.newSingleThreadExecutor();
    private AdminContext m_adminContext;

    public void syncExtAvatar() {
        if (!m_adminContext.isSyncExtAvatar()) {
            return;
        }
        try {
            m_executor.execute(new SyncExtAvatar());
        } catch (Exception ex) {
            LOG.error("Failed running disabled/delete task ", ex);
        }
    }

    @Required
    public void setUserProfileService(UserProfileService userProfileService) {
        m_userProfileService = userProfileService;
    }

    @Required
    public void setAdminContext(AdminContext adminContext) {
        m_adminContext = adminContext;
    }

    private final class SyncExtAvatar implements Runnable {

        @Override
        public void run() {
            List<UserProfile> userProfiles = new ArrayList<UserProfile>();
            try {
                userProfiles = m_userProfileService.getUserProfilesByExtAvatarUrl();
            } catch (Exception ex) {
                LOG.error("Cannot retrieve user profiles ", ex);
            }
            String userName;
            for (UserProfile profile : userProfiles) {
                try {
                    userName = profile.getUserName();
                    LOG.debug("Save external avatar for: " + userName);
                    m_userProfileService.saveExtAvatar(userName, profile.getExtAvatar());
                    //refresh user profile every time execute a save, to keep any concurent changes in the profile
                    UserProfile currentProfile = m_userProfileService.getUserProfileByUsername(userName);
                    currentProfile.setExtAvatarSyncDate(Calendar.getInstance().getTime());
                    m_userProfileService.saveUserProfile(currentProfile);
                } catch (AvatarUploadException e) {
                    LOG.error("Cannot upload external avatar");
                }
            }
        }
    }
}
