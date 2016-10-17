/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement..
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */

package org.sipxcom.verify.userGroup;
import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;
import java.sql.SQLException;




public class AddRemoveUserFromUserGroup extends AbstractTest {

    @Test
    public void canAddUserToUserGroup() throws SQLException{
        System.out.println("\n");
        System.out.println("###Running test " + this.getClass().getSimpleName() + "###\n");
        addUserToUserGroup("user1.name","userGroup1");
    }

    @Test(dependsOnMethods = {"canAddUserToUserGroup"})
    public void validateUserAddedToUserGroup() throws SQLException {
        userAddedToUserGroup("user1.name");
    }

    @Test(dependsOnMethods = {"canAddUserToUserGroup","validateUserAddedToUserGroup"})
    public void canRemoveUserFromUserGroup() throws SQLException {
        removeUserFromUserGroup("user1.name");
    }

//    @Test(dependsOnMethods = {"canAddUserToUserGroup","validateUserAddedToUserGroup","canRemoveUserFromUserGroup"})
//    public void validateUserRemovedFromUserGroup() throws SQLException {
//        userRemovedFromUserGroup();
//    }

}