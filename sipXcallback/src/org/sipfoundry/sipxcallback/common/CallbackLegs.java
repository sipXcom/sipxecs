/**
 *
 *
 * Copyright (c) 2015 eZuce Corp. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
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
package org.sipfoundry.sipxcallback.common;

import java.io.Serializable;

/**
 * Bean to hold callee and caller information.
 */
public class CallbackLegs implements Serializable{

    private String m_calleeName;
    private String m_callerUID;
    private long m_date;

    public CallbackLegs(String calleeName, String callerName, long date) {
        super();
        this.m_calleeName = calleeName;
        this.m_callerUID = callerName;
        this.m_date = date;
    }

    public String getCalleeName() {
        return m_calleeName;
    }

    public void setCalleeName(String calleeName) {
        this.m_calleeName = calleeName;
    }

    public String getCallerUID() {
        return m_callerUID;
    }

    public void setCallerUID(String callerUID) {
        this.m_callerUID = callerUID;
    }

    public String getCallerName() {
        return m_callerUID.split("@")[0];
    }

    public long getDate() {
        return m_date;
    }

    public void setDate(long date) {
        this.m_date = date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_calleeName == null) ? 0 : m_calleeName.hashCode());
        result = prime * result
                + ((m_callerUID == null) ? 0 : m_callerUID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CallbackLegs other = (CallbackLegs) obj;
        if (m_calleeName == null) {
            if (other.m_calleeName != null)
                return false;
        } else if (!m_calleeName.equals(other.m_calleeName))
            return false;
        if (m_callerUID == null) {
            if (other.m_callerUID != null)
                return false;
        } else if (!m_callerUID.equals(other.m_callerUID))
            return false;
        return true;
    }

}
