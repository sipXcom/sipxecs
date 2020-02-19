package org.sipfoundry.sipxconfig.apiban;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.apiban.model.BannedBean;

@Path("/banned")
@Produces({
    MediaType.APPLICATION_JSON
})
@Description("Banned REST API")
public interface BannedApi {

    @GET
    public BannedBean getBanned();
}
