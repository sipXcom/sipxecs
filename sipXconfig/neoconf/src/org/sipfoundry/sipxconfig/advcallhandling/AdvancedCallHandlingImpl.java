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

import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;

public class AdvancedCallHandlingImpl implements AdvancedCallHandling {

    private BeanWithSettingsDao<AdvancedCallHandlingSettings> m_settingsDao;

    public AdvancedCallHandlingSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(AdvancedCallHandlingSettings settings) {
        m_settingsDao.upsert(settings);
    }

    public void setSettingsDao(BeanWithSettingsDao<AdvancedCallHandlingSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    @Override
    public boolean isEnabled() {
        return (Boolean) getSettings().getSettingTypedValue(ENABLED);
    }
}
