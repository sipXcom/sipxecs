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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.RunRequest;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

public class TcpdumpLogImpl implements FeatureProvider, ProcessProvider, TcpdumpLog {

    private static final String PROCESS = "sipxtcpdumplog";
    private static final Log LOG = LogFactory.getLog(TcpdumpLog.class);
    private BeanWithSettingsDao<TcpdumpLogSettings> m_settingsDao;
    private ConfigManager m_configManager;

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public TcpdumpLogSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(TcpdumpLogSettings settings) {
        m_settingsDao.upsert(settings);
    }

    public void setSettingsDao(BeanWithSettingsDao<TcpdumpLogSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.UTILITY_SERVICES) {
            b.addFeature(FEATURE);
        }
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
        Map<Location, Set<LocationFeature>> disabledServices = request.getNewlyDisabledByLocation();

        List<String> serversToUnmount = new ArrayList<String>();
        for (Location location : disabledServices.keySet()) {
            if (!location.isPrimary()) {
                Set<LocationFeature> features = disabledServices.get(location);
                if (features.contains(FEATURE)) {
                    serversToUnmount.add(location.getFqdn());
                }
            }
        }

        if (!serversToUnmount.isEmpty()) {
            Writer servers = null;
            try {
                File gdir = m_configManager.getGlobalDataDirectory();
                servers = new FileWriter(new File(gdir, "tcpdumpunmount"));
                for (String server : serversToUnmount) {
                    servers.write(server);
                    servers.write('\n');
                }

            } catch (IOException ex) {
                LOG.error("failed to unmount tcpdump directories " + ex.getMessage());
            } finally {
                IOUtils.closeQuietly(servers);
            }

            Location primary = m_configManager.getLocationManager().getPrimaryLocation();
            RunRequest runRequest = new RunRequest("Unmounting tcpdump directories", Collections.singleton(primary));
            runRequest.setBundles("unmount_tcpdump");
            m_configManager.run(runRequest);
        }
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        boolean enabled = manager.getFeatureManager().isFeatureEnabled(FEATURE, location);
        return (enabled ? Collections.singleton(ProcessDefinition.sipx("tcpdump", PROCESS, PROCESS)) : null);
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }
}
