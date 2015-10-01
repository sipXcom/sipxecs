/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.sipxconfig.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.phone.Line;

public class JitsiProfileContext extends ProfileContext<JitsiPhone> {

    public JitsiProfileContext(JitsiPhone device, String profileTemplate) {
        super(device, profileTemplate);
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = super.getContext();
        mapDataInContext(context);
        return context;
    }

    public void mapDataInContext(Map<String, Object> context) {
        JitsiPhone phone = getDevice();
        context.put("phone", phone);
        List<Line> lines = phone.getLines();

        HashMap<Long, Line> linesSettings = new HashMap<Long, Line>();
        //this is how account id is computed in Jitsi software to ensure uniquenes
        //here we generate multiple lines for an user and we make sure to ensure uniquenes
        //based on the same pattern
        long currentTime = System.currentTimeMillis();
        for (Line line : lines) {
            linesSettings.put(currentTime+=2, line);
        }
        context.put("lines", linesSettings);
    }

}
