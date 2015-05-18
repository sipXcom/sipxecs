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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.CallbackRule;
import org.sipfoundry.sipxconfig.dialplan.DialingRule;
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

public class CallbackOnBusyImpl implements FeatureProvider, CallbackOnBusy, ProcessProvider {
    private static final Log LOG = LogFactory.getLog(CallbackOnBusyImpl.class);

    private BeanWithSettingsDao<CallbackSettings> m_settingsDao;
    private FeatureManager m_featureManager;
    private AddressManager m_addressManager;

    @Override
    public List<? extends DialingRule> getDialingRules(Location location) {
        List<Location> locations = m_featureManager.getLocationsForEnabledFeature(FEATURE);
        if (locations.isEmpty()) {
            return Collections.emptyList();
        }

        Address fsAddress = m_addressManager.getSingleAddress(FreeswitchFeature.SIP_ADDRESS);
        List<DialingRule> dialingRules = new ArrayList<DialingRule>();
        CallbackSettings settings = getSettings();
        String prefix = settings.getCallbackPrefix();
        if (StringUtils.isEmpty(prefix)) {
            return Collections.emptyList();
        }
        String fsAddressPort = locations.get(0).getAddress() + ':' + fsAddress.getPort();
        CallbackRule rule = new CallbackRule(prefix, fsAddressPort);
        rule.appendToGenerationRules(dialingRules);
        return dialingRules;
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager,
            FeatureChangeValidator validator) {
        validator.requiredOnSameHost(FEATURE, FreeswitchFeature.FEATURE);
        validator.singleLocationOnly(FEATURE);
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
    public void setAddressManager(AddressManager addressManager) {
        m_addressManager = addressManager;
    }

}
