/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.commserver.imdb;

import org.sipfoundry.sipxconfig.admin.ConfigurationFile;
import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.permission.Permission;

/**
 * Interface to replication.cgi
 */
public interface ReplicationManager {
    /**
     * Replicates IMDB data sets to remore locations
     *
     * @param locations list of locations that will receive replicated data
     * @param generator data set to be replicated
     * @return true if the replication has been successful, false otherwise
     */
    /**
     * Replicates file content to remore locations
     *
     * @param locations list of locations that will receive replicated file
     * @param file object representing file content
     *
     * @return true if the replication has been successful, false otherwise
     */
    boolean replicateFile(Location[] locations, ConfigurationFile file);

    void replicateEntity(Replicable entity);

    void removeEntity(Replicable entity);

    void replicateLocation(Location location);

    void removeLocation(Location location);

    void replicateAllData();
    void replicateAllData(DataSet ds);

    void resyncSlave(Location location);

    /**
     * Adds this permission to entities that support permissions and do not have it.
     * Used when adding a permission with default value checked.
     */
    void addPermission(Permission perm);

    /**
     * Removes this permission from entities that have it.
     * Used when deleting a permission.
     */
    void removePermission(Permission perm);
}
