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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.springframework.beans.factory.annotation.Required;

public class TcpdumpLogConfiguration implements ConfigProvider {

    private static final String TCPDUMPLOG = "sipxtcpdumplog";
    private static final String TCPDUMPLOG_CFDAT = "sipxtcpdumplog.cfdat";
    private static final String FILE_SIZE = "SIP_TCPDUMP_FILE_SIZE";
    private static final String FILE_COUNT = "SIP_TCPDUMP_FILE_COUNT";

    private TcpdumpLog m_tcpdumplog;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(TcpdumpLog.FEATURE)) {
            return;
        }

        FeatureManager fm = manager.getFeatureManager();
        Set<Location> locations = request.locations(manager);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = fm.isFeatureEnabled(TcpdumpLog.FEATURE, location);
            ConfigUtils.enableCfengineClass(dir, TCPDUMPLOG_CFDAT, enabled, TCPDUMPLOG);
            if (!enabled) {
                continue;
            }
            // write settings to sipxtcpdumplog.properties.part
            TcpdumpLogSettings settings = m_tcpdumplog.getSettings();
            Writer config = new FileWriter(new File(dir, "sipxtcpdumplog.properties.part"));
            try {
                write(config, settings);
            } finally {
                IOUtils.closeQuietly(config);
            }
        }
    }

    private void write(Writer wtr, TcpdumpLogSettings settings) throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        Setting root = settings.getSettings();
        config.writeSettings(root.getSetting("tcpdumpParameters"));
    }

    @Required
    public void setTcpdumpLog(TcpdumpLog tcpdumplog) {
        m_tcpdumplog = tcpdumplog;
    }
}
