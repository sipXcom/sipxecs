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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.contrib.table.model.IBasicTableModel;
import org.apache.tapestry.contrib.table.model.ITableColumn;

public class TcpdumpLogReportsTableModel implements IBasicTableModel {

    private static Comparator<TcpdumpLogReport> s_reportNameComparator = new Comparator<TcpdumpLogReport>() {
        @Override
        public int compare(TcpdumpLogReport report1, TcpdumpLogReport report2) {
            String reportName1 = report1.getReportName().toUpperCase();
            String reportName2 = report2.getReportName().toUpperCase();

            if (s_sortAscending) {
                return reportName1.compareTo(reportName2);
            } else {
                return reportName2.compareTo(reportName1);
            }
        }
    };

    private static Comparator<TcpdumpLogReport> s_fileSizeComparator = new Comparator<TcpdumpLogReport>() {
        @Override
        public int compare(TcpdumpLogReport report1, TcpdumpLogReport report2) {
            Long fileSize1 = report1.getFileSizeLong();
            Long fileSize2 = report2.getFileSizeLong();

            if (s_sortAscending) {
                return fileSize1.compareTo(fileSize2);
            } else {
                return fileSize2.compareTo(fileSize1);
            }
        }
    };

    private static Comparator<TcpdumpLogReport> s_modifiedDateComparator = new Comparator<TcpdumpLogReport>() {
        @Override
        public int compare(TcpdumpLogReport report1, TcpdumpLogReport report2) {
            Long modifiedDate1 = report1.getModifiedDateLong();
            Long modifiedDate2 = report2.getModifiedDateLong();

            if (s_sortAscending) {
                return modifiedDate1.compareTo(modifiedDate2);
            } else {
                return modifiedDate2.compareTo(modifiedDate1);
            }
        }
    };

    private static boolean s_sortAscending;
    private List<TcpdumpLogReport> m_fileInfos;

    public void setSortAscending(boolean sortOrder) {
        s_sortAscending = sortOrder;
    }

    @Override
    public Iterator getCurrentPageRows(int first, int pageSize, ITableColumn sortColumn, boolean sortOrder) {
        if (m_fileInfos == null) {
            return Collections.emptyList().iterator();
        }

        // Sort each row due to sort order
        setSortAscending(sortOrder);
        sortByColumnType(sortColumn, m_fileInfos);

        // Convert sorted array to readable format for each row
        for (TcpdumpLogReport report : m_fileInfos) {
            report.setFileSize(report.getFileSize());
            report.setModifiedDate(report.getModifiedDate());
        }

        // Make sub list of files according to page
        int numFiles = m_fileInfos.size();
        if (first < numFiles) {
            m_fileInfos = m_fileInfos.subList(first, Math.min(first + pageSize, numFiles));
        } else {
            m_fileInfos.clear();
        }

        return m_fileInfos.iterator();
    }
    /*
     * Return sorted TcpdumpLogReport list by selected column type : Report Name, File Size or Last Modified Date
     * Method use Arraylist sorting using Comparator
     */
    private void sortByColumnType(ITableColumn sortColumn, List<TcpdumpLogReport> fileInfos) {
        String orderBy;
        if (sortColumn == null) {
            orderBy = "modifiedDate";
        } else {
            orderBy = sortColumn.getColumnName();
        }

        if (orderBy.equals("reportName")) {
            Collections.sort(fileInfos, TcpdumpLogReportsTableModel.s_reportNameComparator);
        } else if (orderBy.equals("fileSize")) {
            Collections.sort(fileInfos, TcpdumpLogReportsTableModel.s_fileSizeComparator);
        } else {
            Collections.sort(fileInfos, TcpdumpLogReportsTableModel.s_modifiedDateComparator);
        }
    }

    @Override
    public int getRowCount() {
        if (m_fileInfos == null) {
            return 0;
        } else {
            return m_fileInfos.size();
        }
    }

    public void setLogReports(List<TcpdumpLogReport> fileInfos) {
        m_fileInfos = fileInfos;
    }
}
