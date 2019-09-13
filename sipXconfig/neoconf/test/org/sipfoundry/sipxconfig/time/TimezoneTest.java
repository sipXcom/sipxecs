/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.time;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

public class TimezoneTest extends TestCase {

    StringReader sr = null;

    private class TimezoneMock extends Timezone {

        @Override
        protected Reader getReaderForTimezoneIni() {
            return sr;
        }
    }

    public void testInitalizeTimezoneFromClockFilewithQuotes() {
        // double quotes around ZONE="test"
        sr = new StringReader("test1");
        TimezoneMock tzm = new TimezoneMock();
        assertEquals("test1", tzm.getInitialTimezone());
    }

    public void testInitalizeTimezoneFromClockFilewithNoQuotes() {
        // no quotes around ZONE=test
        sr = new StringReader("test2");
        TimezoneMock tzm = new TimezoneMock();
        assertEquals("test2", tzm.getInitialTimezone());

    }

    public void testListAllTimezonesWithCurrentTZInList() {
        sr = new StringReader("Europe/Dublin");
        TimezoneMock tzm = new TimezoneMock();

        String currentTz = tzm.getInitialTimezone();
        NtpManagerImpl mgr = new NtpManagerImpl();
        mgr.setTimezone(tzm);
        List<String> timezonesList = mgr.getAvailableTimezones(currentTz);
        assertEquals("Europe/Dublin", currentTz);
        assertTrue(timezonesList.contains("Africa/Timbuktu"));
        assertTrue(timezonesList.contains("America/Los_Angeles"));

        // Brazil/Acres should not be in list.
        assertFalse(timezonesList.contains("Brazil/Acres"));
        assertTrue(timezonesList.contains(currentTz));
    }

    public void testListAllTimezonesWithCurrentTZNotInList() {
        // no ZONE= in the file
        sr = new StringReader("Continent/City");
        TimezoneMock tzm = new TimezoneMock();
        String currentTz = tzm.getInitialTimezone();
        NtpManagerImpl mgr = new NtpManagerImpl();
        mgr.setTimezone(tzm);

        List<String> timezonesList = mgr.getAvailableTimezones(currentTz);
        assertEquals("Continent/City", currentTz);

        //
        // The Timezone files will not contain Continent/City.
        // So the code adds the Continent/City to the list.
        //
        assertTrue(timezonesList.contains(currentTz));
    }
}
