package com.viewme.verify;

import org.sikuli.script.*;
import org.testng.annotations.Test;

/**
 * Created by cmoisa on 19/10/2016.
 */

public class LoginLogout extends AbstractTestViewMe {

    @Test
    public void canLoginAsRegsteredUser() throws FindFailed {
        System.out.println("\n");
        System.out.println("\n");
        System.out.println("#### Running test " + this.getClass().getSimpleName()+" ####\n");
        openVibeApp();
        loginAsRegisteredUser("registeredUsername","registeredPassword");
        System.out.println("\n");
    }

    @Test(dependsOnMethods = {"canLoginAsRegsteredUser"})
    public void canLogout() throws FindFailed {
        logout();
    }

    @Test
    public void canLoginAsGuestUser() throws FindFailed {
        System.out.println("\n");
        System.out.println("\n");
        System.out.println("#### Running test " + this.getClass().getSimpleName()+" ####\n");
        openVibeApp();
        System.out.println("\n");
    }
}


