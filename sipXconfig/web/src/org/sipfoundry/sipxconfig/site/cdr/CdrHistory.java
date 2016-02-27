/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.cdr;

import java.util.Date;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.apache.tapestry.valid.ValidatorException;
import org.sipfoundry.commons.util.TimeZoneUtils;
import org.sipfoundry.sipxconfig.cdr.CdrSearch;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.components.ObjectSelectionModel;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.site.admin.time.EditTimeZoneSettings;
import org.sipfoundry.sipxconfig.time.NtpManager;

public abstract class CdrHistory extends BaseComponent implements PageBeginRenderListener {
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
    public abstract ObjectSelectionModel getTimezones();

    public abstract void setTimezones(ObjectSelectionModel timezones);

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

    @Persist
    @InitialValue(value = "literal:active")
    public abstract String getTab();

    public void pageBeginRender(PageEvent event_) {
        if (getEndTime() == null) {
            setEndTime(TimeZoneUtils.getDefaultEndTime(getSelectedTimezone()));
        }

        if (getStartTime() == null) {
            Date startTime = TimeZoneUtils.getDefaultStartTime(getEndTime(), getSelectedTimezone());
            setStartTime(startTime);
        }

        if (getSelectedTimezone() == null || getSelectedTimezone().equals(getDefaultTimezone())) {
            String defaultTimezone = CdrPage.getDefaultTimeZoneId(getUser(), getTimeManager());
            setSelectedTimezone(defaultTimezone);
            setDefaultTimezone(defaultTimezone);
        }

        if (getCdrSearch() == null) {
            setCdrSearch(new CdrSearch());
        }

        if (getStartTime().after(getEndTime())) {
            TapestryUtils.getValidator(getPage()).record(
                    new ValidatorException(getMessages().getMessage("message.invalidDates")));
            return;
        }
    }

}
