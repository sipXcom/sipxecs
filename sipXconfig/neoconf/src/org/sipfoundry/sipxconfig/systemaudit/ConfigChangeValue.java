/**
 *
 * Copyright (c) 2013 Karel Electronics Corp. All rights reserved.
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
 *
 */

package org.sipfoundry.sipxconfig.systemaudit;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;

public class ConfigChangeValue implements Serializable {

    public static final List<String> EXCEPTED_PROPERTIES = Arrays.asList(
            "pintoken", "sip_password", "voicemailPintoken");

    @Expose
    private String m_propertyName;
    @Expose
    private String m_valueBefore;
    @Expose
    private String m_valueAfter;

    public ConfigChangeValue() {
    }

    public String getValueBefore() {
        return m_valueBefore;
    }

    public void setValueBefore(String valueBefore) {
        if (isExcepted()) {
            return;
        }
        this.m_valueBefore = valueBefore;
    }

    public String getValueAfter() {
        return m_valueAfter;
    }

    public void setValueAfter(String valueAfter) {
        if (isExcepted()) {
            return;
        }
        this.m_valueAfter = valueAfter;
    }

    public String getPropertyName() {
        return m_propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.m_propertyName = propertyName;
    }

    /**
     * Decide if we need to store the valueBefore and valueAfter. For instance we
     * need to keep track of password changes, but not the actual password
     * values.
     */
    private boolean isExcepted() {
        if (getPropertyName() != null
                && ConfigChangeValue.EXCEPTED_PROPERTIES
                        .contains(getPropertyName())) {
            return true;
        }
        return false;
    }
}
