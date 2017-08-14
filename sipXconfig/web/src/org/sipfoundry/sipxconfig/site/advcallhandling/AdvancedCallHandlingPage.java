/**
 * Copyright (c) 2017 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.site.advcallhandling;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.advcallhandling.AdvancedCallHandling;
import org.sipfoundry.sipxconfig.advcallhandling.AdvancedCallHandlingSettings;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;

public abstract class AdvancedCallHandlingPage extends PageWithCallback implements PageBeginRenderListener {

    public static final String PAGE = "advcallhandling/AdvancedCallHandlingPage";

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:advCallHandling")
    public abstract AdvancedCallHandling getAdvCallHandling();

    public abstract AdvancedCallHandlingSettings getSettings();

    public abstract void setSettings(AdvancedCallHandlingSettings settings);

    @Override
    public void pageBeginRender(PageEvent arg0) {
        if (getSettings() == null) {
            setSettings(getAdvCallHandling().getSettings());
        }
    }

    public void apply() {
        getAdvCallHandling().saveSettings(getSettings());
    }
}
