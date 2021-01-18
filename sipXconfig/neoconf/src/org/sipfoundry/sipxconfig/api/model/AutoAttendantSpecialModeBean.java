package org.sipfoundry.sipxconfig.api.model;

public class AutoAttendantSpecialModeBean {
    private boolean m_useSpecialAa;
    private String m_specialAaName;
    
    public boolean isUseSpecialAa() {
        return m_useSpecialAa;
    }
    public void setUseSpecialAa(boolean useSpecialAa) {
        m_useSpecialAa = useSpecialAa;
    }
    public String getSpecialAaName() {
        return m_specialAaName;
    }
    public void setSpecialAaName(String specialAaName) {
        m_specialAaName = specialAaName;
    } 
}
