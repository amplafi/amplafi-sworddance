/*
 * LapTimer.java
 *
 * Created on January 22, 2003, 12:19 PM
 */

package com.sworddance.util.perf;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.dgc.VMID;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is a lightweight timer that lets developers perform many timing
 * operations including remote transmission of timers to collect data across many servers.
 * <p/>
 * A LapTimer is used to record a series of intervals (laps) between calls to {@link #start()}
 * and {@link #stop()} calls. When the LapTimer's {@link #toString()} is called the lap
 * information will be broken out.
 * <p/>
 * A LapTimer to be attached to a Thread and the static {@link #sLap()} and {@link #sLap(String)}
 * can be used to record lap information
 * This code needs to be added to the top-level Serializable object that is being sent as a message.
 *
 * <pre>
 * private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
 *     out.defaultWriteObject();
 *     com.sworddance.util.LapTimer.send(out);
 * }
 * private void readObject(java.io.ObjectInputStream in)throws java.io.IOException, ClassNotFoundException{
 *     in.defaultReadObject();
 *     com.sworddance.util.LapTimer.receive(in);
 * }
 * </pre>
 * Alternatively {@link #sendToString()} and {@link #receiveFromString(String)} may be used to
 * send and receive the LapTimers attached to the current thread.
 *
 * 06-04-2004 PSM added begin/end methods that roughly mimic the SPringframework's StopWatch functionality.
 *                fix csv output so that it is better formated.
 *                Let LapTimer.Collection set how many laps the member LapTimers should keep.
 * @author pmoore
 */
public class LapTimer implements Runnable, Serializable {

    private static final long serialVersionUID = 5534508222987180718L;

    /**
     * a simple flag that can be used to decide if LapTimers should be used.
     * This does not do anything within the LapTimer code but can be used by others
     * as a common flag.
     */
    public static final boolean ENABLE = Boolean.getBoolean("lapTimer.enable");

    /**
     * This is used to spot when a serialized LapTimer has been returned.
     */
    private static VMID vmid = new VMID();
    /**
     * This counter always increments. Combined with vmid, this will
     * make a unique enough id for LapTimers that are remoted.
     */
    private static int remotingCounter;
    /**
     * This is a map of all laptimers that are recieved.
     * A LapTimer that is new to this JVM (actually ClassLoader)
     * is stored in this map. When it is sent on (or back) it is removed.
     */
    private static Map<Id, LapTimer> receivedLaptimers = Collections.synchronizedMap(new HashMap<Id, LapTimer>());
    /**
     * This is a map of all LapTimers that are sent out from this JVM/ClassLoader.
     * A LapTimer can never be in both receivedLaptimers and sentLaptimers
     */
    private static Map<Id, LapTimer> sentLaptimers = Collections.synchronizedMap(new HashMap<Id, LapTimer>());
    /**
     * This is used as a way to avoid having to keep track the LapTimer.
     * By attaching it to a thread the LapTimer can be less intrusive.
     */
    private static ThreadLocal<LapTimer> threadLapTimer = new ThreadLocal<LapTimer>();
    private static ThreadLocal<LapTimer> lastThreadLapTimer = new ThreadLocal<LapTimer>();

    /**
     */
    private static Map<Object, LapTimer> keyedTimers = new HashMap<Object, LapTimer>();
    /**
     * CVS Date Format
     */
    private static final SimpleDateFormat CSV_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
    private static final SimpleDateFormat MSG_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * The id of the LapTimer. Because it is used to identify serialize LapTimers that
     * are read in from a socket, it must be unique across all machines.
     */
    private Id home;
    private String timerName;
    /**
     * This is used to indicate the start()/stop() state.
     */
    private boolean running;
    /**
     * This is used to indicate that pause() has been called without
     * a matching {@link #cont()}.
     */
    private boolean paused;
    /**
     * This is only used to show when the LapTimer was created
     */
    private long createTime;
    private long startTime;
    private transient Date startDate;
    private transient String startDateStr;
    private long endTime;
    /**
     * This is the time that the LapTimer has been running. This is the sum of all the lap() return results.
     */
    private long elapsedTime;
    /**
     * system time that is the start of the most current lap
     */
    private long lastLapTime;
    /**
     * This preserves the time spent in the current lap before a pause() occured.
     */
    private long lapOffset;
    /**
     * The lap times. This array is filled from the back to the front.
     *
     * @see #getLapHistory()
     */
    private long[] lapHistory;
    /**
     * Lap names. This array is filled from the back to the front.
     *
     * @see #getLapNames()
     */
    private String[] lapNames;
    private int lapHistoryCount;
    /**
     * Usually the same as {@link #lapHistoryCount} except if the lapCount is
     * greater than the size supplied when the LapTimer is created.
     */
    private int lapCount;
    private transient Runnable runnable;
    /**
     * This is used to do timing nesting. When the LapTimer is
     * on the JVM that created it, it is used to chain together
     * LapTimers that have been pushed on to the {@link #threadLapTimer}
     * stack. When the LapTimer is remoted this is used to chain together
     * LapTimers on the {@link #remotedLapTimers} chains.
     */
    private transient LapTimer prevStackMember;
    /**
     * This is used for a LapTimer that is over on a different
     * JVM (It has been sent). This is how the LapTimer is attached to
     * the given thread
     */
    private transient static final ThreadLocal<LapTimer> remotedLapTimers = new ThreadLocal<LapTimer>();
    /**
     * How many child LapTimers were created for this LapTimer.
     */
    private int childCount;
    /**
     * Divide all the lap times by this number. This is used
     * when each lap will span
     */
    private int denominator = 1;
    /**
     * This LapTimer has been sent remotely and thus cannot be sent again until a reply is received.
     */
    private transient boolean sent;
    /**
     * This LapTimer can be sent remotely.
     */
    private boolean remotable;
    /**
     * This counts how many times a LapTimer was sent over the wire. Divide by 2
     * to get number of round trips.
     */
    private int hopCount;
    private static final boolean DEBUG = false;
    private String pendingLapName;

    /**
     * Create a LapTimer with no name and an allowed lapHistory of 100
     * Must also be available for the serialization of LapTimers.
     *
     * @see #LapTimer(String,Runnable,int)
     */
    public LapTimer() {
        this(null, null, 100);
        this.timerName = "LapTimer#" + System.identityHashCode(this);
    }

    /**
     * Create a LapTimer with the supplied name and an allowed lapHistory of 100
     *
     * @param timerName
     * @see #LapTimer(String,Runnable,int)
     */
    public LapTimer(String timerName) {
        this(timerName, null, 100);
    }

    /**
     * Creates a new instance of LapTimer
     *
     * @param actual
     */
    public LapTimer(Runnable actual) {
        this(null, actual, 100);
        this.timerName = "LapTimer#" + System.identityHashCode(this);
    }

    /**
     * @param actual
     * @param lapHistorySize
     */
    public LapTimer(Runnable actual, int lapHistorySize) {
        this(null, actual, lapHistorySize);
        this.timerName = "LapTimer#" + System.identityHashCode(this);
    }

    /**
     * @param timerName
     * @param actual
     * @param lapHistorySize
     */
    public LapTimer(String timerName, Runnable actual, int lapHistorySize) {
        if (lapHistorySize < 2) {
            lapHistorySize = 2;
        }
        this.timerName = timerName;
        this.lapHistory = new long[lapHistorySize];
        this.lapNames = new String[lapHistorySize];
        this.runnable = actual;
        this.createTime = System.currentTimeMillis();
    }

    public void setRemotable(boolean remotable) {
        this.remotable = remotable;
    }

    /**
     * @return timerName
     */
    public String getName() {
        return this.timerName;
    }

    /**
     * @return true this LapTimer can be sent over to another VM? False by default.
     */
    public boolean isRemotable() {
        return remotable;
    }

    /**
     * When the result returned by 'Per' methods,i.e. lapPerStr, are divided
     * by this number
     *
     * @param denominator
     */
    public void setDenominator(int denominator) {
        this.denominator = denominator;
    }

    /**
     * @param runthis
     */
    public void setRunnable(Runnable runthis) {
        this.runnable = runthis;
    }

    /**
     * Starts the LapTimer running. This will reset the start timer.
     *
     * @return this
     */
    public LapTimer start() {
        if (this.isRunning()) {
            return this;
        }
        this.lastLapTime = this.startTime = System.currentTimeMillis();
        this.lapOffset = 0;
        this.running = true;
        return this;
    }

    /**
     * Like reset() but discards Runnable
     *
     * @see #reset()
     * @return this
     */
    public LapTimer clear() {
        this.runnable = null;
        return this.reset();
    }

    /**
     * Sets all information to 0 - but does not drop the Runnable.
     * Use clear() to do reset() and discard Runnable.
     * @return this
     */
    public LapTimer reset() {
        this.running = false;
        this.startTime = 0;
        this.startDate = null;
        this.startDateStr = null;
        this.endTime = 0;
        this.lastLapTime = 0;
        Arrays.fill(this.lapHistory, 0);
        this.lapHistoryCount = 0;
        this.lapCount = 0;
        this.elapsedTime = 0;
        return this;
    }

    /**
     * @return true this LapTimer has been {@link #start()}ed but not {@link #stop()}ped
     */
    public boolean isRunning() {
        return this.running;
    }

    public boolean isPaused() {
        return this.paused;
    }
    /**
     * This method in combination with {@link #end()}. A laptimer using
     * begin/end will not in general use the lap() methods. But combining
     * the two will not interfere with each other.
     * @param lapName tag marking this "lap".
     */
    public void begin(String lapName) {
        // maybe paused from previous end()
        this.cont();
        this.pendingLapName = lapName;
    }
    /**
     * Equivalent to {@link #lap(String)} where the lap name is the
     * name passed in on the {@link #begin(String)}
     *
     */
    public void end() {
        this.end(true);
    }
    public void end(boolean pauseTimer) {
        this.lap(this.pendingLapName);
        if ( pauseTimer) {
            this.pause();
        }
    }

    /**
     * get time since last call to lap (or start). This will start the lap timer if it is not already started.
     *
     * @return how long this lap was in milliseconds
     */
    public long lap() {
        if (!this.isRunning()) {
            this.start();
            return 0;
        }

        System.arraycopy(this.lapHistory, 1, this.lapHistory, 0, this.lapHistory.length - 1);
        System.arraycopy(this.lapNames, 1, this.lapNames, 0, this.lapNames.length - 1);
        long tmp = System.currentTimeMillis();
        if (!this.isPaused()) {
            this.lapHistory[this.lapHistory.length - 1] = this.lapOffset + (tmp - this.lastLapTime);
        } else {
            // if LapTimer is paused, then just the preserved lapOffset is the time of the lap
            // otherwise, we need to add in the time that has passed.
            this.lapHistory[this.lapHistory.length - 1] = this.lapOffset;
        }
        this.elapsedTime += this.lapHistory[this.lapHistory.length - 1];
        this.lapOffset = 0;
        this.lastLapTime = tmp;
        this.lapCount++;
        return this.lapHistory[this.lapHistory.length - 1];
    }
    static void sBegin(String lapName) {
        LapTimer timer;
        for (timer = LapTimer.remotedLapTimers.get(); timer != null; timer = timer.prevStackMember) {
            timer.begin(lapName);
        }
        timer = _getThreadTimer();
        if (timer != null) {
            timer.begin(lapName);
        }
    }
    static void sEnd() {
        LapTimer timer;
        for (timer = LapTimer.remotedLapTimers.get(); timer != null; timer = timer.prevStackMember) {
            timer.end();
        }
        timer = _getThreadTimer();
        if (timer != null) {
            timer.end();
        }
    }

    /**
     * This will call the current thread's LapTimer' lap(String). It will quietly do nothing is there
     * no current LapTimer, nothing is done.
     * All LapTimers that have been received by this thread from another JVM are also stamped.
     *
     * @param lapName The string that is associated with this lap.
     * @return -1 if there is no current LapTimer otherwise the results of lap(String) are returned.
     */
    public static long sLap(String lapName) {
        // TODO test performance!
        LapTimer timer;
        for (timer = LapTimer.remotedLapTimers.get(); timer != null; timer = timer.prevStackMember) {
            timer.lap(lapName);
        }
        timer = _getThreadTimer();
        if (timer == null) {
            return -1;
        } else {
            return timer.lap(lapName);
        }
    }

    /**
     * This will call the current thread's LapTimer' lap(). It will quietly do nothing is there
     * no current LapTimer, nothing is done.
     * All LapTimers that have been received by this thread from another JVM are also stamped.
     *
     * @return -1 if there is no current LapTimer otherwise the results of lap() are returned.
     */
    public static long sLap() {
        // TODO test performance!
        LapTimer timer;
        for (timer = LapTimer.remotedLapTimers.get(); timer != null; timer = timer.prevStackMember) {
            timer.lap();
        }
        timer = _getThreadTimer();
        if (timer == null) {
            return -1;
        } else {
            return timer.lap();
        }
    }

    /**
     * get time since last call to lap (or start). This will start the lap timer if it is not already started.
     *
     * @param lapName The string that is associated with this lap.
     * @return how long this lap was in milliseconds
     */
    public long lap(String lapName) {
        long time = this.lap();
        this.lapNames[this.lapNames.length - 1] = lapName;
        return time;
    }

    /**
     * get time since last call to lap (or start) as a string. This will start the lap timer if it is not already started.
     *
     * @return how long this lap was in milliseconds formated as a string with "ms " appended.
     */
    public String lapStr() {
        return lap() + "ms ";
    }

    /**
     * get time since last call to lap (or start) as a string. This will start the lap timer if it is not already started.
     *
     * @param lapName The string that is associated with this lap.
     * @return how long this lap was in milliseconds formated as a string with "ms " appended.
     */
    public String lapStr(String lapName) {
        return lap(lapName) + "ms ";
    }
    public float lapPer() {
        return lapPer(this.denominator);
    }
    public String lapPerStr() {
        return lapPerStr(denominator);
    }
    /**
     * Call lap() and return time of this lap divided by denominator
     *
     * @param denominatorOverride
     * @return time of this lap divided by denominator
     */
    public float lapPer(int denominatorOverride) {
        return ((float) this.lap()) / denominatorOverride;
    }

    public String lapPerStr(int denominatorOverride) {
        return lapPer(denominatorOverride) + "ms";
    }

    public float lapPer(String lapName, int denominatorOverride) {
        return ((float) this.lap(lapName)) / denominatorOverride;
    }

    public String lapPerStr(String lapName, int denominatorOverride) {
        return lapPer(lapName, denominatorOverride) + "ms";
    }

    /**
     * includes the time while paused or stopped() if a cont() has occurred.
     * @return elapsed time in milliseconds.
     */
    public long elapsed() {
        // total sum so far + time so far into the current lap + pause offset.
        if (this.isRunning()) {
            long t = this.elapsedTime + this.lapOffset;
            return this.isPaused() ? t : t + (System.currentTimeMillis() - this.lastLapTime);
        } else {
            return this.elapsedTime;
        }
    }

    public float elapsedPer() {
        return elapsedPer(this.denominator);
    }
    public float elapsedPer(int denominatorOverride) {
        return ((float) elapsed()) / denominatorOverride;
    }

    public String elapsedPerStr(int denominatorOverride) {
        return elapsedPer(denominatorOverride) + "ms";
    }

    /**
     * Return the time in milliseconds since the LapTimer was created
     * if the timer was started and is still running. This will be different than
     * the elapsed() time because it includes time that the LapTimer was paused.
     * Will be inaccurate if this LapTimer has been remoted to another machne.
     *
     * @return time since the LapTimer was created.
     */
    public long elapsedSinceCreate() {
        if (this.isStarted()) {
            if (this.isRunning()) {
                return System.currentTimeMillis() - this.createTime;
            } else {
                return this.endTime - this.createTime;
            }
        } else {
            // created but never started.
            return System.currentTimeMillis() - this.createTime;
        }
    }

    /**
     * Calls lap() and then stops the timer and records the end time.
     * Does nothing if the LapTimer is already stopped.
     * @param lapName final lapName.
     * @return this
     */
    public LapTimer stop(String lapName) {
        if (this.isRunning()) {
            this.lap(lapName);
            this.running = false;
            this.endTime = System.currentTimeMillis();
        }
        return this;
    }

    /**
     * @return this
     * @see #stop(String)
     */
    public LapTimer stop() {
        return this.stop(null);
    }

    /**
     * temporarily pauses the LapTimer - does not cause a lap to be done.
     * While paused the elapsed time will not increase.
     * @return this
     */
    public LapTimer pause() {
        if (!this.paused) {
            long tmp = System.currentTimeMillis();
            this.lapOffset += tmp - this.lastLapTime;
            this.paused = true;
        }
        return this;
    }

    /**
     * continue after a {@link #pause()} or a {@link #stop()}
     * If the LapTimer has not yet been {@link #start()}ed then it
     * will be.
     * @return this
     */
    public LapTimer cont() {
        if (!this.isStarted()) {
            this.start();
        } else if (!this.isRunning()) {
            this.running = true;
            this.paused = false;
            this.endTime = 0;
            this.lastLapTime = System.currentTimeMillis();
        } else if (this.isPaused()) {
            this.paused = false;
            this.lastLapTime = System.currentTimeMillis() - this.lapOffset;
            this.lapOffset = 0;
        }
        return this;
    }

    /**
     * Get a copy of lap times.
     *
     * @return an array of lap time durations
     */
    public long[] getLapHistory() {
        int size = this.getLapHistoryCount();
        long[] tmp = new long[size];
        System.arraycopy(this.lapHistory, this.lapHistory.length - size, tmp, 0, size);
        return tmp;
    }

    /**
     * Get the lap time from the previous call to lap() or stop()
     *
     * @return the last lap's time.
     */
    public long getPrevLapTime() {
        return this.lapHistory[this.lapHistory.length - 1];
    }

    /**
     *
     * @return a copy of the lap names
     */
    public String[] getLapNames() {
        int size = this.getLapHistoryCount();
        String[] tmp = new String[size];
        System.arraycopy(this.lapNames, this.lapNames.length - size, tmp, 0, size);
        return tmp;
    }

    public boolean isStarted() {
        return this.startTime != 0;
    }

    /**
     * @return number of valid entries in the getLapHistory() array
     */
    public int getLapHistoryCount() {
        return this.lapCount >= this.lapHistory.length ? this.lapHistory.length : this.lapCount;
    }

    /**
     * @return number of times that lap has been called since the most recent start()/clear()
     */
    public int getLapCount() {
        return this.lapCount;
    }

    public long getStartTime() {
        return this.startTime;
    }

    /**
     * @return the date/time that this LapTimer was started
     */
    public Date getStartDate() {
        if (this.startDate == null && this.isStarted()) {
            this.startDate = new Date(this.startTime);
        }
        return this.startDate;
    }

    /**
     * Get the human readable version of the starting time string.
     *
     * @return {@link #getStartDate()} converted to a String
     */
    public String getStartDateStr() {
        if (!this.isStarted()) {
            return "not started";
        } else if (this.startDateStr == null) {
            StringBuffer sb = new StringBuffer(15);
            MSG_DATE_FORMAT.format(this.getStartDate(), sb, new FieldPosition(0));
            this.startDateStr = sb.toString();
        }
        return this.startDateStr;
    }

    /**
     * @see #run(Runnable)
     * @see #setRunnable(Runnable)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        this.run(this.runnable);
    }

    /**
     * If this LapTimer has been started it will 'lap' around the execution
     * of the runnable. Otherwise it will start and stop the LapTimer around the runnable.
     * @param runthis runnable to time as a unit.
     */
    public void run(Runnable runthis) {
        boolean started = this.isRunning();
        if (!started) {
            this.start();
        } else {
            this.lap();
        }
        try {
            runthis.run();
        } finally {
            if (!started) {
                this.stop();
            } else {
                this.lap();
            }
        }
    }

    /**
     * Get the LapTimer associated with this thread. Create a new LapTimer if none exists.
     *
     * @return the LapTimer assigned to this thread
     */
    public static LapTimer getThreadTimer() {
        LapTimer timer = _getThreadTimer();
        if (timer == null) {
            timer = new LapTimer();
            threadLapTimer.set(timer);
        }
        return timer;
    }

    public static LapTimer getExistingThreadTimer() {
        return LapTimer.hasThreadTimer()?_getThreadTimer():null;
    }

    private static LapTimer _getThreadTimer() {
        return threadLapTimer.get();
    }

    /**
     * This method checks to see if there is a thread timer. This is
     * useful in conjunction with {#sLap(String)} as it lets a caller avoid an
     * unnecessary string creation if there is no attached timer.
     *
     * @return true if there is a thread timer.
     */
    public static boolean hasThreadTimer() {
        return threadLapTimer.get() != null;
    }

    /**
     * @return the most recent LapTimer popped off the Threadstack.
     * @see #popThreadTimer()
     */
    public static LapTimer getLastThreadTimer() {
        return lastThreadLapTimer.get();
    }

    /**
     * Disassociate this thread from any LapTimer. {@link #getThreadTimer()}
     * can be used to new LapTimer.
     */
    public static void clearThreadTimer() {
        threadLapTimer.set(null);
    }

    /**
     * push the given LapTimer on to the Threadbased stack.
     * The pushed Timer is started.
     * @param newTimer the supplied timer.
     * @return newTimer
     */
    public static LapTimer pushThreadTimer(LapTimer newTimer) {
        LapTimer timer = _getThreadTimer();
        newTimer.prevStackMember = timer;
        threadLapTimer.set(newTimer);
        newTimer.start();
        return newTimer;
    }

    /**
     * Create a new LapTimer that will be pushed on to this Thread's stack. This Thread's
     * previously attached LapTimer is saved. The previous LapTimer is restored with a call
     * to {@link #popThreadTimer()}. The new LapTimer is automatically started.
     *
     * @return a started LapTimer
     */
    public static LapTimer pushNewThreadTimer() {
        LapTimer timer = _getThreadTimer();
        String name;
        if (timer != null) {
            name = timer.timerName + '.' + (++timer.childCount);
        } else {
            name = null;
        }
        return pushThreadTimer(new LapTimer(name));
    }

    /**
     * Create a new LapTimer that will be pushed on to this Thread's stack. This Thread's
     * previously attached LapTimer is saved. The previous LapTimer is restored with a call
     * to {@link #popThreadTimer()}. The new LapTimer is automatically started.
     *
     * @param name
     * @return a started LapTimer
     */
    public static LapTimer pushNewThreadTimer(String name) {
        return pushThreadTimer(new LapTimer(name));
    }

    /**
     * This Thread's current LapTimer is stopped and popped off of the Thread's stack.
     * The previous LapTimer attached to this thread is restored. The popped LapTimer
     * is stopped (can be restarted by a call to {@link #cont()}).
     *
     * @return The LapTimer formerly attached to this thread.
     */
    public static LapTimer popThreadTimer() {
        return LapTimer.popThreadTimer(null);
    }

    /**
     * This Thread's current LapTimer is stopped and popped off of the Thread's stack.
     * The previous LapTimer attached to this thread is restored. The popped LapTimer
     * is {@link #stop(String)}'ed (can be restarted by a call to {@link #cont()}).
     *
     * @param lapName name passed to stop
     * @return The LapTimer formerly attached to this thread.
     */
    public static LapTimer popThreadTimer(String lapName) {
        LapTimer timer = _getThreadTimer();
        if (timer != null) {
            timer.stop(lapName);
            threadLapTimer.set(timer.prevStackMember);
            lastThreadLapTimer.set(timer);
        }
        return timer;
    }

    /**
     * Get the LapTimer associated with this key. If none exists, a new LapTimer is created and started.
     *
     * @param key
     * @return the LapTimer associated with this Key
     */
    public static LapTimer getKeyedTimer(Object key) {
        synchronized (keyedTimers) {
            LapTimer timer = keyedTimers.get(key);
            if (timer == null) {
                timer = new LapTimer();
                keyedTimers.put(key, timer);
                timer.start();
            }
            return timer;
        }
    }

    /**
     * remove the LapTimer associated with this key. The LapTimer is not stopped.
     *
     * @param key
     * @return The just removed LapTimer
     */
    public static LapTimer removeKeyedTimer(Object key) {
        synchronized (keyedTimers) {
            return keyedTimers.remove(key);
        }
    }

    public static Object getProxy(Object obj) {
        return ProxyWrapper.getProxyWrapper(obj);
    }

    public static Object getProxy(Object obj, LapTimer timer) {
        return ProxyWrapper.getProxyWrapper(obj, timer);
    }

    /**
     * Execute a toLapString(null) and create a nice short string.
     *
     * @return this.{@link #toLapString(String)}(null)
     *
     */
    public String toLapString() {
        return this.toLapString(null);
    }

    /**
     * Execute a lap(name) and create a nice string for printing that shows this last lap's info.
     *
     * @param name
     * @return {@link #lap(String)}
     */
    public String toLapString(String name) {
        long lapTime = this.lap(name);
        StringBuilder sb = new StringBuilder(50);
        if (this.timerName != null) {
            sb.append(this.timerName).append(':');
        }

        if (name != null) {
            sb.append('(').append(name).append(')');
        }
        sb.append("[start=").append(this.getStartDateStr()).append("; elapsed=").append(elapsed());
        sb.append("ms; last lap=").append(lapTime).append("ms]");
        return sb.toString();
    }

    /**
     * Create a nice string that shows all the information about this LapTimer
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        if (this.timerName != null) {
            sb.append(this.timerName).append(':');
        }
        sb.append("[start=").append(this.getStartDateStr());
        if (!this.isRunning()) {
            sb.append("(stopped)");
        }
        sb.append("; elapsed=").append(elapsed()).append("ms; hops=");
        sb.append(this.hopCount).append(" laps=(");
        long[] history = this.getLapHistory();
        String[] names = this.getLapNames();
        for (int i = 0; i < history.length; i++) {
            if (names[i] != null) {
                sb.append(' ').append(names[i]).append(':');
            }
            sb.append(history[i]).append(';');
        }
        sb.append(")]");
        return sb.toString();
    }

    /**
     * This returns an excel CVS line. The first cell is the name of this LapTimer.
     *
     * @return a comma separated value line.
     */
    public String toCSV() {
        StringBuffer sb = new StringBuffer(50);
        if (this.timerName != null) {
            sb.append(this.timerName);
        }
        sb.append(',');
        CSV_DATE_FORMAT.format(this.getStartDate(), sb, new FieldPosition(0));
        sb.append(',').append(this.hopCount);
        sb.append(',').append(this.elapsed());
        long[] history = this.getLapHistory();
        for (long element : history) {
            sb.append(',').append(element);
            if (this.denominator != 1.0f) {
                sb.append(',').append(element / this.denominator);
            }
        }
        return sb.toString();
    }

    /**
     * This creates a string version of the output of LapTimer.send(ObjectOutputStream)
     * @see LapTimer#send(ObjectOutputStream)
     * @see LapTimer#receiveFromString(String)
     * @return a Base64 encoded string suitable for all methods of transport
     */
    public static String sendToString() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream os;
        String s = null;
        try {
            os = new ObjectOutputStream(b);
            LapTimer.send(os);
            s = Base64.byteArrayToBase64(b.toByteArray());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;
    }

    /**
     * This method is normally called from within a Serializable's writeObject(ObjectOutputStream)
     * It should be the last method called just in case the receipient has
     * not been modified to handle LapTimers. The LapTimer will be screwed up
     * but at least nothing else will be.
     * A LapTimer may only be sent out once. After that LapTimer is paused
     * until the response is received with the data in it.
     *
     * @param out
     * @throws IOException
     */
    public static void send(ObjectOutputStream out) throws IOException {
        LapTimer timer;
        for (timer = LapTimer._getThreadTimer(); timer != null; timer = timer.prevStackMember) {
            if (timer.sent || !timer.isRemotable()) {
                // this timer has already be sent out, hopefully on this request
                continue;
            } else {
                timer.sent = true;
            }
            if (DEBUG ) {
                System.out.println("SENDING LAPTIMER="+timer);
            }
            out.writeObject(timer);
        }
        // TODO test performance!
        for (timer = LapTimer.remotedLapTimers.get(); timer != null; timer = timer.prevStackMember) {
            if (DEBUG ) {
                System.out.println("SENDING REMOTED LAPTIMER="+timer);
            }
            out.writeObject(timer);
        }
        // Once a JVM has passed off non-local LapTimers to a different JVM,
        // that other JVM needs to return them if need be. Because there
        // is no way of knowing if it is being passed to new JVM or
        // back to the originator.
        LapTimer.remotedLapTimers.set(null);

        // signals end of timers
        out.writeObject(null);
    }

    public static void receiveFromString(String s) {
        if ( s == null ) {
            return;
        }
        try {
            byte[] b = Base64.base64ToByteArray(s);
            receive(new ObjectInputStream(new ByteArrayInputStream(b)));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within a Serializable's readObject(ObjectInputStream)
     * It should be the last method called just in case the receipient has
     * not been modified to handle LapTimers. The LapTimer will be screwed up
     * but at least nothing else will be.
     *
     * @param in
     */
    public static void receive(ObjectInputStream in) {
        // we swallow exceptions because if the sender was not modified to
        // send a LapTimer, that is o.k. and should not impact the business
        // logic.

        try {
            LapTimer timer;
            for (; (timer = (LapTimer) in.readObject()) != null;) {
                // it can now be sent again.
                timer.sent = false;
                if ( DEBUG ) {
                    System.out.println("RECVING LAPTIMER="+timer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        boolean skipCheck = this.home == null;

        // check to see if this LapTimer was recieved remotely.
        // if its homeVMID was not set then this JVM/ClassLoader
        // is created it and is sending it out.
        if (skipCheck) {
            synchronized (vmid) {
                this.home = new Id(vmid, remotingCounter++);
            }
        }
        if (skipCheck || LapTimer.receivedLaptimers.remove(this.home) == null) {
            LapTimer.sentLaptimers.put(this.home, this);
        }
        boolean restart = this.isRunning();
        if (restart) {
            this.begin("XMIT");
//            this.pause(); TODO how to handle staying on same machine?
        }
        out.writeBoolean(restart);
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        boolean restart = in.readBoolean();
        in.defaultReadObject();
        this.hopCount++;
        if (restart) {
            this.cont();
            //TODO do we continue?
            this.end(); //("EndXMIT");
        }
    }

    @SuppressWarnings("unused")
    private Object readResolve() throws ObjectStreamException {
        LapTimer t = sentLaptimers.remove(this.home);
        // the server that this is coming back from may have changed it's value
        this.setRemotable(true);
        if (t == null) {
            receivedLaptimers.put(this.home, this);
            // TODO islocal should just be false
//            if ( this.islocal )
//                throw new ObjectStreamException();
            pushRemotedLapTimer(this);
            return this;
        } else {
            // this is the reply to an early send. Copy the remote results back.
            t.copyFrom(this);
            // TODO islocal should just be true
//            if ( !this.islocal )
//                throw new RuntimeException();
            return t;
        }
    }

    public static void pushRemotedLapTimer(LapTimer timer) {
        timer.prevStackMember = LapTimer.remotedLapTimers.get();
        LapTimer.remotedLapTimers.set(timer);
    }

    /**
     * @param timer
     */
    private void copyFrom(LapTimer timer) {
        this.childCount = timer.childCount;
        this.createTime = timer.createTime;
        this.denominator = timer.denominator;
        this.elapsedTime = timer.elapsedTime;
        this.endTime = timer.endTime;
        this.hopCount = timer.hopCount;
        // home is the key for maps as such it should also not be copied.
//        this.home = timer.home;
        this.lapCount = timer.lapCount;
        this.lapHistory = timer.lapHistory;
        this.lapHistoryCount = timer.lapHistoryCount;
        this.lapNames = timer.lapNames;
        this.lapOffset = timer.lapOffset;
        this.lastLapTime = timer.lastLapTime;
        this.paused = timer.paused;
        // this.prevStackMember is preserved because the prevStackMember
        // was not sent across the wire.
        this.remotable = timer.remotable;
        // this.runnable was not sent because there is no way to know
        // if the runnable was serializable.
        this.running = timer.running;
        this.startDate = timer.startDate;
        this.startDateStr = timer.startDateStr;
        this.startTime = timer.startTime;
        this.timerName = timer.timerName;
    }

    private static class Id implements Serializable {

        private static final long serialVersionUID = -4147276090981031760L;

        final int counter;
        final VMID vmidentity;


        /**
         * @param vmidentity
         * @param counter
         */
        Id(VMID vmidentity, int counter) {
            this.vmidentity = vmidentity;
            this.counter = counter;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Id) {
                return this.counter == ((Id) o).counter && vmidentity.equals(((Id) o).vmidentity);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.vmidentity.hashCode() + this.counter;
        }
    }

    /**
     * This will store a collection of LapTimers. All the lap name for the collected LapTimers
     * should be identical for the writeCVS methods to work correctly.
     *
     * @author pat
     */
    public static class Collection {
        private String collectionName;
        private final List<LapTimer> lapTimers;
        private int maxTimers;
        private Comparator<LapTimer> comparator;
        private int lapHistorySize=100;
        /**
         * All LapTimers created by this Collection are remotable.
         */
        private boolean remotable;

        /**
         * Create a collection of LapTimers with the supplied name
         *
         * @param collectionName
         */
        public Collection(String collectionName) {
            this.collectionName = collectionName;
            this.lapTimers = new ArrayList<LapTimer>();
            this.maxTimers = Integer.MAX_VALUE;
        }

        public Collection(String collectionName, int maxTimers) {
            this.collectionName = collectionName;
            this.lapTimers = new ArrayList<LapTimer>(maxTimers);
            this.maxTimers = (maxTimers > 0) ? maxTimers : 1;
        }

        /**
         * @param collectionName
         * @param maxTimers
         * @param comparator     use this comparator to always sort the list and drop the 'least'
         *                       LapTimer as defined by the comparator when adding to a full list.
         */
        public Collection(String collectionName, int maxTimers, Comparator<LapTimer> comparator) {
            this(collectionName, maxTimers);
            this.comparator = comparator;
        }
        public void setLapHistorySize(int lapHistorySize) {
            this.lapHistorySize = lapHistorySize;
        }

        public List<LapTimer> getTimers() {
            return this.lapTimers;
        }

        /**
         * This will add a LapTimer to the Collection. If there is now more than
         * the allowed number of LapTimers in the Collection, the Collection will be sorted
         * and the 'least' LapTimer will be removed as determined by the Collection's
         * comparator. The passed LapTimer will never be removed.
         *
         * @param lapTimer
         */
        public void add(LapTimer lapTimer) {
            if (this.maxTimers != Integer.MAX_VALUE && this.lapTimers.size() == this.maxTimers) {
                this.sort(this.comparator);
                for (int i = 0; i < this.lapTimers.size(); i++) {
                    LapTimer t = this.lapTimers.get(i);
                    if (!t.isRunning()) {
                        this.lapTimers.remove(i);
                        break;
                    }
                }
            }
            this.lapTimers.add(lapTimer);
        }

        /**
         * Get a new LapTimer and add it to this collection.
         *
         * @param name all LapTimers in a Collection should have a name so that
         *             they can be tracked after sorting.
         * @return the create LapTimer
         * @see LapTimer#LapTimer(String)
         */
        public LapTimer getNewTimer(String name) {
            LapTimer timer = new LapTimer(name);
            this.add(timer);
            return timer;
        }

        public LapTimer getNewTimer() {
            String name = this.collectionName + " timer #" + (this.lapTimers.size() + 1);
            return this.getNewTimer(name);
        }

        /**
         * Get a new LapTimer and add it to this collection. The timer is also assigned to this thread.
         * Use {LapTimer.popThreadTimer()}
         *
         * @param name all LapTimers in a Collection should have a name so that
         *             they can be tracked after sorting.
         * @return the create LapTimer
         * @see LapTimer#pushNewThreadTimer(String)
         */
        public LapTimer pushNewThreadTimer(String name) {
            LapTimer timer = new LapTimer(name, null, this.lapHistorySize);
            LapTimer.pushThreadTimer(timer);
            this.add(timer);
            timer.setRemotable(this.remotable);
            timer.start();
            return timer;
        }

        /**
         * The name of the LapTimer is the name of this collection combined with the number of
         * LapTimers in this collection (including this LapTimer)
         *
         * @return the new laptimer.
         * @see #pushNewThreadTimer(String)
         */
        public LapTimer pushNewThreadTimer() {
            String name = this.collectionName + " timer #" + (this.lapTimers.size() + 1);
            return this.pushNewThreadTimer(name);
        }

        public LapTimer popThreadTimer() {
            return LapTimer.popThreadTimer();
        }

        public LapTimer popThreadTimer(String lapName) {
            return LapTimer.popThreadTimer(lapName);
        }

        /**
         * Write out the stored LapTimers in Excel CVS format
         *
         * @param fileName
         * @throws IOException
         */
        public void writeCSV(String fileName) throws IOException {
            FileOutputStream f = new FileOutputStream(fileName);
            OutputStreamWriter f0 = new OutputStreamWriter(f);
            this.writeCSV(f0);
            f0.close();
            f.close();
        }

        public void writeCSV(Writer output) throws IOException {
            this.writeCSV(output, true);
        }

        /**
         * Write out the collection of LapTimers to output.  In CSV (Excel)
         * format.
         *
         * @param output    Writer to use to output CSV information
         * @param firstLine if true a header line for column names is displayed
         * @throws IOException
         */
        public void writeCSV(Writer output, boolean firstLine)
                throws IOException {
            if (firstLine) {
                LapTimer top = this.lapTimers.get(0);
                String[] names = top.getLapNames();
                output.write("LapTimerName,Start Time,Hop Count,Total Time,");
                for (String element : names) {
                    if (element != null) {
                        output.write(element);
                        output.write(',');
                    }
//                    output.write("Averaged Time,");
                }
                output.write("\n");
            }
            for (LapTimer lt : this.lapTimers) {
                output.write(lt.toCSV());
                output.write('\n');
            }
        }

        /**
         * Create the CVS format into a string
         *
         * @return the CVS formated string
         * @see #writeCSV(Writer)
         */
        public String toCSV() {
            try {
                StringWriter sw = new StringWriter();
                this.writeCSV(sw);
                return sw.toString();
            } catch (IOException e) {
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(this.collectionName);
            sb.append(":[");
            for (LapTimer lapTimer : this.lapTimers) {
                sb.append(lapTimer.toString()).append('\n');
            }
            sb.append(']');
            return sb.toString();
        }

        /**
         * Sort the list of LapTimers. Does not sort the stored list.
         *
         * @param comp
         * @return sorted list of LapTimers
         */
        public List<LapTimer> sort(Comparator<LapTimer> comp) {
            // need to make copy so that the use of maxTimers
            // continues to work correctly by dropping oldest
            ArrayList<LapTimer> timers = new ArrayList<LapTimer>(this.lapTimers);
            Collections.sort(timers, comp);
            return timers;
        }

        /**
         * Sort the LapTimers based on the Comparator passed into the
         * constructor.
         *
         * @return sorted list of laptimers
         */
        public List<LapTimer> sort() {
            return sort(this.comparator);
        }

        /**
         * Clear the list of stored laptimers.
         */
        public void clear() {
            this.lapTimers.clear();
        }

        public void setRemotable(boolean remotable) {
            this.remotable = remotable;
        }

        public boolean isRemotable() {
            return remotable;
        }
    }

    /**
     * This comparator sorts LapTimers by how long the lap indicated took.
     *
     * @author pat
     */
    public static class LongestLapComparator implements Comparator<LapTimer> {
        private int index;

        /**
         * @param index which lap are we sorting on.
         */
        LongestLapComparator(int index) {
            this.index = index;
        }

        public int compare(LapTimer lt0, LapTimer lt1) {
            if (lt0.lapHistory[this.index] < lt1.lapHistory[this.index]) {
                return -1;
            } else if (lt0.lapHistory[this.index] > lt1.lapHistory[this.index]) {
                return 1;
            } else {
                return 0;
            }
        }

    }

    public static class LongestRunComparator implements Comparator<LapTimer> {

        /**
         * @param lt0
         * @param lt1
         * @return {@link java.util.Comparator#compare(java.lang.Object, java.lang.Object)}
         *
         */
        public int compare(LapTimer lt0, LapTimer lt1) {
            if (lt0.elapsed() < lt1.elapsed()) {
                return -1;
            } else if (lt0.elapsed() > lt1.elapsed()) {
                return 1;
            }
            return 0;
        }
    }


    public static class LTMap {
        private Map<String, LapTimer> laptimers = Collections.synchronizedMap(new HashMap<String, LapTimer>());
        public LapTimer get(String key) {
            LapTimer t= laptimers.get(key);
            if (t == null) {
                t=new LapTimer(key);
                this.set(t);
            }
            return t;
        }
        public void set(LapTimer timer) {
            laptimers.put(timer.getName(), timer);
        }
    }
    private static class ProxyWrapper implements InvocationHandler {
        private Object real;
        private LapTimer lapTimer;
        private String className;

        private ProxyWrapper(String className) {
            this.className = className;
        }
        @SuppressWarnings("unchecked")
        public static Object getProxyWrapper(Class clazz) {
            if (clazz == null) {
                return null;
            }
            String className = clazz.getName()+'.';
            ClassLoader cl = clazz.getClassLoader();
            ArrayList<Class> l = new ArrayList<Class>();
            while(clazz != null ) {
                Class[]ifaces = clazz.getInterfaces();
                for (Class element : ifaces) {
                    l.add(element);
                }
                clazz = clazz.getSuperclass();
            }
            Class[] interfaces = l.toArray(new Class[l.size()]);
            return Proxy.newProxyInstance(cl, interfaces, new ProxyWrapper(className));
        }

        public static Object getProxyWrapper(Object obj) {
            if (obj == null) {
                return null;
            }
            Object o = getProxyWrapper(obj.getClass());
            ProxyWrapper p = (ProxyWrapper) Proxy.getInvocationHandler(o);
            p.setRealObj(obj);
            return o;
        }

        public static Object getProxyWrapper(Object obj, LapTimer lapTimer) {
            ProxyWrapper o = (ProxyWrapper) getProxyWrapper(obj.getClass());
            ProxyWrapper p = (ProxyWrapper) Proxy.getInvocationHandler(o);
            p.setRealObj(obj);
            p.setLapTimer(lapTimer);
            return o;
        }

        /**
         * @param obj
         */
        private void setRealObj(Object obj) {
            this.real = obj;
        }

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        @SuppressWarnings("unused")
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            if (this.lapTimer != null) {
                this.lapTimer.begin(this.className+method.getName());
            } else {
                sBegin(this.className+method.getName());
            }

            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e ) {
                // unpeel the wrapping and throw the real exception
                throw e.getTargetException();
            } finally {
                if (this.lapTimer != null) {
                    this.lapTimer.end();
                } else {
                    sEnd();
                }
            }
        }

        /**
         * @return Returns the lapTimer.
         */
        public LapTimer getLapTimer() {
            return lapTimer;
        }

        /**
         * @param lapTimer The lapTimer to set.
         */
        public void setLapTimer(LapTimer lapTimer) {
            this.lapTimer = lapTimer;
        }
    }

    private static class Base64 {
        private static byte[] base64ToByteArray(String s) {
            byte[] alphaToInt = base64ToInt;
            int sLen = s.length();
            int numGroups = sLen / 4;
            if (4 * numGroups != sLen) {
                throw new IllegalArgumentException("String length must be a multiple of four.");
            }
            int missingBytesInLastGroup = 0;
            int numFullGroups = numGroups;
            if (sLen != 0) {
                if (s.charAt(sLen - 1) == '=') {
                    missingBytesInLastGroup++;
                    numFullGroups--;
                }
                if (s.charAt(sLen - 2) == '=') {
                    missingBytesInLastGroup++;
                }
            }
            byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];

            // Translate all full groups from base64 to byte array elements
            int inCursor = 0, outCursor = 0;
            for (int i = 0; i < numFullGroups; i++) {
                int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
                int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
                int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
                int ch3 = base64toInt(s.charAt(inCursor++), alphaToInt);
                result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
                result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
                result[outCursor++] = (byte) ((ch2 << 6) | ch3);
            }

            // Translate partial group, if present
            if (missingBytesInLastGroup != 0) {
                int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
                int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
                result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

                if (missingBytesInLastGroup == 1) {
                    int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
                    result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
                }
            }
            // assert inCursor == s.length()-missingBytesInLastGroup;
            // assert outCursor == result.length;
            return result;
        }

        private static String byteArrayToBase64(byte[] a) {
            int aLen = a.length;
            int numFullGroups = aLen / 3;
            int numBytesInPartialGroup = aLen - 3 * numFullGroups;
            int resultLen = 4 * ((aLen + 2) / 3);
            StringBuffer result = new StringBuffer(resultLen);
            char[] intToAlpha = intToBase64;

            // Translate all full groups from byte array elements to Base64
            int inCursor = 0;
            for (int i = 0; i < numFullGroups; i++) {
                int byte0 = a[inCursor++] & 0xff;
                int byte1 = a[inCursor++] & 0xff;
                int byte2 = a[inCursor++] & 0xff;
                result.append(intToAlpha[byte0 >> 2]);
                result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
                result.append(intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
                result.append(intToAlpha[byte2 & 0x3f]);
            }

            // Translate partial group if present
            if (numBytesInPartialGroup != 0) {
                int byte0 = a[inCursor++] & 0xff;
                result.append(intToAlpha[byte0 >> 2]);
                if (numBytesInPartialGroup == 1) {
                    result.append(intToAlpha[(byte0 << 4) & 0x3f]);
                    result.append("==");
                } else {
                    // assert numBytesInPartialGroup == 2;
                    int byte1 = a[inCursor++] & 0xff;
                    result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
                    result.append(intToAlpha[(byte1 << 2) & 0x3f]);
                    result.append('=');
                }
            }
            // assert inCursor == a.length;
            // assert result.length() == resultLen;
            return result.toString();
        }

        /**
         * This array is a lookup table that translates 6-bit positive integer
         * index values into their "Base64 Alphabet" equivalents as specified
         * in Table 1 of RFC 2045.
         */
        private static final char intToBase64[] = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
        };

        /**
         * Translates the specified character, which is assumed to be in the
         * "Base 64 Alphabet" into its equivalent 6-bit positive integer.
         *
         * @throw IllegalArgumentException or ArrayOutOfBoundsException if
         * c is not in the Base64 Alphabet.
         */
        private static int base64toInt(char c, byte[] alphaToInt) {
            int result = alphaToInt[c];
            if (result < 0) {
                throw new IllegalArgumentException("Illegal character " + c);
            }
            return result;
        }

        /**
         * This array is a lookup table that translates unicode characters
         * drawn from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045)
         * into their 6-bit positive integer equivalents.  Characters that
         * are not in the Base64 alphabet but fall within the bounds of the
         * array are translated to -1.
         */
        private static final byte base64ToInt[] = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
            55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
            5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
            24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
        };
    }
}
