package org.sipxcom.verify.userAdminRoles;

import org.sipxcom.verify.AbstractTest;
import org.sipxcom.verify.util.PropertyLoader;
import org.testng.annotations.Test;

import java.sql.SQLException;


public class AdminRoleAssignUnassign extends AbstractTest {
    @Test
    public void canAssignAdminRole(){
        //This will create a new user and add it to the already existing Administrators group
        //Then it will create an Admin Role and assign the above user to this admin role + add Permissions to that role
        addUserToAdministratorsUserGroup("user1.name");
        createAdminRole();
        assignAdminRole();
    }

    @Test (dependsOnMethods = {"canAssignAdminRole"})
    public void validateAdminRoleAssigned(){
        //This will logout the current admin and log in with the newly defined admin and check that it can access stuff
        adminRoleAssigned();
    }

    @Test (dependsOnMethods = {"canAssignAdminRole","validateAdminRoleAssigned" })
    public void canUnassignAdminRole() {
        //This will remove the administrator from the admin role, delete the admin role and remove user from group
        unassignAdminRole();
        deleteAdminRole();
        removeUserFromUserGroup("user1.name");
    }

    @Test (dependsOnMethods = {"canAssignAdminRole","validateAdminRoleAssigned","canUnassignAdminRole" })
    public void validateAdminRoleUnassigned() throws InterruptedException {
        //This will logout current admin, and then try to login new admin which will fail
        // since he is no longer part of the admin group.
        adminRoleUnassigned();
    }
}
