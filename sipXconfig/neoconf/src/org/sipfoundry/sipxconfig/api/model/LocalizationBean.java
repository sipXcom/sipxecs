package org.sipfoundry.sipxconfig.api.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.sipfoundry.sipxconfig.localization.Localization;

@XmlRootElement(name = "localization")
public class LocalizationBean {
    private int m_id;
    private String m_language;
    private String m_region;
        
    public int getId() {
        return m_id;
    }
    public void setId(int id) {
        m_id = id;
    }
    public String getLanguage() {
        return m_language;
    }
    public void setLanguage(String language) {
        m_language = language;
    }
    public String getRegion() {
        return m_region;
    }
    public void setRegion(String region) {
        m_region = region;
    }
    
    public static LocalizationBean convertLocalization(Localization localization) {
        LocalizationBean bean = new LocalizationBean();
        bean.setId(localization.getId());
        bean.setLanguage(localization.getLanguage());
        bean.setRegion(localization.getRegion());
        return bean;
    }
}
