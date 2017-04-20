/*
 * Copyright (c) 2013 SibTelCom, JSC (SIPLABS Communications). All rights reserved.
 * Contributed to SIPfoundry and eZuce, Inc. under a Contributor Agreement.
 *
 * Developed by Konstantin S. Vishnivetsky
 *
 * This library or application is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License (AGPL) as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This library or application is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License (AGPL) for
 * more details.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.SpecialUser;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.mwi.Mwi;
import org.sipfoundry.sipxconfig.permission.PermissionName;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.setting.SettingEntry;
import org.sipfoundry.sipxconfig.speeddial.SpeedDial;
import org.sipfoundry.sipxconfig.speeddial.SpeedDialManager;

public class YealinkLineDefaults {
	private SpeedDialManager m_speedDialManager;
    private final DeviceDefaults m_defaults;
    private final Line m_line;
    private final String m_mac;
    private static final String PROVISION_AOR = "%s~%s";

    YealinkLineDefaults(DeviceDefaults defaults, Line line, String mac, SpeedDialManager speedDialManager) {
        m_defaults = defaults;
        m_line = line;
        m_mac = mac;
        m_speedDialManager = speedDialManager;
    }

    @SettingEntry(paths = {
        YealinkConstants.LABEL_V6X_SETTING,
        YealinkConstants.LABEL_V7X_SETTING,
        YealinkConstants.LABEL_V8X_SETTING
        })
    public String getLineLabel() {
        // load old default for backward compatibility
        String userName = getUserName();
        User user = m_line.getUser();
        // Check if this is a special provisional line
        // if so overwrite label with ID: xxx
        if(user != null && userName.startsWith(
            SpecialUser.SpecialUserType.PHONE_PROVISION.getUserName())) {
            userName = user.getDisplayName();
        }
        return userName;
    }
    
    @SettingEntry(paths = {
            YealinkConstants.USER_ID_V6X_SETTING,
            YealinkConstants.USER_ID_V7X_SETTING,
            YealinkConstants.USER_ID_V8X_SETTING
            })
    public String getUserName() {
        String userName = null;
        User user = m_line.getUser();
        if (user != null) {
            userName = user.getUserName();
            
            if (userName.startsWith(
                SpecialUser.SpecialUserType.PHONE_PROVISION.getUserName())) {
                // If we have a provision user we have to add the specific short HASH
                userName = String.format(PROVISION_AOR,
                    SpecialUser.SpecialUserType.PHONE_PROVISION.getUserName(),
                    user.getLastName());
                
            }
        }
        return userName;
    }

    @SettingEntry(paths = {
            YealinkConstants.AUTH_ID_V6X_SETTING,
            YealinkConstants.AUTH_ID_V7X_SETTING,
            YealinkConstants.AUTH_ID_V8X_SETTING
            })
    public String getAuthId() {
        String userName = null;
        User user = m_line.getUser();
        if (user != null) {
            userName = user.getUserName();
        }
        return userName + "/" + m_mac;
    }

    @SettingEntry(paths = {
            YealinkConstants.DISPLAY_NAME_V6X_SETTING,
            YealinkConstants.DISPLAY_NAME_V7X_SETTING,
            YealinkConstants.DISPLAY_NAME_V8X_SETTING
            })
    public String getDisplayName() {
        String displayName = null;
        User user = m_line.getUser();
        if (user != null) {
            displayName = user.getDisplayName();
        }
        return displayName;
    }

    @SettingEntry(paths = {
            YealinkConstants.PASSWORD_V6X_SETTING,
            YealinkConstants.PASSWORD_V7X_SETTING,
            YealinkConstants.PASSWORD_V8X_SETTING
            })
    public String getPassword() {
        String password = null;
        User user = m_line.getUser();
        if (user != null) {
            password = user.getSipPassword();
        }
        return password;
    }

    @SettingEntry(paths = {
            YealinkConstants.REGISTRATION_SERVER_HOST_V6X_SETTING,
            YealinkConstants.REGISTRATION_SERVER_HOST_V7X_SETTING,
            YealinkConstants.REGISTRATION_SERVER_HOST_V8X_SETTING
            })
    public String getRegistrationServer() {
        return m_defaults.getDomainName();
    }
/*@
    Returns SIP port.
    Port 5060 is internal SIP port in sipXecs by default
*/
    @SettingEntry(paths = {
            YealinkConstants.REGISTRATION_SERVER_PORT_V6X_SETTING,
            YealinkConstants.OUTBOUND_PORT_V6X_SETTING,
            YealinkConstants.BACKUP_OUTBOUND_PORT_V6X_SETTING
            })
    public Integer getRegistrationServerPort() {
        return 5060;
    }

    @SettingEntry(paths = {
            YealinkConstants.OUTBOUND_HOST_V6X_SETTING,
            YealinkConstants.BACKUP_OUTBOUND_HOST_V6X_SETTING,
            YealinkConstants.OUTBOUND_HOST_V7X_SETTING,
            YealinkConstants.BACKUP_OUTBOUND_HOST_V7X_SETTING,
            YealinkConstants.OUTBOUND_HOST_V7X_SETTING,
            YealinkConstants.BACKUP_OUTBOUND_HOST_V7X_SETTING
            })
    public String getOutboundHost() {
        Address outboundProxyAdress = m_defaults.getProxyAddress();
        if (null == outboundProxyAdress) {
            return "";
        }
        return outboundProxyAdress.getAddress();
    }

    @SettingEntry(paths = {
            YealinkConstants.VOICE_MAIL_NUMBER_V6X_SETTING,
            YealinkConstants.VOICE_MAIL_NUMBER_V7X_SETTING
            })
    public String getVoiceMail() {
        String voicemail = null;
        User u = m_line.getUser();
        if (u != null) {
            voicemail = m_defaults.getVoiceMail();
        }
        return voicemail;
    }

    @SettingEntry(paths = {
            YealinkConstants.ADVANCED_MUSIC_SERVER_URI_V6X_SETTING,
            YealinkConstants.ADVANCED_MUSIC_SERVER_URI_V7X_SETTING })
    public String getMusicServerUri() {
        String mohUri;
        User u = m_line.getUser();
        if (u != null) {
            mohUri = u.getMusicOnHoldUri();
        } else {
            mohUri = m_defaults.getMusicOnHoldUri();
        }
        return mohUri;
    }

	@SettingEntry(paths = {
            YealinkConstants.ADVANCED_BLF_SERVER_URI_V7X_SETTING,
            YealinkConstants.ADVANCED_BLF_SERVER_URI_V8X_SETTING})
    public String getRlsServerUri() {
        String rlsUri = "";
        User u = m_line.getUser();
        if (u != null) {
            SpeedDial sd = m_speedDialManager.getSpeedDialForUser(m_line.getUser(), false);
        	if(sd != null && sd.isBlf()) {
                rlsUri = SipUri.format("~~rl~C~"+u.getUserName(), m_defaults.getDomainName(), false);
        	}
        }
        return rlsUri;
    }

	@SettingEntry(paths = {
            YealinkConstants.ACD_USER_ID_V7X_SETTING,
            YealinkConstants.ACD_USER_ID_V8X_SETTING})
    public String getAcdUserId() {
        String acdUserId;
        User u = m_line.getUser();
        if (u != null) {
            acdUserId = u.getUserName();
        } else {
            acdUserId = "";
        }
        return acdUserId;
    }
	
	@SettingEntry(paths = {
            YealinkConstants.SUBSCRIBE_MWI_V6X_SETTING,
            YealinkConstants.SUBSCRIBE_MWI_V7X_SETTING,
            YealinkConstants.SUBSCRIBE_MWI_V8X_SETTING})
    public boolean getSubscribeMwi() {
        boolean isServiceEnabled = m_line.getPhone().getFeatureManager().isFeatureEnabled(Mwi.FEATURE);
        if (isServiceEnabled && m_line.getUser() != null) {
        	return m_line.getUser().hasPermission(PermissionName.VOICEMAIL);
        } else {
        	return false;
        }
    }
}
