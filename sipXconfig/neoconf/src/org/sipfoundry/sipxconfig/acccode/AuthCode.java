/*
 *
 *
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.acccode;

import static org.sipfoundry.commons.mongo.MongoConstants.AUTH_CODE;
import static org.sipfoundry.commons.mongo.MongoConstants.PASSTOKEN;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.common.BeanWithUserPermissions;

public class AuthCode extends BeanWithUserPermissions {
    private String m_code;
    private String m_description;
    private Set<Branch> m_locations = new HashSet<Branch>();

    public String getCode() {
        return m_code;
    }

    public void setCode(String code) {
        m_code = code;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
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
    public Map<String, Object> getMongoProperties(String domain) {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(AUTH_CODE, m_code);
        props.put(UID, getInternalUser().getUserName());
        props.put(PASSTOKEN, getInternalUser().getSipPassword());
        List<String> locations = new ArrayList<String>();
        for (Branch branch : m_locations) {
            locations.add(branch.getName());
        }
        props.put(MongoConstants.LOCATIONS, locations);
        return props;
    }
}
