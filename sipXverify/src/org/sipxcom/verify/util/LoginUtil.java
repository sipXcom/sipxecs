/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.verify.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.sikuli.script.Screen;
import org.sipxcom.verify.AbstractTest;

public class LoginUtil {

    private static final Log LOG = LogFactory.getLog(AbstractTest.class);
    public static final String SUPERADMIN = "superadmin";
    public static final String USER1 = "user1";



    private static void login(WebDriver driver, String userName) {
        driver.findElement(By.id("j_username")).sendKeys(PropertyLoader.getUserName(userName));
        driver.findElement(By.id("j_password")).sendKeys(PropertyLoader.getUserPassword(userName));
        driver.findElement(By.id("login:submit")).click();
    }

    /**
     *  Returns selenium driver logged in with superadmin
     */
    public static WebDriver getRemoteWebDriver(String userName) {
        String browser = PropertyLoader.getProperty("browser");
        WebDriver driver = null;
        if (browser.equals("chrome")) {
            return new ChromeDriver();
        } else {
            driver = new FirefoxDriver();
        }
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(PropertyLoader.getSiteURL());
        login(driver, userName);
        return driver;
    }

    public static Screen getScreen(){
        Screen screen = new Screen();
        return screen;
    }

}
