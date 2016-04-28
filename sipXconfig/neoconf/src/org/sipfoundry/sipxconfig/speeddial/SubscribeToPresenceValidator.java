/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.speeddial;

/**
 * Interface to provide validation method for speed dial subscriptions to presence
 */
public interface SubscribeToPresenceValidator {

    public boolean validateSubscriptions(SpeedDialButtons speedDial, String number);

}
