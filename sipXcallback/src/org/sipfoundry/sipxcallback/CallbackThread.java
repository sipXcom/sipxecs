/**
 *
 *
 * Copyright (c) 2015 sipXcom inc, Inc. All rights reserved.
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
package org.sipfoundry.sipxcallback;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sipfoundry.commons.freeswitch.BridgeCommand;
import org.sipfoundry.commons.freeswitch.Broadcast;
import org.sipfoundry.commons.freeswitch.FreeSwitchEvent;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocketInterface;
import org.sipfoundry.commons.freeswitch.Hangup;
import org.sipfoundry.commons.freeswitch.OriginateCommand;
import org.sipfoundry.sipxcallback.common.CallbackException;
import org.sipfoundry.sipxcallback.common.CallbackUtil;
import org.springframework.beans.factory.annotation.Required;

public class CallbackThread extends Thread {

    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");
    private static final String ORIGINATE_RESPONSE_OK = "+OK ";
    private static final String ORIGINATE_PROPERTIES = "{ignore_early_media=true,originate_timeout=20,fail_on_single_reject=USER_BUSY,hangup_after_bridge=true}";
    public static final String BEAN_NAME = "callbackThread";

    private String m_callerUID;
    private String m_calleeUID;
    private String m_calleeName;
    private String m_callerName;
    private FreeSwitchEventSocketInterface m_fsCmdSocket;
    private CallbackUtil m_callbackUtil;
    private String sipxchangeDomainName;

    private String m_callerPrompt;
    private String m_requestedCallbackPrompt;
    private String m_butTheyAreBusyPrompt;
    private String m_isNotAnsweringPrompt;

    public void initiate(String calleeName, String callerName, FreeSwitchEventSocketInterface fsCmdSocket) {
        this.m_calleeName = calleeName;
        callerName = callerName.replace(";", ".");
        this.m_callerName = callerName.split("@")[0];
        this.m_callerUID = StringUtils.join(new String[] { "sofia/", sipxchangeDomainName, "/", callerName });
        this.m_calleeUID = m_callerUID.replace(m_callerName, m_calleeName);
        this.m_fsCmdSocket = fsCmdSocket;
    }

    @Override
    public void run() {
        LOG.debug("Originating call to " + m_calleeUID);
        OriginateCommand originateCalleeCmd = new OriginateCommand(m_fsCmdSocket,
                ORIGINATE_PROPERTIES + m_calleeUID);
        FreeSwitchEvent responseCallee = originateCalleeCmd.originate();
        String responseContent = responseCallee.getContent();
        if ((responseContent != null) && (responseContent.startsWith(ORIGINATE_RESPONSE_OK))) {
            LOG.debug(m_calleeUID + " answered the call");
            try {
                handleCalleeResponse(responseContent);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
    }

    /**
     *  Action to be taken after the B user answered the call:<br>
     *  - remove the callback flag from B user<br>
     *  - originate a call to A user<br>
     *  - if user A responds: bridge A and B<br>
     *  - if user A busy: say "user A called you but he is busy"<br>
     *  - if user A does not answer: say "user A called you but he does not answer"<br>
     * @throws InterruptedException 
     */
    private void handleCalleeResponse(String responseContent) throws InterruptedException {
        // remove the callback flag from B user
        String callerURL = m_callerUID.split("/")[2];
        try {
            m_callbackUtil.updateCallbackInformation(m_calleeName, callerURL, false);
        } catch (CallbackException e) {
            LOG.error(e);
            return;
        }
        LOG.debug("Originating call to " + m_callerUID);
        String calleeUUID = getUUIDFromResponseContent(responseContent);

        // play "Caller <callerName> requested a callback" to B users
        new Broadcast(m_fsCmdSocket, calleeUUID, m_callerPrompt, false).startResponse();
        new Broadcast(m_fsCmdSocket, calleeUUID, m_calleeName, true).startResponse();
        new Broadcast(m_fsCmdSocket, calleeUUID, m_requestedCallbackPrompt, false).startResponse();
        Thread.sleep(4000);

        // originate a call to A user
        OriginateCommand originateCallerCmd = new OriginateCommand(m_fsCmdSocket,
                ORIGINATE_PROPERTIES + m_callerUID);
        FreeSwitchEvent responseCaller = originateCallerCmd.originate();
        String responseCallerContent = responseCaller.getContent();
        if (responseCallerContent.startsWith(ORIGINATE_RESPONSE_OK)) {
            handleCallbackSuccess(responseCallerContent, calleeUUID);
        } else if (responseCallerContent.contains("USER_BUSY")) {
            handleCallerAnswer(calleeUUID, m_butTheyAreBusyPrompt);
        } else {
            handleCallerAnswer(calleeUUID, m_isNotAnsweringPrompt);
        }
    }

    /**
     * A user responded: bridge B and A calls
     */
    private void handleCallbackSuccess(String responseCallerContent, String calleeUUID) {
        LOG.debug(m_callerUID + " answered the call, bridging this call with " + m_calleeUID);
        String callerUUID = getUUIDFromResponseContent(responseCallerContent);
        BridgeCommand bridge = new BridgeCommand(m_fsCmdSocket, calleeUUID, callerUUID);
        bridge.startResponse();
    }

    /**
     * if A user did not answer, play prompt to B user and hangup 
     */
    private void handleCallerAnswer(String calleeUUID, String promptName) throws InterruptedException {
        new Broadcast(m_fsCmdSocket, calleeUUID, promptName, false).startResponse();
        Thread.sleep(3000);
        new Hangup(m_fsCmdSocket, calleeUUID).startResponse();
    }

    private String getUUIDFromResponseContent(String responseContent){
        return responseContent.split(" ")[1].replace("\n","");
    }

    @Required
    public void setCallbackUtil(CallbackUtil callbackUtil) {
        m_callbackUtil = callbackUtil;
    }

    @Required
    public void setCallerPrompt(String callerPrompt) {
        this.m_callerPrompt = callerPrompt;
    }

    @Required
    public void setRequestedCallbackPrompt(String requestedCallbackPrompt) {
        this.m_requestedCallbackPrompt = requestedCallbackPrompt;
    }

    @Required
    public void setButTheyAreBusyPrompt(String butTheyAreBusyPrompt) {
        this.m_butTheyAreBusyPrompt = butTheyAreBusyPrompt;
    }

    @Required
    public void setIsNotAnsweringPrompt(String isNotAnsweringPrompt) {
        this.m_isNotAnsweringPrompt = isNotAnsweringPrompt;
    }

    @Required
    public void setSipxchangeDomainName(String sipxchangeDomainName) {
        this.sipxchangeDomainName = sipxchangeDomainName;
    }
}
