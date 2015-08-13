/**
 * Copyright (c) 2015 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.api;

import org.junit.Test;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.test.RestApiIntegrationTestCase;

public class UserGroupApiTestIntegration extends RestApiIntegrationTestCase {
    private CoreContext m_coreContext;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
    }

    @Test
    public void testUserGroupJsonApi() throws Exception {
        // query empty user groups
        String emptyGroups = getAsJson("userGroups");
        assertEquals("{\"groups\":[]}", emptyGroups);

        // create user group
        String createGroup = "{\"name\":\"wwwwwwww5\",\"description\":\"ewe\",\"weight\":1,\"count\":null}";
        int code = postJsonString(createGroup, "userGroups");
        assertEquals(200, code);
        assertEquals(1, m_coreContext.getGroups().size());

        Group userGroup = m_coreContext.getGroupByName("wwwwwwww5", false);

        // retrieve user groups
        String userGroups = getAsJson("userGroups");
        assertEquals(
                String.format(
                        "{\"groups\":[{\"id\":%s,\"name\":\"wwwwwwww5\",\"description\":\"ewe\",\"weight\":1,\"count\":null}]}", userGroup.getId()), userGroups);

        // retrieve user group
        String userGroupJson = getAsJson("userGroups/wwwwwwww5");
        assertEquals(
                String.format(
                        "{\"id\":%s,\"name\":\"wwwwwwww5\",\"description\":\"ewe\",\"weight\":1,\"count\":null}", userGroup.getId()), userGroupJson);

        // modify user group
        String modifyUserGroup = "{\"name\":\"wwwwwwww5\",\"description\":\"ewe-modified\",\"weight\":1,\"count\":null}";
        int putCode = putJsonString(modifyUserGroup, "userGroups/wwwwwwww5");
        assertEquals(200, putCode);

        //retrieve modified user group
        userGroupJson = getAsJson("userGroups/wwwwwwww5");
        assertEquals(
                String.format(
                        "{\"id\":%s,\"name\":\"wwwwwwww5\",\"description\":\"ewe-modified\",\"weight\":1,\"count\":null}", userGroup.getId()), userGroupJson);

        // delete user group
        int deleteUserGroup = delete("userGroups/wwwwwwww5");
        assertEquals(200, deleteUserGroup);
        assertEquals(0, m_coreContext.getUsersCount());
    }

    @Test
    public void testUserGroupXmlApi() throws Exception {
        // query empty user groups
        String emptyGroups = getAsXml("userGroups");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Groups/>", emptyGroups);

        // create user group
        String createGroup = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<group><name>wwwwwwww6</name><description>ewe</description><weight>2</weight></group>";
        int code = postXmlString(createGroup, "userGroups");
        assertEquals(200, code);
        assertEquals(1, m_coreContext.getGroups().size());

        Group userGroup = m_coreContext.getGroupByName("wwwwwwww6", false);

        // retrieve user groups
        String userGroups = getAsXml("userGroups");
        assertEquals(
                String.format(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<Groups><Group><id>%s</id><name>wwwwwwww6</name><description>ewe</description><weight>2</weight></Group></Groups>", userGroup.getId()), userGroups);

        // retrieve user group
        String userGroupXml = getAsXml("userGroups/wwwwwwww6");
        assertEquals(
                String.format(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<group><id>%s</id><name>wwwwwwww6</name><description>ewe</description><weight>2</weight></group>", userGroup.getId()), userGroupXml);

        // modify user group
        String modifyUserGroup = "<group><name>wwwwwwww6</name><description>ewe-modified</description><weight>2</weight></group>";
        int putCode = putXmlString(modifyUserGroup, "userGroups/wwwwwwww6");
        assertEquals(200, putCode);

        //retrieve modified user group
        userGroupXml = getAsXml("userGroups/wwwwwwww6");
        assertEquals(
                String.format(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<group><id>%s</id><name>wwwwwwww6</name><description>ewe-modified</description><weight>2</weight></group>", userGroup.getId()), userGroupXml);


        // delete user group
        int deleteUser = delete("userGroups/wwwwwwww6");
        assertEquals(200, deleteUser);
        assertEquals(0, m_coreContext.getUsersCount());
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }
}
