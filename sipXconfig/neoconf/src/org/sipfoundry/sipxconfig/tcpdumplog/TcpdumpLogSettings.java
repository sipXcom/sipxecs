/*
 *
 * Copyright (C) 2015 Karel Electronics Corp. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 */

package org.sipfoundry.sipxconfig.tcpdumplog;

import java.util.Collection;
import java.util.Collections;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class TcpdumpLogSettings extends PersistableSettings implements DeployConfigOnEdit {
    private static final String SIP_TCPDUMP_FILE_SIZE = "tcpdumpParameters/SIP_TCPDUMP_FILE_SIZE";
    private static final String SIP_TCPDUMP_FILE_COUNT = "tcpdumpParameters/SIP_TCPDUMP_FILE_COUNT";

    public String getLogFileSize() {
        return getSettingValue(SIP_TCPDUMP_FILE_SIZE);
    }

    public String getLogFileCount() {
        return getSettingValue(SIP_TCPDUMP_FILE_COUNT);
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxtcpdumplog/sipxtcpdumplog.xml");
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) TcpdumpLog.FEATURE);
    }

    @Override
    public String getBeanId() {
        return "tcpdumpLogSettings";
    }
}
