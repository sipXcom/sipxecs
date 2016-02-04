package org.sipxcom.verify.user;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.sipxcom.verify.AbstractTest;
import org.sipxcom.verify.util.LoginUtil;

import static org.testng.Assert.fail;


public class AbstractUser extends AbstractTest {

    public void createUser() {
        //Go to Users tab
        clickOnItem(LoginUtil.getProperty("usersMenuHeader"));
        //Go to Users section
        clickOnItem(LoginUtil.getProperty("usersMenuSection"));
        //Click the Add New User link
        clickOnItem(LoginUtil.getProperty("addNewUserLink"));
        //Clear out the UserID field
        clearField(LoginUtil.getProperty("userId"));
        //Type the UserID
        sendKeysToField(LoginUtil.getProperty("user1.name"),LoginUtil.getProperty("userId"));
        //Clear the IM ID field
        clearField(LoginUtil.getProperty("imId"));
        //Type the IM ID
        sendKeysToField(LoginUtil.getProperty("user1name"),LoginUtil.getProperty("imId"));
        //Click Ok
        clickOnItem(LoginUtil.getProperty("okButton"));
        //Check that user error does not show up
        assertUserErrorNotPresent(LoginUtil.getProperty("userError"));
    }


    public void userCreated(){
        //Go to Users tab
        clickOnItem(LoginUtil.getProperty("usersMenuHeader"));
        //Go to Users section
        clickOnItem(LoginUtil.getProperty("usersMenuSection"));
        //Verify user was created - compose the xpath
        assertUserCreated(".//*[@id='user_"+LoginUtil.getProperty("user1.name")+"_link']");
    }

    protected void assertUserErrorNotPresent(String xpath){
        try {
            driver.findElement(By.xpath(xpath));
            fail("User error present");
        }catch (NoSuchElementException ex){
            /* do nothing, error is not present, assert is passed */
        }
    }

    protected void assertUserCreated(String xpath){
        driver.findElement(By.xpath(xpath));
    }

}
