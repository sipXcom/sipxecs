/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.branch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class BranchRoutes {
    private List<String> m_domains = new ArrayList<String>();
    private List<String> m_subnets = new ArrayList<String>();

    public void setDomains(List<String> domains) {
        m_domains = domains;
    }

    public List<String> getDomains() {
        return m_domains;
    }

    public void setSubnets(List<String> subnets) {
        m_subnets = subnets;
    }

    public List<String> getSubnets() {
        return m_subnets;
    }

    public boolean addDomain() {
        return getDomains().add(StringUtils.EMPTY);
    }

    public String removeDomain(int index) {
        return getDomains().remove(index);
    }

    public boolean addSubnet() {
        return getSubnets().add(StringUtils.EMPTY);
    }

    public String removeSubnet(int index) {
        return getSubnets().remove(index);
    }

    public boolean isEmpty() {
        return getSubnets().isEmpty() && getDomains().isEmpty();
    }
}
