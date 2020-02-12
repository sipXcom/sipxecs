package org.sipfoundry.sipxconfig.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;

@Path("/apiban/")
@Produces({
    MediaType.APPLICATION_JSON
})
@Description("APIBAN Management REST API")
public interface ApibanApi {

    @Path("banned")
    @GET
    public Response getBanned();
}
