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
import java.util.List;

/**
 * This interface contains DAO methods for ConfigChangeObjects
 */
public interface ConfigChangeContext {

    static final String SYSTEM_AUDIT_INDEX = "audit";
    static final String COMMA_SEPARATOR = ",";
    String GROUP_RESOURCE_ID = "configChange";

    public List<ConfigChange> getConfigChanges();

    public int getConfigChangesCount(SystemAuditFilter filter);

    public List<ConfigChange> loadConfigChangesByPage(int firstRow, int pageSize,
            String[] orderBy, boolean orderAscending, SystemAuditFilter filter);

    /**
     * Method to store a ConfigChange using a different session. This is because
     * most of configChange saves happen in the precommit phase of another
     * entity
     *
     * @param configChange
     * @throws SystemAuditException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void storeConfigChange(ConfigChange configChange) throws SystemAuditException;

    /**
     * Export current System Audit table contents to CSV
     */
    public void dumpSystemAuditLogs(PrintWriter writer, SystemAuditFilter filter);

    public ConfigChange getConfigChangeById(String id);

}
