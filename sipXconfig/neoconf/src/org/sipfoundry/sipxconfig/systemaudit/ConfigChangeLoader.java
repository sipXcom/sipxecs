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

import org.sipfoundry.sipxconfig.elasticsearch.ElasticsearchBean;
import org.sipfoundry.sipxconfig.elasticsearch.ElasticsearchService;
import org.springframework.beans.factory.annotation.Required;

public class ConfigChangeLoader {

    private LinkedTransferQueue<ConfigChange> m_configChangeQueue = new LinkedTransferQueue<ConfigChange>();
    private ElasticsearchService m_elasticsearchService;

    public void run() {
        List<ElasticsearchBean> persistableConfigChanges = new ArrayList<ElasticsearchBean>();
        m_configChangeQueue.drainTo(persistableConfigChanges);
        if (!persistableConfigChanges.isEmpty()) {
            m_elasticsearchService.storeBulkStructures(
                    ConfigChangeContext.SYSTEM_AUDIT_INDEX, persistableConfigChanges);
        }
    }

    public void addConfigChangeToQueue(ConfigChange configChange) {
        m_configChangeQueue.add(configChange);
    }

    @Required
    public void setElasticsearchService(ElasticsearchService elasticsearchService) {
        m_elasticsearchService = elasticsearchService;
    }

}
