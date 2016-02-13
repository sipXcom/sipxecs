/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement..
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.verify.user;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

import java.sql.SQLException;

public class UserCreateDelete extends AbstractUser {

    @Test
    public void canCreateUser(){
        createUser();
    }

    @Test(dependsOnMethods = {"canCreateUser"})
    public void validateUserCreated() throws SQLException {
        userCreated();
    }

    @Test(dependsOnMethods = {"canCreateUser","validateUserCreated"})
    public void canDeleteUser() throws SQLException {
        deleteUser();
    }

    @Test(dependsOnMethods = {"canCreateUser","validateUserCreated","canDeleteUser"})
    public void validateUserDeleted() throws SQLException {
        userDeleted();
    }

}
