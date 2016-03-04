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
package org.sipfoundry.sipxconfig.commserver;

import static java.lang.String.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;

public class LocationsConfig implements ConfigProvider {

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(LocationsManager.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        Location primary = manager.getLocationManager().getPrimaryLocation();
        File gdir = manager.getGlobalDataDirectory();

        Writer servers = new FileWriter(new File(gdir, "servers"));
        try {
            writeServers(servers, locations);
        } finally {
            IOUtils.closeQuietly(servers);
        }

        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            Writer host = new FileWriter(new File(dir, "host.cfdat"));
            try {
                writeHosts(host, location, primary);
            } finally {
                IOUtils.closeQuietly(host);
            }
        }

        Map<Integer, String> hostsEntries = new HashMap<Integer, String>();
        for (Location location : locations) {
            hostsEntries.put(location.getId(), format("%s %s %s # sipXcom cluster\n", location.getAddress(),
                    location.getFqdn(), location.getHostname()));
        }

        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            Writer hostsPart = new FileWriter(new File(dir, "hosts.part"));
            try {
                for (Map.Entry<Integer, String> entry : hostsEntries.entrySet()) {
                    Integer locationId = entry.getKey();
                    if (locationId != location.getId()) {
                        hostsPart.write(entry.getValue());
                    }
                }
            } finally {
                IOUtils.closeQuietly(hostsPart);
            }
        }
    }

    /**
     * legend of all servers and their ip addresses and ids
     */
    void writeServers(Writer w, Set<Location> locations) throws IOException {
        for (Location l : locations) {
            String line = format("%s=%d %s end\n", l.getFqdn(), l.getId(), l.getAddress());
            w.write(line);
        }
    }

    /**
     * host name the server should use
     */
    void writeHosts(Writer w, Location l, Location primary) throws IOException {
        CfengineModuleConfiguration config = new CfengineModuleConfiguration(w);
        config.write("host", l.getHostname());
        config.write("master_address", primary.getAddress());
        config.write("master_fqdn", primary.getFqdn());
    }
}
