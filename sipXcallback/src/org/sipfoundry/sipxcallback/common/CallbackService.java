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

import com.hazelcast.core.IQueue;

public interface CallbackService {

    /**
     * Persists callback info into mongo db
     */
    public void updateCallbackInfoToMongo(CallbackLegs callbackLegs, boolean insertNewRequest)
            throws CallbackException;

    /**
     * Returns the hazelcast queue used to store callback requests
     */
    public IQueue<CallbackLegs> getHazelcastCallbackQueue();

    
    /**
     * Method used to initiate the hazelcast callback queue 
     */
    public void initiateCallbackQueue();

}
