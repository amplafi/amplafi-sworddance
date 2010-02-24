/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.sworddance.scheduling;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.sworddance.core.Expireable;

/**
 * The normal production instance of the time server. This service is to be used
 * for all date/time related methods. This way for testing purposes the "time" can be fixed.
 *
 * @author Patrick Moore
 */
public class TimeServerImpl implements TimeServer {
    private TimeZone gmtTimeZone;
    private List<TimeZone> timeZones;
    public TimeServerImpl() {
        gmtTimeZone = TimeZone.getTimeZone("GMT");
        timeZones = new ArrayList<TimeZone>();
        for(String id: TimeZone.getAvailableIDs()) {
            timeZones.add(TimeZone.getTimeZone(id));
        }
    }

    public TimeZone getGmtTimeZone() {
        return this.gmtTimeZone;
    }

    @Override
    public Calendar getCurrentStandardCalendar() {
        return Calendar.getInstance(gmtTimeZone);
    }

    /**
     * @see com.sworddance.scheduling.TimeServer#getCurrentCalendar()
     */
    public Calendar getCurrentCalendar() {
        return getCurrentStandardCalendar();
    }

    /**
     * @see com.sworddance.scheduling.TimeServer#getApproximateCalendar()
     */
    public Calendar getApproximateCalendar() {
        Calendar cal = getCurrentCalendar();
        approximate(cal);
        return cal;
    }

    @Override
    public Calendar getApproximateCalendar(int daysOffset) {
        Calendar cal = getApproximateCalendar();
        cal.add(Calendar.DAY_OF_YEAR, daysOffset);
        return cal;
    }

    public Calendar getZeroedCalendar() {
        Calendar zero = getApproximateCalendar();
        zero.set(0, 0, 0, 0, 0);
        return zero;
    }
    /**
     * @see com.sworddance.scheduling.TimeServer#getApproximateCalendarOf(java.util.Date)
     */
    public Calendar getApproximateCalendarOf(Date date) {
        Calendar cal = getCurrentCalendar();
        cal.setTime(date);
        approximate(cal);
        return cal;
    }

    /**
     * Used for database queries that don't need precise timestamp range.
     * Allows query caching to be more effective.
     * @return the current day with all time information dropped.
     */
    public Calendar getDayCalendar() {
        Calendar dayCalendar = getApproximateCalendar();
        dayCalendar.clear(Calendar.HOUR);
        dayCalendar.clear(Calendar.MINUTE);
        return dayCalendar;
    }

    /**
     * @see com.sworddance.scheduling.TimeServer#getCurrentDate()
     */
    public Date getCurrentDate() {
        return new Date();
    }

    /**
     * @see com.sworddance.scheduling.TimeServer#nanoTime()
     */
    public long nanoTime() {
        return System.nanoTime();
    }

    /**
     * @see com.sworddance.scheduling.TimeServer#currentTimeMillis()
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Rounds time to the latest minute.
     */
    protected void approximate(Calendar original) {
        original.set(Calendar.SECOND, 0);
        original.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public String formatCurrentTime(String formatString) {
        return new SimpleDateFormat(formatString).format(getCurrentDate());
    }



    /**
     * @see com.sworddance.core.ExpirationChecker#isExpired(com.sworddance.core.Expireable)
     */
    @Override
    public boolean isExpired(Expireable expireable) {
        Calendar expiration;
        if ( !expireable.isExpired() && (expiration=expireable.getExpiration()) != null) {
            expireable.setExpired(getCurrentCalendar().after(expiration));
        }
        return expireable.isExpired();
    }

    /**
     * Returns a copy of the TimeZones.
     * @see com.sworddance.scheduling.TimeServer#getTimeZones()
     */
    @Override
    public List<TimeZone> getTimeZones() {
        return new ArrayList<TimeZone>(this.timeZones);
    }

    /**
     * convert date from one timezone to the standard {@link #getCurrentStandardCalendar()}
     * @param currentDate
     * @param tz
     * @return converted date
     */
    @Override
    public Date convertToStandardCalendar(Date currentDate, TimeZone tz) {
        if ( currentDate == null || tz ==null) {
            return currentDate;
        } else {
            Calendar mbCal = Calendar.getInstance(tz);
            mbCal.setTimeInMillis(currentDate.getTime());

            Calendar cal = getCurrentStandardCalendar();
            cal.set(Calendar.YEAR, mbCal.get(Calendar.YEAR));
            cal.set(Calendar.MONTH, mbCal.get(Calendar.MONTH));
            cal.set(Calendar.DAY_OF_MONTH, mbCal.get(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, mbCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, mbCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, mbCal.get(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND, mbCal.get(Calendar.MILLISECOND));

            return cal.getTime();
        }

    }

}
