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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.sipfoundry.commons.userdb.profile.UserProfile;

@XmlRootElement(name = "UserProfile")
@XmlType(propOrder = {
        "userid", "userName", "authAccountName", "firstName", "lastName", "jobTitle", "jobDept",
        "companyName", "assistantName", "location",
        "homeAddress", "officeAddress", "branchAddress", "cellPhoneNumber", "homePhoneNumber",
        "assistantPhoneNumber", "faxNumber",
        "didNumber", "imId", "imDisplayName", "alternateImId", "emailAddress", "alternateEmailAddress",
        "emailAddressAliasesSet",
        "emailAddressAliases", "useBranchAddress", "branchName", "manager", "salutation", "employeeId",
        "twiterName", "linkedinName",
        "facebookName", "xingName", "timestamp", "avatar", "extAvatar", "useExtAvatar", "enabled",
        "ldapManaged", "lastImportedDate",
        "disabledDate", "custom1", "custom2", "custom3", "userId", "salutationId"
        })
@JsonPropertyOrder({
        "userid", "userName", "authAccountName", "firstName", "lastName", "jobTitle", "jobDept",
        "companyName", "assistantName", "location",
        "homeAddress", "officeAddress", "branchAddress", "cellPhoneNumber", "homePhoneNumber",
        "assistantPhoneNumber", "faxNumber",
        "didNumber", "imId", "imDisplayName", "alternateImId", "emailAddress", "alternateEmailAddress",
        "emailAddressAliasesSet",
        "emailAddressAliases", "useBranchAddress", "branchName", "manager", "salutation", "employeeId",
        "twiterName", "linkedinName",
        "facebookName", "xingName", "timestamp", "avatar", "extAvatar", "useExtAvatar", "enabled",
        "ldapManaged", "lastImportedDate",
        "disabledDate", "custom1", "custom2", "custom3", "userId", "salutationId"
        })

public class UserProfileBean extends UserProfile {
    private static final Log LOG = LogFactory.getLog(UserProfileBean.class);

    public static UserProfileBean convertUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }
        try {
            UserProfileBean bean = new UserProfileBean();
            BeanUtils.copyProperties(bean, userProfile);
            return bean;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void convertToUserProfile(UserProfileBean userProfileBean, UserProfile userProfile) {
        try {
            BeanUtils.copyProperties(userProfile, userProfileBean);
        } catch (Exception e) {
            LOG.error("Cannot marshal properties");
        }
    }
}
