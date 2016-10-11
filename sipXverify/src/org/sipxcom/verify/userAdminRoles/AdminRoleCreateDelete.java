package org.sipxcom.verify.userAdminRoles;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

import java.sql.SQLException;

public class AdminRoleCreateDelete extends AbstractTest {
    @Test
    public void canCreateAdminRole(){
        System.out.println("\n");
        System.out.println("#### Running test " + this.getClass().getSimpleName()+" ####\n");
        createAdminRole();
    }

    @Test(dependsOnMethods = {"canCreateAdminRole"})
    public void validateAdminRoleCreated() throws SQLException {
        adminRoleCreated();
    }

    @Test(dependsOnMethods = {"canCreateAdminRole","validateAdminRoleCreated"})
    public void canDeleteAdminRole() throws SQLException {
        deleteAdminRole();
    }

    @Test(dependsOnMethods = {"canCreateAdminRole","validateAdminRoleCreated","canDeleteAdminRole"})
    public void validateAdminRoleDeleted() throws SQLException {
        adminRoleDeleted();
    }
}
