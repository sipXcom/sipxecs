package org.sipfoundry.sipxconfig.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantBean;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantGenericSettingsBean;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantSpecialModeBean;
import org.sipfoundry.sipxconfig.api.model.UserBean;
import org.apache.cxf.jaxrs.model.wadl.Description;

@Path("/autoattendant/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
public interface AutoAttendantApi {

    @GET
    public Response getAutoAttendants();
    
    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newAutoAttendant(@Description("AutoAttendant bean to save") AutoAttendantBean callGroupBean);
    
    @Path("{name}")
    @GET
    public Response getAutoAttendant(@Description("AutoAttendant name") @PathParam("name") String name);    
    
    @Path("{name}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateAutoAttendant(
            @Description("AutoAttendant name") @PathParam("name") String name,
            @Description("AutoAttendant to save") AutoAttendantBean aaBean);
    
    @Path("settings")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateAutoAttendantGenericSettings (
            @Description("AutoAttendant generic settings to save") AutoAttendantGenericSettingsBean aaGenSettingsBean);
    
    @Path("settings")
    @GET
    public Response getAutoAttendantGenericSettings();     
    
    @Path("specialmode")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateAutoAttendantSpecialMode (
            @Description("AutoAttendant special mode to save") AutoAttendantSpecialModeBean aaSpecialModeBean);
    
    @Path("specialmode")
    @GET
    public Response getAutoAttendantSpecialMode();     
    
}
