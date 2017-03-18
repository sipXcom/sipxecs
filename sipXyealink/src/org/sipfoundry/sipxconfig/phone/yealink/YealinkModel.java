/*
 * Copyright (c) 2013 SibTelCom, JSC (SIPLABS Communications). All rights reserved.
 * Contributed to SIPfoundry and eZuce, Inc. under a Contributor Agreement.
 *
 * Developed by Konstantin S. Vishnivetsky
 *
 * This library or application is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License (AGPL) as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This library or application is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License (AGPL) for
 * more details.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.phone.PhoneModel;

/**
 * Static differences in yealink models
*/
public final class YealinkModel extends PhoneModel {

    private static final Log LOG = LogFactory.getLog(YealinkModel.class);
    /** Firmware 6x or beyond */
    public static final DeviceVersion VER_6X = new DeviceVersion(YealinkPhone.BEAN_ID, "6X");
    public static final DeviceVersion VER_7X = new DeviceVersion(YealinkPhone.BEAN_ID, "7X");
    public static final DeviceVersion VER_8X = new DeviceVersion(YealinkPhone.BEAN_ID, "8X");
    public static final DeviceVersion[] SUPPORTED_VERSIONS = new DeviceVersion[] {
        VER_6X,
        VER_7X,
        VER_8X
    };

    private DeviceVersion m_deviceVersion;

    private boolean m_hasSeparateDialNow;
    private String m_name;
    private String m_directoryProfileTemplate;
    private String m_dialNowProfileTemplate;
    private int m_dSSKeyCount;

    public YealinkModel() {
        super(YealinkPhone.BEAN_ID);
        setEmergencyConfigurable(false);
    }

    public static DeviceVersion getPhoneDeviceVersion(String version) {
        for (DeviceVersion deviceVersion : SUPPORTED_VERSIONS) {
            if (deviceVersion.getName().contains(version)) {
                return deviceVersion;
            }
        }
        return VER_7X;
    }

    public void setDefaultVersion(DeviceVersion value) {
        LOG.debug("YealinkModel:setDefaultVersion");
        m_deviceVersion = value;
    }

    public DeviceVersion getDefaultVersion() {
        LOG.debug("YealinkModel:getDefaultVersion");
        if(m_deviceVersion != null)
        {
            return m_deviceVersion;
        } else {
            return VER_7X;
        }
    }

    public void setName(String value) {
        m_name = value;
    }

    public String getName() {
        return m_name;
    }

    public void setDirectoryProfileTemplate(String value) {
        m_directoryProfileTemplate = value;
    }

    public String getDirectoryProfileTemplate() {
        return m_directoryProfileTemplate;
    }

    public void setDialNowProfileTemplate(String value) {
        m_dialNowProfileTemplate = value;
    }

    public String getDialNowProfileTemplate() {
        return m_dialNowProfileTemplate;
    }

    /**
    *
    * @deprecated Use getMaxDSSKeyCount() instead!!!
    */
    @Deprecated
    public int getMemoryKeyCount() {
        return m_dSSKeyCount;
    }

    /**
    *
    * @deprecated Use setMaxDSSKeyCount(int value) instead!!!
    */
    @Deprecated
    public void setMemoryKeyCount(int value) {
        m_dSSKeyCount = value;
    }

    public int getMaxDSSKeyCount() {
        return m_dSSKeyCount;
    }

    public void setMaxDSSKeyCount(int value) {
        m_dSSKeyCount = value;
    }
}
