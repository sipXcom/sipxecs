/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Timezone {    
    private static final String TIMEZONE_LIST_DIR = "/usr/share/zoneinfo";
    private static final String ERROR_MSG = "Error when reading current time zone info from timezone.ini application timezone file";

    private static final Log LOG = LogFactory.getLog(Timezone.class);
    private static final String EMPTY_STRING = "";    
    private static final String FORWARD_SLASH = "/";

    private static final String[] TIMEZONE_DIRECTORIES = {
        "Africa",
        "America",
        "Antarica",
        "Artic",
        "Asia",
        "Atlantic",
        "Australia",
        "Europe",
        "Indian",
        "Pacific",
        "Etc"
    };
    
    private String m_cfDataDir;

    public List<String> getAllTimezones(String currentTimezone) {
        List<String> timezoneList = new ArrayList<String>();
        for (int i = 0; i < TIMEZONE_DIRECTORIES.length; i++) {
            File topDir = new File(TIMEZONE_LIST_DIR + FORWARD_SLASH + TIMEZONE_DIRECTORIES[i]);
            buildListOfTimezones(topDir, EMPTY_STRING, timezoneList);
        }

        //
        // If the current timezone isn't in the timezonesList then add it.
        //
        if (!timezoneList.contains(currentTimezone)) {
            timezoneList.add(currentTimezone);
        }

        return timezoneList;
    }

    public String getInitialTimezone() {
        String returnStr = EMPTY_STRING;
        Reader readerForTimezoneIni = getReaderForTimezoneIni();
        boolean found = false;
        try {
            if (readerForTimezoneIni != null) {
                BufferedReader in = new BufferedReader(readerForTimezoneIni);
                returnStr = in.readLine();
            }
        } catch (IOException e) {
            LOG.error(ERROR_MSG, e);
        } finally {
            IOUtils.closeQuietly(readerForTimezoneIni);
        }
        if (!found) {
            LOG.error(ERROR_MSG);
        }
        return returnStr;
    }

    // Process all files and directories under dir
    private static void buildListOfTimezones(File dir, String name, List<String> timezoneList) {
        if (dir == null) {
            return;
        }

        String fullname = null;
        if (name == EMPTY_STRING) {
            // Called for the top directory.
            // so fullname is this directory's name.
            fullname = dir.getName();
        } else {
            // for all children of directory other than top dir.
            // name is prevname + / + current dirName.
            fullname = name + FORWARD_SLASH + dir.getName();
        }

        if (dir.isFile()) {
            // This is a file, then add the full name to the timezonelist
            // Note: Filter out any names with a space as these will cause
            // problems setting timezone.
            // First convert the name to Display name.

            if (!fullname.contains(" ")) {
                timezoneList.add(fullname);
            }
        } else if (dir.isDirectory()) {
            // This is another sub-directory, recursively call this method
            // for all children.
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                buildListOfTimezones(new File(dir, children[i]), fullname, timezoneList);
            }
        }
    }

    protected Reader getReaderForTimezoneIni() {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(new File(m_cfDataDir, NtpManager.TIMEZONE_INI));
        } catch (FileNotFoundException e) {
            LOG.error(ERROR_MSG);
        }
        return fileReader;
    }

    public void setCfDataDir(String cfDataDir) {
        m_cfDataDir = cfDataDir;
    }   
}
