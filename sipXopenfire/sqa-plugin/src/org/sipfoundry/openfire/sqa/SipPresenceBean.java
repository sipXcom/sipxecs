/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
 */
package org.sipfoundry.openfire.sqa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sipfoundry.openfire.sqa.SipEventBean.DialogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SipPresenceBean {
    private String m_statusMessage;
    private String m_callingPartiId;
    private static final Logger logger = LoggerFactory.getLogger(SipPresenceBean.class);
    private Map<String, DialogState> m_dialogStates = new HashMap<String, DialogState>();

    public SipPresenceBean(String statusMessage, String callingPartiId) {
        m_statusMessage = statusMessage;
        m_callingPartiId = callingPartiId;
    }

    public String getStatusMessage() {
        return m_statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        m_statusMessage = statusMessage;
    }

    public String getCallingPartiId() {
        return m_callingPartiId;
    }

    public void setDialogState(String dialogId, DialogState dialogState) {
        logger.debug("Set state " + dialogState.getName() + " for dialogId: " + dialogId);
        //save new/existing dialog state or remove dialog if terminated
        if (dialogState == DialogState.terminated) {
            m_dialogStates.remove(dialogId);
        } else {
            m_dialogStates.put(dialogId, dialogState);
        }
    }

    public void removeDialogState(String dialogId) {
        m_dialogStates.remove(dialogId);
    }

    public boolean isConfirmed(String dialogId) {
        return m_dialogStates.get(dialogId) == DialogState.confirmed;
    }

    public Map<String, DialogState> getDialogStates() {
        return m_dialogStates;
    }

    public boolean isConfirmed() {
        Collection<DialogState> states = m_dialogStates.values();
        for (DialogState state : states) {
            if (state == DialogState.confirmed) {
                return true;
            }
        }
        return false;
    }
}
