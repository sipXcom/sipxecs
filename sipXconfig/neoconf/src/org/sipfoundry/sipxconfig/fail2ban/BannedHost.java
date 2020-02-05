/**
 * Copyright (c) 2020 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.fail2ban;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "bannedHosts")
public class BannedHost implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Id
    private String m_id;

    @Field("ipAddress")
    private String m_ipAddress;

    @Field("reason")
    private String m_reason;

    @Field("timeStamp")
    private String m_timeStamp;

    public String getId() {
        return m_id;
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

    public BanReason getReason() {
        return BanReason.valueOf(m_reason.toUpperCase());
    }

    public String getBanTime() {
        long ts = (long) (new Double(m_timeStamp).doubleValue() * 1000);

        return dateFormatter.format(new Date(ts));
    }

}
