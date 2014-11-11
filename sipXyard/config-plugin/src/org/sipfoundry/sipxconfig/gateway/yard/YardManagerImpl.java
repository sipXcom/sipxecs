/**
 *
 *
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.gateway.yard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.firewall.DefaultFirewallRule;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallProvider;
import org.sipfoundry.sipxconfig.firewall.FirewallRule;
import org.sipfoundry.sipxconfig.gateway.GatewayContext;

public class YardManagerImpl implements YardManager, FirewallProvider, AddressProvider {
    private GatewayContext m_gatewayContext;
    private LocationsManager m_locationsManager;

    @Override
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        Collection<DefaultFirewallRule> rules = new ArrayList<DefaultFirewallRule>();
        List< ? extends YardGateway> yardGws = m_gatewayContext.getGatewayByType(YardGateway.class);
        List<Location> locations = m_locationsManager.getLocationsList();
        List<String> locationsAddresses = new ArrayList<String>();
        for (Location l : locations) {
            locationsAddresses.add(l.getAddress());
            locationsAddresses.add(l.getFqdn());
        }
        AddressType wsAddressType = null;
        AddressType httpAddressType = null;
        for (YardGateway yard : yardGws) {
            if (locationsAddresses.contains(yard.getAddress())) {
                wsAddressType = new AddressType("yardWsPort", yard.getWsPort());
                wsAddressType.setLabel(yard.getName());
                httpAddressType = new AddressType("yardHttpPort", yard.getAddressPort());
                httpAddressType.setLabel(yard.getName());
                rules.add(new DefaultFirewallRule(wsAddressType, FirewallRule.SystemId.PUBLIC));
                rules.add(new DefaultFirewallRule(httpAddressType, FirewallRule.SystemId.PUBLIC));
            }
        }
        rules.add(new DefaultFirewallRule(WS_SIP_ADDRESS, FirewallRule.SystemId.PUBLIC));
        rules.add(new DefaultFirewallRule(BRIDGE_ESL_ADDRESS, FirewallRule.SystemId.PUBLIC));
        rules.add(new DefaultFirewallRule(SWITCH_ESL_ADDRESS, FirewallRule.SystemId.PUBLIC));
        rules.add(new DefaultFirewallRule(TCP_UDP_ADDRESS, FirewallRule.SystemId.PUBLIC));
        rules.add(new DefaultFirewallRule(BRIDGE_TCP_UDP_PORT, FirewallRule.SystemId.PUBLIC));
        rules.add(new DefaultFirewallRule(FS_RTP_RTCP_ADDRESS, FirewallRule.SystemId.PUBLIC));
        rules.add(new DefaultFirewallRule(MONIT_ADDRESS, FirewallRule.SystemId.PUBLIC));

        return rules;
    }

    public void setGatewayContext(GatewayContext gatewayContext) {
        m_gatewayContext = gatewayContext;
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        Collection<Address> addresses = new ArrayList<Address>();
        if (!type.equalsAnyOf(FS_RTP_RTCP_ADDRESS, WS_SIP_ADDRESS, BRIDGE_ESL_ADDRESS, SWITCH_ESL_ADDRESS,
                TCP_UDP_ADDRESS, BRIDGE_TCP_UDP_PORT, FS_RTP_RTCP_ADDRESS, MONIT_ADDRESS)
                && !type.getId().contains("yardWsPort") && !type.getId().contains("yardHttpPort")) {
            return null;
        }
        if (type.equals(FS_RTP_RTCP_ADDRESS)) {
            Address fs;
            if (requester == null) {
                fs = new Address(FS_RTP_RTCP_ADDRESS);
            } else {
                fs = new Address(FS_RTP_RTCP_ADDRESS, requester.getAddress());
            }
            fs.setEndPort(32768);
            addresses.add(fs);
        } else {
            if (requester == null) {
                addresses.add(new Address(type));
            } else {
                addresses.add(new Address(type, requester.getAddress()));
            }
        }

        return addresses;
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }
}
