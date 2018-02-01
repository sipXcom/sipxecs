/**
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.api.model.IdsList;
import org.sipfoundry.sipxconfig.api.model.PhoneBean;

@Path("/phones/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("Phone Management REST API")
public interface PhoneApi {
    static final String PHONE_RES = "phone";

    @GET
    public Response getPhones(@Description("First Phone row") @QueryParam("start") Integer startId,
            @Description("Number of phones to be returned") @QueryParam("limit") Integer limit);

    @Path("line")
    @GET
    public Response getPhonesLine(@Description("Phones with line given lineFilter param")
            @QueryParam("lineFilter") String lineFilter);

    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newPhone(@Description("Phone bean to save") PhoneBean phone);

    @Path("models")
    @GET
    public Response getPhoneModels();

    @Path("{phoneId}")
    @GET
    public Response getPhone(@Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId);

    @Path("{phoneId}")
    @DELETE
    public Response deletePhone(@Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId);

    @Path("{phoneId}/sendProfile")
    @PUT
    public Response sendPhoneProfile(@Description("Phone internal id or MAC address")
        @PathParam("phoneId") String phoneId);

    @Path("{phoneId}/sendProfile/restart")
    @PUT
    public Response sendPhoneProfileRestart(@Description("Phone internal id or MAC address")
        @PathParam("phoneId") String phoneId);

    @Path("sendProfile")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response sendPhonesProfile(IdsList idsList);

    @Path("sendProfile/restart")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response sendPhonesProfileRestart(IdsList idsList);

    @Path("sendProfile/all")
    @PUT
    public Response sendPhonesProfile();

    @Path("sendProfile/all/restart")
    @PUT
    public Response sendPhonesProfileRestart();

    @Path("user/id/{userId}/sendProfile")
    @PUT
    public Response sendUserIdPhonesProfile(@Description("User Id to which returned phones belong to")
        @PathParam("userId") String userId);

    @Path("user/id/{userId}/sendProfile/restart")
    @PUT
    public Response sendUserIdPhonesProfileRestart(@Description("User Id to which returned phones belong to")
        @PathParam("userId") String userId);

    @Path("user/name/{userName}/sendProfile")
    @PUT
    public Response sendUserNamePhonesProfile(@Description("User Name to which returned phones belong to")
        @PathParam("userName") String userName);

    @Path("user/name/{userName}/sendProfile/restart")
    @PUT
    public Response sendUserNamePhonesProfileRestart(@Description("User Name to which returned phones belong to")
        @PathParam("userName") String userName);

    @Path("phoneGroup/id/{phoneGroupId}/sendProfile")
    @PUT
    public Response sendPhoneGroupIdPhonesProfile(@Description("Phone group Id to which returned phones belong to")
        @PathParam("phoneGroupId") String phoneGroupId);

    @Path("phoneGroup/id/{phoneGroupId}/sendProfile/restart")
    @PUT
    public Response sendPhoneGroupIdPhonesProfileRestart(
        @Description("Phone group Id to which returned phones belong to")
        @PathParam("phoneGroupId") String phoneGroupId);

    @Path("phoneGroup/name/{phoneGroupName}/sendProfile")
    @PUT
    public Response sendPhoneGroupNamePhonesProfile(
        @Description("Phone group Name to which returned phones belong to")
        @PathParam("phoneGroupName") String phoneGroupName);

    @Path("phoneGroup/name/{phoneGroupName}/sendProfile/restart")
    @PUT
    public Response sendPhoneGroupNamePhonesProfileRestart(
        @Description("Phone group Name to which returned phones belong to")
        @PathParam("phoneGroupName") String phoneGroupName);

    @Path("{phoneId}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updatePhone(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId,
            @Description("Phone bean to save") PhoneBean phone);

    @Path("{phoneId}/groups")
    @GET
    public Response getPhoneGroups(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId);

    @Path("{phoneId}/groups")
    @DELETE
    public Response removePhoneGroups(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId);

    @Path("{phoneId}/groups/{groupName}")
    @POST
    public Response addPhoneInGroup(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId,
            @Description("Phone Group name") @PathParam("groupName") String groupName);

    @Path("{phoneId}/groups/{groupName}")
    @DELETE
    public Response removePhoneFromGroup(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId,
            @Description("Phone Group name") @PathParam("groupName") String groupName);

    @Path("{phoneId}/settings")
    @GET
    public Response getPhoneSettings(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId,
            @Context HttpServletRequest request);

    @Path("{phoneId}/settings/{path:.*}")
    @GET
    public Response getPhoneSetting(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId,
            @Description("Path to Phone setting") @PathParam("path") String path, @Context HttpServletRequest request);

    @Path("{phoneId}/settings/{path:.*}")
    @PUT
    @Consumes({
        MediaType.TEXT_PLAIN
    })
    public Response setPhoneSetting(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId,
            @Description("Path to Phone setting") @PathParam("path") String path, String value);

    @Path("{phoneId}/settings/{path:.*}")
    @DELETE
    public Response deletePhoneSetting(
            @Description("Phone internal id or MAC address") @PathParam("phoneId") String phoneId,
            @Description("Path to Phone setting") @PathParam("path") String path);

}
