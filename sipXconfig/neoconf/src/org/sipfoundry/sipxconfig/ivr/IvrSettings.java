/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.ivr;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.setting.AbstractSettingVisitor;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.type.EnumSetting;
import org.sipfoundry.sipxconfig.setting.type.SettingType;
import org.springframework.beans.factory.annotation.Required;

public class IvrSettings extends PersistableSettings implements DeployConfigOnEdit {
    public static final String IVR_BACKUP_HOST = "ivr/ivr.backup_host";
    private static final String HTTP_PORT = "ivr/ivr.httpPort";
    private static final String AUDIO_FORMAT = "ivr/audio.format";
    private FeatureManager m_featureManager;

    @Override
    public void initialize() {
        getSettings().acceptVisitor(new IvrHostsVisitor());
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) Ivr.FEATURE);
    }

    @Override
    protected Setting loadSettings() {
        Setting ivrSetting = getModelFilesContext().loadModelFile("sipxivr/sipxivr.xml");
        return ivrSetting;
    }

    public int getHttpPort() {
        return (Integer) getSettingTypedValue(HTTP_PORT);
    }

    public String getAudioFormat() {
        return getSettingValue(AUDIO_FORMAT);
    }

    public String getBackupHost() {
        return getSettingValue(IVR_BACKUP_HOST);
    }

    @Override
    public String getBeanId() {
        return "ivrSettings";
    }

    @Required
    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

    public class IvrHostsVisitor extends AbstractSettingVisitor {

        @Override
        public void visitSetting(Setting setting) {
            SettingType type = setting.getType();
            if (type instanceof EnumSetting && setting.getPath().equals(IVR_BACKUP_HOST)) {
                EnumSetting ivrHost = (EnumSetting) type;
                ivrHost.clearEnums();
                List<Location> locations = m_featureManager.getLocationsForEnabledFeature(Ivr.FEATURE);
                for (Location location : locations) {
                    ivrHost.addEnum(location.getAddress(), location.getFqdn());
                }
            }
        }
    }
}
