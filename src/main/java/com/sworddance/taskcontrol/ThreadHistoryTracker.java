/**
 *
 */
package com.sworddance.taskcontrol;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pmoore
 *
 */
public class ThreadHistoryTracker {
    private Map<Long, List<ThreadHistory>> history = new ConcurrentHashMap<Long, List<ThreadHistory>>();

    private AtomicInteger sequence = new AtomicInteger(0);

    /**
     *
     */
    public ThreadHistoryTracker() {
        super();
    }

    public void addStartHistory(String taskName, String status, String note) {
        Thread t = Thread.currentThread();
        Long threadId = t.getId();
        long currentTimeMillis = System.currentTimeMillis();
        addStartHistory(currentTimeMillis, threadId, taskName, status, note);
    }

    /**
     * really just for testing.
     * @param currentTimeMillis
     * @param threadId
     * @param taskName
     * @param status
     * @param note
     */
    public void addStartHistory(long currentTimeMillis, Long threadId,
            String taskName, String status, String note) {
        List<ThreadHistory> l = getHistory(threadId);
        l.add(0, new ThreadHistory(currentTimeMillis, threadId, taskName,
                status, note, Boolean.TRUE));
    }

    public void addStopHistory(String taskName, String status, String note,
            long sequentialTime) {
        Thread t = Thread.currentThread();
        Long threadId = t.getId();
        long currentTimeMillis = System.currentTimeMillis();
        addStopHistory(currentTimeMillis, threadId, taskName, status, note,
                sequentialTime);
    }

    /**
     * really just for testing.
     *
     * @param sequentialTime
     * @param currentTimeMillis
     * @param threadId
     * @param taskName
     * @param status
     * @param note
     */
    public void addStopHistory(long currentTimeMillis, Long threadId,
            String taskName, String status, String note, long sequentialTime) {
        List<ThreadHistory> l = getHistory(threadId);
        ThreadHistory threadHistory = new ThreadHistory(currentTimeMillis,
                threadId, taskName, status, note, Boolean.FALSE);
        threadHistory.setSequentialTime(sequentialTime);
        l.add(0, threadHistory);
    }

    public void addHistoryStatus(String taskName, String status) {
        Thread t = Thread.currentThread();
        Long threadId = t.getId();
        long currentTimeMillis = System.currentTimeMillis();
        addHistoryStatus(currentTimeMillis, threadId, taskName, status);
    }

    /**
     * add a time entry not for this thread. Should only be used at significant
     * points. Use primarily to help justify any effort to break up a task into
     * smaller pieces.
     * @param currentTimeMillis
     * @param threadId
     * @param taskName
     * @param status
     */
    public void addHistoryStatus(long currentTimeMillis, Long threadId,
            String taskName, String status) {
        List<ThreadHistory> l = getHistory(threadId);
        ThreadHistory threadHistory = new ThreadHistory(currentTimeMillis,
                threadId, taskName, status, null, Boolean.TRUE);
        l.add(0, threadHistory);
    }

    /**
     * @param threadId
     * @return
     */
    private List<ThreadHistory> getHistory(Long threadId) {
        List<ThreadHistory> l = history.get(threadId);
        if (l == null) {
            l = new CopyOnWriteArrayList<ThreadHistory>();
            history.put(threadId, l);
        }
        return l;
    }

    /**
     * a record of a thread status change.
     * @author Patrick Moore
     */
    class ThreadHistory {
        long timestamp;

        Long threadId;

        String taskName;

        String status;

        String note;

        /**
         *  null -- no change in status, false = task stopping.
         */
        Boolean threadInUse;

        private final int sequenceId = ThreadHistoryTracker.this.sequence.incrementAndGet();

        private long sequentialTime;

        ThreadHistory(long timestamp, Long threadId, String taskName,
                String status, String note, Boolean threadInUse) {
            this.timestamp = timestamp;
            this.threadId = threadId;
            this.taskName = taskName;
            this.status = status;
            this.note = note;
            this.threadInUse = threadInUse;
        }

        /**
         * @param sequentialTime the sequentialTime to set
         */
        public void setSequentialTime(long sequentialTime) {
            this.sequentialTime = sequentialTime;
        }

        /**
         * @return the sequentialTime
         */
        public long getSequentialTime() {
            return sequentialTime;
        }

        /**
         * @return the sequence
         */
        public int getSequenceId() {
            return sequenceId;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(40);
            sb.append(timestamp).append(":").append(threadId).append(taskName)
                    .append(status);
            return sb.toString();
        }
    }

    /**
     * @return the set of threads with history storied here at some point in
     *         time.
     */
    public Set<Long> getThreadIds() {
        return Collections.unmodifiableSet(history.keySet());
    }

    /**
     * @param threadId
     * @return a thread-safe history of this thread. null if no information
     *         available.
     */
    public List<ThreadHistory> getThreadHistory(Object threadId) {
        return history.get(threadId);
    }
}
