/**
 * Copyright (c) 2015 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxivr;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;

public class HazelcastStarter {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");

    boolean m_hzEnabled;

    public void init() {
        if (m_hzEnabled) {
            LOG.info("Starting Hazelcast instance");
            Hazelcast.newHazelcastInstance();
        } else {
            LOG.warn("Component notification (Hazelcast based) is not enabled. " +
                    "Certain features (like voicemail event notifications over XMPP) are not available");
        }
    }

    public void setHzEnabled(boolean enabled) {
        m_hzEnabled = enabled;
    }

}
