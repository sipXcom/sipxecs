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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.common.User;

@XmlRootElement(name = "User")
@XmlType(propOrder = {
        "id", "userName", "lastName", "firstName", "aliases", "sipPassword", "pintoken",
        "voicemailPin", "branchName", "userProfile", "notified",
        "groups"
        })
@JsonPropertyOrder({
        "id", "userName", "lastName", "firstName", "aliases", "sipPassword", "pintoken",
        "voicemailPin", "branchName", "userProfile", "notified",
        "groups"
    })
public class UserBean {
    private int m_id;
    private String m_userName;
    private String m_lastName;
    private String m_firstName;
    private Set<String> m_aliases = new LinkedHashSet<String>();
    private String m_sipPassword;

    private String m_pintoken;
    private String m_voicemailPin;
    private String m_branchName;
    private UserProfileBean m_userProfile;
    private boolean m_notified;
    private List<GroupBean> m_groups;

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public String getUserName() {
        return m_userName;
    }

    public void setUserName(String name) {
        m_userName = name;
    }

    public String getLastName() {
        return m_lastName;
    }

    public void setLastName(String name) {
        m_lastName = name;
    }

    public String getFirstName() {
        return m_firstName;
    }

    public void setFirstName(String name) {
        m_firstName = name;
    }

    public void setAliases(Set<String> aliases) {
        m_aliases = aliases;
    }

    @XmlElementWrapper(name = "Aliases")
    @XmlElement(name = "Alias")
    public Set<String> getAliases() {
        return m_aliases;
    }

    public String getSipPassword() {
        return m_sipPassword;
    }

    public void setSipPassword(String sipPassword) {
        m_sipPassword = sipPassword;
    }

    public String getPintoken() {
        return m_pintoken;
    }

    public void setPintoken(String pintoken) {
        m_pintoken = pintoken;
    }

    public String getVoicemailPin() {
        return m_voicemailPin;
    }

    public void setVoicemailPin(String voicemailPin) {
        m_voicemailPin = voicemailPin;
    }

    public String getBranchName() {
        return m_branchName;
    }

    public void setBranchName(String branchName) {
        m_branchName = branchName;
    }

    public UserProfileBean getUserProfile() {
        return m_userProfile;
    }

    public void setUserProfile(UserProfileBean userProfile) {
        m_userProfile = userProfile;
    }

    public boolean isNotified() {
        return m_notified;
    }

    public void setNotified(boolean notified) {
        m_notified = notified;
    }

    public static UserBean convertUser(User user) {
        UserBean bean = new UserBean();
        bean.setId(user.getId());
        bean.setAliases(user.getAliases());
        Branch branch = user.getBranch();
        if (branch != null) {
            bean.setBranchName(user.getBranch().getName());
        }
        bean.setVoicemailPin(user.getVoicemailPintoken());
        bean.setFirstName(user.getFirstName());
        bean.setLastName(user.getLastName());
        bean.setNotified(user.isNotified());
        bean.setPintoken(user.getPintoken());
        bean.setSipPassword(user.getSipPassword());
        bean.setUserName(user.getUserName());
        bean.setUserProfile(UserProfileBean.convertUserProfile(user.getUserProfile()));
        bean.setGroups(GroupBean.buildGroupList(user.getGroupsAsList(), null));
        return bean;
    }

    @XmlElementWrapper(name = "Groups")
    @XmlElement(name = "Group")
    public List<GroupBean> getGroups() {
        return m_groups;
    }

    public void setGroups(List<GroupBean> groups) {
        m_groups = groups;
    }
}
