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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchRecordingContext;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchRecordingSettings;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchSettings;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class ModShoutConfigurationTest {
    private ModShoutConfiguration m_configuration;
    
    @Before
    public void setUp() {
        m_configuration = new ModShoutConfiguration();  
        m_configuration.setFsRecordingContext(new MockFsRecordingContext());
        m_configuration.setVelocityEngine(TestHelper.getVelocityEngine());
    }
    
    @Test
    public void config() throws IOException {
        StringWriter actual = new StringWriter();
        FreeswitchSettings settings = new FreeswitchSettings();
        settings.setModelFilesContext(TestHelper.getModelFilesContext());
        Location location = new Location();
        m_configuration.write(actual, location, settings);
        String expected = IOUtils.toString(getClass().getResourceAsStream("shout.conf.test.xml"));
        assertEquals(expected, actual.toString());        
    }

    private static class MockFsRecordingContext implements FreeswitchRecordingContext {
        private FreeswitchRecordingSettings m_settings;

        public MockFsRecordingContext() {
            m_settings = new FreeswitchRecordingSettings();
            m_settings.setModelFilesContext(TestHelper.getModelFilesContext());
        }

        @Override
        public FreeswitchRecordingSettings getSettings() {
            return m_settings;
        }

        @Override
        public void saveSettings(FreeswitchRecordingSettings settings) {
        }
        
    }
}
