/**
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
package org.sipfoundry.sipxconfig.firewall;

import org.sipfoundry.sipxconfig.address.AddressType;

public class DefaultFirewallRule implements FirewallRule {
    private boolean m_priority;
    private SystemId m_systemId;
    private AddressType m_addressType;

    public DefaultFirewallRule(AddressType type, SystemId systemId) {
        m_systemId = systemId;
        m_addressType = type;
    }

    public DefaultFirewallRule(AddressType type, SystemId systemId, boolean priority) {
        this(type, systemId);
        m_priority = priority;
    }

    @Override
    public boolean isPriority() {
        return m_priority;
    }

    @Override
    public AddressType getAddressType() {
        return m_addressType;
    }

    public boolean isChangedFromDefault() {
        return false;
    }

    public SystemId getSystemId() {
        return m_systemId;
    }
}
