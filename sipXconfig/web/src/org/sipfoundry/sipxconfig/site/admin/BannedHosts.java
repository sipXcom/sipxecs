/**
 *
 * Copyright (c) 2020 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.site.admin;

import java.util.Collection;
import java.util.List;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.fail2ban.BannedHost;
import org.sipfoundry.sipxconfig.fail2ban.Fail2banManager;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

public abstract class BannedHosts extends SipxBasePage implements PageBeginRenderListener {

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject(value = "spring:fail2banManager")
    public abstract Fail2banManager getFail2banManager();

    @Bean
    public abstract SelectMap getSelections();

    public abstract List<BannedHost> getBannedHosts();

    public abstract void setBannedHosts(List<BannedHost> bannedHosts);

    @Override
    public void pageBeginRender(PageEvent evt) {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        if (getBannedHosts() == null) {
            List<BannedHost> list = getFail2banManager().getBannedHosts();
            setBannedHosts(list);
        }
    }

    public void unbanSelectedHosts() {
        Collection selection = getSelections().getAllSelected();
        if (selection.size() > 0) {
            getFail2banManager().unbanSelectedHosts(getSelections().getAllSelected());
            getValidator().recordSuccess(getMessages().getMessage("msg.success"));
        }
    }

    public void refresh() {
        setBannedHosts(null);
    }

    public String getBannedHostsCount() {
        int count = getBannedHosts().size();
        return getMessages().format("label.bannedHostsCount", count);
    }
}
