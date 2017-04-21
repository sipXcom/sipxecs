/*
 *
 *
 * Copyright (C) 2016 eZuce, Inc. certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.freeswitch;


public interface FreeswitchRecordingContext {
    public FreeswitchRecordingSettings getSettings();

    public void saveSettings(FreeswitchRecordingSettings settings);
}
