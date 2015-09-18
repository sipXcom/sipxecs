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
import org.sipfoundry.commons.freeswitch.OriginateCommand;
import org.sipfoundry.commons.freeswitch.Set;
import org.sipfoundry.sipxcallback.common.CallbackException;
import org.sipfoundry.sipxcallback.common.CallbackLegs;
import org.sipfoundry.sipxcallback.common.CallbackService;
import org.springframework.beans.factory.annotation.Required;

import com.hazelcast.core.IAtomicReference;

public class CallbackExecutor {

    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");
    private static final String ORIGINATE_RESPONSE_OK = "+OK ";
    private static final String ORIGINATE_PROPERTIES = "{ignore_early_media=true,originate_timeout=20,fail_on_single_reject=USER_BUSY,hangup_after_bridge=true,origination_caller_id_number=00000000}";
    public static final String AROUND = "@";
    public static final String SOFIA = "sofia/";

    private CallbackLegs m_callbackLegs;
    private String m_callerUID;
    private String m_calleeUID;
    private String m_callerName;
    private FreeSwitchEventSocketInterface m_fsCmdSocket;
    private CallbackService m_callbackService;
    private String sipxchangeDomainName;

    private String m_callerPrompt;
    private String m_requestedCallbackPrompt;

    public void initiate(CallbackLegs callbackLegs, FreeSwitchEventSocketInterface fsCmdSocket) {
        m_callbackLegs = callbackLegs;
        String callerName = callbackLegs.getCallerUID().replace(";", ".");
        String[] callerNameSplit = callerName.split(AROUND);
        m_callerName = callerNameSplit[0];
        m_callerUID = StringUtils.join(new String[] { SOFIA, sipxchangeDomainName, "/", callerName });
        m_calleeUID = StringUtils.join(new String[] { SOFIA, sipxchangeDomainName, "/", callbackLegs.getCalleeName(),
                AROUND, callerNameSplit[1] });
        m_callerUID = m_callerUID.split("/")[2];
        m_fsCmdSocket = fsCmdSocket;
    }

    /**
     * Returns true if the processing was successful.
     */
    public boolean execute(CallbackLegs callbackLegs, FreeSwitchEventSocketInterface fsCmdSocket) {
        initiate(callbackLegs, fsCmdSocket);
        LOG.debug("Originating call to " + m_calleeUID);
        // mark callee and caller as processing (so as not to receive other callbacks)
        IAtomicReference<Boolean> calleeReference = m_callbackService.getAtomicReference(m_callbackLegs.getCalleeName());
        calleeReference.set(new Boolean(true));
        IAtomicReference<Boolean> callerReference = m_callbackService.getAtomicReference(m_callbackLegs.getCallerName());
        callerReference.set(new Boolean(true));
        boolean callbackSuccessful = false;

        try {
            String originateProperties = ORIGINATE_PROPERTIES.replace("00000000", m_callerName);
            OriginateCommand originateCalleeCmd = new OriginateCommand(m_fsCmdSocket,
                    originateProperties + m_calleeUID);
            FreeSwitchEvent responseCallee = originateCalleeCmd.originate();
            String responseContent = responseCallee.getContent();
            if ((responseContent != null) && (responseContent.startsWith(ORIGINATE_RESPONSE_OK))) {
                LOG.debug(m_calleeUID + " answered the call");
                handleCalleeResponse(responseContent);
                callbackSuccessful = true;
            }
            return callbackSuccessful;
        } catch (Exception e) {
            LOG.error(e);
            return callbackSuccessful;
        } finally {
            // remove mark for callee and caller as beeing in use
            calleeReference.destroy();
            callerReference.destroy();
        }
    }

    /**
     *  Action to be taken after the B user answered the call:<br>
     *  - remove the callback flag from B user<br>
     *  - originate a call to A user<br>
     *  - if user A responds: bridge A and B<br>
     *  - if user A busy: goes to his voicemail
     */
    private void handleCalleeResponse(String responseContent) throws InterruptedException {
        try {
            // remove the callback flag from B user
            m_callbackService.updateCallbackInfoToMongo(m_callbackLegs, false);
        } catch (CallbackException e) {
            LOG.error(e);
            return;
        }
        LOG.debug("Originating call to " + m_callerUID);
        String calleeUUID = getUUIDFromResponseContent(responseContent);

        // play "Caller <callerName> requested a callback" to B users
        new Broadcast(m_fsCmdSocket, calleeUUID, m_callerPrompt, false).startResponse();
        new Broadcast(m_fsCmdSocket, calleeUUID, m_callerName, true).startResponse();
        new Broadcast(m_fsCmdSocket, calleeUUID, m_requestedCallbackPrompt, false).startResponse();
        Thread.sleep(CallbackTimer.THREAD_WAIT_TIME);

        // bridge B and A legs
        Set set = new Set(m_fsCmdSocket, calleeUUID, "ringback", "${us-ring}");
        set.start();
        BridgeCommand bridge = new BridgeCommand(m_fsCmdSocket, calleeUUID, m_callerUID, sipxchangeDomainName);
        bridge.start();
    }

    private String getUUIDFromResponseContent(String responseContent){
        return responseContent.split(" ")[1].replace("\n","");
    }

    @Required
    public void setCallbackService(CallbackService callbackService) {
        m_callbackService = callbackService;
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
    public void setSipxchangeDomainName(String sipxchangeDomainName) {
        this.sipxchangeDomainName = sipxchangeDomainName;
    }

}
