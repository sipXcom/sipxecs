/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.search;

import java.util.Set;

/**
 * Marker interface to identify objects which are indexed for search manager
 */
public interface IndexedBean {

    /**
     * Returns a set of all the values that need indexing for this bean.
     */
    public Set<String> getIndexValues();

}
