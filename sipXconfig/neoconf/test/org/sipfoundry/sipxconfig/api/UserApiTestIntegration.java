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
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.sipfoundry.sipxconfig.test.RestApiIntegrationTestCase;

public class UserApiTestIntegration extends RestApiIntegrationTestCase {
    private CoreContext m_coreContext;
    private SettingDao m_settingDao;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
        sql("commserver/SeedLocations.sql");
    }

    @Test
    public void testUserJsonApi() throws Exception {
        // query empty users
        String emptyUsers = getAsJson("users?start=0&limit=3");
        assertEquals("{\"users\":[]}", emptyUsers);

        // create user
        String createUser = "{\"aliases\":[\"alias2\",\"alias1\"],\"userName\":\"200\",\"pintoken\":\"11111111\",\"firstName\":\"Halep\","
            + "\"lastName\":\"Simona\",\"sipPassword\":\"12345\",\"userProfile\":{\"location\":\"Boston\","
            + "\"enabled\":true,\"userName\":\"200\",\"imId\":\"200\",\"firstName\":\"Halep\",\"lastName\":\"Simona\",\"emailAddress\":\"simo@gmail.com\","
            + "\"alternateEmailAddress\":null,\"imDisplayName\":\"Halep Simona\",\"alternateImId\":null,\"jobTitle\":\"worker\",\"jobDept\":\"Engineering\","
            + "\"companyName\":\"eZuce\",\"faxNumber\":\"112211\",\"assistantName\":\"Lorena\",\"assistantPhoneNumber\":null,\"homePhoneNumber\":\"54321\","
            + "\"cellPhoneNumber\":\"12345\",\"homeAddress\":{\"country\":\"USA\",\"street\":\"wallstreet\",\"city\":\"New York\",\"zip\":null,"
            + "\"officeDesignation\":null,\"state\":\"NY\"},\"officeAddress\":{\"country\":\"USA\",\"street\":\"flowers\",\"city\":\"Andover\",\"zip\":\"123321\","
            + "\"officeDesignation\":null,\"state\":\"MA\"},\"avatar\":\"https://primary.ex.org/sipxconfig/rest/avatar/200\",\"salutation\":\"None\","
            + "\"manager\":\"Darren\",\"employeeId\":null,\"didNumber\":null,\"twiterName\":null,\"linkedinName\":null,\"facebookName\":\"facebook@fb.com\","
            + "\"xingName\":null,\"authAccountName\":null,\"emailAddressAliases\":\"\",\"custom1\":\"c1\",\"custom2\":\"c2\",\"custom3\":\"c3\","
            + "\"ldapManaged\":false,\"lastImportedDate\":null,\"branchName\":\"branch1\",\"branchAddress\":{\"country\":null,\"street\":\"str1\",\"city\":\"city1\","
            + "\"zip\":null,\"officeDesignation\":null,\"state\":null},\"disabledDate\":null,\"useBranchAddress\":false,\"extAvatar\":null,\"useExtAvatar\":false,"
            + "\"salutationId\":\"None\",\"emailAddressAliasesSet\":[]},\"notified\":true,\"voicemailPin\":\"1234\"}";
        int code = postJsonString(createUser, "users");
        assertEquals(200, code);
        assertEquals(1, m_coreContext.getUsersCount());

        User user = m_coreContext.loadUserByUserNameOrAlias("alias1");

        // retrieve users
        String users = getAsJson("users");
        assertEquals(
                String.format(
                    "{\"users\":[{\"id\":%s,\"userName\":\"200\",\"lastName\":\"Simona\",\"firstName\":\"Halep\",\"aliases\":[\"alias2\",\"alias1\"],"
                    + "\"sipPassword\":\"12345\",\"pintoken\":\"11111111\",\"voicemailPin\":\"2331a3a7bce8433908996c6f9f71f2fe\","
                    + "\"branchName\":null,\"userProfile\":{\"userName\":\"200\",\"authAccountName\":null,\"firstName\":\"Halep\",\"lastName\":\"Simona\","
                    + "\"jobTitle\":\"worker\",\"jobDept\":\"Engineering\",\"companyName\":\"eZuce\",\"assistantName\":\"Lorena\",\"location\":\"Boston\","
                    + "\"homeAddress\":{\"country\":\"USA\",\"street\":\"wallstreet\",\"city\":\"New York\",\"zip\":null,\"officeDesignation\":null,\"state\":\"NY\"},"
                    + "\"officeAddress\":{\"country\":null,\"street\":null,\"city\":null,\"zip\":null,\"officeDesignation\":null,\"state\":null},"
                    + "\"branchAddress\":{\"country\":null,\"street\":null,\"city\":null,\"zip\":null,\"officeDesignation\":null,\"state\":null},"
                    + "\"cellPhoneNumber\":\"12345\",\"homePhoneNumber\":\"54321\",\"assistantPhoneNumber\":null,\"faxNumber\":\"112211\",\"didNumber\":null,"
                    + "\"imId\":\"200\",\"imDisplayName\":\"Halep Simona\",\"alternateImId\":null,\"emailAddress\":\"simo@gmail.com\",\"alternateEmailAddress\":null,"
                    + "\"emailAddressAliasesSet\":[],\"emailAddressAliases\":\"\",\"useBranchAddress\":true,\"branchName\":\"\",\"manager\":\"Darren\","
                    + "\"salutation\":\"None\",\"employeeId\":null,\"twiterName\":null,\"linkedinName\":null,\"facebookName\":\"facebook@fb.com\",\"xingName\":null,"
                    + "\"timestamp\":%s,\"avatar\":\"https://primary.example.org/sipxconfig/rest/avatar/200\",\"extAvatar\":null,\"useExtAvatar\":false,"
                    + "\"enabled\":true,\"ldapManaged\":false,\"lastImportedDate\":null,\"disabledDate\":null,\"custom1\":\"c1\",\"custom2\":\"c2\",\"custom3\":\"c3\","
                    + "\"userId\":\"%s\",\"salutationId\":\"None\"},\"notified\":false,\"groups\":null}]}", user.getId(), user.getUserProfile().getTimestamp(), user.getId()), users);

        // retrieve user
        String userJson = getAsJson("users/200");
        assertEquals(
            String.format(
                "{\"id\":%s,\"userName\":\"200\",\"lastName\":\"Simona\",\"firstName\":\"Halep\",\"aliases\":[\"alias2\",\"alias1\"],"
                + "\"sipPassword\":\"12345\",\"pintoken\":\"11111111\",\"voicemailPin\":\"2331a3a7bce8433908996c6f9f71f2fe\",\"branchName\":null,"
                + "\"userProfile\":{\"userName\":\"200\",\"authAccountName\":null,\"firstName\":\"Halep\",\"lastName\":\"Simona\",\"jobTitle\":\"worker\","
                + "\"jobDept\":\"Engineering\",\"companyName\":\"eZuce\",\"assistantName\":\"Lorena\",\"location\":\"Boston\",\"homeAddress\":{\"country\":\"USA\","
                + "\"street\":\"wallstreet\",\"city\":\"New York\",\"zip\":null,\"officeDesignation\":null,\"state\":\"NY\"},\"officeAddress\":{\"country\":null,"
                + "\"street\":null,\"city\":null,\"zip\":null,\"officeDesignation\":null,\"state\":null},\"branchAddress\":{\"country\":null,\"street\":null,"
                + "\"city\":null,\"zip\":null,\"officeDesignation\":null,\"state\":null},\"cellPhoneNumber\":\"12345\",\"homePhoneNumber\":\"54321\","
                + "\"assistantPhoneNumber\":null,\"faxNumber\":\"112211\",\"didNumber\":null,\"imId\":\"200\",\"imDisplayName\":\"Halep Simona\","
                + "\"alternateImId\":null,\"emailAddress\":\"simo@gmail.com\",\"alternateEmailAddress\":null,\"emailAddressAliasesSet\":[],"
                + "\"emailAddressAliases\":\"\",\"useBranchAddress\":true,\"branchName\":\"\",\"manager\":\"Darren\",\"salutation\":\"None\",\"employeeId\":null,"
                + "\"twiterName\":null,\"linkedinName\":null,\"facebookName\":\"facebook@fb.com\",\"xingName\":null,\"timestamp\":%s,"
                + "\"avatar\":\"https://primary.example.org/sipxconfig/rest/avatar/200\",\"extAvatar\":null,\"useExtAvatar\":false,\"enabled\":true,"
                + "\"ldapManaged\":false,\"lastImportedDate\":null,\"disabledDate\":null,\"custom1\":\"c1\",\"custom2\":\"c2\",\"custom3\":\"c3\","
                + "\"userId\":\"%s\",\"salutationId\":\"None\"},\"notified\":false,\"groups\":null}", user.getId(), user.getUserProfile().getTimestamp(), user.getId()), userJson);

        // modify user Add user in user group
        String modifyUser = "{\"aliases\":[\"alias6\",\"alias1\"],\"userName\":\"200\",\"pintoken\":\"11111111\",\"firstName\":\"Halepeno\","
            + "\"lastName\":\"Simona\",\"sipPassword\":\"12345\",\"userProfile\":{\"location\":\"Boston\","
            + "\"enabled\":true,\"userName\":\"200\",\"imId\":\"200\",\"firstName\":\"Halep\",\"lastName\":\"Simona\",\"emailAddress\":\"simo@gmail.com\","
            + "\"alternateEmailAddress\":null,\"imDisplayName\":\"Halep Simona\",\"alternateImId\":null,\"jobTitle\":\"worker\",\"jobDept\":\"Engineering\","
            + "\"companyName\":\"eZuce\",\"faxNumber\":\"112211\",\"assistantName\":\"Lorena X\",\"assistantPhoneNumber\":null,\"homePhoneNumber\":\"54321\","
            + "\"cellPhoneNumber\":\"12345\",\"homeAddress\":{\"country\":\"USA\",\"street\":\"wallstreet\",\"city\":\"New York\",\"zip\":null,"
            + "\"officeDesignation\":null,\"state\":\"NY\"},\"officeAddress\":{\"country\":\"USA\",\"street\":\"flowers\",\"city\":\"Andover\",\"zip\":\"123321\","
            + "\"officeDesignation\":null,\"state\":\"MA\"},\"avatar\":\"https://primary.ex.org/sipxconfig/rest/avatar/200\",\"salutation\":\"None\","
            + "\"manager\":\"Darren\",\"employeeId\":null,\"didNumber\":\"555\",\"twiterName\":null,\"linkedinName\":null,\"facebookName\":\"facebook@fb.com\","
            + "\"xingName\":null,\"authAccountName\":\"200Account\",\"emailAddressAliases\":\"\",\"custom1\":\"c1\",\"custom2\":\"c2\",\"custom3\":\"c3\","
            + "\"ldapManaged\":false,\"lastImportedDate\":null,\"branchName\":\"branch1\",\"branchAddress\":{\"country\":null,\"street\":\"str1\",\"city\":\"city1\","
            + "\"zip\":null,\"officeDesignation\":null,\"state\":null},\"disabledDate\":null,\"useBranchAddress\":false,\"extAvatar\":null,\"useExtAvatar\":false,"
            + "\"salutationId\":\"None\",\"emailAddressAliasesSet\":[]},\"notified\":true,\"voicemailPin\":\"1234\",\"groups\":[{\"name\":\"testUserGroup\"}]}";
        int putCode = putJsonString(modifyUser, "users/200");
        assertEquals(200, putCode);
        Group group = m_settingDao.getGroupByName("user", "testUserGroup");
        assertNotNull(group);
        // check user with groups
        /*modifyUser = getAsJson("users/200");
        assertEquals(
                String.format(
                        "{\"id\":%s,\"userName\":\"200\",\"lastName\":\"Simona\",\"firstName\":\"Halepeno\",\"aliases\":[\"alias1\",\"alias6\"],\"sipPassword\":\"12345\",\"pintoken\":\"11111111\","
                        + "\"voicemailPin\":\"2331a3a7bce8433908996c6f9f71f2fe\",\"branchName\":null,\"userProfile\":{\"userName\":\"200\",\"authAccountName\":\"200account\","
                        + "\"firstName\":\"Halepeno\",\"lastName\":\"Simona\",\"jobTitle\":\"worker\",\"jobDept\":\"Engineering\",\"companyName\":\"eZuce\",\"assistantName\":\"Lorena X\","
                        + "\"location\":\"Boston\",\"homeAddress\":{\"country\":\"USA\",\"street\":\"wallstreet\",\"city\":\"New York\",\"zip\":null,\"officeDesignation\":null,\"state\":\"NY\"},"
                        + "\"officeAddress\":{\"country\":\"USA\",\"street\":\"flowers\",\"city\":\"Andover\",\"zip\":\"123321\",\"officeDesignation\":null,\"state\":\"MA\"},"
                        + "\"branchAddress\":{\"country\":null,\"street\":null,\"city\":null,\"zip\":null,\"officeDesignation\":null,\"state\":null},\"cellPhoneNumber\":\"12345\",\"homePhoneNumber\":\"54321\","
                        + "\"assistantPhoneNumber\":null,\"faxNumber\":\"112211\",\"didNumber\":\"555\",\"imId\":\"200\",\"imDisplayName\":\"Halepeno Simona\",\"alternateImId\":null,"
                        + "\"emailAddress\":\"simo@gmail.com\",\"alternateEmailAddress\":null,\"emailAddressAliasesSet\":[],\"emailAddressAliases\":\"\",\"useBranchAddress\":false,"
                        + "\"branchName\":\"\",\"manager\":\"Darren\",\"salutation\":\"None\",\"employeeId\":null,\"twiterName\":null,\"linkedinName\":null,\"facebookName\":\"facebook@fb.com\","
                        + "\"xingName\":null,\"timestamp\":%s,\"avatar\":\"https://primary.example.org/sipxconfig/rest/avatar/200\","
                        + "\"extAvatar\":null,\"useExtAvatar\":false,\"enabled\":true,\"ldapManaged\":false,\"lastImportedDate\":null,\"disabledDate\":null,"
                        + "\"custom1\":\"c1\",\"custom2\":\"c2\",\"custom3\":\"c3\",\"userId\":\"%s\",\"salutationId\":\"None\"},\"notified\":false,"
                        + "\"groups\":[{\"id\":%s,\"name\":\"testUserGroup\",\"description\":null,\"weight\":%s,\"count\":null}]}",
                        user.getId(), user.getUserProfile().getTimestamp(), user.getId(), group.getId(), group.getWeight()), modifyUser);*/

        // retrieve user groups
        String groups = getAsJson("users/200/groups");
        assertEquals(String.format("{\"groups\":[{\"id\":%s,\"name\":\"testUserGroup\",\"description\":null,\"weight\":%s,\"count\":null}]}",
                group.getId(), group.getWeight()), groups);

        // remove user from group
        int deleteStatus = delete("users/200/groups/testUserGroup");
        assertEquals(200, deleteStatus);
        String newGroups = getAsJson("users/200/groups");
        assertEquals("{\"groups\":[]}", newGroups);

        // get setting
        String setting = getAsJson("users/200/settings/caller-alias/external-number");
        assertEquals(
                "{\"path\":\"caller-alias/external-number\",\"type\":\"string\",\"options\":null,\"value\":null,\"defaultValue\":null,"
                + "\"label\":\"Caller ID\",\"description\":\"Externally visible user Caller ID. If it is not specified the gateway "
                + "will create a Caller ID based on the caller's extension and additional gateway parameters.\"}",
                setting);

        // modify setting
        int settingCode = putPlainText("12321", "users/200/settings/caller-alias/external-number");
        assertEquals(200, settingCode);
        String modifiedSetting = getAsJson("users/200/settings/caller-alias/external-number");
        assertEquals(
                "{\"path\":\"caller-alias/external-number\",\"type\":\"string\",\"options\":null,\"value\":\"12321\","
                + "\"defaultValue\":null,\"label\":\"Caller ID\",\"description\":"
                + "\"Externally visible user Caller ID. If it is not specified the gateway will create "
                + "a Caller ID based on the caller's extension and additional gateway parameters.\"}",
                modifiedSetting);

        // reset setting
        int resetCode = delete("users/200/settings/caller-alias/external-number");
        assertEquals(200, resetCode);

        // delete user
        int deleteUser = delete("users/200");
        assertEquals(200, deleteUser);
        assertEquals(0, m_coreContext.getUsersCount());
    }

    public void setCoreContext(CoreContext context) {
        m_coreContext = context;
    }

    public void setSettingDao(SettingDao settingDao) {
        m_settingDao = settingDao;
    }
}

