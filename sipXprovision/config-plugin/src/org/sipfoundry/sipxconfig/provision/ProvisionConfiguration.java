/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.provision;

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
import org.springframework.beans.factory.annotation.Required;

public class ProvisionConfiguration implements ConfigProvider {
    private Provision m_provision;
    private String m_etcDir;
    private String m_logDirectory;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(Provision.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        ProvisionSettings settings = m_provision.getSettings();
        for (Location location : locations) {
            boolean enabled = manager.getFeatureManager().isFeatureEnabled(Provision.FEATURE, location);
            File dir = getLocationDataDirectory(location);
            ConfigUtils.enableCfengineClass(dir, "sipxprovision.cfdat", enabled, "sipxprovision");
            if (!enabled) {
                continue;
            }
            Writer prov = new FileWriter(new File(dir, "sipxprovision-config"));
            try {
                write(prov, manager, settings);
            } finally {
                IOUtils.closeQuietly(prov);
            }
        }
    }

    void write(Writer wtr, ConfigManager manager, ProvisionSettings settings) throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        config.writeSettings(settings.getSettings().getSetting("provision-config"));
        config.write("log.file", m_logDirectory + "/sipxprovision.log");
        config.write("provision.sipxchangeDomainName", manager.getDomainManager().getDomainName());
        config.write("provision.hostname", "sipxprovision");
    }

    public void setProvision(Provision provision) {
        m_provision = provision;
    }

    private File getLocationDataDirectory(Location location) {
        File d = new File(m_etcDir + "/conf", String.valueOf(location.getId()));
        if (!d.exists()) {
            d.mkdirs();
        }
        return d;
    }

    @Required
    public void setEtcDir(String etcDir) {
        m_etcDir = etcDir;
    }

    @Required
    public void setLogDirectory(String logDir) {
        m_logDirectory = logDir;
    }
}
