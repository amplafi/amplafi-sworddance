/*
 * Created on Feb 16, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Fake time server for testing. The intent of this service
 * is to have all time functions be under the direct control
 * of the tester. A test may run for a variable amount of
 * time. For tests that involve testing for behavior or reporting
 * based on duration of an event, this is not acceptable.
 *
 * For example, the test needs to check code that should be
 * executed if a task did not complete within 100ms. In a test
 * environment the failure case may only happen if debugging
 * the test. Likewise, the success-case test will not reliably
 * be successful when debugging it.
 *
 * Additionally, this service allows for tests that include:
 * <ul>
 * <li>printing the current day
 * <li>change of month, day or year
 * <li>leap year testing
 * <li>different time zones
 * </ul>
 *
 * Note that this service does not have a 'running' clock. The time
 * must be advanced manually.
 * @author Patrick Moore
 */
public interface FakeTimeServer extends TimeServer {
    /**
     * @param baseTime the baseTime to set
     */
    public void setBaseTime(Date baseTime);
    public void setBaseTime(int year, int month, int day, int hour, int minute, int second, String timezoneId);
    public void setBaseTime(Calendar baseTime);
    /**
     * @return the baseTime
     */
    public Calendar getBaseTime();

    /**
     * Advance the 'current' time by the given amount of time.
     *
     * @param duration
     * @param units
     */
    public void runClock(long duration, TimeUnit units);

    public long getNanoOffset();

}
