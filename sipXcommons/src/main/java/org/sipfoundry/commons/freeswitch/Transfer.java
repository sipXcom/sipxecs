/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.commons.freeswitch;

import org.sipfoundry.commons.userdb.ValidUsers;

public class Transfer extends CallCommand {

    private String m_uuid;

    public Transfer(FreeSwitchEventSocketInterface fses, String sipURI, boolean bridge) {
        super(fses);
        if (bridge) {
            createBridgeCommand(sipURI);
        } else {
            // Send a REFER
            createDeflectCommand(sipURI);
        }
    }

    public Transfer(FreeSwitchEventSocketInterface fses, String uuid, String sipURI, boolean bridge) {
        super(fses);
        m_uuid = uuid;
        if (bridge) {
            createBridgeCommand(sipURI);
        } else {
            createDeflectCommand(sipURI);
        }
    }

    private void createDeflectCommand(String sipURI) {
        m_command = "deflect\nexecute-app-arg: ";
        // sipURI MUST have sip: in there (Can be display URI)
        if (sipURI.toLowerCase().contains("sip:")) {
            m_command += sipURI;
        } else {
            m_command += "sip:" + sipURI;
        }
    }

    private void createBridgeCommand(String sipURI) {
        m_command = "transfer\nexecute-app-arg: transferBridged"+ ValidUsers.getUserPart(sipURI) + " XML default";
    }

    @Override
    public void go() {
        if(m_uuid == null) {
            super.go();
        } else {
            m_finished = false;
            // Send the command to the socket
            m_fses.cmd("sendmsg " + m_uuid +
                    "\ncall-command: execute\nexecute-app-name: " + m_command);
        }
    }
}
