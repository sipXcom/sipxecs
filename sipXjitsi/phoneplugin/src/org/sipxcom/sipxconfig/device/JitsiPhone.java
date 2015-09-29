/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.sipxconfig.device;

import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.Device;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.Profile;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.device.ProfileFilter;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class JitsiPhone extends Phone {
    public static final String MIME_TYPE_PLAIN = "text/plain";

    private static final String SIP_ACC_SETTING_ACCOUNT_UID = "net.java.sip.communicator.impl.protocol.sip/acc/ACCOUNT_UID";
    private static final String SIP_ACC_SETTING_PASSWORD = "net.java.sip.communicator.impl.protocol.sip/acc/PASSWORD";
    private static final String SIP_ACC_SETTING_SERVER_ADDRESS = "net.java.sip.communicator.impl.protocol.sip/connection/SERVER_ADDRESS";
    private static final String SIP_ACC_SETTING_USER_ID = "net.java.sip.communicator.impl.protocol.sip/acc/USER_ID";

    private static final String JABBER_ACC_SETTING_ACCOUNT_UID = "net.java.sip.communicator.impl.protocol.jabber/acc/ACCOUNT_UID";
    private static final String JABBER_ACC_SETTING_PASSWORD = "net.java.sip.communicator.impl.protocol.jabber/acc/PASSWORD";
    private static final String JABBER_ACC_SETTING_SERVER_ADDRESS = "net.java.sip.communicator.impl.protocol.jabber/connection/server_options/SERVER_ADDRESS";
    private static final String JABBER_ACC_SETTING_USER_ID = "net.java.sip.communicator.impl.protocol.jabber/acc/USER_ID";

    @Override
    protected void setLineInfo(Line line, LineInfo info) {
        line.setSettingValue(SIP_ACC_SETTING_USER_ID, info.getUserId());
        line.setSettingValue(SIP_ACC_SETTING_PASSWORD, info.getPassword());
        line.setSettingValue(SIP_ACC_SETTING_SERVER_ADDRESS, info.getRegistrationServer());
    }

    @Override
    protected LineInfo getLineInfo(Line line) {
        LineInfo info = new LineInfo();
        info.setUserId(line.getSettingValue(SIP_ACC_SETTING_USER_ID));
        info.setPassword(line.getSettingValue(SIP_ACC_SETTING_PASSWORD));
        info.setRegistrationServer(line.getSettingValue(SIP_ACC_SETTING_SERVER_ADDRESS));
        return info;
    }

    @Override
    public void initializeLine(Line line) {
        line.addDefaultBeanSettingHandler(new JitsiLineDefaults(line));
    }

    @Override
    public String getProfileFilename() {
        return getSerialNumber() + ".properties";
    }

    @Override
    public Profile[] getProfileTypes() {
        Profile[] profileTypes = new Profile[] {
            new PhoneProfile(getPhoneFilename())
        };

        return profileTypes;
    }

    static class PhoneProfile extends Profile {
        public PhoneProfile(String name) {
            super(name, MIME_TYPE_PLAIN);
        }

        @Override
        protected ProfileFilter createFilter(Device device) {
            return null;
        }

        @Override
        protected ProfileContext<JitsiPhone> createContext(Device device) {
            JitsiPhone phone = (JitsiPhone) device;
            return new JitsiProfileContext(phone, phone.getModel().getProfileTemplate());
        }
    }

    public static class JitsiLineDefaults {
        private final Line m_line;
        private final User m_user;

        public JitsiLineDefaults(Line line) {
            m_line = line;
            m_user = m_line.getUser();
        }

        @SettingEntry(path = SIP_ACC_SETTING_ACCOUNT_UID)
        public String getSipAccountUid() {
            return "SIP\\:" + m_line.getUserName() +
                "@" + m_line.getPhoneContext().getPhoneDefaults().getDomainName();
        }

        @SettingEntry(path = SIP_ACC_SETTING_PASSWORD)
        public String getSipPassword() {
            if (m_user == null) {
                return null;
            }
            return m_user.getSipPassword();
        }

        @SettingEntry(path = SIP_ACC_SETTING_SERVER_ADDRESS)
        public String getSipServerAddress() {
            DeviceDefaults defaults = m_line.getPhoneContext().getPhoneDefaults();
            return defaults.getDomainName();
        }

        @SettingEntry(path = SIP_ACC_SETTING_USER_ID)
        public String getSipUserId() {
            return m_line.getUserName() +
                "@" + m_line.getPhoneContext().getPhoneDefaults().getDomainName();
        }

        @SettingEntry(path = JABBER_ACC_SETTING_ACCOUNT_UID)
        public String getJabberAccountUid() {
            return "Jabber\\:" + m_line.getUserName() +
                "@" + m_line.getPhoneContext().getPhoneDefaults().getDomainName();
        }

        @SettingEntry(path = JABBER_ACC_SETTING_PASSWORD)
        public String getJabberPassword() {
            if (m_user == null) {
                return null;
            }
            return m_user.getPintoken();
        }

        @SettingEntry(path = JABBER_ACC_SETTING_SERVER_ADDRESS)
        public String getJabberServerAddress() {
            DeviceDefaults defaults = m_line.getPhoneContext().getPhoneDefaults();
            return defaults.getDomainName();
        }

        @SettingEntry(path = JABBER_ACC_SETTING_USER_ID)
        public String getJabberUserId() {
            return m_line.getUser().getImId() +
                "@" + m_line.getPhoneContext().getPhoneDefaults().getDomainName();
        }
    }

    public String getPhoneFilename() {
        return getProfileFilename();
    }

}
