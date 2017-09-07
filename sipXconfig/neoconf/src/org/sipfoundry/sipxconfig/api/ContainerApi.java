package org.sipfoundry.sipxconfig.api;

import java.util.List;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.api.model.ContainerBean;
import org.sipfoundry.sipxconfig.api.model.ContainersBean;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/containers")
@Description("Container API")
@Produces({
    MediaType.APPLICATION_JSON
})
public interface ContainerApi {
    
    @GET
    @Path("/json")
    public List<ContainersBean> getContainersBeans(@QueryParam("all") int all);    
        
    @GET
    @Path("/json2")
    public Response getContainers();        

    @GET
    @Path("/json2/all")
    public Response getAllContainers();    
    
    @GET
    @Path("{containerName}/json")
    public ContainerBean getContainerBean(@Description("Container name") @PathParam("containerName") String containerName);


    @GET
    @Path("{containerName}/json2")
    public Response getContainer(@Description("Container name") @PathParam("containerName") String containerName);
    
    @POST
    @Path("{containerName}/restart")
    public Response restartContainer(@Description("Container name") @PathParam("containerName") String containerName);

}
