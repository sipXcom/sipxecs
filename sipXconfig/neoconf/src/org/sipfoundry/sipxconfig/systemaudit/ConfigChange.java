/**
 *
 * Copyright (c) 2013 Karel Electronics Corp. All rights reserved.
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
 *
 */

package org.sipfoundry.sipxconfig.systemaudit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sipfoundry.sipxconfig.search.SearchableBean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfigChange implements SearchableBean {

    public static final String DATE_TIME = "dateTime";
    public static final String USER_NAME = "userName";
    public static final String DETAILS = "details";
    public static final String ACTION = "action";
    public static final String CONFIG_CHANGE_TYPE = "configChangeType";
    public static final String IP_ADDRESS = "ipAddress";

    private String m_id;
    @Expose
    @SerializedName(DATE_TIME)
    private long m_dateTime = new Date().getTime();
    @Expose
    @SerializedName(USER_NAME)
    private String m_userName;
    @Expose
    @SerializedName(IP_ADDRESS)
    private String m_ipAddress;
    @Expose
    @SerializedName(ACTION)
    private String m_action;
    @Expose
    @SerializedName(CONFIG_CHANGE_TYPE)
    private String m_configChangeType;
    @Expose
    @SerializedName(DETAILS)
    private String m_details;
    @Expose
    @SerializedName("values")
    private List<ConfigChangeValue> m_values = new ArrayList<ConfigChangeValue>(0);

    public ConfigChange() {
    }

    public ConfigChange(String id) {
        this();
        this.m_id = id;
    }

    public void addValue(ConfigChangeValue values) {
        this.m_values.add(values);
    }

    public List<ConfigChangeValue> getValues() {
        return m_values;
    }

    public void setValues(List<ConfigChangeValue> values) {
        this.m_values = values;
    }

    public String getConfigChangeType() {
        return m_configChangeType;
    }

    public void setConfigChangeType(String configChangeType) {
        this.m_configChangeType = configChangeType;
    }

    public Date getDateTime() {
        return new Date(m_dateTime);
    }

    public void setDateTime(long dateTime) {
        this.m_dateTime = dateTime;
    }

    public String getUserName() {
        return m_userName;
    }

    public void setUserName(String userName) {
        this.m_userName = userName;
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.m_ipAddress = ipAddress;
    }

    public String getDetails() {
        return m_details;
    }

    public void setDetails(String identifier) {
        this.m_details = identifier;
    }

    public String getAction() {
        return m_action;
    }

    public void setAction(String action) {
        this.m_action = action;
    }

    @Override
    public String getPrimaryKey() {
        return m_id;
    }

    public void setId(String id) {
        this.m_id = id;
    }

}
