package org.sipxcom.verify.phone;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

public class PhoneAddLineToAutoProvisionedPhone extends AbstractTest {

    @Test
    public void canConfigureLineOnPhone(){
        createUser();
        configureLineOnAutoProvisionedPhone();
    }



}
