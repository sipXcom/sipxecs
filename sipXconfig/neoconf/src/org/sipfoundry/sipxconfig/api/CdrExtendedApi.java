package org.sipfoundry.sipxconfig.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.commons.extendedcdr.ExtendedCdrBean;


@Path("/extendedcdrs/")
@Description("Extended CDR Management REST API")
public interface CdrExtendedApi extends BaseCdrApi {
    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newCdr(@Description("ExtendedCdr bean to save") ExtendedCdrBean extendedCdrBean);
    
    @Path("delete/{prefix}")
    @DELETE
    public Response deletePrefixCdrHistory(@Description("Prefix") @PathParam("prefix") String prefix);
}
