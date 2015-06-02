/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.registrar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.sipfoundry.sipxconfig.commserver.imdb.RegistrationItem;
import org.sipfoundry.sipxconfig.registrar.RegistrationMetrics;

public class RegistrationMetricsTest extends TestCase {
    private RegistrationMetrics m_metrics;

    protected void setUp() {
        m_metrics = new RegistrationMetrics();
    }

    public void testUniqueRegistrations() {
        String[][] regData = {
                {
                    "contact1", "10"
                }, {
                    "contact2", "11"
                }, {
                    "contact1", "12"
                }, {
                    "contact3", "13"
                }, {
                    "contact2", "9"
                }, {
                    "contact2", "11"
                }
            };
            List regs = new ArrayList();
            Calendar calendar = new GregorianCalendar(2015,6,2,13,24,30);
            for (int i = 0; i < regData.length; i++) {
                RegistrationItem item = new RegistrationItem();
                item.setContact(regData[i][0]);
                item.setExpires(calendar.getTime());
                regs.add(item);
            }
            
            Calendar now = new GregorianCalendar(2015,6,2,13,24,20);
            Date dateNow = now.getTime();
            long nowSeconds = dateNow.getTime() / 1000;

            m_metrics.setRegistrations(regs);
            List cleanRegs = new ArrayList(m_metrics.getUniqueRegistrations());
            assertEquals(3, cleanRegs.size());
            assertEquals("contact1", ((RegistrationItem) cleanRegs.get(0)).getContact());
            assertEquals("contact2", ((RegistrationItem) cleanRegs.get(1)).getContact());
            assertEquals("contact3", ((RegistrationItem) cleanRegs.get(2)).getContact());
            assertEquals(10, ((RegistrationItem) cleanRegs.get(0)).timeToExpireAsSeconds(nowSeconds));
    }

    public void testCalculateMetricsEmpty() {
        RegistrationMetrics metrics = new RegistrationMetrics();
        assertTrue(1.0 == metrics.getLoadBalance());

        metrics.setRegistrations(Collections.EMPTY_LIST);
        assertTrue(1.0 == metrics.getLoadBalance());
    }

    public void testCalculateMetricsSingleMachine() {
        RegistrationItem[] items = new RegistrationItem[] {
                newRegistrationItem("mallard"),
                newRegistrationItem("mallard"),
                newRegistrationItem("mallard"),
                newRegistrationItem("mallard")
        };
        RegistrationMetrics metrics = new RegistrationMetrics();
        metrics.setRegistrations(Arrays.asList(items));
        assertTrue(1.0 == metrics.getLoadBalance());
    }

    public void testCalculateExcellentMetrics() {
        RegistrationItem[] items = new RegistrationItem[] {
                newRegistrationItem("mallard"),
                newRegistrationItem("mallard"),
                newRegistrationItem("bigbird"),
                newRegistrationItem("bigbird")
        };
        RegistrationMetrics metrics = new RegistrationMetrics();
        metrics.setUniqueRegistrations(Arrays.asList(items));
        assertTrue(2.0 == metrics.getLoadBalance());
    }

    public void testCalculateGoodMetrics() {
        RegistrationItem[] items = new RegistrationItem[] {
                newRegistrationItem("mallard"),
                newRegistrationItem("bigbird"),
                newRegistrationItem("bigbird"),
                newRegistrationItem("bigbird"),
                newRegistrationItem("bigbird")
        };
        RegistrationMetrics metrics = new RegistrationMetrics();
        metrics.setUniqueRegistrations(Arrays.asList(items));
        assertTrue(1.4705882352941173 == metrics.getLoadBalance());
    }

    public RegistrationItem newRegistrationItem(String server) {
        RegistrationItem item = new RegistrationItem();
        item.setPrimary(server);
        return item;
    }
}
