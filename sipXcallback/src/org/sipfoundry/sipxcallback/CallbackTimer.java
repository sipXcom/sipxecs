/**
 *
 *
 * Copyright (c) 2015 sipXcom, Inc. All rights reserved.
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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.freeswitch.ConfBasicThread;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocket;
import org.sipfoundry.sipxcallback.common.CallbackLegs;
import org.sipfoundry.sipxcallback.common.CallbackService;
import org.sipfoundry.sipxcallback.common.CallbackServiceImpl;
import org.sipfoundry.sipxcallback.common.FreeSwitchConfigurationImpl;
import org.springframework.beans.factory.annotation.Required;

/**
 *  Daemon task class that handles callback requests registered in the system
 */
public class CallbackTimer {
    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");
    public static final long THREAD_WAIT_TIME = 4000;

    private CallbackService m_callbackService;
    private CallbackExecutor m_callbackExecutor;
    private int m_expires;

    private FreeSwitchConfigurationImpl fsConfig;
    private FreeSwitchEventSocket m_fsCmdSocket;

    public void run() {
        initiateSocket();
        // make sure callback_queue is initiated
        m_callbackService.initiateCallbackQueue();

        Queue<CallbackLegs> hazelcastQueue = m_callbackService.getCallbackQueue();
        LOG.debug("Retrieving data from callback request queue.");
        Set<CallbackLegs> failedCallbackLegs = new HashSet<CallbackLegs>();
        while (!hazelcastQueue.isEmpty()) {
            CallbackLegs callbackLegs = hazelcastQueue.poll();
            boolean processingFailed = true;
            try {
                // process this request ONLY if this callee is not currently being processed by another callback thread
                if (m_callbackService.isCallbackLegsFreeToProcess(callbackLegs)) {
                    LOG.debug("Processing callback request from " + callbackLegs.getCallerName() +
                            " to " + callbackLegs.getCalleeName());
                    long currentDate = CallbackServiceImpl.getCurrentTimestamp();
                    long timeDiff = currentDate - callbackLegs.getDate();
                    if (timeDiff < m_expires * 60000) {
                        processingFailed = !m_callbackExecutor.execute(callbackLegs, m_fsCmdSocket);
                        Thread.sleep(THREAD_WAIT_TIME);
                    } else {
                        //callback request expired, remove it from mongo
                        LOG.debug("Callback request from " + callbackLegs.getCallerName() +
                                " to " + callbackLegs.getCalleeName() + " expired.");
                        m_callbackService.updateCallbackInfoToMongo(callbackLegs, false);
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            } finally {
                if (processingFailed) {
                    failedCallbackLegs.add(callbackLegs);
                }
            }
        }
        hazelcastQueue.addAll(failedCallbackLegs);
    }

    private void initiateSocket() {
        if (m_fsCmdSocket == null) {
            try {
                if (m_fsCmdSocket == null) {
                    fsConfig = new FreeSwitchConfigurationImpl();
                }
                m_fsCmdSocket = new FreeSwitchEventSocket(fsConfig);
                m_fsCmdSocket.connect(getSocket(), ConfBasicThread.fsPassword);
            } catch (IOException e) {
                LOG.error(e);
                return;
            }
        }
    }

    private Socket getSocket() {
        Socket socket = null;
        while(socket == null) {
            // freeswitch may be slow to start especially on a different machine
            try {
                socket = new Socket("localhost", Integer.parseInt(ConfBasicThread.fsListenPort));
            } catch (UnknownHostException e) {
                LOG.error("Can't create connection to freeswitch " + e.getMessage());
            } catch (IOException e) {
                // freeswitch likely is not up yet
                try {
                    Thread.sleep(THREAD_WAIT_TIME);
                } catch (InterruptedException e1) {
                    LOG.error(e1);
                }
            }
        }
        return socket;
    }

    @Required
    public void setCallbackService(CallbackService callbackService) {
        m_callbackService = callbackService;
    }

    @Required
    public void setCallbackExecutor(CallbackExecutor callbackExecutor) {
        m_callbackExecutor = callbackExecutor;
    }

    @Required
    public void setExpires(int expires) {
        m_expires = expires;
    }

}
