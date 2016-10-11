package org.sipxcom.verify.phone;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

public class PhoneAddLineToAutoProvisionedPhone extends AbstractTest {

    @Test
    public void canConfigureLineOnPhone() throws InterruptedException {
        createUser("user1.name");
        configureLineOnAutoProvisionedPhone();
    }

    @Test
    public void validateLineRegistration(){
        lineRegistered("user1.name");
    }



}
