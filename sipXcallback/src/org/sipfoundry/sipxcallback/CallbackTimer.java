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
import java.text.ParseException;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.freeswitch.ConfBasicThread;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocket;
import org.sipfoundry.sipxcallback.common.CallbackException;
import org.sipfoundry.sipxcallback.common.CallbackLegs;
import org.sipfoundry.sipxcallback.common.CallbackService;
import org.sipfoundry.sipxcallback.common.CallbackServiceImpl;
import org.sipfoundry.sipxcallback.common.FreeSwitchConfigurationImpl;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.hazelcast.core.IAtomicReference;

/**
 *  Daemon task class that handles callback requests registered in the system
 */
public class CallbackTimer {
    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");
    public static final long THREAD_WAIT_TIME = 4000;

    private CallbackService m_callbackService;
    private ThreadPoolTaskExecutor m_taskExecutor;
    private CallbackThread m_callbackThread;
    private int m_expires;

    private FreeSwitchConfigurationImpl fsConfig;
    private FreeSwitchEventSocket m_fsCmdSocket;

    public void run() throws ParseException, InterruptedException, CallbackException {
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

        // make sure callback_queue is initiated
        m_callbackService.initiateCallbackQueue();

        Queue<CallbackLegs> hazelcastQueue = m_callbackService.getCallbackQueue();
        LOG.debug("Retrieving data from hazelcast queue");
        boolean queueIsEmpty = hazelcastQueue.isEmpty();
        while (!queueIsEmpty) {
            CallbackLegs callbackLegs = hazelcastQueue.poll();
            queueIsEmpty = hazelcastQueue.isEmpty();
            IAtomicReference<Boolean> reference = m_callbackService.getAtomicReference(callbackLegs.getCalleeName());
            Boolean calleeIsProcessing = reference.get();
            // process this request ONLY if this callee is not currently beeing processed by another callback thread
            if (calleeIsProcessing == null || calleeIsProcessing.equals(false)) {
                long currentDate = CallbackServiceImpl.getCurrentTimestamp();
                long timeDiff = currentDate - callbackLegs.getDate();
                if (timeDiff < m_expires * 60000) {
                    executeCallbackThread(callbackLegs);
                    Thread.sleep(THREAD_WAIT_TIME);
                } else {
                    //callback request expired, remove it from mongo
                    m_callbackService.updateCallbackInfoToMongo(callbackLegs, false);
                }
            } else {
                hazelcastQueue.add(callbackLegs);
            }
        }
    }

    private void executeCallbackThread(CallbackLegs callbackLegs) {
        try {
            if (m_taskExecutor.getThreadPoolExecutor().getQueue().isEmpty()) {
                m_callbackThread.initiate(callbackLegs, m_fsCmdSocket);
                m_taskExecutor.execute(m_callbackThread);
            } else {
                LOG.debug("Wait for queue to become empty and look "+
                 "for more users that need to receive callbacks") ;
            }
        } catch (Exception ex) {
            LOG.error("Error during callback execution: ", ex);
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
    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        m_taskExecutor = taskExecutor;
    }

    @Required
    public void setCallbackService(CallbackService callbackService) {
        m_callbackService = callbackService;
    }

    @Required
    public void setCallbackThread(CallbackThread callbackThread) {
        m_callbackThread = callbackThread;
    }

    @Required
    public void setExpires(int expires) {
        m_expires = expires;
    }

}
