package com.viewme.verify;

import org.apache.xpath.SourceTree;
import org.sikuli.script.*;
import org.sipxcom.verify.util.LoginUtil;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.sipxcom.verify.util.PropertyLoader;

import java.sql.SQLException;

/**
 * Created by cmoisa on 26/10/2016.
 */
public abstract class AbstractTestViewMe {
    protected Screen screen;

    @BeforeTest
    public void init() {
        System.out.println("\n");
        System.out.println("Starting test suite run\n");
        System.out.println("Initializing Sikulix\n");
        screen = LoginUtil.getScreen();

    }

    @AfterTest
    public void cleanup() throws SQLException {
        System.out.println("\nTest suite execution finished");
    }

    public void openVibeApp() throws FindFailed {
        System.out.println("Opening Vibe..");
        System.out.println("Double clicking Vibe desktop screenshot");
        screen.doubleClick(PropertyLoader.getProperty("desktopShortcut"));
        System.out.println("Waiting for app to load..");
        screen.wait(PropertyLoader.getProperty("loginScreen"),60);
        screen.click(PropertyLoader.getProperty("loginScreen"));
        System.out.println("Login screen is displayed");
    }

    public void loginAsRegisteredUser(String username, String password) throws FindFailed {
        System.out.println("Logging in as registered user..");
        System.out.println("Clicking registered tab");
        screen.click(PropertyLoader.getProperty("loginScreen.RegisteredUserSection"));
        System.out.println("Inserting Name..");
        // Name and Password fields are too similar so we need to identify them using regions and target offset
        Region region = screen.find(PropertyLoader.getProperty("loginScreen.NameField")).below(8);
        screen.click(region);
        screen.type("a", KeyModifier.CTRL);
        screen.type(Key.BACKSPACE);
        screen.type(PropertyLoader.getProperty(username));
        System.out.println("Inserting Password..");
        region = screen.find(PropertyLoader.getProperty("loginScreen.PasswordField")).below(8);
        screen.click(region);
        screen.type("a", KeyModifier.CTRL);
        screen.type(Key.BACKSPACE);
        screen.type(PropertyLoader.getProperty(password));
        System.out.println("Clicking Login button");
        screen.click(PropertyLoader.getProperty("loginScreen.LoginButton"));
        System.out.println("Waiting for login..");
        screen.wait(PropertyLoader.getProperty("inApp.TopLeftFavIcon"),30);
        screen.wait(3.0);
        System.out.println("You are now logged in.");
    }

    public void logout() throws FindFailed {
        System.out.println("Logging out of Vibe app..");
        System.out.println("Going to Menu");
        screen.click(PropertyLoader.getProperty("menu.MenButton"),30);
        System.out.println("Clicking Exit option");
        screen.wait(PropertyLoader.getProperty("menu.ExitOption"),30).click();
        System.out.println("Clicking Yes");
        screen.wait(PropertyLoader.getProperty("exit.ConfirmationWindow"),30);
        screen.wait(PropertyLoader.getProperty("exit.QuitVibe"),30);
        screen.click(PropertyLoader.getProperty("exit.YesButton"));
        screen.wait(PropertyLoader.getProperty("desktopShortcut"),30);
        System.out.println("Vibe app closed.");
    }

}
