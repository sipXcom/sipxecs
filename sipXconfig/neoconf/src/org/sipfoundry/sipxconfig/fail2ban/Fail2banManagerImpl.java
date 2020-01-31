/**
 * Copyright (c) 2013 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.fail2ban;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sipfoundry.sipxconfig.alarm.AlarmDefinition;
import org.sipfoundry.sipxconfig.alarm.AlarmProvider;
import org.sipfoundry.sipxconfig.alarm.AlarmServerManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.setup.SetupListener;
import org.sipfoundry.sipxconfig.setup.SetupManager;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class Fail2banManagerImpl implements Fail2banManager, FeatureProvider, SetupListener, AlarmProvider {
    private BeanWithSettingsDao<Fail2banSettings> m_settingsDao;
    private MongoTemplate m_fail2banDb;
    private ConfigManager m_configManager;

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
    }

    @Override
    public boolean setup(SetupManager manager) {
        if (manager.isFalse(FEATURE.getId())) {
            manager.getFeatureManager().enableGlobalFeature(FEATURE, true);
            manager.setTrue(FEATURE.getId());
        }
        return true;
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return null;
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE) {
            b.addFeature(FEATURE);
        }
    }

    public void setSettingsDao(BeanWithSettingsDao<Fail2banSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    @Override
    public Fail2banSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(Fail2banSettings settings) {
        settings.validate();
        m_settingsDao.upsert(settings);
    }

    @Override
    public Collection<AlarmDefinition> getAvailableAlarms(AlarmServerManager manager) {
        if (manager.getFeatureManager().isFeatureEnabled(Fail2banManager.FEATURE)
                && manager.getFeatureManager().isFeatureEnabled(FirewallManager.FEATURE)) {
            return Arrays.asList(new AlarmDefinition[] {
                SECURITY_IP_BANNED, SECURITY_IP_UNBANNED
            });
        }
        return null;
    }

    @Override
    public List<BannedHost> getBannedHosts() {
        DBCollection col = m_fail2banDb.getCollection("bannedHosts");
        return m_fail2banDb.find(new Query(), BannedHost.class, "bannedHosts");
    }

    @Override
    public void unbanSelectedHosts(Collection<BannedHost> bannedHosts) {
        DBCollection collection = m_fail2banDb.getCollection("unbanHosts");

        for (BannedHost host : bannedHosts) {
            BasicDBObject obj = new BasicDBObject("ipAddress", host.getIpAddress());
            obj.append("jail", "sip-" + host.getReason().toString().toLowerCase());
            collection.insert(obj);
        }

        // start sipxagent to execute python script which unbans hosts
        m_configManager.configureEverywhere(FEATURE);
    }

    public void setFail2banDb(MongoTemplate fail2banDb) {
        m_fail2banDb = fail2banDb;
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }
}
