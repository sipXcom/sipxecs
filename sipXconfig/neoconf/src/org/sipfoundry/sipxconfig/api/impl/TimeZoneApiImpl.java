package org.sipfoundry.sipxconfig.api.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.sipfoundry.sipxconfig.api.TimeZoneApi;
import org.sipfoundry.sipxconfig.api.model.SettingBean;
import org.sipfoundry.sipxconfig.api.model.SettingsList;
import org.sipfoundry.sipxconfig.api.model.TimeZoneBean;

import org.sipfoundry.sipxconfig.time.NtpManager;
import org.sipfoundry.sipxconfig.time.NtpSettings;

public class TimeZoneApiImpl implements TimeZoneApi {

    private NtpManager m_ntpManager;
    
    @Override
    public Response getTimeZone() {
        m_ntpManager.getAvailableTimezones();
        return getTimeZone(m_ntpManager.getSystemTimezone(), m_ntpManager.getAvailableTimezones());
    }
    
    private Response getTimeZone(String selectedTimeZone, List<String> availableTimeZones) {
          return Response.ok().entity(TimeZoneBean.convertTimeZone(selectedTimeZone, availableTimeZones)).build();
    }    

    @Override
    public Response updateTimeZone(TimeZoneBean timezoneBean) {
        String selectedTimezone = timezoneBean.getSelectedTimezone();
        m_ntpManager.setSystemTimezone(selectedTimezone);
        return Response.ok().entity(selectedTimezone).build();
    }

    @Override
    public Response getNtpSettings(HttpServletRequest request) {
        NtpSettings ntpSettings = m_ntpManager.getSettings();
        return Response.ok().entity(
            SettingsList.convertSettingsList(ntpSettings.getSettings(), request.getLocale())).build();
    }

    @Override
    public Response setNtpSettings(SettingsList settingsList) {
        List<SettingBean> settingsBean =  settingsList.getSettings();
        NtpSettings cqSettings = m_ntpManager.getSettings();
        for (SettingBean bean : settingsBean) {
            cqSettings.setSettingValue(bean.getPath(), bean.getValue());
        }
        m_ntpManager.saveSettings(cqSettings);
        return Response.ok().build();
    }
    
    public void setNtpManager(NtpManager ntpManager) {
        m_ntpManager = ntpManager;
    }   
}
