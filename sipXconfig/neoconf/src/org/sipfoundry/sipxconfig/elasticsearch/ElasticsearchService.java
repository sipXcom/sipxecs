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

import java.util.List;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public interface ElasticsearchService {

    public static String TYPE = "_type";
    public static String CONFIG = "Config";
    public static final LocationFeature FEATURE = new LocationFeature("elasticsearch");

    IndexResponse storeStructure(String index, ElasticsearchBean source);

    BulkResponse storeBulkStructures(String index, List<ElasticsearchBean> source);

    <T extends ElasticsearchBean> List<T> searchDocs(String indexName, QueryBuilder queryBuilder,
            int start, int size, Class<T> clazz, String orderBy, boolean orderAscending);

    int countDocs(String indexName, QueryBuilder queryBuilder);

    <T extends ElasticsearchBean> T searchDocById(String indexName, String id, Class<T> clazz);

    void deleteDocs(String indexName, QueryBuilder queryBuilder);
}
