package org.sipfoundry.commons.util;

import java.util.Calendar;
import java.util.Date;

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
}
