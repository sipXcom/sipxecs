/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
 */
package org.sipfoundry.sipxconfig.mongo;

import static java.lang.String.format;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.BasicBSONObject;
import org.sipfoundry.commons.mongo.MongoUtil;
import org.sipfoundry.commons.mongo.MongoUtil.MongoCommandException;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureListener;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Add/remove servers from Mongo's replica set server list stored in the "local" database on each
 * server.
 */
public class MongoReplicaSetManager implements FeatureListener {
    private static final Log LOG = LogFactory.getLog(MongoReplicaSetManager.class);
    private static final String REPLSET = "sipxecs";
    private static final String CHECK_COMMAND = "rs.config()";
    private static final String INIT_COMMAND = "rs.initiate({\"_id\" : \"sipxecs\", \"version\" : 1, \"members\" : "
            + "[ { \"_id\" : 0, \"host\" : \"%s:%d\" } ] })";
    private static final String ADD_SERVER_COMMAND = "rs.add(\"%s\")";
    private static final String REMOVE_SERVER_COMMAND = "rs.remove(\"%s\")";
    private static final String ADD_ARBITER_COMMAND = "rs.addArb(\"%s\")";
    private static final String REMOVE_ARBITER_COMMAND = REMOVE_SERVER_COMMAND;
    private MongoTemplate m_localDb;
    private JdbcTemplate m_jdbcTemplate;

    public void checkState() {
        BasicBSONObject ret = MongoUtil.runCommand(m_localDb.getDb(), CHECK_COMMAND);
        if (ret == null) {
            initialize();
        }
    }

    public void initialize() {
        // cannot use business object because they are not initialized yet
        try {
            String fqdn = InetAddress.getLocalHost().getHostName();
            LOG.info("initializing mongo replicaset to host " + fqdn);
            String cmd = format(INIT_COMMAND, fqdn, MongoSettings.SERVER_PORT);
            MongoUtil.runCommand(m_localDb.getDb(), cmd);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot get FQDN to initialize mongo.");
        }
    }

    public void checkMembers() {
        Set<String> mongoMembers = new HashSet<String>();
        Set<String> mongoArbiters = new HashSet<String>();
        Set<String> postgresMembers = new HashSet<String>();
        Set<String> postgresArbiters = new HashSet<String>();
        getMongoMembers(mongoMembers, mongoArbiters);
        getPostgresMembers(postgresMembers, postgresArbiters);
        List<String> cmds = updateMembersAndArbitors(mongoMembers, mongoArbiters, postgresMembers, postgresArbiters);
        for (String cmd : cmds) {
            boolean complete = false;
            // A new mongo server takes a while before it will accept connections
            // hang out until it's available
            for (int i = 0; !complete || i < 3; i++) {
                try {
                    BasicBSONObject result = MongoUtil.runCommand(m_localDb.getDb(), cmd);
                    MongoUtil.checkForError(result);
                    complete = true;
                } catch (MongoCommandException e) {
                    try {
                        Thread.sleep((i + 1) * 10000);
                        i++;
                    } catch (InterruptedException ignore) {
                        i++;
                    }
                }
            }
        }
    }

    List<String> updateMembersAndArbitors(Set<String> mongoMembers, Set<String> mongoArbiters,
            Set<String> postgresMembers, Set<String> postgresArbiters) {
        List<String> commands = new ArrayList<String>();
        Collection<String> missingServers = CollectionUtils.subtract(postgresMembers, mongoMembers);
        for (String add : missingServers) {
            String cmd = format(ADD_SERVER_COMMAND, add);
            commands.add(cmd);
        }
        Collection<String> additionalServers = CollectionUtils.subtract(mongoMembers, postgresMembers);
        for (String remove : additionalServers) {
            String cmd = format(REMOVE_SERVER_COMMAND, remove);
            commands.add(cmd);
        }
        Collection<String> missingArbiters = CollectionUtils.subtract(postgresArbiters, mongoArbiters);
        for (String add : missingArbiters) {
            String cmd = format(ADD_ARBITER_COMMAND, add);
            commands.add(cmd);
        }
        Collection<String> additionaArbiters = CollectionUtils.subtract(mongoArbiters, postgresArbiters);
        for (String remove : additionaArbiters) {
            String cmd = format(REMOVE_ARBITER_COMMAND, remove);
            commands.add(cmd);
        }
        return commands;
    }

    void getPostgresMembers(Set<String> members, Set<String> arbiters) {
        String sql = "select fqdn, feature_id from feature_local f, location l where f.location_id = l.location_id "
                + "and feature_id in (? , ?)";
        List<Map<String, Object>> pgLocations = m_jdbcTemplate.queryForList(sql, MongoManager.FEATURE_ID.getId(),
                MongoManager.ARBITER_FEATURE.getId());
        for (Map<String, Object> line : pgLocations) {
            boolean arbiter = MongoManager.ARBITER_FEATURE.getId().equals(line.get("feature_id"));
            String host = (String) line.get("fqdn");
            if (arbiter) {
                arbiters.add(host + ':' + MongoSettings.ARBITER_PORT);
            } else {
                members.add(host + ':' + MongoSettings.SERVER_PORT);
            }
        }
        String primary = m_jdbcTemplate.queryForObject("select fqdn from location where primary_location = true",
                String.class);
        members.add(primary + ':' + MongoSettings.SERVER_PORT);
    }

    void getMongoMembers(Set<String> members, Set<String> arbiters) {
        DB ds = m_localDb.getDb();
        DBCollection registrarCollection = ds.getCollection("system.replset");
        DBObject repl = registrarCollection.findOne(new BasicDBObject("_id", REPLSET));
        BasicDBList membersObj = (BasicDBList) repl.get("members");
        for (Object o : membersObj) {
            BasicDBObject dbo = ((BasicDBObject) o);
            String host = dbo.getString("host");
            if (dbo.getBoolean("arbitorOnly")) { // note: wrong spelling from mongo
                arbiters.add(host);
            } else {
                members.add(host);
            }
        }
    }

    @Override
    public void enableLocationFeature(FeatureManager manager, FeatureEvent event, LocationFeature feature,
            Location location) {

        if (!feature.equals(MongoManager.FEATURE_ID) || feature.equals(MongoManager.ARBITER_FEATURE)) {
            return;
        }

        if (event != FeatureEvent.POST_ENABLE || event != FeatureEvent.POST_DISABLE) {
            return;
        }
        //checkMembers();
    }

    @Override
    public void enableGlobalFeature(FeatureManager manager, FeatureEvent event, GlobalFeature feature) {
    }

    public void setLocalDb(MongoTemplate localDb) {
        m_localDb = localDb;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }
}
