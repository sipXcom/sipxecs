/**
 * Copyright (c) 2016 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.api.impl;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.sipfoundry.sipxconfig.api.MyMohApi;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.moh.MusicOnHoldManager;
import org.sipfoundry.sipxconfig.permission.PermissionName;

public class MyMohApiImpl extends PromptsApiImpl implements MyMohApi {

    private MusicOnHoldManager m_manager;

    public void setMusicOnHoldManager(MusicOnHoldManager manager) {
        m_manager = manager;
    }

    @Override
    public Response getPrompts() {
        createPath();
        setPath(m_manager.getAudioDirectoryPath() + File.separator + getCurrentUser().getUserName());
        return super.getPrompts();
    }

    @Override
    public Response uploadPrompts(List<Attachment> attachments, HttpServletRequest request) {
        createPath();
        setPath(m_manager.getAudioDirectoryPath() + File.separator + getCurrentUser().getUserName());
        return super.uploadPrompts(attachments, request);
    }

    public Response downloadPrompt(String promptName) {
        createPath();
        setPath(m_manager.getAudioDirectoryPath() + File.separator + getCurrentUser().getUserName());
        return super.downloadPrompt(promptName);
    }

    public Response removePrompt(String promptName) {
        createPath();
        setPath(m_manager.getAudioDirectoryPath() + File.separator + getCurrentUser().getUserName());
        return super.removePrompt(promptName);
    }

    public Response streamPrompt(String promptName) {
        createPath();
        setPath(m_manager.getAudioDirectoryPath() + File.separator + getCurrentUser().getUserName());
        return super.streamPrompt(promptName);
    }

    @Override
    public Response getMohAudioSourceSetting(HttpServletRequest request) {
        return ResponseUtils.buildSettingResponse(getCurrentUser(), User.MOH_AUDIO_SOURCE_SETTING, request.getLocale());
    }

    @Override
    public Response setMohAudioSourceSetting(String value) {
        User user = getCurrentUser();
        user.setSettingValue(User.MOH_AUDIO_SOURCE_SETTING, value);
        getCoreContext().saveUser(user);
        return Response.ok().build();
    }

    @Override
    public Response deleteMohAudioSourceSetting() {
        User user = getCurrentUser();
        user.setSettingValue(User.MOH_AUDIO_SOURCE_SETTING, user.getSettingDefaultValue(User.MOH_AUDIO_SOURCE_SETTING));
        getCoreContext().saveUser(user);
        return Response.ok().build();
    }

    @Override
    public Response createCurrentUserPath() {
        createPath();
        return Response.ok().build();
    }

    private void createPath() {
        String path = m_manager.getAudioDirectoryPath() + File.separator + getCurrentUser().getUserName();
        File f = new File(path);
        if (!f.exists()) {
            f.mkdir();
        }
    }

    @Override
    public Response getUserMohPermission(HttpServletRequest request) {
        User user = getCurrentUser();
        Boolean hasPermission = user.hasPermission(PermissionName.MUSIC_ON_HOLD);
        if (hasPermission && user.getGroupsAsList().size() > 0) {
            hasPermission = user.hasPermission(PermissionName.GROUP_MUSIC_ON_HOLD);
        }

        return Response.ok().entity(hasPermission).build();
    }
}
