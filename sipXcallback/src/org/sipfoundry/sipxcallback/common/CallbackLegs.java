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
    private String m_callerName;
    private boolean m_processing;

    public CallbackLegs(String calleeName, String callerName) {
        super();
        this.m_calleeName = calleeName;
        this.m_callerName = callerName;
    }

    public String getCalleeName() {
        return m_calleeName;
    }

    public void setCalleeName(String calleeName) {
        this.m_calleeName = calleeName;
    }

    public String getCallerName() {
        return m_callerName;
    }

    public void setCallerName(String callerName) {
        this.m_callerName = callerName;
    }

    public boolean isProcessing() {
        return m_processing;
    }

    public void setProcessing(boolean processing) {
        this.m_processing = processing;
    }

}
