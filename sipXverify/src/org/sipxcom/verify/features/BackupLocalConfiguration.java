package org.sipxcom.verify.features;

import org.apache.xpath.SourceTree;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.sipxcom.verify.AbstractTest;
import org.sipxcom.verify.util.DatabaseConnector;
import org.sipxcom.verify.util.PropertyLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by HP on 7/4/2016.
 */
public class BackupLocalConfiguration extends AbstractTest {

    @Test
    public void canBackupConfigLocally() throws InterruptedException, SQLException {
        backupConfigLocally();

    }
}
