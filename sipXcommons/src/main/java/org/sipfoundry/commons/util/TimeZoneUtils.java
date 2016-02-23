package org.sipfoundry.commons.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

public class TimeZoneUtils {

    public static Date convertJodaTimezone(LocalDateTime date, String srcTz, String destTz) {
        DateTime srcDateTime = date.toDateTime(DateTimeZone.forID(srcTz));
        DateTime dstDateTime = srcDateTime.withZone(DateTimeZone.forID(destTz));
        return dstDateTime.toLocalDateTime().toDateTime().toDate();
    }

    /**
     * Returns the currentDate minus x daysAgo
     */
    public static Date getDateXDaysAgo(int daysAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysAgo);
        return calendar.getTime();
    }

    /**
     * By default set start at next midnight
     */
    public static Date getDefaultEndTime(String timezone) {
        Calendar now = Calendar.getInstance();
        if (timezone != null) {
            now.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        now.add(Calendar.DAY_OF_MONTH, 1);
        Calendar end = DateUtils.truncate(now, Calendar.DAY_OF_MONTH);
        return end.getTime();
    }

    /**
     *  start a day before end time
     */
    public static Date getDefaultStartTime(Date endTime, String timezone) {
        Calendar then = Calendar.getInstance();
        if (timezone != null) {
            then.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        then.setTime(endTime);
        then.add(Calendar.DAY_OF_MONTH, -1);
        return then.getTime();
    }

    /**
     * Return a date with the same values as the input date but a different time zone
     */
    public static Date getSameDateWithNewTimezone(Date initialDate, TimeZone timezone) {
        Calendar calTime = Calendar.getInstance();
        calTime.setTime(initialDate);

        Calendar cal = Calendar.getInstance(timezone);
        cal.set(Calendar.YEAR, calTime.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, calTime.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, calTime.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, calTime.get(Calendar.SECOND));
        return cal.getTime();
    }

    /**
     * Return a date with converted to the new timezone
     */
    public static Date convertDateToNewTimezone(Date initialDate, TimeZone timezone) {
        return new DateTime(initialDate).withZone(DateTimeZone.forTimeZone(timezone))
                .toLocalDateTime().toDate();
    }

}
