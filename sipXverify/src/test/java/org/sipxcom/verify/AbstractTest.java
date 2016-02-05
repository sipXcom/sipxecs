/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.verify;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.sipxcom.verify.util.DatabaseConnector;
import org.sipxcom.verify.util.LoginUtil;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

public abstract class AbstractTest {

    protected WebDriver driver;

    @BeforeSuite
    //Initialize WebDriver and connect to Database
    public void init() {
        driver = LoginUtil.getRemoteWebDriver(LoginUtil.SUPERADMIN);
        DatabaseConnector.setDBConnection();
    }

    @AfterSuite
    //Close WebDriver
    public void cleanup() {
        driver.close();
    }

    protected void clickOnItem(String xpath) {
        WebElement element = driver.findElement(By.xpath(xpath));
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
}
