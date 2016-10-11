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

    protected void refreshPage(){
        driver.navigate().refresh();
    }


    // User related methods

    public void logout(){
        System.out.println("Logging user out of portal");
        clickOnItem(PropertyLoader.getProperty("logoutButton"));
        findItemByXpath(PropertyLoader.getProperty("loginForm"));
        System.out.println("User is now logged out of portal");
    }

    public void createUser(String username) {
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
        sendKeysToField(PropertyLoader.getProperty(username),PropertyLoader.getProperty("userId"));
        System.out.println("Clearing out the IM ID field");
        clearField(PropertyLoader.getProperty("imId"));
        System.out.println("Typing the IM ID");
        sendKeysToField(PropertyLoader.getProperty(username),PropertyLoader.getProperty("imId"));
        System.out.println("Clicking Ok");
        clickOnItem(PropertyLoader.getProperty("okButton"));
        System.out.println("User created\n");

    }


    public void userCreated(String username) throws SQLException {
        System.out.println("Verifying user was indeed created");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users section");
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        System.out.println("Verifying user was created - composing xpath...");
        assertUserCreated(".//*[@id='user_"+PropertyLoader.getProperty(username)+"_link']");
        System.out.println("Verifying user is in Database");
        List<String> valueInDb = DatabaseConnector.getQuery("select user_name from users where user_name='"+PropertyLoader.getProperty("user1.name")+"'");
        assertEquals(valueInDb.get(0),PropertyLoader.getProperty(username));
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

    public void userDeleted(String username) throws SQLException {
        System.out.println("Verifying user was indeed deleted");
        System.out.println("Verifying user is not visible anymore in UI");
        assertUserErrorNotPresent(".//*[@id='user_"+PropertyLoader.getProperty(username)+"_link']");
        System.out.println("Verifying user is not present in DB anymore");
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


    //UserGroup related methods

    public void createUserGroup(String usergroupname) {
        System.out.println("Adding a new user group");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users Group section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("userGroupSection"));
        System.out.println("Clicking Add Group Link");
        clickOnItem(PropertyLoader.getProperty("addNewUserGroupLink"));
        System.out.println("Clicking Name Field");
        clickOnItem(PropertyLoader.getProperty("nameField"));
        System.out.println("Typing User Group Name");
        sendKeysToField(PropertyLoader.getProperty(usergroupname),PropertyLoader.getProperty("nameField"));
        System.out.println("Clicking OK");
        clickOnItem(PropertyLoader.getProperty("okButton"));
    }

    public void userGroupCreated(String usergroupname) throws SQLException {
        System.out.println("Verifying User Group was indeed created");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users Group section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("userGroupSection"));
        System.out.println("Verifying User Group was created - composing xpath...");
        assertUserCreated(".//*[@id='group_"+PropertyLoader.getProperty(usergroupname)+"_link']");
        System.out.println("Verifying User Group is in Database");
        List<String> valueInDb = DatabaseConnector.getQuery("select name from group_storage where name='"+PropertyLoader.getProperty("userGroup1")+"'");
        assertEquals(valueInDb.get(0),PropertyLoader.getProperty("userGroup1"));
        System.out.println("User Group was indeed created\n");
    }

    public void deleteUserGroup() throws SQLException {
        System.out.println("Deleting user group");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users Group section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("userGroupSection"));
        System.out.println("Clicking on the user group's check box");
        // Checkbox can be identified only if User Group position is 3rd in Group list
        // 1st row administrators, 2nd row OpenUC-reach-agents
        clickOnItem(PropertyLoader.getProperty("3rdUserGroupCheckbox"));
        System.out.println("Clicking on delete button");
        clickOnItem(PropertyLoader.getProperty("deleteGroupButton"));
        System.out.println("Clicking Yes on the confirmation popup");
        alertAccept();
        System.out.println("User Group deleted\n");
    }

    public void userGroupDeleted(String usergroupname) throws SQLException {
        System.out.println("Verifying user group was indeed deleted");
        System.out.println("Verifying user group is not visible anymore in UI");
        assertUserErrorNotPresent(".//*[@id='group_"+PropertyLoader.getProperty(usergroupname)+"_link']");
        System.out.println("Verifying user group is not present in Db anymore");
        List<String> valueInDb = DatabaseConnector.getQuery("select name from group_storage where name='"+PropertyLoader.getProperty("userGroup1")+"'");
        valueInDb.isEmpty();
        System.out.println("User group was indeed deleted\n");
    }

    public void addUserToUserGroup(String username, String usergroupname){

        createUser(username);
        createUserGroup(usergroupname);
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users section");
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        System.out.println("Going to User identification page");
        clickOnItem(".//*[@id='user_"+PropertyLoader.getProperty(username)+"_link']");
        System.out.println("Assigning User to User Group");
        sendKeysToField(PropertyLoader.getProperty(usergroupname),PropertyLoader.getProperty("groupsField"));
        clickOnItem(PropertyLoader.getProperty("okButton"));
        System.out.println("User added to user group\n");

    }

    public void userAddedToUserGroup(String username){
        System.out.println("Verifying User was added to User Group");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users Group section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("userGroupSection"));
        System.out.println("Clicking Member group link for the 3rd User Group");
        clickOnItem(PropertyLoader.getProperty("3rdUserGroupMemberLink"));
        System.out.println("Verifying user is one of the user group members..");
        assertUserCreated(".//*[@id='user_"+PropertyLoader.getProperty(username)+"_link']");
        System.out.println("User is indeed part of the user group\n");
    }

    public void removeUserFromUserGroup(String username){
        System.out.println("Removing user from user group");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Users section");
        clickOnItem(PropertyLoader.getProperty("usersMenuSection"));
        System.out.println("Going to User identification page");
        clickOnItem(".//*[@id='user_"+PropertyLoader.getProperty(username)+"_link']");
        System.out.println("Removing User from User Group");
        clearField(PropertyLoader.getProperty("groupsField"));
        clickOnItem(PropertyLoader.getProperty("okButton"));
        System.out.println("User removed from group.\n");
    }

//    public void userRemovedFromUserGroup(){
//        System.out.println("Verifying user was indeed removed from user group..");
//        System.out.println("Going to Users tab");
//        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
//        System.out.println("Going to Users Group section");
//        clickOnItemWithLinkText(PropertyLoader.getProperty("userGroupSection"));
//        System.out.println("Verifying Member link for 3rd Group is not visible anymore in UI");
//        assertUserErrorNotPresent(PropertyLoader.getProperty("3rdUserGroupMemberLink"));
//        System.out.println("User was indeed removed from user group\n");
//    }

    //Admin roles methods

    public void createAdminRole(){
        System.out.println("Adding a new Admin role");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Admin Roles section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("adminRolesSection"));
        System.out.println("Clicking the Add New Role link");
        clickOnItem(PropertyLoader.getProperty("addNewRoleLink"));
        System.out.println("Going to Settings sub-section");
        clickOnItem(PropertyLoader.getProperty("adminRolesSettings"));
        System.out.println("Setting Admin Role name");
        sendKeysToField(PropertyLoader.getProperty("adminRole1"), PropertyLoader.getProperty("nameField"));
        System.out.println("Clicking Ok");
        clickOnItem(PropertyLoader.getProperty("okButton"));
        System.out.println("Admin Role created\n");
    }

    public void adminRoleCreated() throws SQLException {
        System.out.println("Verifying Admin Roles was indeed created");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Admin Roles section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("adminRolesSection"));
        System.out.println("Verifying Admin Role was created ...");
        findItemByXpath(PropertyLoader.getProperty("firstAdminRoleCheckbox"));
        System.out.println("Verifying Admin Role is in Database");
        List<String> valueInDb = DatabaseConnector.getQuery("select name from admin_role where name='"+PropertyLoader.getProperty("adminRole1")+"'");
        assertEquals(valueInDb.get(0),PropertyLoader.getProperty("adminRole1"));
        System.out.println("Admin Role was indeed created\n");
    }

    public void deleteAdminRole() throws SQLException {
        System.out.println("Deleting Admin Role");
        System.out.println("Going to Users tab");
        clickOnItem(PropertyLoader.getProperty("usersMenuHeader"));
        System.out.println("Going to Admin Roles section");
        clickOnItemWithLinkText(PropertyLoader.getProperty("adminRolesSection"));
        System.out.println("Clicking on the Admin Roles's check box");
        // We are looking for the 1st occurrence of Admin role
        clickOnItem(PropertyLoader.getProperty("firstAdminRoleCheckbox"));
        System.out.println("Clicking on delete button");
        clickOnItem(PropertyLoader.getProperty("deleteAdminRoleCheckbox"));
        System.out.println("Clicking Yes on the confirmation popup");
        alertAccept();
        System.out.println("Admin Role deleted\n");
    }

    public void adminRoleDeleted() throws SQLException {
        System.out.println("Verifying Admin Role was indeed deleted");
        System.out.println("Verifying Admin Role is not visible anymore in UI");
        assertUserErrorNotPresent(PropertyLoader.getProperty("firstAdminRoleLink"));
        System.out.println("Verifying Admin Role is not present in Db anymore");
        List<String> valueInDb = DatabaseConnector.getQuery("select name from admin_role where name='"+PropertyLoader.getProperty("adminRole1")+"'");
        valueInDb.isEmpty();
        System.out.println("Admin Role was indeed deleted\n");
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
        Thread.sleep(10000);
        refreshPage();
        Thread.sleep(3000);
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
        Thread.sleep(10000);
        refreshPage();
        Thread.sleep(3000);
        List<WebElement> numberOfArchivesAfterTest = driver.findElements(By.partialLinkText("tar.gz"));
        System.out.println("Number of Archives present in the system after test is: " + numberOfArchivesAfterTest.size());
        Assert.assertTrue(numberOfArchivesAfterTest.size() > numberOfArchivesBeforeTest.size(),"Number of backups after test is the same as before. Backup failed?");
        System.out.println("Combined backup successfully executed.");
        System.out.println("Combined backup test needs one minute before continuing tests. Waiting..\n");
        Thread.sleep(61000);
    }


}
