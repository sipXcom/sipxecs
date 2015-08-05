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
import java.util.Map;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.freeswitch.ConfBasicThread;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocket;
import org.sipfoundry.sipxcallback.common.CallbackService;
import org.sipfoundry.sipxcallback.common.FreeSwitchConfigurationImpl;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 *  Daemon task class that handles callback requests registered in the system
 */
public class CallbackTimer {
    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");

    private CallbackService m_callbackService;
    private ThreadPoolTaskExecutor m_taskExecutor;
    private CallbackThread m_callbackThread;

    private FreeSwitchConfigurationImpl fsConfig;
    private FreeSwitchEventSocket m_fsCmdSocket;

    public void run() throws ParseException {
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

        Map<String, String> calls = m_callbackService.runCallbackTimer();
        // create a callback thread for each element from the map
        for (String callCalleeName : calls.keySet()) {
            executeCallbackThread(callCalleeName, calls.get(callCalleeName), m_fsCmdSocket);
        }
    }

    private void executeCallbackThread(String callerName, String calleeName,
            FreeSwitchEventSocket m_fsCmdSocket) {
        try {
            if (m_taskExecutor.getThreadPoolExecutor().getQueue().isEmpty()) {
                m_callbackThread.initiate(callerName, calleeName, m_fsCmdSocket);
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
                    Thread.sleep(4000);
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
    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        m_taskExecutor = taskExecutor;
    }

    @Required
    public void setCallbackThread(CallbackThread callbackThread) {
        m_callbackThread = callbackThread;
    }
}
