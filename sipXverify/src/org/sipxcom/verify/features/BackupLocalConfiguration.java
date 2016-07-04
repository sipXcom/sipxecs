package org.sipxcom.verify.features;

import org.sipxcom.verify.AbstractTest;
import org.testng.annotations.Test;

/**
 * Created by HP on 7/4/2016.
 */
public class BackupLocalConfiguration extends AbstractTest {

    @Test
    public void canBackupConfigLocally(){
        backupConfigLocally();
    }
}
