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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.api.model.UserBean;

@Path("/users/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("User Management REST API")
public interface UserApi {
    @GET
    public Response getUsers(@Description("First User row") @QueryParam("start") Integer startId,
            @Description("Number of users to be returned") @QueryParam("limit") Integer limit);

    @Path("{userNameOrAlias}/settings")
    @GET
    public Response getUserSettings(
            @Description("User name or alias") @PathParam("userNameOrAlias") String userNameOrAlias,
            @Context HttpServletRequest request);

    @Path("{userNameOrAlias}/settings/{path:.*}")
    @GET
    public Response getUserSetting(
            @Description("User name or alias") @PathParam("userNameOrAlias") String userNameOrAlias,
            @Description("Path to User setting") @PathParam("path") String path, @Context HttpServletRequest request);

    @Path("{userNameOrAlias}/settings/{path:.*}")
    @PUT
    @Consumes({
        MediaType.TEXT_PLAIN
    })
    public Response setUserSetting(
            @Description("User extension") @PathParam("userNameOrAlias") String userNameOrAlias,
            @Description("Path to User setting") @PathParam("path") String path, String value);

    @Path("{userNameOrAlias}/settings/{path:.*}")
    @DELETE
    public Response deleteUserSetting(
            @Description("User extension") @PathParam("userNameOrAlias") String userNameOrAlias,
            @Description("Path to User setting") @PathParam("path") String path);
    @PUT
    @Path("/upload/settings/{path:.*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response setUsersSetting(List<Attachment> attachments,
            @Description("Path to User setting") @PathParam("path") String path);

    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newUser(@Description("User bean to save") UserBean user);

    @Path("{userNameOrAlias}")
    @GET
    public Response getUser(@Description("User name") @PathParam("userNameOrAlias") String userNameOrAlias);

    @Path("{userNameOrAlias}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateUser(
            @Description("User name") @PathParam("userNameOrAlias") String userNameOrAlias,
            @Description("User bean to save") UserBean user);

    @Path("{userNameOrAlias}")
    @DELETE
    public Response deleteUser(@Description("User name or alias") @PathParam("userNameOrAlias") String userNameOrAlias);

    @Path("{userNameOrAlias}/groups")
    @GET
    public Response getUserGroups(
            @Description("User name or alias") @PathParam("userNameOrAlias") String userId);

    @Path("{userNameOrAlias}/groups")
    @DELETE
    public Response removeUserGroups(
            @Description("User name or alias") @PathParam("userNameOrAlias") String userId);

    @Path("{userNameOrAlias}/groups/{groupName}")
    @POST
    public Response addUserInGroup(
            @Description("User name or alias") @PathParam("userNameOrAlias") String userId,
            @Description("User Group name") @PathParam("groupName") String groupName);

    @Path("{userNameOrAlias}/groups/{groupName}")
    @DELETE
    public Response removeUserFromGroup(
            @Description("User name or alias") @PathParam("userNameOrAlias") String phoneId,
            @Description("Phone Group name") @PathParam("groupName") String groupName);
}
