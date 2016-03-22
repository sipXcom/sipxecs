/**
 * Copyright (c) 2016 eZuce, Inc. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
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
package org.sipfoundry.sipxconfig.api.impl;

import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.security.StandardUserDetailsService;
import org.sipfoundry.sipxconfig.security.UserDetailsImpl;

public class CurrentUser {

    private CoreContext m_coreContext;

    public User getCurrentUser() {
        if (m_coreContext != null) {
            UserDetailsImpl userDetails = StandardUserDetailsService.getUserDetails();
            return (userDetails != null) ? m_coreContext.loadUser(userDetails.getUserId()) : null;
        } else {
            return null;
        }
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public CoreContext getCoreContext() {
        return m_coreContext;
    }
}
