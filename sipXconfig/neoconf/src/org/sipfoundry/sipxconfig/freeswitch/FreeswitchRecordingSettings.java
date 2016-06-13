/*
 *
 *
 * Copyright (C) 2016 eZuce, Inc. certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.freeswitch;

import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class FreeswitchRecordingSettings extends PersistableSettings {

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("freeswitch/recordings.xml");
    }

    public int getBitRate() {
        return (Integer) getSettingTypedValue("fs-recording/brate");
    }

    public int getResample() {
        return (Integer) getSettingTypedValue("fs-recording/resample");
    }

    public int getQuality() {
        String value = (String) getSettingTypedValue("fs-recording/quality");
        if (value.equalsIgnoreCase("low")) {
            return 7;
        } else if (value.equalsIgnoreCase("medium")) {
            return 5;
        }
        return 2;
    }

    @Override
    public String getBeanId() {
        return "fsRecordingSettings";
    }
}
