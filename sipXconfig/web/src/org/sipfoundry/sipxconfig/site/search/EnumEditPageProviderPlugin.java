/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.site.search;

/**
 * Marker interface to allow plugins for mapping Classes to Pages
 */
public interface EnumEditPageProviderPlugin {

    public Object[] getPages();

}
