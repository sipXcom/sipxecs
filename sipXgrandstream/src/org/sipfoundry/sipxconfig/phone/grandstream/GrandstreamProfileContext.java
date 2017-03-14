/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.phone.grandstream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.setting.Setting;

public class GrandstreamProfileContext extends ProfileContext {
    private GrandstreamProfileWriter m_writer;
    private GrandstreamPhone phone;

    public GrandstreamProfileContext(GrandstreamPhone device) {
        super(device, device.getGsModel().getProfileTemplate());
        phone = device;
    }
    public GrandstreamProfileContext(GrandstreamPhone device, boolean isTextFormatEnabled) {
        
       super(device, null);
        if (isTextFormatEnabled) {
            m_writer = new GrandstreamProfileWriter(device);
        } else {
            m_writer = new GrandstreamBinaryProfileWriter(device);
        }
    }

    public GrandstreamProfileWriter getWriter() {
        return m_writer;
    }
 
    boolean isCompositeProfileName(String name) {
        return name.indexOf('-') >= 0;
    }

    boolean isCompositeIpAddress(String name) {
        return name.indexOf(',') >= 0;
    }
    public Collection<Line> getLines() {
        int lineCount = phone.getModel().getMaxLineCount();
        Collection<Line> lines = new ArrayList(lineCount);
        if (phone.getLines().isEmpty()) {
            Line line = phone.createSpecialPhoneProvisionUserLine();
            line.setSettingValue("port/P270-P417-P517-P617-P1717-P1817", line.getUser().getDisplayName());
            lines.add(line);
        } else {
            lines.addAll(phone.getLines());
            // copy in blank lines of all unused lines
            for (int i = lines.size(); i < lineCount; i++) {
                Line line = phone.createLine();
                line.setSettingValue("port/P270-P417-P517-P617-P1717-P1817", " ");
                line.setSettingValue("port/P3-P407-P507-P607-P1707-P1807", " ");
                line.setSettingValue("port/P35-P404-P504-P604-P1704-P1804", " ");
                line.setSettingValue("port/P36-P405-P505-P605-P1705-P1805", " ");
                line.setSettingValue("port/P34-P406-P506-P606-P1706-P1806", " ");
                lines.add(line);
            }
        }

        return lines;
    }
}
