/**
 *
 * Copyright (c) 2013 Karel Electronics Corp. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
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
 *
 */

package org.sipfoundry.sipxconfig.systemaudit;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.search.SearchableService;
import org.sipfoundry.sipxconfig.setting.Group;
import org.springframework.beans.factory.annotation.Required;

public class ConfigChangeContextImpl implements ConfigChangeContext {

    private static final Log LOG = LogFactory.getLog(ConfigChangeContextImpl.class);
    private static final String FIRST_DELIMTER = ":*";
    private static final String LAST_DELIMTER = "*";

    private SearchableService m_searchableService;
    private CoreContext m_coreContext;
    private ConfigChangeLoader m_configChangeLoader;

    @Override
    public List<ConfigChange> getConfigChanges() {
        List<ConfigChange> docs = m_searchableService.searchDocs(
                SYSTEM_AUDIT_INDEX, null, 0, Integer.MAX_VALUE, ConfigChange.class, null, true);
        return docs;
    }

    @Override
    public ConfigChange getConfigChangeById(String id) {
        ConfigChange configChange = m_searchableService.searchDocById(SYSTEM_AUDIT_INDEX, id, ConfigChange.class);
        return configChange;
    }

    @Override
    public List<ConfigChange> loadConfigChangesByPage(int firstRow, int pageSize, String[] orderBy,
            boolean orderAscending, SystemAuditFilter filter) {
        QueryBuilder queryBuilder = getQueryBuilder(filter);
        List<ConfigChange> docs = m_searchableService.searchDocs(
                SYSTEM_AUDIT_INDEX, queryBuilder, firstRow, pageSize, ConfigChange.class, orderBy[0], orderAscending);
        return docs;
    }

    @Override
    public void storeConfigChange(final ConfigChange configChange) throws SystemAuditException {
        try {
            m_configChangeLoader.addConfigChangeToQueue(configChange);
        } catch (Exception e) {
            LOG.error("Error persisting System Audit event:", e);
        }
    }

    @Override
    public int getConfigChangesCount(SystemAuditFilter filter) {
        QueryBuilder queryBuilder = getQueryBuilder(filter);
        return m_searchableService.countDocs(SYSTEM_AUDIT_INDEX, queryBuilder);
    }

    /**
     * Creates a QueryBuilder object from SystemAuditFilter object
     */
    private QueryBuilder getQueryBuilder(SystemAuditFilter filter) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (filter.getStartDate() != null || filter.getEndDate() != null) {
            RangeQueryBuilder dateQuery = QueryBuilders.rangeQuery(ConfigChange.DATE_TIME);
            if (filter.getStartDate() != null) {
                dateQuery.from(filter.getStartDate().getTime());
            }
            if (filter.getEndDate() != null) {
                dateQuery.to(filter.getEndDate().getTime());
            }
            queryBuilder.must(dateQuery);
        }
        if (filter.getUserName() != null) {
            queryBuilder.must(QueryBuilders.queryStringQuery(ConfigChange.USER_NAME + FIRST_DELIMTER
                    + filter.getUserName() + LAST_DELIMTER));
        }
        if (filter.getDetails() != null) {
            queryBuilder.must(QueryBuilders.queryStringQuery(ConfigChange.DETAILS + FIRST_DELIMTER
                    + filter.getDetails().toLowerCase() + LAST_DELIMTER));
        }
        if (filter.getType() != null) {
            queryBuilder.must(QueryBuilders.matchQuery(ConfigChange.CONFIG_CHANGE_TYPE, filter.getType()));
        }
        if (filter.getAction() != null) {
            queryBuilder.must(QueryBuilders.matchQuery(ConfigChange.ACTION, filter.getAction()));
        }
        Set<Group> userGroups = filter.getUserGroup();
        Set<String> userNames = new HashSet<String>();
        if (!userGroups.isEmpty()) {
            for (Group userGroup : userGroups) {
                Collection<String> userNamesInGroup = m_coreContext.getGroupMembersNames(userGroup);
                userNames.addAll(userNamesInGroup);
            }
        }
        if (!userNames.isEmpty()) {
            queryBuilder.must(QueryBuilders.termsQuery(ConfigChange.USER_NAME, userNames));
        }
        return queryBuilder;
    }

    private List<ConfigChange> loadConfigChangesByFilter(String[] orderBy,
            boolean orderAscending, SystemAuditFilter filter) {
        QueryBuilder queryBuilder = getQueryBuilder(filter);
        List<ConfigChange> docs = m_searchableService.searchDocs(
                SYSTEM_AUDIT_INDEX, queryBuilder, 0, Integer.MAX_VALUE, ConfigChange.class, null, true);
        return docs;
    }

    @Override
    public void dumpSystemAuditLogs(PrintWriter writer, SystemAuditFilter filter) {
        // create header
        writer.print(ConfigChange.DATE_TIME + COMMA_SEPARATOR);
        writer.print(ConfigChange.USER_NAME + COMMA_SEPARATOR);
        writer.print(ConfigChange.IP_ADDRESS + COMMA_SEPARATOR);
        writer.print(ConfigChange.CONFIG_CHANGE_TYPE + COMMA_SEPARATOR);
        writer.print(ConfigChange.ACTION + COMMA_SEPARATOR);
        writer.println(ConfigChange.DETAILS + COMMA_SEPARATOR);

        // fill the table
        List<ConfigChange> configChangesList = loadConfigChangesByFilter(
                new String[] {ConfigChange.DATE_TIME}, false, filter);
        for (ConfigChange configChange : configChangesList) {
            writer.print(configChange.getDateTime() + COMMA_SEPARATOR);
            writer.print(configChange.getUserName() + COMMA_SEPARATOR);
            writer.print(configChange.getIpAddress() + COMMA_SEPARATOR);
            writer.print(configChange.getConfigChangeType() + COMMA_SEPARATOR);
            writer.print(configChange.getAction() + COMMA_SEPARATOR);
            writer.println(configChange.getDetails() + COMMA_SEPARATOR);
        }
    }

    @Required
    public void setSearchableService(SearchableService searchableService) {
        m_searchableService = searchableService;
    }

    @Required
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    @Required
    public void setConfigChangeLoader(ConfigChangeLoader configChangeLoader) {
        m_configChangeLoader = configChangeLoader;
    }

}
