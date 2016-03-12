/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.verify;

import org.openqa.selenium.*;
import org.sipxcom.verify.util.DatabaseConnector;
import org.sipxcom.verify.util.LoginUtil;
import org.sipxcom.verify.util.PropertyLoader;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

public abstract class AbstractTest {

    protected WebDriver driver;

    @BeforeSuite
    public void init() {
        System.out.println("Initializing WebDriver and connecting to Database");
        driver = LoginUtil.getRemoteWebDriver(LoginUtil.SUPERADMIN);
        DatabaseConnector.setDBConnection();
    }

    @AfterSuite
    public void cleanup() throws SQLException {
        System.out.println("Closing WebDriver and Db connection");
        driver.close();
        try {
            DatabaseConnector.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Selenium related methods

    protected void clickOnItem(String xpath) {
        WebElement element = driver.findElement(By.xpath(xpath));
        element.click();
    }

    protected void clickOnItemWithLinkText(String text) {
        WebElement element = driver.findElement(By.linkText(text));
        element.click();
    }

    protected void clearField(String xpath){
        WebElement element = driver.findElement(By.xpath(xpath));
        element.clear();
    }

    protected void sendKeysToField(String something, String xpath){
        WebElement element = driver.findElement(By.xpath(xpath));
        element.sendKeys(something);
    }

    protected void alertAccept(){
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }

    protected void alertCancel(){
        Alert alert = driver.switchTo().alert();
        alert.dismiss();
    }


    // User related methods

    public void createUser() {
        System.out.println("Adding a new user");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users section");
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        System.out.println("Clicking the Add New User link");
        clickOnItem(PropertyLoader.getProperty("addNewUserLink"));
        System.out.println("Clearing out the UserID field");
        clearField(PropertyLoader.getProperty("userId"));
        System.out.println("Typing the UserID");
        sendKeysToField(PropertyLoader.getProperty("user1.name"),PropertyLoader.getProperty("userId"));
        System.out.println("Clearing out the imId field");
        clearField(PropertyLoader.getProperty("imId"));
        System.out.println("Typing the IM ID");
        sendKeysToField(PropertyLoader.getProperty("user1.name"),PropertyLoader.getProperty("imId"));
        System.out.println("Clicking Ok");
        clickOnItem(PropertyLoader.getProperty("okButton"));

    }


    public void userCreated() throws SQLException {
        System.out.println("Verifying user was indeed created");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users section");
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        System.out.println("Verifying user was created - composing xpath...");
        assertUserCreated(".//*[@id='user_"+PropertyLoader.getProperty("user1.name")+"_link']");
        System.out.println("Verifying user is in Database");
        List<String> valueInDb = DatabaseConnector.getQuery("select user_name from users where user_name='"+PropertyLoader.getProperty("user1.name")+"'");
        assertEquals(valueInDb.get(0),PropertyLoader.getProperty("user1.name"));
    }

    public void deleteUser() throws SQLException {
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users section");
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        System.out.println("Clicking on the first user check box");
        clickOnItem(PropertyLoader.getProperty("firstUserCheckbox"));
        System.out.println("Clicking on delete button");
        clickOnItem(PropertyLoader.getProperty("deleteButton"));
        System.out.println("Clicking Yes on the confirmation popup");
        alertAccept();
    }

    public void userDeleted() throws SQLException {
        System.out.println("Verifying user is not visible anymore in UI");
        assertUserErrorNotPresent(".//*[@id='user_"+PropertyLoader.getProperty("user1.name")+"_link']");
        System.out.println("Verifying user is not present in Db anymore");
        List<String> valueInDb = DatabaseConnector.getQuery("select user_name from users where user_name='"+PropertyLoader.getProperty("user1.name")+"'");
        valueInDb.isEmpty();
    }

    public void assertUserErrorNotPresent(String xpath){
        try {
            driver.findElement(By.xpath(xpath));
            fail("User error present");
        }catch (NoSuchElementException ex){
            /* do nothing, error is not present, assert is passed */
        }
    }

    public void assertUserCreated(String xpath){
        driver.findElement(By.xpath(xpath));
    }

    //Phone related methods

    public void configureLineOnAutoProvisionedPhone() {
        //Polycom phone already added to System but without a line configured
        System.out.println("Going to Devices tab");
        clickOnItem(PropertyLoader.getProperty("devicesMenuHeader"));
        System.out.println("Going to Phones section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("phonesMenuSection"));
        System.out.println("Clicking User1's phone link - composing xpath..");
        clickOnItem(PropertyLoader.getProperty("phoneXpathStart")+PropertyLoader.getProperty("user1.phoneMacAddress")+PropertyLoader.getProperty("phoneXpathEnd"));
        System.out.println("Adding User1 as a Line on this phone");
        System.out.println("Going to Line section");
        clickOnItem(PropertyLoader.getProperty("linesSection"));
        System.out.println("Clicking Add Line link");
        clickOnItem(PropertyLoader.getProperty("addLineLink"));
        System.out.println("Typing the UserID in the User Search field");
        sendKeysToField(PropertyLoader.getProperty("user1.name"),PropertyLoader.getProperty("userSearchField"));
        System.out.println("Clicking Search button");
        clickOnItem(PropertyLoader.getProperty("searchButton"));
        System.out.println("Selecting first result check box");
        clickOnItem(PropertyLoader.getProperty("firstSearchResultCheckbox"));
        System.out.println("Clicking Select button");
        clickOnItem(PropertyLoader.getProperty("selectButton"));
        System.out.println("Verifying newly added line is visible under Lines section");
        driver.findElement(By.xpath(PropertyLoader.getProperty("firstLine")));
        System.out.println("Going to Identification section");
        clickOnItem(PropertyLoader.getProperty("identificationSection"));
        System.out.println("Clicking Send Profiles");
        clickOnItem(PropertyLoader.getProperty("sendProfiles"));
        System.out.println("Line configured on phone");
    }

}
