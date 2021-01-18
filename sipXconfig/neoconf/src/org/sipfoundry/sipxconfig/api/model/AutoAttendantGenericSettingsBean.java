package org.sipfoundry.sipxconfig.api.model;

public class AutoAttendantGenericSettingsBean {
    private String m_liveDid;
    private String m_enablePrefix;
    private String m_disablePrefix;
    private String m_expireTime;
    private String m_maxDigits;
    private String m_firstDigitTimeout;
    private String m_interDigitTimeout;
    private String m_extraDigitTimeout;
    public String getLiveDid() {
        return m_liveDid;
    }
    public void setLiveDid(String liveDid) {
        m_liveDid = liveDid;
    }
    public String getEnablePrefix() {
        return m_enablePrefix;
    }
    public void setEnablePrefix(String enablePrefix) {
        m_enablePrefix = enablePrefix;
    }
    public String getDisablePrefix() {
        return m_disablePrefix;
    }
    public void setDisablePrefix(String disablePrefix) {
        m_disablePrefix = disablePrefix;
    }
    public String getExpireTime() {
        return m_expireTime;
    }
    public void setExpireTime(String expireTime) {
        m_expireTime = expireTime;
    }
    public String getMaxDigits() {
        return m_maxDigits;
    }
    public void setMaxDigits(String maxDigits) {
        m_maxDigits = maxDigits;
    }
    public String getFirstDigitTimeout() {
        return m_firstDigitTimeout;
    }
    public void setFirstDigitTimeout(String firstDigitTimeout) {
        m_firstDigitTimeout = firstDigitTimeout;
    }
    public String getInterDigitTimeout() {
        return m_interDigitTimeout;
    }
    public void setInterDigitTimeout(String interDigitTimeout) {
        m_interDigitTimeout = interDigitTimeout;
    }
    public String getExtraDigitTimeout() {
        return m_extraDigitTimeout;
    }
    public void setExtraDigitTimeout(String extraDigitTimeout) {
        m_extraDigitTimeout = extraDigitTimeout;
    }    
}
