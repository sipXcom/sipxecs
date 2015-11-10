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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.contrib.table.model.IBasicTableModel;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.request.IUploadFile;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.tcpdumplog.TcpdumpLog;
import org.sipfoundry.sipxconfig.tcpdumplog.TcpdumpLogSettings;

public abstract class EditTcpdumpLogService extends PageWithCallback implements PageBeginRenderListener  {

    public static final String PAGE = "plugin/EditTcpdumpLogService";
    public static final String TCPDUMP = "tcpdump";
    public static final String LOG_FOLDER = "/var/log/sipxpbx/" + TCPDUMP;

    @Persist
    @InitialValue(value = "literal:configure")
    public abstract String getTab();

    @Bean
    public abstract SelectMap getSelections();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:tcpdumpLog")
    public abstract TcpdumpLog getTcpdumpLog();

    @InjectObject("spring:locationsManager")
    public abstract LocationsManager getLocationsManager();

    public abstract String getPrimaryFqdn();

    public abstract void setPrimaryFqdn(String primaryFqdn);

    public abstract TcpdumpLogSettings getSettings();

    public abstract void setSettings(TcpdumpLogSettings settings);

    @Override
    public void pageBeginRender(PageEvent arg0) {
        if (getSettings() == null) {
            setSettings(getTcpdumpLog().getSettings());
        }
        if (getPrimaryFqdn() == null) {
            setPrimaryFqdn(getLocationsManager().getPrimaryLocation().getFqdn());
        }
    }

    public void apply() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }
        getTcpdumpLog().saveSettings(getSettings());
    }

    public abstract TcpdumpLogReport getCurrentRow();

    public abstract IUploadFile getUploadFile();


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<TcpdumpLogReport> getFileInfos(String dir) throws IOException {
        List<TcpdumpLogReport> tcpdumpList = new ArrayList<TcpdumpLogReport>();
        Collection files = FileUtils.listFiles(new File(dir), new RegexFileFilter("(.*?)\\.(pcap[0-9]+)"), DirectoryFileFilter.DIRECTORY);
        Iterator<File> iterator = files.iterator();

        while (iterator.hasNext()) {
            File file = iterator.next();
            if (file.isFile()) {
                tcpdumpList.add(new TcpdumpLogReport(file, getPrimaryFqdn()));
            }
        }
        return tcpdumpList;
    }

    public IBasicTableModel getTableModel() throws IOException {
        TcpdumpLogReportsTableModel tableModel = new TcpdumpLogReportsTableModel();
        List<TcpdumpLogReport> list = null;
        File f = new File(LOG_FOLDER);
        if (f.exists()) {
            list = getFileInfos(LOG_FOLDER);
        }
        tableModel.setLogReports(list);
        return tableModel;
    }
}
