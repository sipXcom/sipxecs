package org.sipfoundry.sipxconfig.api.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.sipfoundry.sipxconfig.api.AutoAttendantApi;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantBean;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantGenericSettingsBean;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantList;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantMenuBean;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantSpecialModeBean;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.branch.BranchManager;
import org.sipfoundry.sipxconfig.common.DialPad;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendantManager;
import org.sipfoundry.sipxconfig.dialplan.attendant.AutoAttendantSettings;

import org.sipfoundry.sipxconfig.dialplan.AttendantMenu;
import org.sipfoundry.sipxconfig.dialplan.AttendantMenuAction;
import org.sipfoundry.sipxconfig.dialplan.AttendantMenuItem;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendant;

public class AutoAttendantApiImpl implements AutoAttendantApi {
	private AutoAttendantManager m_autoAttendantManager;
	private BranchManager m_branchManager;

	@Override
	public Response getAutoAttendants() {
		List<AutoAttendant> autoAttendants = m_autoAttendantManager.getAutoAttendants();
		return buildAutoAttendantList(autoAttendants);
	}

	@Override
	public Response newAutoAttendant(AutoAttendantBean autoAttendantBean) {
        AutoAttendant autoAttendant = m_autoAttendantManager.newAutoAttendantWithDefaultGroup();
        autoAttendant.resetToFactoryDefault();
        convertToAutoAttendant(autoAttendantBean, autoAttendant);
        m_autoAttendantManager.storeAutoAttendant(autoAttendant);
        return Response.ok().entity(autoAttendant.getId()).build();
	}

    @Override
    public Response getAutoAttendant(String name) {
        AutoAttendant autoAttendant = m_autoAttendantManager.getAutoAttendantByName(name);
        return getAutoAttendant(autoAttendant);
    }

    private Response getAutoAttendant(AutoAttendant autoAttendant) {
        if (autoAttendant != null) {
            return Response.ok().entity(AutoAttendantBean.convertAutoAttendant(autoAttendant)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
    @Override
    public Response updateAutoAttendant(String name, AutoAttendantBean aaBean) {
        AutoAttendant autoAttendant = m_autoAttendantManager.getAutoAttendantByName(name);
        return updateAutoAttendant(autoAttendant, aaBean);
    }

    public Response updateAutoAttendant(AutoAttendant autoAttendant, AutoAttendantBean autoAttendantBean) {
        if (autoAttendant != null) {
            convertToAutoAttendant(autoAttendantBean, autoAttendant);
            m_autoAttendantManager.storeAutoAttendant(autoAttendant);
            return Response.ok().entity(autoAttendant.getId()).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
    @Override
    public Response deleteAutoAttendant(String name) {
        m_autoAttendantManager.deleteAutoAttendant(m_autoAttendantManager.getAutoAttendantByName(name));
        return Response.ok().build();
    }    
    
    @Override
    public Response updateAutoAttendantGenericSettings(AutoAttendantGenericSettingsBean aaGenSettingsBean) {
        AutoAttendantSettings settings = m_autoAttendantManager.getSettings();
        settings.setSettingValue(AutoAttendantSettings.LIVE_DID, aaGenSettingsBean.getLiveDid());
        settings.setSettingValue(AutoAttendantSettings.DISABLE_PREFIX, aaGenSettingsBean.getDisablePrefix());
        settings.setSettingValue(AutoAttendantSettings.ENABLE_PREFIX, aaGenSettingsBean.getEnablePrefix());
        settings.setSettingValue(AutoAttendantSettings.EXPIRE_TIME, aaGenSettingsBean.getExpireTime());
        settings.setSettingValue(AutoAttendantSettings.EXTRA_DIGIT_TIMEOUT, aaGenSettingsBean.getExtraDigitTimeout());
        settings.setSettingValue(AutoAttendantSettings.FIRST_DIGIT_TIMEOUT, aaGenSettingsBean.getFirstDigitTimeout());
        settings.setSettingValue(AutoAttendantSettings.INTER_DIGIT_TIMEOUT, aaGenSettingsBean.getInterDigitTimeout());
        settings.setSettingValue(AutoAttendantSettings.MAX_DIGITS, aaGenSettingsBean.getMaxDigits());
        m_autoAttendantManager.saveSettings(settings);
        return Response.ok().entity(settings.getId()).build();
    }

    @Override
    public Response getAutoAttendantGenericSettings() {
        AutoAttendantSettings settings = m_autoAttendantManager.getSettings();
        AutoAttendantGenericSettingsBean aaGenericSettingsBean = new AutoAttendantGenericSettingsBean();
        aaGenericSettingsBean.setDisablePrefix(settings.getDisablePrefix());
        aaGenericSettingsBean.setEnablePrefix(settings.getEnablePrefix());
        aaGenericSettingsBean.setExpireTime(settings.getSettingValue(AutoAttendantSettings.EXPIRE_TIME));
        aaGenericSettingsBean.setExtraDigitTimeout(settings.getSettingValue(AutoAttendantSettings.EXTRA_DIGIT_TIMEOUT));
        aaGenericSettingsBean.setFirstDigitTimeout(settings.getSettingValue(AutoAttendantSettings.FIRST_DIGIT_TIMEOUT));
        aaGenericSettingsBean.setInterDigitTimeout(settings.getSettingValue(AutoAttendantSettings.INTER_DIGIT_TIMEOUT));
        aaGenericSettingsBean.setLiveDid(settings.getLiveDid());
        aaGenericSettingsBean.setMaxDigits(settings.getSettingValue(AutoAttendantSettings.MAX_DIGITS));
        return Response.ok().entity(aaGenericSettingsBean).build();
    }

    @Override
    public Response updateAutoAttendantSpecialMode(AutoAttendantSpecialModeBean aaSpecialModeBean) {
        AutoAttendant specialAa = m_autoAttendantManager.getAutoAttendantByName(aaSpecialModeBean.getSpecialAaName());
        m_autoAttendantManager.setAttendantSpecialMode(aaSpecialModeBean.isUseSpecialAa(), specialAa == null ? null : specialAa);
        return Response.ok().entity(aaSpecialModeBean.getSpecialAaName()).build();
    }

    @Override
    public Response getAutoAttendantSpecialMode() {
        m_autoAttendantManager.getSpecialMode();
        AutoAttendant specialAa = m_autoAttendantManager.getSelectedSpecialAttendant();
        AutoAttendantSpecialModeBean bean = new AutoAttendantSpecialModeBean();
        bean.setSpecialAaName(specialAa == null ? null : specialAa.getName());
        bean.setUseSpecialAa(m_autoAttendantManager.getSpecialMode());
        return Response.ok().entity(bean).build();
    }	
	
	public void convertToAutoAttendant(AutoAttendantBean autoAttendantBean, AutoAttendant autoAttendant) {
		String name = autoAttendantBean.getName();
		if (name != null) {
			autoAttendant.setName(name);
		}
		String allowDial = autoAttendantBean.getAllowDial();
		if (allowDial != null) {
			autoAttendant.setAllowDial(allowDial);
		}
		String denyDial = autoAttendantBean.getDenyDial();
		if (denyDial != null) {
			autoAttendant.setDenyDial(denyDial);
		}
		String description = autoAttendantBean.getDescription();
		if (description != null) {
			autoAttendant.setDescription(description);
		}
		String lang = autoAttendantBean.getLang();
		if (lang != null) {
			autoAttendant.setLanguage(lang);
		}
		String prompt = autoAttendantBean.getPrompt();
		if (prompt != null) {
			autoAttendant.setPrompt(prompt);
		}
		String promptsDirectory = autoAttendantBean.getPromptsDirectory();
		if (promptsDirectory != null) {
			autoAttendant.setPromptsDirectory(promptsDirectory);
		}
		String sysDirectory = autoAttendantBean.getSysDirectory();
		if (sysDirectory != null) {
			autoAttendant.setSysDirectory(sysDirectory);
		}
		String systemId = autoAttendantBean.getSystemId();
		if (systemId != null) {
			autoAttendant.setSystemId(systemId);
		}
		Set<Branch> locations = new HashSet<Branch>(); 
		List<String> locationNames = autoAttendantBean.getLocations();
		List<Branch> branches = m_branchManager.getBranches();
		for (Branch branch : branches) {
			if (locationNames.contains(branch.getName())) {
				locations.add(branch);
			}
		}
		autoAttendant.setLocations(locations);
		List<AutoAttendantMenuBean> autoAttendantMenuBeans = autoAttendantBean.getAutoattendantMenus();
		Map<DialPad, AttendantMenuItem> menuItems = new HashMap<DialPad, AttendantMenuItem> ();
		AttendantMenu menu = new AttendantMenu();
		for (AutoAttendantMenuBean bean : autoAttendantMenuBeans) {
			AttendantMenuItem item = new AttendantMenuItem();
			switch(bean.getAction()) {
				case "operator" :
					item.setAction(AttendantMenuAction.OPERATOR);
					break;
				case "dial_by_name" :
					item.setAction(AttendantMenuAction.DIAL_BY_NAME);
					break;
				case "repeat_prompt" :
					item.setAction(AttendantMenuAction.REPEAT_PROMPT);
					break;
				case "voicemail_access" :
					item.setAction(AttendantMenuAction.VOICEMAIL_LOGIN);
					break;
				case "disconnect" :
					item.setAction(AttendantMenuAction.DISCONNECT);
					break;
				case "transfer_to_another_aa_menu" :
					item.setAction(AttendantMenuAction.AUTO_ATTENDANT);
					break;
				case "transfer_out" :
					item.setAction(AttendantMenuAction.TRANSFER_OUT);
					break;
				case "voicemail_deposit" :
					item.setAction(AttendantMenuAction.VOICEMAIL_DEPOSIT);
				default :
					break;
			}
			item.setParameter(bean.getParameter());
			switch(bean.getDialPad()) {
				case "0" :
					menuItems.put(DialPad.NUM_0, item);
					break;
				case "1" :
					menuItems.put(DialPad.NUM_1, item);
					break;
				case "2" :
					menuItems.put(DialPad.NUM_2, item);
					break;
				case "3" :
					menuItems.put(DialPad.NUM_3, item);
					break;
				case "4" :
					menuItems.put(DialPad.NUM_4, item);
					break;
				case "5" :
					menuItems.put(DialPad.NUM_5, item);
					break;
				case "6" :
					menuItems.put(DialPad.NUM_6, item);
					break;
				case "7" :
					menuItems.put(DialPad.NUM_7, item);
					break;
				case "8" :
					menuItems.put(DialPad.NUM_8, item);
					break;
				case "9" :
					menuItems.put(DialPad.NUM_9, item);
					break;
				case "*" :
					menuItems.put(DialPad.STAR, item);
					break;
				case "#" :
					menuItems.put(DialPad.POUND, item);
					break;
				default :
					break;
			}
		}
		menu.setMenuItems(menuItems);
		autoAttendant.setMenu(menu);
		autoAttendant.setSettingValue(AutoAttendant.DTMF_INTERDIGIT_TIMEOUT, autoAttendantBean.getAutoattendantSettings().getDtmfInterTimeout());
		autoAttendant.setSettingValue(AutoAttendant.OVERALL_DIGIT_TIMEOUT, autoAttendantBean.getAutoattendantSettings().getDtmfOverallTimeout());
		autoAttendant.setSettingValue(AutoAttendant.MAX_DIGITS, autoAttendantBean.getAutoattendantSettings().getMaxDtmfNumberTones());
		autoAttendant.setSettingTypedValue(AutoAttendant.ON_TRANSFER_PLAY_PROMPT, autoAttendantBean.getAutoattendantSettings().isPlayPromptWhenTransfer());
		autoAttendant.setSettingValue(AutoAttendant.ONFAIL_NOINPUT_COUNT, autoAttendantBean.getAutoattendantSettings().getReplayCount());
		autoAttendant.setSettingValue(AutoAttendant.ONFAIL_NOMATCH_COUNT, autoAttendantBean.getAutoattendantSettings().getInvalidResponseCount());
		autoAttendant.setSettingTypedValue(AutoAttendant.ONFAIL_TRANSFER, autoAttendantBean.getAutoattendantSettings().isTransferOnFailures());
		autoAttendant.setSettingValue(AutoAttendant.ONFAIL_TRANSFER_EXT, autoAttendantBean.getAutoattendantSettings().getTransferExtension());
		autoAttendant.setSettingValue(AutoAttendant.ONFAIL_TRANSFER_PROMPT, autoAttendantBean.getAutoattendantSettings().getPromptToPlay());
	}

    private Response buildAutoAttendantList(List<AutoAttendant> autoAttendants) {
        if (autoAttendants != null) {
            return Response.ok().entity(AutoAttendantList.convertAutoAttendantList(autoAttendants)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
	public void setAutoAttendantManager(AutoAttendantManager autoAttendantManager) {
		m_autoAttendantManager = autoAttendantManager;
	}

	public void setBranchManager(BranchManager branchManager) {
		m_branchManager = branchManager;
	}	
}
