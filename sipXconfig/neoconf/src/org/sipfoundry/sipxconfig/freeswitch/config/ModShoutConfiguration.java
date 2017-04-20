/*
 *
 *
 * Copyright (C) 2016 eZuce, Inc. certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.freeswitch.config;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.VelocityContext;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchRecordingContext;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchRecordingSettings;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchSettings;
import org.springframework.beans.factory.annotation.Required;

public class ModShoutConfiguration extends AbstractFreeswitchConfiguration {

    private FreeswitchRecordingContext m_fsRecording;

    @Override
    protected String getTemplate() {
        return "freeswitch/shout.conf.xml.vm";
    }

    @Override
    protected String getFileName() {
        return "autoload_configs/shout.conf.xml";
    }

    @Override
    public void write(Writer writer, Location location, FreeswitchSettings settings) throws IOException {
        VelocityContext context = new VelocityContext();
        FreeswitchRecordingSettings recSettings = m_fsRecording.getSettings();
        context.put("brate", recSettings.getBitRate());
        context.put("resample", recSettings.getResample());
        context.put("quality", recSettings.getQuality());
        write(writer, context);
    }

    @Required
    public void setFsRecordingContext(FreeswitchRecordingContext context) {
        m_fsRecording = context;
    }
}
