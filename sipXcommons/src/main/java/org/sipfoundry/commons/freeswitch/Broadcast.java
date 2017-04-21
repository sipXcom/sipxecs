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

public class Broadcast extends CallCommand {

    private String m_uuid;

    public Broadcast(FreeSwitchEventSocketInterface fses, String uuid, String params, boolean isSay) {
        super(fses);
        m_uuid = uuid;
        m_sendAsApi = true;
        if (!isSay) {
            m_command = "uuid_broadcast " + m_uuid + " " + params + " aleg";
        } else {
            m_command = "uuid_broadcast " + m_uuid + " say::en\\stelephone_number\\siterated\\s" + params + " aleg";
        }
    }

}
