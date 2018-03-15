/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.cdr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.cdr.CdrSearch;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.components.ReportBean;
import org.sipfoundry.sipxconfig.components.ReportComponent;
import org.sipfoundry.sipxconfig.components.TapestryContext;
import org.sipfoundry.sipxconfig.components.selection.AdaptedSelectionModel;
import org.sipfoundry.sipxconfig.jasperreports.JasperReportContext;
import org.sipfoundry.sipxconfig.site.admin.time.EditTimeZoneSettings;
import org.sipfoundry.sipxconfig.time.NtpManager;

public abstract class CdrReports extends BaseComponent implements PageBeginRenderListener {
    public static final String PAGE = "cdr/CdrReports";

    public static final String TITLE_TABLE_REPORT_KEY = "report.cdrTable";

    public static final String TITLE_LONGDISTANCE_REPORT_KEY = "report.cdrLongDistance";

    public static final String TITLE_CALLDIRECTION_REPORT_KEY = "report.cdrCallDirection";

    public static final String TITLE_EXTENSION_REPORT_KEY = "report.cdrExtension";

    public static final String TITLE_ACTIVE_CALLERS_GRAPH_KEY = "report.cdrActiveCallers";

    public static final String TITLE_ACTIVE_RECEIVERS_GRAPH_KEY = "report.cdrActiveReceivers";

    public static final String TITLE_MINUTES_OUTGOING_EXTENSION_GRAPH_KEY = "report.cdrMinutesOutgoingExtension";

    public static final String TITLE_TERMINATION_CALLS_PIE_KEY = "report.cdrTerminationCalls";

    public static final String TABLE_REPORT_NAME = "cdr-table-report";

    public static final String LONGDISTANCE_REPORT_NAME = "cdr-longdistance-report";

    public static final String CALLDIRECTION_REPORT_NAME = "cdr-calldirection-report";

    public static final String EXTENSION_REPORT_NAME = "cdr-extension-report";

    public static final String ACTIVE_CALLERS_GRAPH_NAME = "cdr-active-callers-graph";

    public static final String ACTIVE_RECEIVERS_GRAPH_NAME = "cdr-active-receivers-graph";

    public static final String MINUTES_OUTGOING_EXTENSION_GRAPH_NAME = "cdr-minutes-outgoing-graph";

    public static final String TERMINATION_CALLS_PIE_NAME = "cdr-termination-calls-pie";

    @InjectObject(value = "spring:jasperReportContextImpl")
    public abstract JasperReportContext getJasperReportContext();

    @InjectObject(value = "spring:tapestry")
    public abstract TapestryContext getTapestry();

    @Persist
    @InitialValue(value = "literal:active")
    public abstract String getTab();

    @Persist
    public abstract Date getStartTime();

    public abstract void setStartTime(Date startTime);

    @Persist
    public abstract Date getEndTime();

    public abstract void setEndTime(Date endTime);

    @Parameter
    public abstract User getUser();

    @Persist
    public abstract CdrSearch getCdrSearch();

    public abstract void setCdrSearch(CdrSearch cdrSearch);

    @Persist
    public abstract ReportBean getReportBean();

    public abstract void setReportBean(ReportBean bean);

    @Persist
    public abstract String getReportName();

    public abstract void setReportName(String reportName);

    @Persist
    public abstract String getReportLabel();

    public abstract void setReportLabel(String reportLabel);

    public abstract boolean isShowXlsLink();

    public abstract void setShowXlsLink(boolean show);

    @Persist
    public abstract String getSelectedTimezone();

    public abstract void setSelectedTimezone(String selectedTimezone);

    @Persist
    public abstract String getDefaultTimezone();

    public abstract void setDefaultTimezone(String defaultTimezone);

    @InjectObject(value = "spring:ntpManager")
    public abstract NtpManager getTimeManager();

    public IPropertySelectionModel getTimezoneSelectionModel() {
        return EditTimeZoneSettings.getTimezoneSelectionModel(getTimeManager());
    }

    public IPropertySelectionModel decorateModel(IPropertySelectionModel model) {
        return getTapestry().addExtraOption(model, getMessages(), "label.select");
    }

    public IPropertySelectionModel getReportModel() {
        Collection<ReportBean> beans = new ArrayList<ReportBean>();
        beans.add(new ReportBean(TABLE_REPORT_NAME, getMessages().getMessage(
                TITLE_TABLE_REPORT_KEY)));
        beans.add(new ReportBean(CALLDIRECTION_REPORT_NAME, getMessages().getMessage(
                TITLE_CALLDIRECTION_REPORT_KEY)));
        beans.add(new ReportBean(TERMINATION_CALLS_PIE_NAME, getMessages().getMessage(
                TITLE_TERMINATION_CALLS_PIE_KEY)));
        beans.add(new ReportBean(EXTENSION_REPORT_NAME, getMessages().getMessage(
                TITLE_EXTENSION_REPORT_KEY)));
        beans.add(new ReportBean(ACTIVE_CALLERS_GRAPH_NAME, getMessages().getMessage(
                TITLE_ACTIVE_CALLERS_GRAPH_KEY)));
        beans.add(new ReportBean(ACTIVE_RECEIVERS_GRAPH_NAME, getMessages().getMessage(
                TITLE_ACTIVE_RECEIVERS_GRAPH_KEY)));
        beans.add(new ReportBean(MINUTES_OUTGOING_EXTENSION_GRAPH_NAME, getMessages().getMessage(
                TITLE_MINUTES_OUTGOING_EXTENSION_GRAPH_KEY)));
        beans.add(new ReportBean(LONGDISTANCE_REPORT_NAME, getMessages().getMessage(
                TITLE_LONGDISTANCE_REPORT_KEY)));

        AdaptedSelectionModel model = new AdaptedSelectionModel();
        model.setCollection(beans);
        return model;
    }

    @Override
    public void pageBeginRender(PageEvent event_) {
        if (getEndTime() == null) {
            setEndTime(getDefaultEndTime());
        }
        if (getStartTime() == null) {
            Date startTime = getDefaultStartTime(getEndTime());
            setStartTime(startTime);
        }
        if (getCdrSearch() == null) {
            setCdrSearch(new CdrSearch());
        }
        if (getSelectedTimezone() == null || getSelectedTimezone().equals(getDefaultTimezone())) {
            String defaultTimezone = CdrPage.getDefaultTimeZoneId(getUser(), getTimeManager());
            setSelectedTimezone(defaultTimezone);
            setDefaultTimezone(defaultTimezone);
        }
        //setReportData(null);
    }

    public void formSubmit() {
        // Process form submission
        if (getReportBean() != null) {
            String reportName = getReportBean().getReportName();
            setReportName(reportName);
            setReportLabel(getReportBean().getReportLabel());

            // Generate reports
            ReportComponent reportComponent = (ReportComponent) getPage().getNestedComponent("cdrReports.report");
            reportComponent.generateReports();
            setShowXlsLink(true);
        }
    }

    /**
     * By default set start at next midnight
     */
    public static Date getDefaultEndTime() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, 1);
        Calendar end = DateUtils.truncate(now, Calendar.DAY_OF_MONTH);
        return end.getTime();
    }

    /**
     * start a day before end time
     */
    public static Date getDefaultStartTime(Date endTime) {
        Calendar then = Calendar.getInstance();
        then.setTime(endTime);
        then.add(Calendar.DAY_OF_MONTH, -1);
        return then.getTime();
    }
}
