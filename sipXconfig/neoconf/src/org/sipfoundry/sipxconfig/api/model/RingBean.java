package org.sipfoundry.sipxconfig.api.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RingBean {
	private String m_typeStr;
	private String m_userName;
	private boolean m_enabled;
	private int m_expiration;

	public String getTypeStr() {
		return m_typeStr;
	}

	public void setTypeStr(String typeStr) {
		this.m_typeStr = typeStr;
	}	

	public String getUserName() {
		return m_userName;
	}

	public void setUserName(String userName) {
		m_userName = userName;
	}

	public boolean isEnabled() {
		return m_enabled;
	}

	public void setEnabled(boolean m_enabled) {
		this.m_enabled = m_enabled;
	}

	public int getExpiration() {
		return m_expiration;
	}

	public void setExpiration(int expiration) {
		m_expiration = expiration;
	}
}
