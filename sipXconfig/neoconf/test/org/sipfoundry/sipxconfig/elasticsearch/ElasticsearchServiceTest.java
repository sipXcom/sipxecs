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
package org.sipfoundry.sipxconfig.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.sipfoundry.sipxconfig.systemaudit.ConfigChange;
import org.sipfoundry.sipxconfig.systemaudit.ConfigChangeValue;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditException;

import com.google.gson.GsonBuilder;

public class ElasticsearchServiceTest extends TestCase {

    private static String INDEX = "testindex";
    private static String UNIQUE_DETAILS = "52653";

    private Node m_node;
    private Client m_client;
    private ElasticsearchServiceImpl m_elasticsearchService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (m_client != null) {
            return;
        }
        m_node = NodeBuilder.nodeBuilder().local(true).node();
        m_client = m_node.client();
        m_elasticsearchService = new ElasticsearchServiceImpl();
        m_elasticsearchService.setClient(m_client);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        m_elasticsearchService.setGson(gsonBuilder.create());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            m_client.admin().indices().delete(new DeleteIndexRequest(INDEX)).actionGet();
        } catch (Exception e) {
            // do nothing
        }
        m_client.close();
        m_node.close();
    }

    public void testStoreElasticsearchBean() {
        try {
            waitForRefresh();
            ElasticsearchBean testConfigChange = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);
            IndexResponse response = m_elasticsearchService.storeStructure(INDEX, testConfigChange);
            assertNotNull(response.getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testOperations() {
        try {
            waitForRefresh();
            ElasticsearchBean testConfigChange1 = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);
            ElasticsearchBean testConfigChange2 = buildElasticsearchBean("Modified", "User",
                    UNIQUE_DETAILS, "200", "192.168.1.1", null, null, null);
            ElasticsearchBean testConfigChange3 = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);
            List<ElasticsearchBean> docs = new ArrayList<ElasticsearchBean>();
            docs.add(testConfigChange1);
            docs.add(testConfigChange2);
            docs.add(testConfigChange3);
            BulkResponse response = m_elasticsearchService.storeBulkStructures(INDEX, docs);
            assert(response.hasFailures());

            List<ConfigChange> searchResponse = m_elasticsearchService.searchDocs(
                    INDEX, null, 0, 10, ConfigChange.class, ConfigChange.ACTION, true);
            // expect 3 items because one is duplicated
            boolean itemFound = false;
            for (ConfigChange configChange : searchResponse) {
                if (configChange.getDetails().equals(UNIQUE_DETAILS)) {
                    ConfigChange configChange2 = searchResponse.get(2);
                    assertEquals(configChange.getAction(), configChange2.getAction());
                    assertEquals(configChange.getConfigChangeType(), configChange2.getConfigChangeType());
                    assertEquals(configChange.getUserName(), configChange2.getUserName());
                    assertEquals(configChange.getDetails(), configChange2.getDetails());
                    assertEquals(configChange.getDateTime(), configChange2.getDateTime());
                    assertEquals(configChange.getIpAddress(), configChange2.getIpAddress());
                    itemFound = true;
                }
            }
            assert(itemFound);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testStoreBulkEmptyElasticsearchBeans() {
        try {
            waitForRefresh();
            List<ElasticsearchBean> docs = new ArrayList<ElasticsearchBean>();
            BulkResponse response = m_elasticsearchService.storeBulkStructures(INDEX, docs);
            assertNull(response);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testCountElasticsearchBeans() {
        try {
            waitForRefresh();

            ElasticsearchBean testConfigChange1 = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);
            ElasticsearchBean testConfigChange2 = buildElasticsearchBean("Modified", "User",
                    UNIQUE_DETAILS, "200", "192.168.1.1", null, null, null);
            ElasticsearchBean testConfigChange3 = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);
            List<ElasticsearchBean> docs = new ArrayList<ElasticsearchBean>();
            docs.add(testConfigChange1);
            docs.add(testConfigChange2);
            docs.add(testConfigChange3);
            m_elasticsearchService.storeBulkStructures(INDEX, docs);

            int docCount = m_elasticsearchService.countDocs(INDEX, null);
            assert(docCount > 0);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void waitForRefresh() throws InterruptedException {
        m_client.admin().indices().refresh(new RefreshRequest(INDEX));
    }

    protected ElasticsearchBean buildElasticsearchBean(String action, String type,
            String details, String userName, String ipAddress,
            String propertyName, String before, String after)
            throws SystemAuditException {
        ConfigChange configChange = new ConfigChange();
        configChange.setAction(action);
        configChange.setConfigChangeType(type);
        configChange.setDetails(details);
        configChange.setUserName(userName);
        configChange.setIpAddress(ipAddress);
        if (propertyName != null) {
            ConfigChangeValue configChangeValue = new ConfigChangeValue();
            configChangeValue.setPropertyName(propertyName);
            configChangeValue.setValueBefore(before);
            configChangeValue.setValueAfter(after);
            configChange.addValue(configChangeValue);
        }
        return configChange;
    }
}
