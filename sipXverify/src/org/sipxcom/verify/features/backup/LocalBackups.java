package org.sipxcom.verify.features.backup;

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
public class LocalBackups extends AbstractTest {

    @Test
    public void canBackupSingleLocally() throws SQLException, InterruptedException {
        //Executes a single backup of each type. The passed string is used for the SQL query.
        backupSingleLocally("configuration.tar.gz");
        backupSingleLocally("voicemail.tar.gz");
        backupSingleLocally("cdr.tar.gz");
    }

    @Test(dependsOnMethods = {"canBackupSingleLocally"})
    public void canBackupCombinedLocally() throws SQLException, InterruptedException {
        //Executes local backups in different combinations. The passed string is used for the SQL query.
        backupCombinedLocally("configuration.tar.gz,voicemail.tar.gz,cdr.tar.gz");
        backupCombinedLocally("configuration.tar.gz,voicemail.tar.gz");
        backupCombinedLocally("configuration.tar.gz,cdr.tar.gz");
        backupCombinedLocally("voicemail.tar.gz,cdr.tar.gz");
    }
}
