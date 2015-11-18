/**
 *
 * Copyright (c) 2014 Karel Electronics Corp. All rights reserved.
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
 *
 */
package org.sipfoundry.sipxconfig.site.vm;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Parameter;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences;

public abstract class MailboxSecurity extends BaseComponent {

    public abstract MailboxPreferences getMailboxPreferences();

    public abstract void setMailboxPreferences(MailboxPreferences preferences);

    @Parameter(required = false, defaultValue = "ognl:false")
    public abstract boolean isAdvanced();

    @Parameter(required = true)
    public abstract User getUser();

    public abstract boolean isForcePinChange();

    public abstract void setForcePinChange(boolean forcePinChange);

    public abstract boolean isAutoEnterPinExtension();

    public abstract void setAutoEnterPinExtension(boolean autoEnterPinExtension);

    public abstract boolean isAutoEnterPinExternal();

    public abstract void setAutoEnterPinExternal(boolean autoEnterPinExternal);

    public abstract int getDaysToKeepVM();

    public abstract void setDaysToKeepVM(int daysToKeepVM);

    @Override
    protected void renderComponent(IMarkupWriter writer, IRequestCycle cycle) {

        setForcePinChange(getUser().isForcePinChange());
        setAutoEnterPinExtension(getUser().isAutoEnterPinExtension());
        setAutoEnterPinExternal(getUser().isAutoEnterPinExternal());
        setDaysToKeepVM(getUser().getDaysToKeepVM());
        super.renderComponent(writer, cycle);
        if (TapestryUtils.isRewinding(cycle, this)) {
            getUser().setForcePinChange(isForcePinChange());
            getUser().setAutoEnterPinExtension(isAutoEnterPinExtension());
            getUser().setAutoEnterPinExternal(isAutoEnterPinExternal());
            getUser().setDaysToKeepVM(getDaysToKeepVM());
        }
    }
}
