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

import java.util.Collections;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.sipfoundry.sipxconfig.api.UserGroupApi;
import org.sipfoundry.sipxconfig.api.model.GroupBean;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.springframework.beans.factory.annotation.Required;

public class UserGroupApiImpl extends GroupApiImpl implements UserGroupApi {
    private CoreContext m_coreContext;
    private SettingDao m_settingDao;

    @Override
    public Response getUserGroups() {
        return buildGroupList(m_coreContext.getGroups(),
            m_settingDao.getGroupMemberCountIndexedByGroupId(User.class));
    }

    @Override
    public Response newUserGroup(GroupBean groupBean) {
        Group group = new Group();
        GroupBean.convertToUserGroup(groupBean, group);
        m_coreContext.storeGroup(group);
        return Response.ok().entity(group.getId()).build();
    }

    @Override
    public Response getUserGroup(String userGroupId) {
        Group group = getUserGroupByIdOrName(userGroupId);
        if (group != null) {
            GroupBean groupBean = new GroupBean();
            GroupBean.convertGroup(group, groupBean);
            return Response.ok().entity(groupBean).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response deleteUserGroup(String id) {
        Group group = getUserGroupByIdOrName(id);
        if (group != null) {
            if (m_coreContext.deleteGroups(Collections.singletonList(group.getId()))) {
                return Response.status(Status.FORBIDDEN).build();
            }
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    private Group getUserGroupByIdOrName(String id) {
        Group group = null;
        try {
            int groupId = Integer.parseInt(id);
            group = m_settingDao.loadGroup(groupId);
        } catch (NumberFormatException e) {
            group = m_coreContext.getGroupByName(id, false);
        }
        return group;
    }

    @Override
    public Response updateUserGroup(String groupId, GroupBean groupBean) {
        Group group = getUserGroupByIdOrName(groupId);
        if (group != null) {
            GroupBean.convertToUserGroup(groupBean, group);
            m_coreContext.storeGroup(group);
            return Response.ok().entity(group.getId()).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response moveUserGroupUp(String groupId) {
        Group group = getUserGroupByIdOrName(groupId);
        if (group != null) {
            m_settingDao.moveGroups(m_coreContext.getGroups(), Collections.singletonList(group.getId()), -1);
            return Response.ok().entity(group.getId()).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response moveUserGroupDown(String groupId) {
        Group group = getUserGroupByIdOrName(groupId);
        if (group != null) {
            m_settingDao.moveGroups(m_coreContext.getGroups(), Collections.singletonList(group.getId()), 1);
            return Response.ok().entity(group.getId()).build();
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
}
