/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement..
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.verify.userGroup;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;
import java.sql.SQLException;

public class UserGroupCreateDelete extends AbstractTest{
    @Test
    public void canCreateUserGroup(){
        System.out.println("\n");
        createUserGroup();
    }

    @Test(dependsOnMethods = {"canCreateUserGroup"})
    public void validateUserGroupCreated() throws SQLException {
        userGroupCreated();
    }

    @Test(dependsOnMethods = {"canCreateUserGroup","validateUserGroupCreated"})
    public void canDeleteUserGroup() throws SQLException {
        deleteUserGroup();
    }

    @Test(dependsOnMethods = {"canCreateUserGroup","validateUserGroupCreated","canDeleteUserGroup"})
    public void validateUserGroupDeleted() throws SQLException {
        userGroupDeleted();
    }

}