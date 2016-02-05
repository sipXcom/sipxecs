package org.sipxcom.verify.user;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.sipxcom.verify.AbstractTest;
import org.sipxcom.verify.util.DatabaseConnector;
import org.sipxcom.verify.util.PropertyLoader;
import org.sipxcom.verify.util.PropertyLoader;

import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;


public class AbstractUser extends AbstractTest {

    public void createUser() {
        //Go to Users tab
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        //Go to Users section
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        //Click the Add New User link
        clickOnItem(PropertyLoader.getProperty("addNewUserLink"));
        //Clear out the UserID field
        clearField(PropertyLoader.getProperty("userId"));
        //Type the UserID
        sendKeysToField(PropertyLoader.getProperty("user1.name"),PropertyLoader.getProperty("userId"));
        //Clear the IM ID field
        clearField(PropertyLoader.getProperty("imId"));
        //Type the IM ID
        sendKeysToField(PropertyLoader.getProperty("user1.name"),PropertyLoader.getProperty("imId"));
        //Click Ok
        clickOnItem(PropertyLoader.getProperty("okButton"));
        //Check that user error does not show up
        assertUserErrorNotPresent(PropertyLoader.getProperty("userError"));
    }


    public void userCreated() throws SQLException {
        //Go to Users tab
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        //Go to Users section
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        //Verify user was created - compose the xpath
        assertUserCreated(".//*[@id='user_"+PropertyLoader.getProperty("user1.name")+"_link']");
        //Verify the user is in Database
        List<String> valueInDb = DatabaseConnector.getQuery("select user_name from users where user_name='"+PropertyLoader.getProperty("user1.name")+"'");
        assertEquals(valueInDb.get(0),PropertyLoader.getProperty("user1.name"));
    }

    public void deleteUser(){
        //Go to Users tab
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        //Go to Users section
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        //Click on the first user checkbox
        clickOnItem(PropertyLoader.getProperty("firstUserCheckbox"));
        //Click on delete button
        clickOnItem(PropertyLoader.getProperty("deleteButton"));
        assertUserErrorNotPresent(".//*[@id='user_"+PropertyLoader.getProperty("user1.name")+"_link']");

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
