package org.sipfoundry.sipxconfig.api.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.common.DialPad;
import org.sipfoundry.sipxconfig.dialplan.AttendantMenu;
import org.sipfoundry.sipxconfig.dialplan.AttendantMenuItem;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendant;
import org.sipfoundry.sipxconfig.localization.LocalizationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

@XmlRootElement(name = "AutoAttendant")
public class AutoAttendantBean {
    private int m_id;
    private String m_name;
    private String m_description;
    private String m_prompt;
    private String m_systemId;
    private String m_promptsDirectory;
    private String m_sysDirectory;
    private String m_systemName;
    private String m_lang = LocalizationContext.DEFAULT;
    private String m_allowDial = StringUtils.EMPTY;
    private String m_denyDial = StringUtils.EMPTY;
    private List<String> m_locations = new ArrayList<String>();
    private List<AutoAttendantMenuBean> m_autoattendantMenus = new ArrayList<AutoAttendantMenuBean>();
    private AutoAttendantSettingsBean m_autoattendantSettings = new AutoAttendantSettingsBean(); 
    
    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }    
    
	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		m_name = name;
	}
	public String getDescription() {
		return m_description;
	}
	public void setDescription(String description) {
		m_description = description;
	}
	public String getPrompt() {
		return m_prompt;
	}
	public void setPrompt(String prompt) {
		m_prompt = prompt;
	}
	public String getSystemId() {
		return m_systemId;
	}
	public void setSystemId(String systemId) {
		m_systemId = systemId;
	}
	public String getPromptsDirectory() {
		return m_promptsDirectory;
	}
	public void setPromptsDirectory(String promptsDirectory) {
		m_promptsDirectory = promptsDirectory;
	}
	public String getSystemName() {
		return m_systemName;
	}
	public void setSystemName(String systemName) {
		m_systemName = systemName;
	}
	public String getLang() {
		return m_lang;
	}
	public void setLang(String lang) {
		m_lang = lang;
	}
	public String getAllowDial() {
		return m_allowDial;
	}
	public void setAllowDial(String allowDial) {
		m_allowDial = allowDial;
	}
	public String getDenyDial() {
		return m_denyDial;
	}
	public void setDenyDial(String denyDial) {
		m_denyDial = denyDial;
	}
	public List<String> getLocations() {
		return m_locations;
	}
	public void setLocations(List<String> locations) {
		m_locations = locations;
	}
	public List<AutoAttendantMenuBean> getAutoattendantMenus() {
		return m_autoattendantMenus;
	}
	public void setAutoattendantMenus(List<AutoAttendantMenuBean> autoattendantMenus) {
		m_autoattendantMenus = autoattendantMenus;
	}
	
    public String getSysDirectory() {
		return m_sysDirectory;
	}

	public void setSysDirectory(String sysDirectory) {
		m_sysDirectory = sysDirectory;
	}

	public static AutoAttendantBean convertAutoAttendant(AutoAttendant autoAttendant) {
    	AutoAttendantBean bean = new AutoAttendantBean();
        bean.setId(autoAttendant.getId());
        bean.setAllowDial(autoAttendant.getAllowDial());
        bean.setDenyDial(autoAttendant.getDenyDial());
        bean.setDescription(autoAttendant.getDescription());
        bean.setLang(autoAttendant.getLanguage());
        bean.setName(autoAttendant.getName());
        bean.setPrompt(autoAttendant.getPrompt());
        bean.setPromptsDirectory(autoAttendant.getPromptsDirectory());
        bean.setSystemId(autoAttendant.getSystemId());
        bean.setSystemName(autoAttendant.getSystemName());
        Set<Branch> locations = autoAttendant.getLocations();
        List<String> locationNames = new ArrayList<String>();
        for (Branch location : locations) {
        	locationNames.add(location.getName());
        }
        bean.setLocations(locationNames);
        List<AutoAttendantMenuBean> listMenu = new ArrayList<AutoAttendantMenuBean>();
        AttendantMenu menu = autoAttendant.getMenu();
        Map<DialPad, AttendantMenuItem> menuItems = menu.getMenuItems();
        for (DialPad dPad : menuItems.keySet()) {
        	AutoAttendantMenuBean autoAttendantMenuBean = new AutoAttendantMenuBean();
        	autoAttendantMenuBean.setDialPad(dPad.getName());
        	AttendantMenuItem ami = menuItems.get(dPad);
        	autoAttendantMenuBean.setAction(ami.getAction() != null ? ami.getAction().getName() : null);
        	autoAttendantMenuBean.setParameter(ami.getParameter());
        	listMenu.add(autoAttendantMenuBean);
        }
        bean.setAutoattendantMenus(listMenu);
        AutoAttendantSettingsBean aaSettings = new AutoAttendantSettingsBean();
        aaSettings.setDtmfInterTimeout(autoAttendant.getSettingValue(AutoAttendant.DTMF_INTERDIGIT_TIMEOUT));
        aaSettings.setMaxDtmfNumberTones(autoAttendant.getSettingValue(AutoAttendant.MAX_DIGITS));
        aaSettings.setDtmfOverallTimeout(autoAttendant.getSettingValue(AutoAttendant.OVERALL_DIGIT_TIMEOUT));
        aaSettings.setInvalidResponseCount(autoAttendant.getSettingValue(AutoAttendant.ONFAIL_NOMATCH_COUNT));
        aaSettings.setReplayCount(autoAttendant.getSettingValue(AutoAttendant.ONFAIL_NOINPUT_COUNT));
        aaSettings.setTransferOnFailures((Boolean)autoAttendant.getSettingTypedValue(AutoAttendant.ONFAIL_TRANSFER));
        aaSettings.setTransferExtension(autoAttendant.getSettingValue(AutoAttendant.ONFAIL_TRANSFER_EXT));
        aaSettings.setPromptToPlay(autoAttendant.getSettingValue(AutoAttendant.ONFAIL_TRANSFER_PROMPT));
        aaSettings.setPlayPromptWhenTransfer((Boolean)autoAttendant.getSettingTypedValue(AutoAttendant.ON_TRANSFER_PLAY_PROMPT));
        bean.setAutoattendantSettings(aaSettings);
        return bean;
    }

	public AutoAttendantSettingsBean getAutoattendantSettings() {
		return m_autoattendantSettings;
	}

	public void setAutoattendantSettings(AutoAttendantSettingsBean autoattendantSettings) {
		m_autoattendantSettings = autoattendantSettings;
	}
}
