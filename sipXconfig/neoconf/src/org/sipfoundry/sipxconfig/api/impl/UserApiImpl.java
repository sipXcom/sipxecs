/**
 * Copyright (c) 2015 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.api.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.sipfoundry.commons.userdb.profile.UserProfile;
import org.sipfoundry.sipxconfig.api.UserApi;
import org.sipfoundry.sipxconfig.api.model.GroupBean;
import org.sipfoundry.sipxconfig.api.model.GroupList;
import org.sipfoundry.sipxconfig.api.model.SettingsList;
import org.sipfoundry.sipxconfig.api.model.UserBean;
import org.sipfoundry.sipxconfig.api.model.UserProfileBean;
import org.sipfoundry.sipxconfig.api.model.UsersList;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.branch.BranchManager;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.springframework.beans.factory.annotation.Required;

public class UserApiImpl implements UserApi {
    private static final String COMMA = ",";
    private static final Log LOG = LogFactory.getLog(UserApiImpl.class);
    private CoreContext m_coreContext;
    private SettingDao m_settingDao;
    private BranchManager m_branchManager;

    public UserApiImpl() {
        //Required by: https://issues.apache.org/jira/browse/BEANUTILS-387
        ConvertUtils.register(new DateConverter(null), Date.class);
    }

    @Override
    public Response setUserSetting(String userNameOrAlias, String path, String value) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            user.setSettingValue(path, value);
            m_coreContext.saveUser(user);
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response deleteUserSetting(String userNameOrAlias, String path) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            Setting setting = user.getSettings().getSetting(path);
            setting.setValue(setting.getDefaultValue());
            m_coreContext.saveUser(user);
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response setUsersSetting(List<Attachment> attachments, String path) {
        for (Attachment attachment : attachments) {
            DataHandler dataHandler = attachment.getDataHandler();
            InputStream inputStream = null;
            BufferedReader br = null;
            try {
                // parse csv file
                inputStream = dataHandler.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] userSetting = line.split(COMMA);
                    User user = m_coreContext.loadUserByUserName(userSetting[0]);
                    if (user != null) {
                        Setting setting = user.getSettings().getSetting(path);
                        setting.setValue(userSetting[1]);
                        LOG.debug("User : " + userSetting[0] + " " + userSetting[1]);
                        m_coreContext.saveUser(user);
                    }
                }
            } catch (Exception ex) {
                return Response.status(Status.EXPECTATION_FAILED).build();
            } finally {
                IOUtils.closeQuietly(br);
                IOUtils.closeQuietly(inputStream);
            }
        }
        return Response.ok("upload success").build();
    }

    @Override
    public Response getUsers(Integer startId, Integer pageSize) {
        if (startId != null && pageSize != null) {
            return buildUserList(m_coreContext.loadUsersByPage(startId, pageSize));
        }
        return buildUserList(m_coreContext.loadUsers());
    }

    @Override
    public Response newUser(UserBean userBean) {
        User user = m_coreContext.newUser();
        convertToUser(userBean, user);
        m_coreContext.saveUser(user);
        return Response.ok().entity(user.getId()).build();
    }

    private Response buildUserList(List<User> users) {
        if (users != null) {
            return Response.ok().entity(UsersList.convertUserList(users)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    private void convertToUser(UserBean userBean, User user) {
        user.setUserName(userBean.getUserName());
        user.setFirstName(userBean.getFirstName());
        user.setLastName(userBean.getLastName());
        user.setAliases(userBean.getAliases());
        String branchName = userBean.getBranchName();
        if (branchName != null) {
            m_branchManager.getBranch(branchName);
            Branch branch = m_branchManager.getBranch(branchName);
            user.setBranch(branch);
        }
        user.setVoicemailPin(userBean.getVoicemailPin());
        user.setPintoken(userBean.getPintoken());
        user.setSipPassword(userBean.getSipPassword());
        UserProfileBean userProfileBean = userBean.getUserProfile();
        if (userProfileBean != null) {
            UserProfile userProfile = new UserProfile();
            UserProfileBean.convertToUserProfile(userBean.getUserProfile(), userProfile);
            user.setUserProfile(userProfile);
        }

        if (userBean.getGroups() != null) {
            String groupNames = user.getGroupsNames();
            for (GroupBean groupBean : userBean.getGroups()) {
                String groupName = groupBean.getName();
                if (!StringUtils.contains(groupNames, groupName)) {
                    // add group only if it doesn't already exist
                    Group g = m_settingDao.getGroupCreateIfNotFound(User.GROUP_RESOURCE_ID, groupBean.getName());
                    user.addGroup(g);
                }
            }
        }
    }

    @Override
    public Response getUser(String userNameOrAlias) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            return Response.ok().entity(UserBean.convertUser(user)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response updateUser(String userNameOrAlias, UserBean userBean) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            convertToUser(userBean, user);
            m_coreContext.saveUser(user);
            return Response.ok().entity(user.getId()).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response getUserGroups(String userNameOrAlias) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            return Response.ok().entity(GroupList.convertGroupList(user.getGroupsAsList(), null)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response removeUserGroups(String userNameOrAlias) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            user.setGroupsAsList(new ArrayList<Group>());
            m_coreContext.saveUser(user);
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response addUserInGroup(String userNameOrAlias, String groupName) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            Group g = m_settingDao.getGroupCreateIfNotFound(User.GROUP_RESOURCE_ID, groupName);
            user.addGroup(g);
            m_coreContext.saveUser(user);
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response removeUserFromGroup(String userNameOrAlias, String groupName) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            Group group = m_settingDao.getGroupByName(User.GROUP_RESOURCE_ID, groupName);
            if (group != null) {
                user.removeGroup(group);
                m_coreContext.saveUser(user);
                return Response.ok().build();
            }
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Required
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    @Required
    public void setSettingDao(SettingDao settingDao) {
        m_settingDao = settingDao;
    }

    public void setBranchManager(BranchManager branchManager) {
        m_branchManager = branchManager;
    }

    @Override
    public Response getUserSettings(String userNameOrAlias, HttpServletRequest request) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAlias);
        if (user != null) {
            Setting settings = user.getSettings();
            return Response.ok().entity(SettingsList.convertSettingsList(settings, request.getLocale())).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response getUserSetting(String userId, String path, HttpServletRequest request) {
        return ResponseUtils.buildSettingResponse(
            m_coreContext.loadUserByUserNameOrAlias(userId), path, request.getLocale());
    }

    @Override
    public Response deleteUser(String userNameOrAlias) {
        m_coreContext.deleteUsersByUserName(Collections.singleton(userNameOrAlias));
        return Response.ok().build();
    }
}
