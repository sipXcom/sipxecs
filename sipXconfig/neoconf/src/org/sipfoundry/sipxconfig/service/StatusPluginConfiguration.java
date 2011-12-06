/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.service;

import org.apache.velocity.VelocityContext;
import org.sipfoundry.sipxconfig.commserver.Location;

public class StatusPluginConfiguration extends SipxServiceConfiguration {
    @Override
    protected VelocityContext setupContext(Location location) {
        VelocityContext context = super.setupContext(location);
        SipxService ivrService = getService(SipxIvrService.BEAN_ID);
        context.put("ivrService", ivrService);
        return context;
    }

    @Override
    public boolean isReplicable(Location location) {
        return getSipxServiceManager().isServiceInstalled(location.getId(), SipxStatusService.BEAN_ID);
    }
}
