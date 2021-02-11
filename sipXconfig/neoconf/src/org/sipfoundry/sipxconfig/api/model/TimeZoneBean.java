package org.sipfoundry.sipxconfig.api.model;

import java.util.List;

public class TimeZoneBean {
    private List<String> m_timezoneList;
    private String m_selectedTimezone;
    
    public List<String> getTimezoneList() {
        return m_timezoneList;
    }
    public void setTimezoneList(List<String> timezoneList) {
        m_timezoneList = timezoneList;
    }
    public String getSelectedTimezone() {
        return m_selectedTimezone;
    }
    public void setSelectedTimezone(String selectedTimezone) {
        m_selectedTimezone = selectedTimezone;
    }
    
    public static TimeZoneBean convertTimeZone(String selectedTimeZone, List<String> availableTimeZones) {
        TimeZoneBean bean = new TimeZoneBean();
        bean.setSelectedTimezone(selectedTimeZone);
        bean.setTimezoneList(availableTimeZones);
        return bean;
    }
}
