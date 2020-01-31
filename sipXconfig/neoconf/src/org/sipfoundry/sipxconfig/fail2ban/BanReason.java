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

enum BanReason {
    DOS,
    ACK,
    INVITE,
    OPTIONS,
    REGISTER,
    SUBSCRIBE;

    @Override
    public String toString() {
        switch (this) {
        case DOS:
            return "DoS";
        case ACK:
            return "Ack";
        case INVITE:
            return "Invite";
        case OPTIONS:
            return "Options";
        case REGISTER:
            return "Register";
        case SUBSCRIBE:
            return "Subscribe";
        default:
            return "";
        }
    }
}
