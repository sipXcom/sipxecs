/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.zoiper.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.phone.Line;

public class ZoiperProfileContext extends ProfileContext<ZoiperPhone> {

    public ZoiperProfileContext(ZoiperPhone device, String profileTemplate) {
        super(device, profileTemplate);
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = super.getContext();
        mapDataInContext(context);
        return context;
    }

    public void mapDataInContext(Map<String, Object> context) {
        ZoiperPhone phone = getDevice();
        context.put("phone", phone);
        List<Line> lines = phone.getLines();

        HashMap<String, Line> linesSettings = new HashMap<String, Line>();

        for (Line line : lines) {
            linesSettings.put(line.getUserName(), line);
        }
        context.put("lines", linesSettings);
    }

}