/*
 *
 * Copyright (C) 2015 Karel Electronics Corp. All rights reserved.
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

package org.sipfoundry.sipxconfig.site.tcpdumplog;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;

public class TcpdumpLogReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private String m_serverFqdn;
    private String m_reportName;
    private final Long m_fileSizeLong;
    private String m_fileSize;
    private final Long m_modifiedDateLong;
    private String m_modifiedDate;
    private String m_dirName;

    public TcpdumpLogReport(File file, String primaryFqdn) throws IOException {
        super();
        this.m_reportName = file.getName();
        this.m_fileSizeLong = file.length();
        this.m_fileSize = FileUtils.byteCountToDisplaySize(file.length());
        this.m_modifiedDateLong = file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        this.m_modifiedDate = sdf.format(m_modifiedDateLong);
        this.m_dirName = file.getParentFile().getCanonicalPath();
        String parentFileName = file.getParentFile().getName();
        if (parentFileName.equals(EditTcpdumpLogService.TCPDUMP)) {
            parentFileName = primaryFqdn;
        }
        this.m_serverFqdn = parentFileName;
    }

    public String getServerFqdn() {
        return m_serverFqdn;
    }

    public String getReportName() {
        return m_reportName;
    }

    public void setReportName(String reportName) {
        m_reportName = reportName;
    }
    public String getFileSize() {
        return m_fileSize;
    }

    public Long getFileSizeLong() {
        return m_fileSizeLong;
    }

    public void setFileSize(String fileSize) {
        m_fileSize = fileSize;
    }

    public String getModifiedDate() {
        return m_modifiedDate;
    }

    public Long getModifiedDateLong() {
        return m_modifiedDateLong;
    }

    public void setModifiedDate(String modifiedDate) {
        m_modifiedDate = modifiedDate;
    }

    public void setDirName(String dirName) {
        this.m_dirName = dirName;
    }

    public String getDirName() {
        return m_dirName;
    }
}
