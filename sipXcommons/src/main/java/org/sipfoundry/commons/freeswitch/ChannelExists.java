/**
 *
 *
 * Copyright (c) 2015 eZuce Corp. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
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

public class ChannelExists extends CallCommand {

    public ChannelExists(FreeSwitchEventSocketInterface fses, String uuid) {
        super(fses);
        m_command = "uuid_exists " + uuid;
    }

    public boolean isUUIDActive() {
        m_finished = false;
        FreeSwitchEvent event = m_fses.apiCmdResponse(m_command);
        return event.getContent().contains("true");
    }

}