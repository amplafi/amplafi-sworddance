/*
 * Created on Feb 4, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * The implementer of this interface should be used as the source of all date/time
 * related values. This allows the clock to be controlled for testing.
 *
 * Calendar.getInstance() -- must never be used. The day/time returned will vary depending
 * on the current machine's time zone.
 *
 * @author Patrick Moore
 */
public interface TimeServer {

    /**
     * Calendar in GMT. This is the timezone that *MUST* be used for all
     * dates stored in the database.
     * @return the standardize Calendar.
     */
    public Calendar getCurrentStandardCalendar();
    /**
     * @return the current calendar with the highest possible accuracy.
     */
    public Calendar getCurrentCalendar();

    /**
     * @return the current calendar
     * - with seconds and milliseconds zeroed. This is
     *  an accuracy such that all parts of the system
     * can handle.
     *
     */
    public Calendar getApproximateCalendar();

    public Date getCurrentDate();

    /**
     * @param date
     * @return a calendar for the given date - with an accuracy such that all parts of the system
     * can handle. (nanoseconds trimmed)
     */
    public Calendar getApproximateCalendarOf(Date date);
    /**
     * Used for database queries that don't need precise timestamp range.
     * Allows query caching to be more effective.
     * @return the current day with all time information dropped.
     */
    public Calendar getDayCalendar();
    /**
     * @return 10^-9 seconds.
     */
    public long nanoTime();

    public long currentTimeMillis();

    public String formatCurrentTime(String formatString);

    /**
     * @return a copy of all the timezones
     */
    public List<TimeZone> getTimeZones();

    /**
     *
     * @param daysOffset number of days after the current date.
     * @return {@link #getApproximateCalendar()} with the offset applied.
     */
    public Calendar getApproximateCalendar(int daysOffset);

    public Date convertToStandardCalendar(Date date, TimeZone timeZone);
}