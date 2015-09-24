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
import java.util.concurrent.ExecutionException;

import org.elasticsearch.index.query.QueryBuilder;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public interface ElasticsearchService {

    public static String TYPE = "_type";
    public static String CONFIG = "Config";
    public static final LocationFeature FEATURE = new LocationFeature("elasticsearch");

    void storeStructure(String index, Object source) throws InterruptedException, ExecutionException;

    <T extends ElasticsearchBean> List<T> searchDocs(String indexName, QueryBuilder queryBuilder,
            int start, int size, Class<T> clazz, String orderBy, boolean orderAscending);

    int countDocs(String indexName, QueryBuilder queryBuilder);

    <T extends ElasticsearchBean> T searchDocById(String indexName, String id, Class<T> clazz);

    void deleteDocs(String indexName, QueryBuilder queryBuilder);
}
