/**
 *
 *
 * Copyright (c) 2015 sipXcom, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.CallbackRule;
import org.sipfoundry.sipxconfig.dialplan.DialingRule;
import org.sipfoundry.sipxconfig.dns.DnsManager;
import org.sipfoundry.sipxconfig.dns.DnsProvider;
import org.sipfoundry.sipxconfig.dns.ResourceRecord;
import org.sipfoundry.sipxconfig.dns.ResourceRecords;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;
import org.springframework.beans.factory.annotation.Required;

public class CallbackOnBusyImpl implements FeatureProvider, CallbackOnBusy,
    ProcessProvider, DnsProvider, AddressProvider {

    public static final AddressType CBB_SIP_ADDRESS = AddressType.sipTcp("cbb-sip");
    private static final Collection<AddressType> ADDRESSES = Arrays.asList(new AddressType[] {
        CBB_SIP_ADDRESS
    });
    private static final String CBB = "cbb";

    private BeanWithSettingsDao<CallbackSettings> m_settingsDao;
    private FeatureManager m_featureManager;
    private DomainManager m_domainManager;
    private FreeswitchFeature m_fsFeature;

    @Override
    public List<? extends DialingRule> getDialingRules(Location location) {
        List<Location> locations = m_featureManager.getLocationsForEnabledFeature(FEATURE);
        if (locations.isEmpty()) {
            return Collections.emptyList();
        }

        List<DialingRule> dialingRules = new ArrayList<DialingRule>();
        CallbackSettings settings = getSettings();
        String prefix = settings.getCallbackPrefix();
        if (StringUtils.isEmpty(prefix)) {
            return Collections.emptyList();
        }
        String fsAddressLocation = locations.get(0).getFqdn();
        CallbackRule rule = new CallbackRule(prefix, fsAddressLocation);
        rule.appendToGenerationRules(dialingRules);
        return dialingRules;
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager,
            FeatureChangeValidator validator) {
        validator.requiredOnSameHost(FEATURE, FreeswitchFeature.FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager,
            FeatureChangeRequest request) {
        if (request.getAllNewlyEnabledFeatures().contains(FEATURE)) {
            CallbackSettings settings = getSettings();
            if (settings.isNew()) {
                saveSettings(settings);
            }
        }
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        boolean enabled = manager.getFeatureManager().isFeatureEnabled(FEATURE, location);
        return (enabled ? Collections.singleton(ProcessDefinition.sipxByRegex("sipxcallback",
                ".*\\s-Dprocname=sipxcallback\\s.*")) : null);
    }

    @Override
    public CallbackSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(CallbackSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(
            FeatureManager featureManager, Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE_TELEPHONY) {
            b.addFeature(FEATURE);
        }
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        return getAvailableAddresses(manager, type);
    }

    Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type) {
        if (!ADDRESSES.contains(type)) {
            return null;
        }
        List<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        List<Address> addresses = new ArrayList<Address>(locations.size());
        for (Location location : locations) {
            Address address = null;
            if (type.equals(CBB_SIP_ADDRESS)) {
                address = new Address(CBB_SIP_ADDRESS,
                        location.getAddress(), m_fsFeature.getSettings(location).getFreeswitchSipPort());
            }
            addresses.add(address);
        }
        return addresses;
    }

    @Override
    public Address getAddress(DnsManager manager, AddressType t,
            Collection<Address> addresses, Location whoIsAsking) {
        if (!t.equals(CBB_SIP_ADDRESS) || !m_featureManager.isFeatureEnabled(FEATURE)) {
            return null;
        }

        if (whoIsAsking != null && m_featureManager.isFeatureEnabled(FEATURE, whoIsAsking)) {
            return new Address(t, getAddress(whoIsAsking.getHostnameInSipDomain()));
        }
        return new Address(t, getAddress(m_domainManager.getDomainName()));
    }

    private String getAddress(String host) {
        return String.format("%s.%s", CBB, host);
    }

    @Override
    public Collection<ResourceRecords> getResourceRecords(DnsManager manager) {
        FeatureManager fm = manager.getAddressManager().getFeatureManager();
        List<Location> locations = fm.getLocationsForEnabledFeature(FEATURE);
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }
        ResourceRecords records = new ResourceRecords("_sip._tcp", CBB, true);
        for (Location l : locations) {
            int port = m_fsFeature.getSettings(l).getFreeswitchSipPort();
            records.addRecord(new ResourceRecord(l.getHostname(), port, l.getRegionId()));
        }
        return Collections.singleton(records);
    }

    public boolean isEnabled() {
        return m_featureManager.isFeatureEnabled(FEATURE);
    }

    @Required
    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

    @Required
    public void setSettingsDao(BeanWithSettingsDao<CallbackSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    @Required
    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

    @Required
    public void setFreeswitchFeature(FreeswitchFeature fsFeature) {
        m_fsFeature = fsFeature;
    }

}
