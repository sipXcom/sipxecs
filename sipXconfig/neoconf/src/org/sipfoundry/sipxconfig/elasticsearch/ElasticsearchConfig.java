/**
 *
 *
 * Copyright (c) 2015 eZuce, Inc. All rights reserved.
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
 */
package org.sipfoundry.sipxconfig.elasticsearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.backup.BackupManager;
import org.sipfoundry.sipxconfig.backup.BackupSettings;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.LoggerKeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.springframework.beans.factory.annotation.Required;

public class ElasticsearchConfig implements ConfigProvider {

    private BackupManager m_backupManager;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(ElasticsearchServiceImpl.FEATURE)) {
            return;
        }
        FeatureManager featureManager = manager.getFeatureManager();
        Set<Location> locations = request.locations(manager);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = featureManager.isFeatureEnabled(ElasticsearchServiceImpl.FEATURE, location);
            ConfigUtils.enableCfengineClass(dir, "elasticsearch.cfdat", enabled, "elasticsearch");

            BackupSettings backupSettings = m_backupManager.getSettings();
            File f = new File(dir, "elasticsearch.yml.part");
            Writer wtr = new FileWriter(f);
            try {
                write(wtr, backupSettings);
            } finally {
                IOUtils.closeQuietly(wtr);
            }
        }
    }

    private void write(Writer wtr, BackupSettings backupSettings) throws IOException {
        LoggerKeyValueConfiguration config = LoggerKeyValueConfiguration.equalsSeparated(wtr);
        config.write("tmpDir", backupSettings.getTmpDir());
    }

    @Required
    public void setBackupManager(BackupManager backupManager) {
        m_backupManager = backupManager;
    }

}
