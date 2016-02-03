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
import org.sipxcom.verify.AbstractTest;

public class LoginUtil {

    private static final Log LOG = LogFactory.getLog(AbstractTest.class);
    public static final String SUPERADMIN = "superadmin";

    private static Properties m_properties = new Properties();

    static {
        try {
            InputStream propertiesFile = LoginUtil.class.getResourceAsStream("/conf.properties");
            m_properties.load(propertiesFile);
        } catch (Exception e) {
            LOG.error("Error reading conf.properties:", e);
            System.exit(1);
        }
    }

    public static String getSiteURL() {
        return m_properties.getProperty("site.url");
    }

    public static String getProperty(String propertyKey){
        return m_properties.getProperty(propertyKey);
    }

    public static String getUserName(String userName) {
        return m_properties.getProperty(userName + ".name");
    }

    public static String getUserPassword(String userName) {
        return m_properties.getProperty(userName + ".password");
    }


    private static void login(WebDriver driver, String userName) {
        driver.findElement(By.id("j_username")).sendKeys(userName);
        driver.findElement(By.id("j_password")).sendKeys(getUserPassword(userName));
        driver.findElement(By.id("login:submit")).click();
    }

    /**
     *  Returns selenium driver logged in with superadmin
     */
    public static WebDriver getRemoteWebDriver(String userName) {
        String browser = m_properties.getProperty("browser");
        WebDriver driver = null;
        if (browser.equals("chrome")) {
            return new ChromeDriver();
        } else {
            driver = new FirefoxDriver();
        }
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(getSiteURL());
        login(driver, userName);
        return driver;
    }

}
