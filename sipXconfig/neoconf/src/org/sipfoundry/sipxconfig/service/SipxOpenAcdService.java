/*
 *
 *
 * Copyright (C) 2010 eZuce, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.service;

public class SipxOpenAcdService extends SipxService {
    public static final String BEAN_ID = "sipxOpenAcdService";
    private String m_audioDirectory;

    public String getAudioDir() {
        return m_audioDirectory;
    }

    public void setAudioDir(String audioDirectory) {
        m_audioDirectory = audioDirectory;
    }
}
