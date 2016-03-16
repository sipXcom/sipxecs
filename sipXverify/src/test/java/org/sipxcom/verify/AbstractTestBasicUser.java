package org.sipxcom.verify;

import org.openqa.selenium.By;
import org.sipxcom.verify.util.DatabaseConnector;
import org.sipxcom.verify.util.LoginUtil;
import org.sipxcom.verify.util.PropertyLoader;
import org.testng.annotations.BeforeTest;

/**
 * Created by cmoisa on 15/03/2016.
 */
public class AbstractTestBasicUser extends AbstractTest {
    @Override
    @BeforeTest
    public void init() {
        System.out.println("Initializing WebDriver and connecting to Database");
        driver = LoginUtil.getRemoteWebDriver(LoginUtil.USER1);
        DatabaseConnector.setDBConnection();
    }

    //Not working yet
    public void logout(){
        driver.findElement(By.className("col-xs-1 main-buttons main-menu-btn")).click();
        clickOnItem("//*[@id='webchat-main']/div[1]/div[1]/div[1]/div[4]/div[7]/a");
    }

}
