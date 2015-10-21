/*
 *
 *
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.callback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.LoggerKeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingUtil;
import org.springframework.beans.factory.annotation.Required;

public class CallbackConfiguration implements ConfigProvider {
    private CallbackOnBusyImpl m_callbackImpl;
    private String m_callbackSettingKeyString = "callback-config";

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(CallbackOnBusy.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        Address fs = manager.getAddressManager().getSingleAddress(FreeswitchFeature.CALLBACK_EVENT_ADDRESS);
        Domain domain = manager.getDomainManager().getDomain();
        CallbackSettings settings = m_callbackImpl.getSettings();
        Setting calllbackSettings = settings.getSettings().getSetting(m_callbackSettingKeyString);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = manager.getFeatureManager().isFeatureEnabled(CallbackOnBusy.FEATURE, location);
            ConfigUtils.enableCfengineClass(dir, "sipxcallback.cfdat", enabled, "sipxcallback");
            if (!enabled) {
                continue;
            }

            String log4jFileName = "log4j-callback.properties.part";
            String[] logLevelKeys = {"log4j.logger.org.sipfoundry.sipxcallback"};
            SettingUtil.writeLog4jSetting(calllbackSettings, dir, log4jFileName, logLevelKeys);

            Writer flat = new FileWriter(new File(dir, "sipxcallback.properties.part"));
            try {
                writeConfig(flat, settings, domain, fs.getPort());
            } finally {
                IOUtils.closeQuietly(flat);
            }
        }
    }

    void writeConfig(Writer wtr, CallbackSettings settings, Domain domain, int freeswithPort) throws IOException {
        LoggerKeyValueConfiguration config = LoggerKeyValueConfiguration.equalsSeparated(wtr);
        config.writeSettings(settings.getSettings().getSetting(m_callbackSettingKeyString));
        config.write("freeswitch.eventSocketPort", freeswithPort);
    }

    @Required
    public void setCallbackImpl(CallbackOnBusyImpl callbackImpl) {
        m_callbackImpl = callbackImpl;
    }
}
