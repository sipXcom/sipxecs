/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.site.branch;

import org.apache.tapestry.IPage;

public interface LocationsAware extends IPage {
    void setFeatureId(Integer featureId);
}
