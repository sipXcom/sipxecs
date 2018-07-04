/*
 *
 * Copyright (C) 2018 eZuce
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

package org.sipfoundry.sipxconfig.systemaudit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.admin.AdminContext;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureManager;

public class SystemAuditConfiguration implements ConfigProvider {
    private AdminContext m_adminContext;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (request.applies(SystemAuditManager.FEATURE) || request.applies(AdminContext.FEATURE)) {
            Set<Location> locations = request.locations(manager);
            FeatureManager featureManager = manager.getFeatureManager();
            int daysToKeepLogs = m_adminContext.getSettings().getSystemAuditKeepChanges();
            for (Location location : locations) {
                File dir = manager.getLocationDataDirectory(location);
                boolean enabled = featureManager.isFeatureEnabled(SystemAuditManager.FEATURE, location);
                Writer w = new FileWriter(new File(dir, "sysaudit.cfdat"));

                try {
                    CfengineModuleConfiguration config = new CfengineModuleConfiguration(w);
                    config.writeClass("sysaudit", enabled);
                    config.write("AUDIT_DAYS_TO_KEEP", daysToKeepLogs);
                } finally {
                    IOUtils.closeQuietly(w);
                }
            }
        }
        
    }

    public void setAdminContext(AdminContext adminContext) {
        m_adminContext = adminContext;
    }
}
