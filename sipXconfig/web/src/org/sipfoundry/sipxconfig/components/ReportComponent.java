/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.components;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.j2ee.servlets.BaseHttpServlet;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IAsset;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.services.RequestGlobals;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultKeyedValuesDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.sipfoundry.sipxconfig.cdr.Cdr;
import org.sipfoundry.sipxconfig.cdr.CdrGraphBean;
import org.sipfoundry.sipxconfig.cdr.CdrManager;
import org.sipfoundry.sipxconfig.cdr.CdrManagerImpl;
import org.sipfoundry.sipxconfig.cdr.CdrMinutesGraphBean;
import org.sipfoundry.sipxconfig.cdr.CdrSearch;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.dialplan.CallTag;
import org.sipfoundry.sipxconfig.jasperreports.JasperReportContext;
import org.sipfoundry.sipxconfig.site.cdr.CdrReports;
import org.sipfoundry.sipxconfig.site.cdr.decorators.CdrCallDirectionDecorator;
import org.sipfoundry.sipxconfig.site.cdr.decorators.CdrCallLongDistanceDecorator;
import org.sipfoundry.sipxconfig.site.cdr.decorators.CdrCallerDecorator;
import org.sipfoundry.sipxconfig.site.cdr.decorators.CdrDecorator;

@ComponentClass(allowBody = false, allowInformalParameters = true)
public abstract class ReportComponent extends BaseComponent {
    private static final String REPORT_DESIGN_TYPE = ".jrxml";

    private static final String REPORT_JASPER_TYPE = ".jasper";

    private static final String REPORT_HTML_TYPE = ".html";

    private static final String REPORT_PDF_TYPE = ".pdf";

    private static final String REPORT_CSV_TYPE = ".csv";

    private static final String REPORT_XLS_TYPE = ".xlsx";
    
    private static final String TITLE_REPORT = "title";

    private static final String TITLE_STARTREPORT = "start";

    private static final String TITLE_ENDREPORT = "end";
    
    private static final String EMPTY_TITLE = "";

    private static final String PIECHART_SECTIONLABEL_FORMAT = "{0} = {1} ({2})";    

    @InjectObject(value = "service:tapestry.globals.RequestGlobals")
    public abstract RequestGlobals getRequestGlobals();

    @InjectObject(value = "spring:jasperReportContextImpl")
    public abstract JasperReportContext getJasperReportContext();

    @InjectObject(value = "spring:tapestry")
    public abstract TapestryContext getTapestry();

    @Parameter(name = "reportLabel", required = true)
    public abstract String getReportLabel();

    @Parameter(name = "reportName", required = true)
    public abstract String getReportName();
    
    @Parameter(name = "selectedTimezone", required = true)
    public abstract String getSelectedTimezone();
    
    @Parameter(name = "startTime", required = true)
    public abstract Date getStartTime();
    
    @Parameter(name = "endTime", required = true)
    public abstract Date getEndTime();
    
    @Parameter(name = "cdrSearch", required = true)
    public abstract CdrSearch getCdrSearch();
    
    @Parameter
    public abstract User getUser();

    @Parameter(name = "showPdfLink", required = false, defaultValue = "true")
    public abstract boolean getShowPdfLink();

    @Parameter(name = "showCsvLink", required = false, defaultValue = "true")
    public abstract boolean getShowCsvLink();

    @Parameter(name = "showXlsLink", required = true)
    public abstract boolean getShowXlsLink();
    
    @InjectObject(value = "spring:cdrManagerImpl")
    public abstract CdrManagerImpl getCdrManager();
    
    public abstract void setReportParameters(Map<String, Object> parameters);
    
    public abstract List< ? > getReportData();

    public abstract void setReportData(List< ? > data);

    public abstract Map<String, Object> getReportParameters();

    public boolean isReportsGenerated() {
        File htmlReport = new File(getHtmlReportPath());
        if (htmlReport.exists()) {
            return true;
        }
        return false;
    }

    public String getDesignReportPath() {
        return getJasperReportContext().getReportsDirectory() + File.separator + getReportName()
                + REPORT_DESIGN_TYPE;
    }

    public String getHtmlReportPath() {
        return getJasperReportContext().getTmpDirectory() + File.separator + getReportName() + REPORT_HTML_TYPE;
    }

    public String getPdfReportPath() {
        return getJasperReportContext().getTmpDirectory() + File.separator + getReportName() + REPORT_PDF_TYPE;
    }

    public String getCsvReportPath() {
        return getJasperReportContext().getTmpDirectory() + File.separator + getReportName() + REPORT_CSV_TYPE;
    }

    public String getXlsReportPath() {
        return getJasperReportContext().getTmpDirectory() + File.separator + getReportName() + REPORT_XLS_TYPE;
    }

    private String getJasperPath() {
        return getJasperReportContext().getReportsDirectory() + File.separator + getReportName()
                + REPORT_JASPER_TYPE;
    }
    
    private void computeReportData(String reportName) {
        TimeZone timezone = null;
        if (getSelectedTimezone() != null) {
            timezone = TimeZone.getTimeZone(getSelectedTimezone());
        }
        int limit = getCdrManager().getSettings().getReportLimit();
        int count = getCdrManager().getCdrCount(getStartTime(), getEndTime(), getCdrSearch(), getUser());
        if (limit != 0 && count > limit) {
            count = limit;
        }
        count = (count > CdrManager.MAX_COUNT2) ? CdrManager.MAX_COUNT2 : count;

        int offset = 0;
        int pages = count / CdrManager.DUMP_PAGE2;
        int remaining = count - pages * CdrManager.DUMP_PAGE2;
        List<Cdr> cdrs = new ArrayList<Cdr> ();
        for (int i = 0; i < pages; i++) {
            List<Cdr> cdrsTemp = getCdrManager().getCdrs(getStartTime(), getEndTime(), getCdrSearch(),
                getUser(), timezone, CdrManager.DUMP_PAGE2, offset);
            offset += CdrManager.DUMP_PAGE2;
            cdrs.addAll(cdrsTemp);
        }
        if (remaining > 0) {
            List<Cdr> cdrsTemp = getCdrManager().getCdrs(getStartTime(), getEndTime(), getCdrSearch(),
                getUser(), timezone, remaining, offset);
            cdrs.addAll(cdrsTemp);
        }

        Locale locale = getPage().getLocale();
        Date startdate = getStartTime();
        Date enddate = getEndTime();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy kk:mm");
        Map<String, Object> mapParameters = new HashMap<String, Object>();

        String xAxis = getMessages().getMessage("report.cdrActiveExtensions.xAxis");
        String yAxis;
        if (reportName.equals(CdrReports.TABLE_REPORT_NAME)) {
            setReportData(getTableReportData(cdrs, locale));
            mapParameters.put(TITLE_REPORT, getMessages().getMessage(CdrReports.TITLE_TABLE_REPORT_KEY));
            mapParameters.put(TITLE_STARTREPORT, dateformat.format(startdate));
            mapParameters.put(TITLE_ENDREPORT, dateformat.format(enddate));
            setReportParameters(mapParameters);
            return;
        } else if (reportName.equals(CdrReports.LONGDISTANCE_REPORT_NAME)) {
            setReportData(getLongDistanceReportData(cdrs, locale));
            mapParameters.put(TITLE_REPORT, getMessages().getMessage(CdrReports.TITLE_LONGDISTANCE_REPORT_KEY));
            mapParameters.put(TITLE_STARTREPORT, dateformat.format(startdate));
            mapParameters.put(TITLE_ENDREPORT, dateformat.format(enddate));
            setReportParameters(mapParameters);
            return;
        } else if (reportName.equals(CdrReports.CALLDIRECTION_REPORT_NAME)) {
            List<CdrCallDirectionDecorator> data = getCallDirectionReportData(cdrs, locale);
            setReportData(data);
            mapParameters.put(TITLE_REPORT, getMessages().getMessage(CdrReports.TITLE_CALLDIRECTION_REPORT_KEY));
            mapParameters.put(TITLE_STARTREPORT, dateformat.format(startdate));
            mapParameters.put(TITLE_ENDREPORT, dateformat.format(enddate));
            setReportParameters(mapParameters);
            List<CdrGraphBean> gdata = getCallDirectionGraphData(data);
            mapParameters.put("calldirectionCallsPieImage", createCallDirectionCallsPieImage(gdata));
            setReportParameters(mapParameters);
            return;
        } else if (reportName.equals(CdrReports.EXTENSION_REPORT_NAME)) {
            setReportData(getExtensionReportData(cdrs, locale));
            mapParameters.put(TITLE_REPORT, getMessages().getMessage(CdrReports.TITLE_EXTENSION_REPORT_KEY));
            mapParameters.put(TITLE_STARTREPORT, dateformat.format(startdate));
            mapParameters.put(TITLE_ENDREPORT, dateformat.format(enddate));
            setReportParameters(mapParameters);
            return;
        } else if (reportName.equals(CdrReports.ACTIVE_CALLERS_GRAPH_NAME)) {
            List<CdrGraphBean> data = getActiveCallersData(cdrs);
            setReportData(data);
            mapParameters.put(TITLE_REPORT, getMessages().getMessage(
                    CdrReports.TITLE_ACTIVE_CALLERS_GRAPH_KEY));
            mapParameters.put(TITLE_STARTREPORT, dateformat.format(startdate));
            mapParameters.put(TITLE_ENDREPORT, dateformat.format(enddate));
            yAxis = getMessages().getMessage("report.cdrActiveCallers.yAxis");
            mapParameters.put("callersChart", createExtensionsChartImage(data, xAxis, yAxis));
            setReportParameters(mapParameters);
            return;
        } else if (reportName.equals(CdrReports.ACTIVE_RECEIVERS_GRAPH_NAME)) {
            List<CdrGraphBean> data = getActiveReceiversData(cdrs);
            setReportData(data);
            mapParameters.put(TITLE_REPORT, getMessages().getMessage(
                    CdrReports.TITLE_ACTIVE_RECEIVERS_GRAPH_KEY));
            mapParameters.put(TITLE_STARTREPORT, dateformat.format(startdate));
            mapParameters.put(TITLE_ENDREPORT, dateformat.format(enddate));
            yAxis = getMessages().getMessage("report.cdrActiveReceivers.yAxis");
            mapParameters.put("receiversChart", createExtensionsChartImage(data, xAxis, yAxis));
            setReportParameters(mapParameters);
            return;
        } else if (reportName.equals(CdrReports.MINUTES_OUTGOING_EXTENSION_GRAPH_NAME)) {
            List<CdrMinutesGraphBean> data = getOutgoingExtensionData(cdrs);
            setReportData(data);
            mapParameters.put(TITLE_REPORT, getMessages().getMessage(
                    CdrReports.TITLE_MINUTES_OUTGOING_EXTENSION_GRAPH_KEY));
            mapParameters.put(TITLE_STARTREPORT, dateformat.format(startdate));
            mapParameters.put(TITLE_ENDREPORT, dateformat.format(enddate));
            yAxis = getMessages().getMessage("report.cdrMinutesOutgoingExtension.yAxis");
            mapParameters.put("minutesOutgoingExtensionChart",
                    createMinutesOutgoingCallsChartImage(data, xAxis, yAxis));
            setReportParameters(mapParameters);
            return;
        } else if (reportName.equals(CdrReports.TERMINATION_CALLS_PIE_NAME)) {
            List<CdrGraphBean> data = getTerminationCallsData(cdrs, locale);
            setReportData(data);
            mapParameters.put(TITLE_REPORT, getMessages().getMessage(
                    CdrReports.TITLE_TERMINATION_CALLS_PIE_KEY));
            mapParameters.put(TITLE_STARTREPORT, dateformat.format(startdate));
            mapParameters.put(TITLE_ENDREPORT, dateformat.format(enddate));
            mapParameters.put("terminationCallsPieImage", createTerminationCallsPieImage(data));
            setReportParameters(mapParameters);
            return;
        }
    }
    
    // Get data for CDR table report
    private List<CdrDecorator> getTableReportData(List<Cdr> cdrs, Locale locale) {
        List<CdrDecorator> cdrsData = new ArrayList<CdrDecorator>();
        for (Cdr cdr : cdrs) {
            CdrDecorator cdrDecorator = new CdrDecorator(cdr, locale, getMessages());
            cdrsData.add(cdrDecorator);
        }
        return cdrsData;
    }

    // Get data for CDR Long Distance report
    private List<CdrCallLongDistanceDecorator> getLongDistanceReportData(List<Cdr> cdrs, Locale locale) {
        String calleeroute;

        List<CdrCallLongDistanceDecorator> cdrsData = new ArrayList<CdrCallLongDistanceDecorator>();
        for (Cdr cdr : cdrs) {
            calleeroute = cdr.getCalleeRoute();
            if ((calleeroute != null)
                && (calleeroute.endsWith(CallTag.LD.toString())
                 || calleeroute.endsWith(CallTag.INTN.toString())
                 || calleeroute.endsWith(CallTag.REST.toString())
                 || calleeroute.endsWith(CallTag.TF.toString()))
            ) {
                CdrCallLongDistanceDecorator cdrLongDistanceDecorator =
                    new CdrCallLongDistanceDecorator(cdr, locale, getMessages());
                cdrsData.add(cdrLongDistanceDecorator);
            }
        }
        Collections.sort(cdrsData);
        return cdrsData;
    }

    // Get data for CDR call direction (Incoming/Outgoing/Tandem) report
    private List<CdrCallDirectionDecorator> getCallDirectionReportData(List<Cdr> cdrs, Locale locale) {
        List<CdrCallDirectionDecorator> cdrsCaller = new ArrayList<CdrCallDirectionDecorator>();
        for (Cdr cdr : cdrs) {
            CdrCallDirectionDecorator cdrCallDirectionDecorator = new CdrCallDirectionDecorator(cdr, locale,
                    getMessages());
            cdrsCaller.add(cdrCallDirectionDecorator);
        }
        Collections.sort(cdrsCaller);
        return cdrsCaller;
    }

    // Get graph data for CDR call direction pie.
    private List<CdrGraphBean> getCallDirectionGraphData(List<CdrCallDirectionDecorator> cdrdecorated) {
        List<CdrGraphBean> directionCalls = new ArrayList<CdrGraphBean>();
        Bag directionCallsBag = new HashBag();
        for (CdrCallDirectionDecorator cdr : cdrdecorated) {
            directionCallsBag.add(cdr.getCallDirection());
        }
        Set uniqueSetDirection = directionCallsBag.uniqueSet();
        for (Object key : uniqueSetDirection) {
            CdrGraphBean bean = new CdrGraphBean((String) key, directionCallsBag.getCount(key));
            directionCalls.add(bean);
        }
        return directionCalls;
    }

    // Get data for CDR extension report
    private List<CdrCallerDecorator> getExtensionReportData(List<Cdr> cdrs, Locale locale) {
        List<CdrCallerDecorator> cdrsCaller = new ArrayList<CdrCallerDecorator>();
        for (Cdr cdr : cdrs) {
            CdrCallerDecorator cdrCallerDecorator = new CdrCallerDecorator(cdr, locale,
                    getMessages());
            cdrsCaller.add(cdrCallerDecorator);
        }
        Collections.sort(cdrsCaller);
        return cdrsCaller;
    }

    // Get data for CDR most active callers graphs
    private List<CdrGraphBean> getActiveCallersData(List<Cdr> cdrs) {
        Bag bagCallers = new HashBag();
        for (Cdr cdr : cdrs) {
            bagCallers.add(cdr.getCaller());
        }
        List<CdrGraphBean> activeCallers = mostActiveExtensions(bagCallers);
        return activeCallers;
    }

    // Get data for CDR most active receivers graphs
    private List<CdrGraphBean> getActiveReceiversData(List<Cdr> cdrs) {
        Bag bagReceivers = new HashBag();
        for (Cdr cdr : cdrs) {
            bagReceivers.add(cdr.getCallee());
        }
        List<CdrGraphBean> activeReceivers = mostActiveExtensions(bagReceivers);
        return activeReceivers;
    }

    // Get data for CDR minutes of outgoing calls per extension graph
    private List<CdrMinutesGraphBean> getOutgoingExtensionData(List<Cdr> cdrs) {
        List<CdrMinutesGraphBean> minutesOutgoingCalls = new ArrayList<CdrMinutesGraphBean>();
        Map<String, CdrMinutesGraphBean> outgoingCalls = new HashMap<String, CdrMinutesGraphBean>();
        for (Cdr cdr : cdrs) {
            if (cdr.getDuration() > 0) {
                boolean callerInternal = cdr.getCallerInternal();
                if (callerInternal) {
                    String extension = cdr.getCaller();
                    CdrMinutesGraphBean bean = outgoingCalls.get(extension);
                    if (bean == null) {
                        outgoingCalls.put(extension,
                                new CdrMinutesGraphBean(extension, (double) cdr.getDuration()));
                    } else {
                        bean.setMinutes((bean.getMinutes() + cdr.getDuration()));
                    }
                }
            }
        }
        minutesOutgoingCalls.addAll(outgoingCalls.values());
        Collections.sort(minutesOutgoingCalls, Collections.reverseOrder());
        if (minutesOutgoingCalls.size() > 10) {
            minutesOutgoingCalls = minutesOutgoingCalls.subList(0, 10);
        }
        return minutesOutgoingCalls;
    }
    
    private List<CdrGraphBean> mostActiveExtensions(Bag bagExtensions) {
        List<CdrGraphBean> activeExtensions = new ArrayList<CdrGraphBean>();
        Set uniqueSetCallers = bagExtensions.uniqueSet();
        for (Object key : uniqueSetCallers) {
            CdrGraphBean bean = new CdrGraphBean((String) key, bagExtensions.getCount(key));
            activeExtensions.add(bean);
        }
        Collections.sort(activeExtensions, Collections.reverseOrder());
        if (activeExtensions.size() > 10) {
            activeExtensions = activeExtensions.subList(0, 10);
        }

        return activeExtensions;
    }

    private static Image createExtensionsChartImage(List<CdrGraphBean> extensions,
            String xAxisLabel, String yAxisLabel) {
        // Create a dataset...
        DefaultCategoryDataset data = new DefaultCategoryDataset();

        // Fill dataset with beans data
        for (CdrGraphBean extension : extensions) {
            data.setValue(extension.getCount(), extension.getKey(), extension.getKey());
        }

        // Create a chart with the dataset
        JFreeChart barChart = ChartFactory.createBarChart3D(EMPTY_TITLE, xAxisLabel, yAxisLabel,
                data, PlotOrientation.VERTICAL, true, true, true);
        barChart.setBackgroundPaint(Color.lightGray);
        barChart.getTitle().setPaint(Color.BLACK);
        CategoryPlot p = barChart.getCategoryPlot();
        p.setRangeGridlinePaint(Color.red);

        // Create and return the image with the size specified in the XML design
        return barChart.createBufferedImage(500, 220, BufferedImage.TYPE_INT_RGB, null);
    }

    private static Image createMinutesOutgoingCallsChartImage(
            List<CdrMinutesGraphBean> minutesOutgoingCalls, String xAxisLabel, String yAxisLabel) {
        // Create a dataset...
        DefaultCategoryDataset data = new DefaultCategoryDataset();

        // Fill dataset with beans data
        for (CdrMinutesGraphBean minutesOutgoingCall : minutesOutgoingCalls) {
            data.setValue(minutesOutgoingCall.getMinutes() / 60000, minutesOutgoingCall
                    .getExtension(), minutesOutgoingCall.getExtension());
        }

        // Create a chart with the dataset
        JFreeChart barChart = ChartFactory.createBarChart3D(EMPTY_TITLE, xAxisLabel, yAxisLabel,
                data, PlotOrientation.VERTICAL, true, true, true);
        barChart.setBackgroundPaint(Color.lightGray);
        barChart.getTitle().setPaint(Color.BLACK);
        CategoryPlot p = barChart.getCategoryPlot();
        p.setRangeGridlinePaint(Color.red);

        // Create and return the image with the size specified in the XML design
        return barChart.createBufferedImage(500, 220, BufferedImage.TYPE_INT_RGB, null);
    }

    private static Image createTerminationCallsPieImage(List<CdrGraphBean> beans) {
        // Create a dataset
        DefaultPieDataset data = new DefaultPieDataset();

        // Fill dataset with beans data
        for (CdrGraphBean terminationCall : beans) {
            data.setValue(terminationCall.getKey(), terminationCall.getCount());
        }

        // Create a chart with the dataset
        JFreeChart chart = ChartFactory.createPieChart(EMPTY_TITLE, data, true, true, true);
        chart.setBackgroundPaint(Color.lightGray);
        chart.getTitle().setPaint(Color.BLACK);

        PiePlot chartplot = (PiePlot) chart.getPlot();
        chartplot.setCircular(true);
        chartplot.setLabelGenerator(new StandardPieSectionLabelGenerator(PIECHART_SECTIONLABEL_FORMAT));
        // Create and return the image
        return chart.createBufferedImage(500, 220, BufferedImage.TYPE_INT_RGB, null);
    }


    private Image createCallDirectionCallsPieImage(List<CdrGraphBean> beans) {
        // Create a dataset
        DefaultKeyedValuesDataset data = new DefaultKeyedValuesDataset();

        // Fill dataset with beans data
        for (CdrGraphBean directionCall : beans) {
            data.setValue(directionCall.getKey(), directionCall.getCount());
        }

        // Create a chart with the dataset
        JFreeChart chart = ChartFactory.createPieChart(EMPTY_TITLE, data, true, true, false);
        chart.setBackgroundPaint(Color.lightGray);
        chart.setTitle("Summary - " + getMessages().getMessage(CdrReports.TITLE_CALLDIRECTION_REPORT_KEY));
        chart.getTitle().setPaint(Color.BLACK);

        PiePlot chartplot = (PiePlot) chart.getPlot();
        chartplot.setCircular(true);
        chartplot.setLabelGenerator(new StandardPieSectionLabelGenerator(PIECHART_SECTIONLABEL_FORMAT));

        // Create and return the image
        return chart.createBufferedImage(500, 220, BufferedImage.TYPE_INT_RGB, null);
    }    

    // Get data for CDR termination calls pie
    private List<CdrGraphBean> getTerminationCallsData(List<Cdr> cdrs, Locale locale) {
        List<CdrGraphBean> terminationCalls = new ArrayList<CdrGraphBean>();
        Bag terminationCallsBag = new HashBag();
        for (Cdr cdr : cdrs) {
            CdrDecorator cdrDecorator = new CdrDecorator(cdr, locale, getMessages());
            terminationCallsBag.add(cdrDecorator.getTermination());
        }
        Set uniqueSetTermination = terminationCallsBag.uniqueSet();
        for (Object key : uniqueSetTermination) {
            CdrGraphBean bean = new CdrGraphBean((String) key, terminationCallsBag.getCount(key));
            terminationCalls.add(bean);
        }
        return terminationCalls;
    }

    public void generateReports() {
        JRSwapFile sf = new JRSwapFile(getJasperReportContext().getTmpDirectory(), 12288, 400);
        JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(400, sf, true);
        IAsset logoAsset = getTapestry().getSkinControl().getAsset("logo.png");
        IAsset backgroundAsset = getTapestry().getSkinControl().getAsset("banner-background.png");
        computeReportData(getReportName());
        try {
            Map parameters = getReportParameters();
            parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
            parameters.put("logopath", logoAsset.getResourceLocation().getResourceURL().toString());
            parameters.put("bannerpath", backgroundAsset.getResourceLocation().getResourceURL().toString());
            JasperPrint jasperPrint = getJasperReportContext().getJasperPrint(getJasperPath(),
                    getReportParameters(), getReportData());

            generateHtmlReport(jasperPrint);
            generatePdfReport(jasperPrint);
            generateCsvReport(jasperPrint);
            generateXlsReport(jasperPrint);
        } finally {
            // Exceptions will be caught at a higher layer but ensure we clean up the files (if
            // any) generated from the virtualizer.
            virtualizer.cleanup();
        }
    }

    private void generateHtmlReport(JasperPrint jasperPrint) {
        HttpSession session = getRequestGlobals().getRequest().getSession(true);
        // Put JasperPrint object in the session for displaying html report images
        session.setAttribute(BaseHttpServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
        getJasperReportContext().generateHtmlReport(jasperPrint, getHtmlReportPath());
    }

    private void generatePdfReport(JasperPrint jasperPrint) {
        getJasperReportContext().generatePdfReport(jasperPrint, getPdfReportPath());
    }

    private void generateCsvReport(JasperPrint jasperPrint) {
        getJasperReportContext().generateCsvReport(jasperPrint, getCsvReportPath());
    }

    private void generateXlsReport(JasperPrint jasperPrint) {
        getJasperReportContext().generateXlsReport(jasperPrint, getXlsReportPath());
    }
}
