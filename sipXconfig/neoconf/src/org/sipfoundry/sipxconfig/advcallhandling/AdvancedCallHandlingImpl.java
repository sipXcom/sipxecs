/**
 * Copyright (c) 2017 eZuce, Inc. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
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
package org.sipfoundry.sipxconfig.advcallhandling;

import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.proxy.ProxySettings;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.springframework.beans.factory.annotation.Required;

public class AdvancedCallHandlingImpl implements AdvancedCallHandling {

    private BeanWithSettingsDao<AdvancedCallHandlingSettings> m_settingsDao;
    private ProxyManager m_proxyManager;

    public AdvancedCallHandlingSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(AdvancedCallHandlingSettings settings) {
        m_settingsDao.upsert(settings);
        if (isEnabled()) {
            m_proxyManager.getSettings().setLogLevel(ProxySettings.INFO_LOG_LEVEL);
            m_proxyManager.saveSettings(m_proxyManager.getSettings());
        }
    }

    public void setSettingsDao(BeanWithSettingsDao<AdvancedCallHandlingSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    @Override
    public boolean isEnabled() {
        return (Boolean) getSettings().getSettingTypedValue(ENABLED);
    }

    @Override
    public boolean isEnabledAnchor() {
        return (Boolean) getSettings().getSettingTypedValue(ENABLE_ANCHOR);
    }

    @Required
    public void setProxyManager(ProxyManager proxyManager) {
        m_proxyManager = proxyManager;
    }
}
