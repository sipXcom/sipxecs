package org.sipxcom.verify.user;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

/**
 * Created by cmoisa on 10/10/2016.
 */
public class UserLogout extends AbstractTest {

    @Test
    public void canLogout(){
        System.out.println("\n");
        System.out.println("#### Running test " + this.getClass().getSimpleName()+" ####\n");
        logout();

    }
}
