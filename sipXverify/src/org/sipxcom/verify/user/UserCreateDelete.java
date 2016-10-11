/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement..
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.verify.user;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

import java.sql.SQLException;

public class UserCreateDelete extends AbstractTest {

    // This test will create an user (based on conf.properties user1.name field), verify it was created,then delete it
    // and verify it was deleted

    @Test
    public void canCreateUser(){
        System.out.println("\n");
        System.out.println("#### Running test " + this.getClass().getSimpleName()+" ####\n");
        createUser("user1.name");
    }

    @Test(dependsOnMethods = {"canCreateUser"})
    public void validateUserCreated() throws SQLException {
        userCreated("user1.name");
    }

    @Test(dependsOnMethods = {"canCreateUser","validateUserCreated"})
    public void canDeleteUser() throws SQLException {
        deleteUser();
    }

    @Test(dependsOnMethods = {"canCreateUser","validateUserCreated","canDeleteUser"})
    public void validateUserDeleted() throws SQLException {
        userDeleted("user1.name");
    }

}
