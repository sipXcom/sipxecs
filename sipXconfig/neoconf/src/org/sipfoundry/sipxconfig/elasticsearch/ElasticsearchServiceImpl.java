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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.search.SearchableBean;
import org.sipfoundry.sipxconfig.search.SearchableService;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;
import org.springframework.beans.factory.annotation.Required;

import com.google.gson.Gson;

/**
 * Elastic search implementation for SearchableService
 */
public class ElasticsearchServiceImpl implements SearchableService, FeatureProvider, ProcessProvider {

    public static final String ELASTICSEARCH = "elasticsearch";
    public static final LocationFeature FEATURE = new LocationFeature(ELASTICSEARCH);

    private static final Log LOG = LogFactory.getLog(ElasticsearchServiceImpl.class);
    private static final String FILTERING_ERROR_MESSAGE = "Filtering is supported only by QueryBuilder objects.";
    private static final String NO_NODE_AVAILABLE_ERROR_MESSAGE = "No available nodes in ElasticSearch.";
    private static final String CONFIG = "config";
    private static final String ELASTICSEARCH_REGEXP = ".*/java -Xms256m -Xmx1g -Djava.awt.headless=true";

    private Client m_client;
    private String m_hostName;
    private int m_port;
    private Gson m_gson;

    @Required
    public void setHostName(String hostName) {
        m_hostName = hostName;
    }

    @Required
    public void setPort(int port) {
        m_port = port;
    }

    @Required
    public void setGson(Gson gson) {
        m_gson = gson;
    }

    private Client getClient() {
        if (m_client == null) {
            try {
                m_client = new TransportClient()
                        .addTransportAddress(new InetSocketTransportAddress(
                                m_hostName, m_port));
            } catch (Exception e) {
                LOG.debug("Cannot create elasticsearch client, probably elasticsearh service is not up yet.", e);
            }
        }
        return m_client;
    }

    public void setClient(Client client) {
        m_client = client;
    }

    private IndexRequest getIndexRequest(String index, SearchableBean source) {
        IndexRequest indexRequest = new IndexRequest(index, CONFIG);
        String sourceString = m_gson.toJson(source);
        indexRequest.source(sourceString);
        return indexRequest;
    }

    @Override
    public void storeDoc(String index, SearchableBean source) {
        IndexRequest indexRequest = getIndexRequest(index, source);
        try {
            getClient().index(indexRequest).actionGet();
        } catch (NoNodeAvailableException e) {
            LOG.debug(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void storeBulkDocs(String index, List<SearchableBean> source) {
        BulkRequestBuilder bulkRequest = getClient().prepareBulk();
        for (SearchableBean elasticsearchBean : source) {
            bulkRequest.add(getIndexRequest(index, elasticsearchBean));
        }
        if (bulkRequest.numberOfActions() <= 0) {
            return;
        }
        try {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                LOG.error("Perstisting searchable object encountered errors:"
                        + bulkResponse.buildFailureMessage());
            }
        } catch (NoNodeAvailableException e) {
            LOG.debug(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
        }
    }

    @Override
    public <T extends SearchableBean> List<T> searchDocs(String indexName, Object filter,
            int start, int size, Class<T> clazz, String orderBy, boolean orderAscending) {
        if (!checkIndexExists(indexName)) {
            return new ArrayList<T>();
        }
        SearchRequestBuilder searchBuilder = getClient().prepareSearch(indexName)
                .setFrom(start).setSize(size).setTypes(CONFIG);
        if (orderBy != null) {
            searchBuilder.addSort(orderBy, orderAscending ? SortOrder.ASC : SortOrder.DESC);
        }
        if (filter != null) {
            if (!(filter instanceof QueryBuilder)) {
                LOG.error(FILTERING_ERROR_MESSAGE);
            }
            QueryBuilder queryBuilder = (QueryBuilder) filter;
            searchBuilder.setQuery(queryBuilder);
        }
        try {
            SearchResponse response = searchBuilder.execute().actionGet();
            return mapSearchResponseToObject(response, clazz);
        } catch (NoNodeAvailableException e) {
            LOG.debug(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
            return new ArrayList<T>();
        }
    }

    private <T extends SearchableBean> List<T> mapSearchResponseToObject(SearchResponse response, Class<T> clazz) {
        List<T> results = new ArrayList<T>();
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            T object = m_gson.fromJson(searchHit.sourceAsString(), clazz);
            object.setId(searchHit.getId());
            results.add(object);
        }
        return results;
    }

    @Override
    public <T extends SearchableBean> T searchDocById(String indexName, String id, Class<T> clazz) {
        if (!checkIndexExists(indexName)) {
            return null;
        }
        SearchRequestBuilder req = getClient().prepareSearch(indexName);
        IdsQueryBuilder qb = QueryBuilders.idsQuery().addIds(id);
        req.setQuery(qb);
        try {
            SearchResponse response = req.execute().actionGet();
            return mapSearchResponseToObject(response, clazz).get(0);
        } catch (NoNodeAvailableException e) {
            LOG.debug(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
            return null;
        }
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        boolean enabled = manager.getFeatureManager().isFeatureEnabled(FEATURE, location);
        return (enabled ? Collections.singleton(ProcessDefinition.sysvByRegex(
                ELASTICSEARCH, ELASTICSEARCH_REGEXP, true)) : null);
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        validator.primaryLocationOnly(FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE) {
            b.addFeature(FEATURE);
        }
    }

    @Override
    public int countDocs(String indexName, Object filter) {
        if (!checkIndexExists(indexName)) {
            return 0;
        }
        CountRequestBuilder countBuilder = getClient().prepareCount().setIndices(indexName);
        if (filter != null) {
            if (!(filter instanceof QueryBuilder)) {
                LOG.error(FILTERING_ERROR_MESSAGE);
            }
            QueryBuilder queryBuilder = (QueryBuilder) filter;
            countBuilder.setQuery(queryBuilder);
        }
        CountResponse response = countBuilder.execute().actionGet();
        return (int) response.getCount();
    }

    private boolean checkIndexExists(String indexName) {
        try {
            return getClient().admin().indices().prepareExists(indexName).execute()
                .actionGet().isExists();
        } catch (NoNodeAvailableException e) {
            LOG.debug(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
            return false;
        }
    }

    @Override
    public void deleteDocs(String indexName, Object filter) {
        if (!checkIndexExists(indexName)) {
            return;
        }
        SearchRequestBuilder searchBuilder = getClient().prepareSearch(indexName)
                .setTypes(CONFIG)
                .setSize(Integer.MAX_VALUE);

        if (filter != null) {
            if (!(filter instanceof QueryBuilder)) {
                LOG.error(FILTERING_ERROR_MESSAGE);
            }
            QueryBuilder queryBuilder = (QueryBuilder) filter;
            searchBuilder.setQuery(queryBuilder);
        }
        try {
            SearchResponse response = searchBuilder.execute().actionGet();
            SearchHit[] searchHits = response.getHits().getHits();
            if (searchHits.length > 0) {
                // Create bulk request
                final BulkRequestBuilder bulkRequest = getClient()
                        .prepareBulk().setRefresh(true);

                // Add search results to bulk request
                for (final SearchHit searchHit : searchHits) {
                    final DeleteRequest deleteRequest = new DeleteRequest(
                            indexName, CONFIG, searchHit.getId());
                    bulkRequest.add(deleteRequest);
                }

                // Run bulk request
                final BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    LOG.error(bulkResponse.buildFailureMessage());
                }
            }
        } catch (NoNodeAvailableException e) {
            LOG.debug(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
        }
    }

}
