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

import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;

public class FreeswitchRecordingContextImpl implements FreeswitchRecordingContext {
    private BeanWithSettingsDao<FreeswitchRecordingSettings> m_settingsDao;
    private ConfigManager m_configManager;

    @Override
    public FreeswitchRecordingSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(FreeswitchRecordingSettings settings) {
        m_settingsDao.upsert(settings);
        m_configManager.configureEverywhere(FreeswitchFeature.FEATURE);
    }

    public void setSettingsDao(BeanWithSettingsDao<FreeswitchRecordingSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }

}
