/*
 *
 *
 * Copyright (C) 2015 sipXcom., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.admin.systemaudit;

import java.util.List;

import org.apache.tapestry.components.IPrimaryKeyConverter;
import org.sipfoundry.sipxconfig.systemaudit.ConfigChange;

public class ConfigChangeSqueezer implements IPrimaryKeyConverter {
    List<ConfigChange> m_page;
    public ConfigChangeSqueezer(List<ConfigChange> page) {
        m_page = page;
    }

    public Object getPrimaryKey(Object object) {
        ConfigChange vm = (ConfigChange) object;
        return vm.getPrimaryKey();
    }

    public Object getValue(Object key) {
        for (ConfigChange configChange : m_page) {
            if (configChange.getPrimaryKey().equals(key)) {
                return configChange;
            }
        }
        return null;
    }
}
