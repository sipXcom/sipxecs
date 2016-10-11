package org.sipxcom.verify.userAdminRoles;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

/**
 * Created by cmoisa on 11/10/2016.
 */
public class AdminRoleAssignUnassign extends AbstractTest {
    @Test
    public void canAssignAdminRole(){
        createAdminRole();
        assignAdminRole();
    }

    @Test (dependsOnMethods = {"canAssignAdminRole"})
    public void validateAdminRoleAssigned(){
        adminRoleAssigned();
    }
}
