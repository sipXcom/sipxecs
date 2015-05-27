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
package org.sipfoundry.commons.freeswitch;

public class OriginateCommand extends CallCommand {

    public OriginateCommand(FreeSwitchEventSocketInterface fses, String calledURI) {
        super(fses);
        m_command = "originate " + calledURI + ";sipx-noroute=VoiceMail;sipx-userforward=false &park";
    }

    public FreeSwitchEvent originate() {
        m_finished = false;
        return m_fses.apiCmdResponse(m_command);
    }

}
