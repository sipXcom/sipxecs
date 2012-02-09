/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.openfire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.im.ImManager;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;

public class OpenfireImpl extends ImManager implements FeatureProvider, AddressProvider, Openfire {
    private static final Collection<AddressType> ADDRESSES = Arrays.asList(new AddressType[] {
        XMPP_ADDRESS, XMLRPC_ADDRESS, WATCHER_ADDRESS
    });
    private BeanWithSettingsDao<OpenfireSettings> m_settingsDao;

    @Override
    public OpenfireSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(OpenfireSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures() {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public Collection<AddressType> getSupportedAddressTypes(AddressManager manager) {
        return ADDRESSES;
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type,
            Object requester) {
        if (!ADDRESSES.contains(type)) {
            return null;
        }
        Collection<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        // only zero or one at this time allowed
        if (locations.isEmpty()) {
            return null;
        }

        OpenfireSettings settings = getSettings();
        List<Address> addresses = new ArrayList<Address>(locations.size());
        for (Location location : locations) {
            Address address = null;
            if (type.equals(XMPP_ADDRESS)) {
		address = new Address(XMPP_ADDRESS, location.getAddress(), settings.getXmppPort());
            } else if (type.equals(XMLRPC_ADDRESS)) {
		address = new Address(XMLRPC_ADDRESS, location.getAddress(), settings.getXmlRpcPort());
            } else if (type.equals(WATCHER_ADDRESS)) {
		address = new Address(WATCHER_ADDRESS, location.getAddress(), settings.getWatcherPort());
            }
            addresses.add(address);
        }
        
        return addresses;            
    }

    public void setSettingsDao(BeanWithSettingsDao<OpenfireSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }
}
