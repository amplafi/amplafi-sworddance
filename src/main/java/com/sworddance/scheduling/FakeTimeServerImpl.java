/*
 * Created on Feb 4, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.scheduling;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Explanatory comments in {@link FakeTimeServer}.
 * @author Patrick Moore
 */
public class FakeTimeServerImpl extends TimeServerImpl implements FakeTimeServer {

    private Calendar baseTime;
    private long nanoOffset;
    private Calendar currentCalendar;

    @Override
    public void initializeService() {
        super.initializeService();
        setBaseTime(Calendar.getInstance());
    }

    public FakeTimeServerImpl init() {
        initializeService();
        return this;
    }
    @Override
    public long currentTimeMillis() {
        return getCurrentDate().getTime();
    }

    @Override
    public Calendar getCurrentCalendar() {
        return (Calendar) currentCalendar.clone();
    }

    @Override
    public Date getCurrentDate() {
        return getCurrentCalendar().getTime();
    }

    /**
     * @param nanoOffset the nanoOffset to set
     */
    public void setNanoOffset(long nanoOffset) {
        this.nanoOffset = nanoOffset;
    }

    /**
     * @return the nanoOffset
     */
    public long getNanoOffset() {
        return nanoOffset;
    }

    @Override
    public long nanoTime() {
        return MILLISECONDS.toNanos(getBaseTime().getTimeInMillis())  + nanoOffset;
    }

    /**
     * @param baseTime the baseTime to set
     */
    public void setBaseTime(Date baseTime) {
        Calendar c = Calendar.getInstance();
        c.setTime(baseTime);
        setBaseTime(c);
    }
    public void setBaseTime(Calendar baseTime) {
        currentCalendar = (Calendar) baseTime.clone();
        this.baseTime = (Calendar) baseTime.clone();
    }

    @Override
    public void setBaseTime(int year, int month, int day, int hour, int minute, int second, String timezoneId) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, second);
        if (timezoneId!=null) {
            TimeZone value = TimeZone.getTimeZone(timezoneId);
            date.setTimeZone(value);
        }
        setBaseTime(date);
    }
    /**
     * @return the baseTime
     */
    public Calendar getBaseTime() {
        return baseTime;
    }

    public void runClock(int i, TimeUnit timeUnit) {
        this.nanoOffset += NANOSECONDS.convert(i, timeUnit);
        currentCalendar = (Calendar) getBaseTime().clone();
        currentCalendar.add(Calendar.MILLISECOND,
                (int)MILLISECONDS.convert(nanoOffset, NANOSECONDS));
    }

}
