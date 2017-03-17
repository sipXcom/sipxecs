/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.phone.polycom;

import static org.easymock.EasyMock.createMock;
import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.moh.MusicOnHoldManager;
import org.sipfoundry.sipxconfig.mwi.Mwi;
import org.sipfoundry.sipxconfig.permission.PermissionName;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class PolycomLineDefaultsTest extends TestCase {
    private PolycomLineDefaults m_defaults;
    private Line m_line;
    private User m_user;

    @Override
    protected void setUp() {
        FeatureManager featureManagerMock = createMock(FeatureManager.class);
        featureManagerMock.isFeatureEnabled(Mwi.FEATURE);
        EasyMock.expectLastCall().andReturn(true).anyTimes();

        featureManagerMock.isFeatureEnabled(MusicOnHoldManager.FEATURE);
        EasyMock.expectLastCall().andReturn(true).anyTimes();

        PolycomPhone phone = new PolycomPhone();
        m_line = phone.createLine();
        phone.setFeatureManager(featureManagerMock);
        EasyMock.replay(featureManagerMock);
        DeviceDefaults defaults = new DeviceDefaults();
        defaults.setDomainManager(TestHelper.getTestDomainManager("example.org"));
        m_defaults = new PolycomLineDefaults(defaults, m_line);
    }

    public void testGetMwi() {
        m_user = new UserTest(true);
        m_user.setUserName("bluejay");
        assertNull(m_defaults.getMwiSubscribe());
        m_line.setUser(m_user);
        assertEquals("bluejay", m_defaults.getMwiSubscribe());
    }

    public void testGetNoMwi() {
        m_user = new UserTest(false);
        m_user.setUserName("bluejay");
        assertNull(m_defaults.getMwiSubscribe());
        m_line.setUser(m_user);
        assertNull(m_defaults.getMwiSubscribe());
    }
    
    class UserTest extends User {
        boolean m_voicemailPermission;

        UserTest(boolean permission) {
            m_voicemailPermission = permission;
        }
        @Override
        public boolean hasPermission(PermissionName permissionName) {
            return m_voicemailPermission;
        }
    }
}
