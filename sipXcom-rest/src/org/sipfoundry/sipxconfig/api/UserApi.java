package org.sipfoundry.sipxconfig.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.model.wadl.Description;

@Path("/users/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("User Management REST API")
public interface UserApi {
    static final String USER_RES = "user";

    @Path("{userName}/settings/{path:.*}")
    @PUT
    @Consumes({
        MediaType.TEXT_PLAIN
    })
    public Response setUserSetting(
            @Description("User extension") @PathParam("userName") String userName,
            @Description("Path to User setting") @PathParam("path") String path, String value);

    @Path("{userName}/settings/{path:.*}")
    @DELETE
    public Response deleteUserSetting(
            @Description("User extension") @PathParam("userName") String userName,
            @Description("Path to User setting") @PathParam("path") String path);
    @PUT
    @Path("/upload/settings/{path:.*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response setUsersSetting(List<Attachment> attachments,
            @Description("Path to User setting") @PathParam("path") String path);
}
