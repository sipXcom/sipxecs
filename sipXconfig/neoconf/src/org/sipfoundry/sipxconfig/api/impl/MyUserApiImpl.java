/**
 * Copyright (c) 2017 eZuce, Inc. All rights reserved.
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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.sipfoundry.sipxconfig.api.MyUserApi;
import org.sipfoundry.sipxconfig.api.model.SettingsList;
import org.sipfoundry.sipxconfig.api.model.UserBean;
import org.sipfoundry.sipxconfig.common.User;
import org.springframework.beans.factory.annotation.Required;

public class MyUserApiImpl extends CurrentUser implements MyUserApi {
    /**
     * Only GET functionality is permitted for the moment due to security reasons
     */
    private UserApiImpl m_userApiImpl;

    @Override
    public Response getUserSettings(HttpServletRequest request) {
        User user = getCurrentUser();
        return m_userApiImpl.getUserSettings(user, request);
    }

    @Override
    public Response setUserSettings(SettingsList settingsList) {
        return Response.status(Status.FORBIDDEN).build();
    }

    @Override
    public Response getUserSetting(String path, HttpServletRequest request) {
        return ResponseUtils.buildSettingResponse(getCurrentUser(), path, request.getLocale());
    }

    @Override
    public Response setUserSetting(String path, String value) {
        return Response.status(Status.FORBIDDEN).build();
    }

    @Override
    public Response deleteUserSetting(String path) {
        return Response.status(Status.FORBIDDEN).build();

    }

    @Override
    public Response getUser() {
        User user = getCurrentUser();
        return m_userApiImpl.getUser(user);
    }

    @Override
    public Response updateUser(UserBean userBean) {
        return Response.status(Status.FORBIDDEN).build();
    }

    @Required
    public void setUserApiImpl(UserApiImpl userApiImpl) {
        m_userApiImpl = userApiImpl;
    }
}
