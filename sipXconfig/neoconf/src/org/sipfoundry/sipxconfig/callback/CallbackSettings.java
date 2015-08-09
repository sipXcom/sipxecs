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
package org.sipfoundry.sipxconfig.callback;

import java.util.Arrays;
import java.util.Collection;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class CallbackSettings extends PersistableSettings implements DeployConfigOnEdit {

    public static final String CALLBACK_PREFIX = "callback-config/callback-prefix";

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) CallbackOnBusy.FEATURE, DialPlanContext.FEATURE);
    }

    @Override
    public String getBeanId() {
        return "callbackSettings";
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxcallback/sipxcallback.xml");
    }

    public String getCallbackPrefix() {
        return getSettingValue(CALLBACK_PREFIX);
    }
}
