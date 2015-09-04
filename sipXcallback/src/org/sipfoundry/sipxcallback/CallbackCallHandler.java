/*
 *
 *
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxcallback;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.freeswitch.Answer;
import org.sipfoundry.commons.freeswitch.Broadcast;
import org.sipfoundry.commons.freeswitch.DisconnectException;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocketInterface;
import org.sipfoundry.commons.freeswitch.Hangup;
import org.sipfoundry.commons.freeswitch.eslrequest.EslRequestScopeRunnable;
import org.sipfoundry.sipxcallback.common.CallbackException;
import org.sipfoundry.sipxcallback.common.CallbackLegs;
import org.sipfoundry.sipxcallback.common.CallbackService;
import org.springframework.beans.factory.annotation.Required;

/**
 *  Class used to register callback requests in the system
 */
public abstract class CallbackCallHandler extends EslRequestScopeRunnable {
    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");

    private Socket m_clientSocket;
    private String m_prefix;
    private CallbackService m_callbackService;
    private String m_welcomePrompt;
    private String m_errorPrompt;

    protected abstract FreeSwitchEventSocketInterface getFsEventSocket();

    @Override
    public void runEslRequest() {
        FreeSwitchEventSocketInterface fses = getFsEventSocket();
        try {
            if (fses.connect(m_clientSocket, null)) {
                LOG.info(String.format(
                        "SipXcallback::run Accepting call-id %s from %s to %s",
                        fses.getVariable("variable_sip_call_id"),
                        fses.getVariable("variable_sip_from_uri"),
                        fses.getVariable("variable_sip_req_uri")));
                new Answer(fses).go();
                run(fses);
            }
        } catch (DisconnectException e) {
            LOG.info("sipXcallback::run Far end hungup.");
        } catch (Throwable t) {
            LOG.error("sipXcallback::run", t);
        } finally {
            try {
                new Hangup(fses).go();
                fses.close();
            } catch (IOException e) {
                // Nothing to do, no where to go home...
            }
        }
    }

    /**
     * Marks the callee user for callback on busy
     */
    public final void run(FreeSwitchEventSocketInterface fses) {
        HashMap<String, String> variables = fses.getVariables();
        String callerUserName = variables.get("caller-username");
        String callerChannelName = variables.get("caller-channel-name");
        String callerURL = callerChannelName.split("/")[2].replace(".", ";");
        String toUri = fses.getToUri();
        toUri = toUri.replace(m_prefix, "");
        String[] splittedToUri = toUri.split("@");
        String calleeUserName = splittedToUri[0];
        if (calleeUserName == null || calleeUserName.isEmpty()
                || calleeUserName.equals(callerUserName)) {
            // callback user not found
            LOG.warn("Callback user " + calleeUserName + " was not found.");
            new Broadcast(fses, fses.getVariable("variable_sip_call_id"), m_errorPrompt, false).startResponse();
            return;
        }
        try {
            CallbackLegs callbackLegs = new CallbackLegs(calleeUserName,callerURL, new Date().getTime());
            m_callbackService.updateCallbackInfoToMongo(callbackLegs, true);
        } catch (CallbackException e) {
            // callback user not found
            LOG.warn("Callback user " + calleeUserName + " was not found.");
            new Broadcast(fses, fses.getVariable("variable_sip_call_id"), m_errorPrompt, false).startResponse();
            return;
        }
        new Broadcast(fses, fses.getVariable("variable_sip_call_id"), m_welcomePrompt, false).startResponse();
    }

    @Required
    public void setClient(Socket clientSocket) {
        m_clientSocket = clientSocket;
    }

    @Required
    public void setPrefix(String prefix) {
        m_prefix = prefix;
    }

    @Required
    public void setCallbackService(CallbackService callbackService) {
        m_callbackService = callbackService;
    }

    @Required
    public void setWelcomePrompt(String welcomePrompt) {
        this.m_welcomePrompt = welcomePrompt;
    }

    @Required
    public void setErrorPrompt(String errorPrompt) {
        this.m_errorPrompt = errorPrompt;
    }
}
