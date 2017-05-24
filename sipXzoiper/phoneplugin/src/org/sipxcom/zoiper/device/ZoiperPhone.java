/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.zoiper.device;

import org.sipfoundry.sipxconfig.device.Device;
import org.sipfoundry.sipxconfig.device.Profile;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.device.ProfileFilter;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.Phone;

public class ZoiperPhone extends Phone {

    public static final String MIME_TYPE_PLAIN = "text/plain";
    
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

    @Override
    public Profile[] getProfileTypes() {
        Profile[] profileTypes = new Profile[] {
            new PhoneProfile(getProfileFilename())
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
        protected ProfileContext<ZoiperPhone> createContext(Device device) {
            ZoiperPhone phone = (ZoiperPhone) device;
            return new ZoiperProfileContext(phone, phone.getModel().getProfileTemplate());
        }
    }     

}
