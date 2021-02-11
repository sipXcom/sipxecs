package org.sipfoundry.sipxconfig.api.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.sipfoundry.sipxconfig.api.LocalizationApi;
import org.sipfoundry.sipxconfig.api.model.LocalizationBean;
import org.sipfoundry.sipxconfig.localization.Localization;
import org.sipfoundry.sipxconfig.localization.LocalizationContext;
import org.springframework.beans.factory.annotation.Required;

public class LocalizationApiImpl implements LocalizationApi {
    
    private LocalizationContext m_localizationContext;

    @Override
    public Response getLocalization() {
        Localization localization = m_localizationContext.getLocalization();
        return getLocalization(localization);
    }
    
    private Response getLocalization(Localization localization) {
        if (localization != null) {
            return Response.ok().entity(LocalizationBean.convertLocalization(localization)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }    
    
    @Override
    public Response updateLocalization(LocalizationBean localizationBean) {
        Localization localization = m_localizationContext.updateLocalization(localizationBean.getLanguage(), localizationBean.getRegion());           
        return Response.ok().entity(localization.getId()).build();
    }
    
    @Required
    public void setLocalizationContext(LocalizationContext localizationContext) {
        m_localizationContext = localizationContext;
    }    
}
