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
package org.sipfoundry.sipxconfig.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.api.model.GroupBean;
import org.sipfoundry.sipxconfig.api.model.SettingsList;

@Path("/userGroups/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("User Groups Management REST API")

public interface UserGroupApi {
    @GET
    public Response getUserGroups();

    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newUserGroup(@Description("Phone Group bean to save") GroupBean userGroup);

    @Path("{userGroupId}")
    @GET
    public Response getUserGroup(@Description("User group id or name")
        @PathParam("userGroupId") String userGroupId);

    @Path("{groupId}")
    @DELETE
    public Response deleteUserGroup(@Description("Group internal id or name") @PathParam("groupId") String groupId);

    @Path("{groupId}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateUserGroup(
            @Description("Phone group internal id or name") @PathParam("groupId") String groupId,
            @Description("Phone group bean to save") GroupBean groupBean);

    @Path("{groupId}/up")
    @PUT
    public Response moveUserGroupUp(
            @Description("Phone group internal id or name") @PathParam("groupId") String groupId);

    @Path("{groupId}/down")
    @PUT
    public Response moveUserGroupDown(
            @Description("Phone group internal id or name") @PathParam("groupId") String groupId);

    @Path("{groupName}/settings")
    @GET
    public Response getGroupSettings(
            @Description("Group name") @PathParam("groupName") String groupName,
            @Context HttpServletRequest request);

    @Path("{groupName}/settings")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response setGroupSettings(
            @Description("Group name") @PathParam("groupName") String groupName,
            @Description("Settings to save") SettingsList settingsList);

    @Path("{groupName}/settings/{path:.*}")
    @GET
    public Response getGroupSetting(
            @Description("Group name") @PathParam("groupName") String groupName,
            @Description("Path to Group setting") @PathParam("path") String path, @Context HttpServletRequest request);

    @Path("{groupName}/settings/{path:.*}")
    @PUT
    @Consumes({
        MediaType.TEXT_PLAIN
    })
    public Response setGroupSetting(
            @Description("User extension") @PathParam("groupName") String groupName,
            @Description("Path to User setting") @PathParam("path") String path, String value);

    @Path("{groupName}/settings/{path:.*}")
    @DELETE
    public Response deleteGroupSetting(
            @Description("Group name") @PathParam("groupName") String name,
            @Description("Path to Group setting") @PathParam("path") String path);
}
