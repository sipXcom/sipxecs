/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement..
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.verify.user;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

import java.sql.SQLException;

public class UserTest extends AbstractUser {

    @Test
    public void canCreateUser(){
        createUser();
    }

    @Test
    public void validateUserCreated() throws SQLException {
        userCreated();
    }

    @Test
    public void zcanDeleteUser() throws SQLException {
        deleteUser();
    }

}
