package org.sipfoundry.sipxconfig.api;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.api.model.ContainerBean;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/containers/")
@Description("Phone Management REST API")
public interface ContainerApi {

    @GET
    public ContainerBean getContainerBean(String containerName);

    @Path("{containerName}/json")
    @GET
    public Response getContainer(String containerName);


}
