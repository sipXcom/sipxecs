/**
 *
 *
 * Copyright (c) 2015 eZuce Corp. All rights reserved.
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
package org.sipfoundry.sipxconfig.systemaudit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.search.SearchableBean;
import org.sipfoundry.sipxconfig.search.SearchableService;
import org.springframework.beans.factory.annotation.Required;

public class ConfigChangeLoader {

    private LinkedTransferQueue<ConfigChange> m_configChangeQueue = new LinkedTransferQueue<ConfigChange>();
    private SearchableService m_searchableService;
    private FeatureManager m_featureManager;

    public void run() {
        if (!m_featureManager.isFeatureEnabled(SystemAuditManager.FEATURE)) {
            return;
        }
        List<SearchableBean> persistableConfigChanges = new ArrayList<SearchableBean>();
        m_configChangeQueue.drainTo(persistableConfigChanges);
        if (!persistableConfigChanges.isEmpty()) {
            m_searchableService.storeBulkDocs(
                    ConfigChangeContext.SYSTEM_AUDIT_INDEX, persistableConfigChanges);
        }
    }

    public void addConfigChangeToQueue(ConfigChange configChange) {
        m_configChangeQueue.add(configChange);
    }

    @Required
    public void setSearchableService(SearchableService searchableService) {
        m_searchableService = searchableService;
    }

    @Required
    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

}
