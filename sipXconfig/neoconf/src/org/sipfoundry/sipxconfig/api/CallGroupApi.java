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
import org.sipfoundry.sipxconfig.api.model.StringList;

@Path("/callgroups/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
public interface CallGroupApi {

    @GET
    public Response getCallGroups();
    
    @Path("prefix/{prefix}")
    @GET
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response getPrefixedCallGroups(@Description("Call Group Extension prefix") @PathParam("prefix") String prefix);
    
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

    @Path("{callGroupExtension}/duplicate/{assignedExtension}")
    @POST
    public Response duplicateCallGroup(@Description("Call Group Extension") @PathParam("callGroupExtension") String callGroupExtension, 
    		@Description("Extension to assign to new group") @PathParam("assignedExtension") String assignedExtension);
    
    /** rings 1,2,3,4,5 where ringExtension = 3 will be 4,5,1,2,3*/
    @Path("{callGroupExtension}/rotate/{ringExtension}")
    @POST
    public Response rotateRings(@Description("Call Group Extension") @PathParam("callGroupExtension") String callGroupExtension, 
    		@Description("Ring extension to rotate rings") @PathParam("ringExtension") String ringExtension);
    
    @Path("rotate")
    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })    
    public Response rotateRings(@Description("Call Group Extensions") StringList callGroupExtensions);    
        
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
