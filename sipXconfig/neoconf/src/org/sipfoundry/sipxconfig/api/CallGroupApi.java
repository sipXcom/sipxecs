package org.sipfoundry.sipxconfig.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.api.model.CallGroupBean;

@Path("/callgroups/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
public interface CallGroupApi {

    @GET
    public Response getCallGroups();
    
    @Path("{callGroupExtension}")
    @GET
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response getCallGroup(
            @Description("Call Group Extension") @PathParam("callGroupExtension") String callGroupExtension);    
    
    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newCallGroup(@Description("CallGroup bean to save") CallGroupBean callGroupBean);
    
    @Path("{callGroupExtension}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateCallGroup(
            @Description("Call Group Extension") @PathParam("callGroupExtension") String callGroupExtension,
            @Description("Call group bean to save") CallGroupBean callGroup);

    @Path("{callGroupExtension}")
    @DELETE
    public Response deleteCallGroup(@Description("Call Group Extension") @PathParam("callGroupExtension") String callGroupExtension);
}
