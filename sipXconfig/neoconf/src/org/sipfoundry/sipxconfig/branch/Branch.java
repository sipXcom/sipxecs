/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.branch;

import static org.sipfoundry.commons.mongo.MongoConstants.LOCATION_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.LOCATION_RESTRICTIONS_DOMAINS;
import static org.sipfoundry.commons.mongo.MongoConstants.LOCATION_RESTRICTIONS_SUBNETS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sipfoundry.sipxconfig.common.BeanWithId;
import org.sipfoundry.sipxconfig.common.NamedObject;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.phonebook.Address;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditable;


public class Branch extends BeanWithId implements NamedObject, SystemAuditable, Replicable {
    private String m_name;
    private String m_description;
    private Address m_address = new Address();
    private String m_phoneNumber;
    private String m_faxNumber;
    private String m_timeZone;
    private BranchRoutes m_routes = new BranchRoutes();

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public Address getAddress() {
        return m_address;
    }

    /**
     * (hibernate injects null value here when all homeAddress fields are empty) see:
     * http://opensource.atlassian.com/projects/hibernate/browse/HB-31
     */
    public void setAddress(Address address) {
        m_address = address == null ? new Address() : address;
    }

    public String getPhoneNumber() {
        return m_phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        m_phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return m_faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        m_faxNumber = faxNumber;
    }

    public String getTimeZone() {
        return m_timeZone;
    }

    public void setTimeZone(String timeZone) {
        m_timeZone = timeZone;
    }

    @Override
    public String getEntityIdentifier() {
        return getName();
    }

    @Override
    public String getConfigChangeType() {
        return Branch.class.getSimpleName();
    }

    public BranchRoutes getRoutes() {
        return m_routes;
    }

    public void setRoutes(BranchRoutes routes) {
        m_routes = routes;
    }

    @Override
    public Set<DataSet> getDataSets() {
        return null;
    }

    @Override
    public String getIdentity(String domainName) {
        return null;
    }

    @Override
    public Collection<AliasMapping> getAliasMappings(String domainName) {
        return null;
    }

    @Override
    public boolean isValidUser() {
        return false;
    }

    @Override
    public Map<String, Object> getMongoProperties(String domain) {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(LOCATION_NAME, getName());
        props.put(LOCATION_RESTRICTIONS_DOMAINS, m_routes.getDomains());
        props.put(LOCATION_RESTRICTIONS_SUBNETS, m_routes.getSubnets());
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
