/**
 * Copyright (c) 2017 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.api;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.model.wadl.Description;

@Path("/my/greetings/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("My Greetings Management REST API")

public interface MyGreetingsApi {

    @Path("{name}/{extension}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadGreeting(@Description("Greeting type name") @PathParam("name") String name,
        @Description("Greeting type extension mp3 or wav") @PathParam("extension") String extension,
        Attachment attachment, @Context HttpServletRequest request);

    @Path("{name}/{extension}/newFilename")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadGreetingSetNewFilename(@Description("Greeting type name") @PathParam("name") String name,
        @Description("Greeting type extension mp3 or wav") @PathParam("extension") String extension,
        Attachment attachment, @Context HttpServletRequest request);

    @Path("{name}/{extension}")
    @DELETE
    public Response removeGreeting(@Description("Greeting type name") @PathParam("name") String name,
        @Description("Greeting type extension mp3 or wav") @PathParam("extension") String extension,
        @Context HttpServletRequest request);

    @Path("{name}/{extension}")
    @GET
    @Produces({
        "audio/x-wav", "audio/mpeg"
    })
    public Response streamGreeting(@Description("Greeting type name") @PathParam("name") String name,
        @Description("Greeting type extension mp3 or wav") @PathParam("extension") String extension,
        @Context HttpServletRequest request);

    @Path("{name}/{extension}/newFilename")
    @GET
    public Response getGreetingNewFilename(@Description("Greeting type name") @PathParam("name") String name,
        @Description("Greeting type extension mp3 or wav") @PathParam("extension") String extension,
        @Context HttpServletRequest request);

    @Path("custom/{name}/{extension}")
    @GET
    public Response isCustomGreeting(@Description("Greeting type name") @PathParam("name") String name,
        @Description("Greeting type extension mp3 or wav") @PathParam("extension")  String extension,
        @Context HttpServletRequest request);
}
