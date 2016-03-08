/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.search;

/**
 * Marker interface to identify objects which are indexed for search manager
 */
public interface IndexedBean {

    public String getIndexValue();

}
