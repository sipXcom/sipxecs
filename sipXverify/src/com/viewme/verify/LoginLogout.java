package com.viewme.verify;

import org.sikuli.script.*;
import org.testng.annotations.Test;

/**
 * Created by cmoisa on 19/10/2016.
 */

public class LoginLogout extends AbstractTestViewMe {

    @Test
    public void canLoginAsRegsteredUser() throws FindFailed {
        openVibeApp();
        loginAsRegisteredUser("registeredUsername","registeredPassword");
    }

    @Test(dependsOnMethods = {"canLoginAsRegsteredUser"})
    public void canLogout() throws FindFailed {
        logout();
    }
}


