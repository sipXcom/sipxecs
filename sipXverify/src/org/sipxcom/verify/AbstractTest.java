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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

public abstract class AbstractTest {

    protected WebDriver driver;

    @BeforeTest
    public void init() {
        System.out.println("\n");
        System.out.println("Starting test suite run\n");
        System.out.println("Initializing WebDriver and connecting to Database\n");
        driver = LoginUtil.getRemoteWebDriver(LoginUtil.SUPERADMIN);
        DatabaseConnector.setDBConnection();
    }

    @AfterTest
    public void cleanup() throws SQLException {
        System.out.println("Test suite execution finished");
        System.out.println("Closing WebDriver and Db connection\n");
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

    protected WebElement findItemByXpath(String xpath){
        WebElement element = driver.findElement(By.xpath(xpath));
        return element;
    }

    protected WebElement findItemById(String id){
        WebElement element = driver.findElement(By.id(id));
        return element;
    }

    protected String findItemAndGetText(String xpath) {
        WebElement element = driver.findElement(By.xpath(xpath));
        String text = element.getText();
        return text;
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
        System.out.println("User created\n");

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
        System.out.println("User was indeed created\n");
    }

    public void deleteUser() throws SQLException {
        System.out.println("Deleting user");
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
        System.out.println("User deleted\n");
    }

    public void userDeleted() throws SQLException {
        System.out.println("Verifying user was indeed deleted");
        System.out.println("Verifying user is not visible anymore in UI");
        assertUserErrorNotPresent(".//*[@id='user_"+PropertyLoader.getProperty("user1.name")+"_link']");
        System.out.println("Verifying user is not present in Db anymore");
        List<String> valueInDb = DatabaseConnector.getQuery("select user_name from users where user_name='"+PropertyLoader.getProperty("user1.name")+"'");
        valueInDb.isEmpty();
        System.out.println("User was indeed deleted\n");
    }

    public void assertUserErrorNotPresent(String xpath){
        try {
            driver.findElement(By.xpath(xpath));
            fail("User error present\n");
        }catch (NoSuchElementException ex){
            /* do nothing, error is not present, assert is passed */
        }
    }

    public void assertUserCreated(String xpath){
        driver.findElement(By.xpath(xpath));
    }

    //Phone related methods

    public void configureLineOnAutoProvisionedPhone() throws InterruptedException {
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
        System.out.println("Clicking Ok to Send Profiles");
        clickOnItem(PropertyLoader.getProperty("okToSendProfiles"));
        System.out.println("Waiting for profiles to be sent");
        Thread.sleep(6000);
        System.out.println("Line configured on phone\n");
    }

    public void lineRegistered(String lineName) {
        System.out.println("Going to Diagnostics tab");
        clickOnItem(PropertyLoader.getProperty("diagnosticsMenuHeader"));
        System.out.println("Going to Registrations page");
        clickOnItemWithLinkText(PropertyLoader.getProperty("registrationsMenuSection"));
        System.out.println("Getting registrations...");
        String allRegistrations = findItemAndGetText(PropertyLoader.getProperty("registrationsTable"));
        System.out.println("Finding registration...");
        String registrationToFind = "sip:"+PropertyLoader.getProperty(lineName);
        boolean b = allRegistrations.contains(registrationToFind);
        assertTrue(b);
        System.out.println("Line registered\n");
    }

    // Features related methods

    // Backup methods

    // backups.localBackups

    public void backupSingleLocally(String whatToBackup) throws SQLException, InterruptedException {
        System.out.println("Executing a " + whatToBackup + " backup..");
        System.out.println("Making sure only the correct checkbox is selected..");
        DatabaseConnector.executeUpdate("update backup_plan SET def='" + whatToBackup +"';");
        System.out.println("Checkboxes set through DB");
        System.out.println("Going to System tab");
        clickOnItem(PropertyLoader.getProperty("systemMenuHeader"));
        System.out.println("Going to Backup section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("Backup"));
        clickOnItemWithLinkText(PropertyLoader.getProperty("LocalBackups"));
        List<WebElement> numberOfArchivesBeforeTest = driver.findElements(By.linkText(whatToBackup));
        System.out.println("Waiting for Backups list to get populated..");
        Thread.sleep(3000);
        System.out.println("Number of " + whatToBackup + " archives present in the system before test is: " + numberOfArchivesBeforeTest.size());
        System.out.println("Clicking Backup Now button");
        clickOnItem(PropertyLoader.getProperty("backupNow"));
        Thread.sleep(3000);
        System.out.println("Switching to another page to refresh Backups..");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        System.out.println("Switching back to Backups page..");
        clickOnItem(PropertyLoader.getProperty("systemMenuHeader"));
        clickOnItemWithLinkText(PropertyLoader.getProperty("Backup"));
        clickOnItemWithLinkText(PropertyLoader.getProperty("LocalBackups"));
        System.out.println("Waiting for Backups list to get populated..");
        Thread.sleep(10000);
        List<WebElement> numberOfArchivesAfterTest = driver.findElements(By.linkText(whatToBackup));
        System.out.println("Number of " + whatToBackup + " archives present in the system after test is: " + numberOfArchivesAfterTest.size());
        Assert.assertTrue(numberOfArchivesAfterTest.size() > numberOfArchivesBeforeTest.size(),"Number of " + whatToBackup + " backups after test is the same as before. Backup failed?");
        System.out.println(whatToBackup + " backup successfully executed.\n");
    }

    public void backupCombinedLocally(String whatToBackup) throws SQLException, InterruptedException {
        System.out.println("Executing a combined local backup..");
        System.out.println("Making sure only the correct checkboxes are selected..");
        DatabaseConnector.executeUpdate("update backup_plan SET def='" + whatToBackup +"';");
        System.out.println("Checkboxes set through DB");
        System.out.println("Going to System tab");
        clickOnItem(PropertyLoader.getProperty("systemMenuHeader"));
        System.out.println("Going to Backup section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("Backup"));
        clickOnItemWithLinkText(PropertyLoader.getProperty("LocalBackups"));
        List<WebElement> numberOfArchivesBeforeTest = driver.findElements(By.partialLinkText("tar.gz"));
        System.out.println("Waiting for Backups list to get populated..");
        Thread.sleep(3000);
        System.out.println("Number of Archives present in the system before test is: " + numberOfArchivesBeforeTest.size());
        System.out.println("Clicking Backup Now button");
        clickOnItem(PropertyLoader.getProperty("backupNow"));
        Thread.sleep(3000);
        System.out.println("Switching to another page to refresh Backups..");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        System.out.println("Switching back to Backups page..");
        clickOnItem(PropertyLoader.getProperty("systemMenuHeader"));
        clickOnItemWithLinkText(PropertyLoader.getProperty("Backup"));
        clickOnItemWithLinkText(PropertyLoader.getProperty("LocalBackups"));
        System.out.println("Waiting for Backups list to get populated..");
        Thread.sleep(10000);
        List<WebElement> numberOfArchivesAfterTest = driver.findElements(By.partialLinkText("tar.gz"));
        System.out.println("Number of Archives present in the system after test is: " + numberOfArchivesAfterTest.size());
        Assert.assertTrue(numberOfArchivesAfterTest.size() > numberOfArchivesBeforeTest.size(),"Number of backups after test is the same as before. Backup failed?");
        System.out.println("Combined backup successfully executed.");
        System.out.println("Combined backup test needs one minute before continuing tests. Waiting..\n");
        Thread.sleep(61000);
    }
}
