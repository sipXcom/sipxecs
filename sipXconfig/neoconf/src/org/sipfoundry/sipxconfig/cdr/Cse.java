package org.sipfoundry.sipxconfig.cdr;
/**
 * the purpose is to map call_state_events table
 * @author mirceac
 *
 */
public class Cse {
	private String m_fromUrl;
	private String m_toUrl;
	
	public Cse(String fromUrl, String toUrl) {
		m_fromUrl = fromUrl;
		m_toUrl = toUrl;
	}
	
	public String getFromUrl() {
		return m_fromUrl;
	}
	public void setFromUrl(String fromUrl) {
		m_fromUrl = fromUrl;
	}
	public String getToUrl() {
		return m_toUrl;
	}
	public void setToUrl(String toUrl) {
		this.m_toUrl = toUrl;
	}
	public String getFrom() {
		return m_fromUrl.split(" ")[0];
	}
	public String getTo() {
		return m_toUrl.split(" ")[0];
	}
}
