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
package org.sipfoundry.sipxconfig.site.callback;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.callback.CallbackOnBusy;
import org.sipfoundry.sipxconfig.callback.CallbackSettings;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;

public abstract class EditCallback extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "callback/EditCallback";

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:callback")
    public abstract CallbackOnBusy getCallbackManager();

    public abstract CallbackSettings getSettings();

    public abstract void setSettings(CallbackSettings settings);

    @Override
    public void pageBeginRender(PageEvent arg0) {
        if (getSettings() == null) {
            setSettings(getCallbackManager().getSettings());
        }
    }

    public void apply() {
        getCallbackManager().saveSettings(getSettings());
    }
}
