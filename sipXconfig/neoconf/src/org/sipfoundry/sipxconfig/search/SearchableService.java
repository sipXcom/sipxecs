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
package org.sipfoundry.sipxconfig.search;

import java.util.List;

/**
 * Generic interface to handle CRUD operations for SearchableBean entities.
 */
public interface SearchableService {

    void storeDoc(String index, SearchableBean source);

    void storeBulkDocs(String index, List<SearchableBean> source);

    <T extends SearchableBean> List<T> searchDocs(String index, Object filter,
            int start, int size, Class<T> clazz, String orderBy, boolean orderAscending);

    int countDocs(String index, Object filter);

    <T extends SearchableBean> T searchDocById(String index, String id, Class<T> clazz);

    void deleteDocs(String index, Object filter);
}
