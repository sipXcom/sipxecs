package org.sipfoundry.sipxconfig.api;

import javax.ws.rs.Path;

import org.apache.cxf.jaxrs.model.wadl.Description;

@Path("/cdrs/")
@Description("CDR Management REST API")
public interface CdrApi extends BaseCdrApi {

}
