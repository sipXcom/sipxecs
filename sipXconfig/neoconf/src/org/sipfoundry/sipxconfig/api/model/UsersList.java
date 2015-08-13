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
package org.sipfoundry.sipxconfig.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.sipfoundry.sipxconfig.common.User;

@XmlRootElement(name = "Users")
public class UsersList {

    private List<UserBean> m_users;

    public void setUsers(List<UserBean> users) {
        m_users = users;
    }

    @XmlElement(name = "User")
    public List<UserBean> getUsers() {
        if (m_users == null) {
            m_users = new ArrayList<UserBean>();
        }
        return m_users;
    }

    public static UsersList convertUserList(List<User> users) {
        List<UserBean> userList = new ArrayList<UserBean>();
        for (User user : users) {
            userList.add(UserBean.convertUser(user));
        }
        UsersList list = new UsersList();
        list.setUsers(userList);
        return list;
    }
}
