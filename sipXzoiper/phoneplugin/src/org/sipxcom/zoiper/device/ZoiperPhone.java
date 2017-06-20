/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.zoiper.device;

import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.Device;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.Profile;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.device.ProfileFilter;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class ZoiperPhone extends Phone {

    public static final String MIME_TYPE_PLAIN = "text/plain";
    
    private static final String IDENT = "generic/ident";
    private static final String NAME = "generic/name";
    private static final String USERNAME = "credentials/username";
    private static final String PASSWORD = "credentials/password";
    private static final String DOMAIN = "sip_credentials/SIP_domain";
    
    @Override
    protected void setLineInfo(Line line, LineInfo lineInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected LineInfo getLineInfo(Line line) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getProfileFilename() {
        return getSerialNumber() + ".xml";
    }
    
    public String getPhonebookFilename() {
        return getSerialNumber() + "_phonebook" + ".xml";
    }

    @Override
    public Profile[] getProfileTypes() {
        
        Profile[] profileTypes = new Profile[]{
            new PhoneProfile(getProfileFilename())
        };
        
        if (getPhonebookManager().getPhonebookManagementEnabled()) {
            PhonebookManager pbm = getPhonebookManager();
            if (null != pbm) {
                User user = getPrimaryUser();
                if (null != user) {
                    Collection<Phonebook> phonebooks = pbm.getAllPhonebooksByUser(user);
                    Collection<PhonebookEntry> entries = pbm.getEntries(phonebooks, user);
                    if (null != entries) {
                        profileTypes = (Profile[]) ArrayUtils.add(profileTypes,
                            new PhonebookProfile(getPhonebookFilename(), entries));
                    }
                }
            }
        }
        
        return profileTypes;
    }
    
    @Override
    public void initializeLine(Line line) {
        line.addDefaultBeanSettingHandler(new ZoiperLineDefaults(line));
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
        protected ProfileContext<ZoiperPhone> createContext(Device device) {
            ZoiperPhone phone = (ZoiperPhone) device;
            return new ZoiperProfileContext(phone, phone.getModel().getProfileTemplate());
        }
    }
    
    static class PhonebookProfile extends Profile {
        Collection<PhonebookEntry> m_entries;

        public PhonebookProfile(String name, Collection<PhonebookEntry> entries) {
            super(name, MIME_TYPE_PLAIN);
            m_entries = entries;
        }

        @Override
        protected ProfileFilter createFilter(Device device) {
            return null;
        }

        @Override
        protected ProfileContext createContext(Device device) {
            ZoiperPhone phone = (ZoiperPhone) device;
            ZoiperPhoneModel model = (ZoiperPhoneModel) phone.getModel();
            
            return new ZoiperPhonebookProfileContext(phone, model.getPhonebookProfileTemplate(), m_entries);
        }
    }    
    
    public static class ZoiperLineDefaults {
        private final Line m_line;
        private final User m_user;

        public ZoiperLineDefaults(Line line) {
            m_line = line;
            m_user = m_line.getUser();
        }

        @SettingEntry(path = IDENT)
        public String getSipAccountUid() {
            return m_line.getUserName() +
                "@" + m_line.getPhoneContext().getPhoneDefaults().getDomainName();
        }

        @SettingEntry(path = PASSWORD)
        public String getSipPassword() {
            if (m_user == null) {
                return null;
            }
            return m_user.getSipPassword();
        }

        @SettingEntry(path = DOMAIN)
        public String getSipServerAddress() {
            DeviceDefaults defaults = m_line.getPhoneContext().getPhoneDefaults();
            return defaults.getDomainName();
        }

        @SettingEntry(path = USERNAME)
        public String getUsername() {
            return m_line.getUserName();
        }

        @SettingEntry(path = NAME)
        public String getSipDisplayName() {
            if (m_user == null) {
                return null;
            }
            return m_user.getDisplayName();
        }
    }    

}
