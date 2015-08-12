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
package org.sipfoundry.sipxcallback.common;

import java.util.Set;

public interface CallbackService {

    public void updateCallbackInformation(String calleeUserName,
            String callerChannelName, boolean insertNewRequest)
            throws CallbackException;

    /**
     * Method used to assemble callback information and decide what requests are expired.
     * @return a Map which consists of calleeName and callerName from which you can build the callback thread
     */
    public Set<CallbackLegs> runCallbackTimer();

}
