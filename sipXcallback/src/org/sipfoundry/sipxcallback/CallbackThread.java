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
import org.sipfoundry.commons.freeswitch.FreeSwitchEvent;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocketInterface;
import org.sipfoundry.commons.freeswitch.OriginateCommand;
import org.sipfoundry.sipxcallback.common.CallbackUtil;
import org.springframework.data.mongodb.core.MongoTemplate;

public class CallbackThread extends Thread {

    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");
    private static final String ORIGINATE_RESPONSE_OK = "+OK ";

    private String m_callerUID;
    private String m_calleeUID;
    private String m_calleeName;
    private String m_callerName;
    private FreeSwitchEventSocketInterface m_fsCmdSocket;
    private MongoTemplate m_imdbTemplate;

    public CallbackThread(MongoTemplate imdbTemplate, String callerName, String calleeName,
            FreeSwitchEventSocketInterface fsCmdSocket, String sipxchangeDomainName) {
        super();
        this.m_imdbTemplate = imdbTemplate;
        this.m_calleeName = calleeName;
        callerName = callerName.replace(";", ".");
        this.m_callerName = callerName.split("@")[0];
        this.m_callerUID = StringUtils.join(new String[] { "sofia/",
                sipxchangeDomainName, "/", callerName });
        this.m_calleeUID = m_callerUID.replace(m_callerName, m_calleeName);
        this.m_fsCmdSocket = fsCmdSocket;
    }

    @Override
    public void run() {
        LOG.debug("Originating call to " + m_calleeUID);
        OriginateCommand originateCalleeCmd = new OriginateCommand(m_fsCmdSocket,
                "{ignore_early_media=true,fail_on_single_reject=USER_BUSY}" + m_calleeUID);
        FreeSwitchEvent responseCallee = originateCalleeCmd.originate();
        String responseContent = responseCallee.getContent();
        if (responseContent.startsWith(ORIGINATE_RESPONSE_OK)) {
            LOG.debug(m_calleeUID + " answered the call");
            handleCalleeResponse(responseContent);
        }
    }
    
    /**
     *  Action to be taken after the B user answered the call:<br>
     *  - remove the callback flag from B user<br>
     *  - originate a call to A user<br>
     *  - if user A responds: bridge A and B<br>
     *  - if user A busy: say "user A called you but he is busy"<br>
     *  - if user A does not answer: say "user A called you but he does not answer"<br>
     */
    private void handleCalleeResponse(String responseContent) {
        // remove the callback flag from B user
        String callerURL = m_callerUID.split("/")[2];
        CallbackUtil.updateCallbackInformation(m_imdbTemplate, m_calleeName, callerURL, false);
        // originate a call to A user
        LOG.debug("Originating call to " + m_callerUID);
        String calleeUUID = getUUIDFromResponseContent(responseContent);
        OriginateCommand originateCallerCmd = new OriginateCommand(m_fsCmdSocket,
                "{ignore_early_media=true,fail_on_single_reject=USER_BUSY}" + m_callerUID);
        FreeSwitchEvent responseCaller = originateCallerCmd.originate();
        String responseCallerContent = responseCaller.getContent();
        if (responseCallerContent.startsWith(ORIGINATE_RESPONSE_OK)) {
            // A user responded: bridge B and A calls
            LOG.debug(m_callerUID + " answered the call, bridging this call with " + m_calleeUID);
            String callerUUID = getUUIDFromResponseContent(responseCallerContent);
            BridgeCommand bridge = new BridgeCommand(m_fsCmdSocket, calleeUUID, callerUUID);
            bridge.start();
        } else if (responseCallerContent.contains("USER_BUSY")) {
            LOG.debug(m_callerUID + " is busy");
            // A user busy
        } else {
            LOG.debug(m_callerUID + " didn't answer the call");
            // A user didn't respond
        }
    }

    private String getUUIDFromResponseContent(String responseContent){
        return responseContent.split(" ")[1].replace("\n","");
    }
}
