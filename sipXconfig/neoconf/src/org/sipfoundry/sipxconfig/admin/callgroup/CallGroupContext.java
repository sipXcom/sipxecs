/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.callgroup;

import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.alias.AliasOwner;

public interface CallGroupContext extends AliasOwner {
    public static final String CONTEXT_BEAN_NAME = "callGroupContext";

    CallGroup loadCallGroup(Integer id);

    List<CallGroup> getCallGroups();

    void storeCallGroup(CallGroup callGroup);

    void removeCallGroups(Collection<Integer> ids);

    void removeCallGroupByAlias(String alias);

    void duplicateCallGroups(Collection ids);

    void removeUser(Integer userId);

    void addUsersToCallGroup(Integer callGroupId, Collection ids);

    void clear();

    void generateSipPasswords();
}
