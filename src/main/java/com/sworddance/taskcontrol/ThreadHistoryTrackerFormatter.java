/**
 *
 */
package com.sworddance.taskcontrol;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sworddance.taskcontrol.ThreadHistoryTracker.ThreadHistory;

/**
 * @author pmoore
 *
 */
public class ThreadHistoryTrackerFormatter implements Iterator {
    /**
     *
     */
    private static final String THREAD_INACTIVE = "-";

    /**
     *
     */
    private static final String THREAD_ACTIVE = "|";

    /**
     *
     */
    protected static final int TIME_COL = 0;

    protected static final int NOTES_COL = 1;

    protected static final int ACTIVE_THREADS_COL = 2;

    protected static final int SEQUENTIAL_TIME = 3;

    protected static final int DELTA_TIME_COL = 4;

    private static final int EXTRA_COLUMNS = 5;

    protected int maxThreads = 5;

    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    private HashMap<Long, List<ThreadHistory>> history;

    private List<ThreadHistory> nextEntries;

    private List<Iterator<?>> iterators;

    private List<Boolean> threadStatus;

    private boolean headerOutputed;

    private String[] outputStrings;

    protected Map<String, long[]> timeMap = new HashMap<String, long[]>();

    private int previousActiveThreads;

    private Set<String> activeTaskNames = new HashSet<String>();

    ThreadHistoryTrackerFormatter(ThreadHistoryTracker tracker) {
        history = new HashMap<Long, List<ThreadHistory>>();
        // deep copy of history in a thread safe manner
        Set<Long> threadIds = tracker.getThreadIds();
        for (Long threadId: threadIds) {
            history.put(threadId, tracker.getThreadHistory(threadId));
        }
        nextEntries = new ArrayList<ThreadHistory>(history.values().size());
        iterators = new ArrayList<Iterator<?>>(history.values().size());
        threadStatus = new ArrayList<Boolean>(history.values().size());
        outputStrings = new String[history.values().size() + EXTRA_COLUMNS];
        // prime iterators
        for (Iterator<List<ThreadHistory>> iter = history.values().iterator(); iter
                .hasNext();) {
            Iterator<?> iterator = iter.next().iterator();
            iterators.add(iterator);
            if (iterator.hasNext()) {
                ThreadHistory next = (ThreadHistory) iterator.next();
                if (next.threadInUse != null) {
                    threadStatus.add(next.threadInUse);
                } else {
                    // TODO for now assume that it is active since messages
                    // are only issued by active threads
                    threadStatus.add(Boolean.TRUE);
                }
                nextEntries.add(next);
            } else {
                threadStatus.add(Boolean.FALSE);
                nextEntries.add(null);
            }
        }
    }

    public boolean hasNext() {
        if (headerOutputed) {
            return isMore();
        } else {
            return true;
        }
    }

    public Object next() {
        if (!headerOutputed) {
            headerOutputed = true;
            outputStrings[TIME_COL] = "Time";
            outputStrings[NOTES_COL] = "Notes";
            outputStrings[SEQUENTIAL_TIME] = "Sequential Time";
            outputStrings[ACTIVE_THREADS_COL] = "Active Threads";
            outputStrings[DELTA_TIME_COL] = "Delta Time";
            for (int i = 0; i < history.values().size(); i++) {
                outputStrings[EXTRA_COLUMNS + i] = "Thread #" + i;
            }
            return outputStrings;
        } else if (isMore()) {
            int newest = getNextEntryIndex();
            ThreadHistory newestHistory = nextEntries.get(newest);
            // check to see if there is any earlier ThreadHistory for this
            // thread
            if (iterators.get(newest).hasNext()) {
                ThreadHistory next = (ThreadHistory) iterators.get(newest)
                        .next();
                nextEntries.set(newest, next);
                /*
                 * if going back in time we have a ThreadHistory that indicates
                 * that a task has started then that means that before this the
                 * thread was not doing any thing.
                 */
                if (next.threadInUse != null) {
                    threadStatus.set(newest, next.threadInUse);
                }
            } else {
                // no earlier history for this thread.
                if (newestHistory.threadInUse != null) {
                    // if last history was completing a task then
                    // that means that earlier the thread was idle.
                    threadStatus
                            .set(
                                    newest,
                                    newestHistory.threadInUse == Boolean.FALSE ? Boolean.TRUE
                                            : Boolean.FALSE);
                }
                nextEntries.set(newest, null);
            }
            // create
            outputStrings[TIME_COL] = format.format(new Date(
                    newestHistory.timestamp));
            outputStrings[NOTES_COL] = newestHistory.note != null ? newestHistory.note
                    : "";
            outputStrings[SEQUENTIAL_TIME] = Long
                    .toString(newestHistory.getSequentialTime());
            int outputStringIndex = EXTRA_COLUMNS;
            int activeThreads = 0;
            // output character indicating thread status until the column
            // corresponding
            // to the thread that changed state.
            for (int i = 0; i < newest; i++) {
                boolean threadActive = threadStatus.get(i).booleanValue();
                if (threadActive) {
                    activeThreads++;
                }
                outputStrings[outputStringIndex++] = threadActive ? THREAD_ACTIVE
                        : THREAD_INACTIVE;
            }
            outputStrings[outputStringIndex++] = newestHistory.taskName + ":"
                    + newestHistory.status;
            if (newestHistory.threadInUse.booleanValue()) {
                activeThreads++;
                this.activeTaskNames.remove(newestHistory.taskName);
            } else {
                this.activeTaskNames.add(newestHistory.taskName);
            }
            for (int i = newest + 1; i < nextEntries.size(); i++) {
                boolean threadActive = threadStatus.get(i).booleanValue();
                if (threadActive) {
                    activeThreads++;
                }
                outputStrings[outputStringIndex++] = threadActive ? THREAD_ACTIVE
                        : THREAD_INACTIVE;
            }
            outputStrings[ACTIVE_THREADS_COL] = Integer.toString(activeThreads);
            long deltaTime;
            if (isMore()) {
                int nextEntry = getNextEntryIndex();
                long nextTime = nextEntries.get(nextEntry).timestamp;
                deltaTime = newestHistory.timestamp - nextTime;
                outputStrings[DELTA_TIME_COL] = Long.toString(deltaTime);
            } else {
                // beginning of run
                outputStrings[DELTA_TIME_COL] = "0";
                deltaTime = 0;
            }
            int timeIndex;
            if (activeThreads == 0
                    || (activeThreads == 1 && previousActiveThreads == 1)) {
                timeIndex = 0;
            } else {
                timeIndex = 1;
            }
            previousActiveThreads = activeThreads;
            for (Iterator<String> iter = this.activeTaskNames.iterator(); iter
                    .hasNext();) {
                String taskName = iter.next();
                long[] time = timeMap.get(taskName);
                if (time == null) {
                    time = new long[2];
                    timeMap.put(taskName, time);
                }
                time[timeIndex] += deltaTime;
            }
            previousActiveThreads = activeThreads;
        }
        return outputStrings;
    }

    private int getNextEntryIndex() {
        int newest = 0;
        ThreadHistory newestHistory = null;
        for (int i = 0; i < nextEntries.size(); i++) {
            final ThreadHistory threadHistory = nextEntries.get(i);
            if (threadHistory != null) {
                if (newestHistory == null) {
                    newest = i;
                    newestHistory = nextEntries.get(newest);
                } else if ((threadHistory.timestamp > newestHistory.timestamp)
                        || ((threadHistory.timestamp == newestHistory.timestamp) && (threadHistory.getSequenceId() > newestHistory.getSequenceId()))) {
                    newest = i;
                    newestHistory = nextEntries.get(newest);
                }
            }
        }
        return newest;
    }

    /**
     * @return
     */
    private boolean isMore() {
        for (Iterator<ThreadHistory> iter = nextEntries.iterator(); iter
                .hasNext();) {
            if (iter.next() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException("TODO");
    }
}
