/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.paging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.common.BeanWithId;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditable;

public class PagingGroup extends BeanWithId implements DeployConfigOnEdit, SystemAuditable, Replicable {

    private int m_pageGroupNumber;

    private String m_description;

    private boolean m_enabled = true; // default enabled

    private String m_sound;

    private int m_timeout = 60;       // default to 60 seconds

    private Set<User> m_users = new HashSet<User>();

    private Set<Branch> m_locations = new HashSet<Branch>();

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }

    public int getPageGroupNumber() {
        return m_pageGroupNumber;
    }

    public void setPageGroupNumber(int number) {
        m_pageGroupNumber = number;
    }

    public String getSound() {
        return m_sound;
    }

    public void setSound(String sound) {
        m_sound = sound;
    }

    public Set<User> getUsers() {
        return m_users;
    }

    public void setUsers(Set<User> users) {
        m_users = users;
    }

    public String formatUserList(String domain) {
        List<String> users = new ArrayList<String>();
        for (User user : m_users) {
            users.add(user.getUserName() + "@" + domain);
        }
        return StringUtils.join(users.toArray(), ',');
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public Set<Branch> getLocations() {
        return m_locations;
    }

    public void setLocations(Set<Branch> locations) {
        m_locations = locations;
    }

    public List<Branch> getLocationsList() {
        return new ArrayList<Branch>(m_locations);
    }

    public void setLocationsList(List<Branch> locations) {
        m_locations.clear();
        m_locations.addAll(locations);
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singletonList((Feature) PagingContext.FEATURE);
    }

    @Override
    public String getEntityIdentifier() {
        return String.valueOf(getPageGroupNumber());
    }

    @Override
    public String getConfigChangeType() {
        return PagingGroup.class.getSimpleName();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public Set<DataSet> getDataSets() {
        return Collections.singleton(DataSet.CALLER_ALIAS);
    }

    @Override
    public String getIdentity(String domainName) {
        return SipUri.stripSipPrefix(SipUri.format(null, String.valueOf(getPageGroupNumber()), domainName));
    }

    @Override
    public Collection<AliasMapping> getAliasMappings(String domainName) {
        return Collections.emptyList();
    }

    @Override
    public boolean isValidUser() {
        return false;
    }

    @Override
    public Map<String, Object> getMongoProperties(String domain) {
        Map<String, Object> props = new HashMap<String, Object>();
        List<String> locations = new ArrayList<String>();
        for (Branch branch : m_locations) {
            locations.add(branch.getName());
        }
        props.put(MongoConstants.LOCATIONS, locations);
        return props;
    }

    @Override
    public String getEntityName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isReplicationEnabled() {
        return true;
    }
}
