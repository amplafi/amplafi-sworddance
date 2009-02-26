/**
 *
 */
package com.sworddance.util.perf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pmoore
 *
 */
public class ThreadHistoryTracker {
    private ConcurrentMap<Long, List<ThreadHistory>> history = new ConcurrentHashMap<Long, List<ThreadHistory>>();

    private AtomicInteger sequence = new AtomicInteger(0);

    /**
     *
     */
    public ThreadHistoryTracker() {
        super();
    }

    public void addStartHistory(String taskName, String status, String note) {
        Thread t = Thread.currentThread();
        // TODO: use System.identityHashCode() getId() may be reused.
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
        ThreadHistory threadHistory = new ThreadHistory(currentTimeMillis, threadId, taskName,
                status, note, Boolean.TRUE, this.sequence.incrementAndGet());
        addHistoryStatus(threadId, threadHistory);
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
     * @param sequentialTimeInMillis
     * @param currentTimeMillis
     * @param threadId
     * @param taskName
     * @param status
     * @param note
     */
    void addStopHistory(long currentTimeMillis, Long threadId,
            String taskName, String status, String note, long sequentialTimeInMillis) {
        ThreadHistory threadHistory = new ThreadHistory(currentTimeMillis,
                threadId, taskName, status, note, Boolean.FALSE, sequentialTimeInMillis, this.sequence.incrementAndGet());
        addHistoryStatus(threadId, threadHistory);
    }

    /**
     * @param threadId
     * @param threadHistory
     */
    public void addHistoryStatus(Long threadId, ThreadHistory threadHistory) {
        List<ThreadHistory> l = getHistory(threadId);
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
        ThreadHistory threadHistory = new ThreadHistory(currentTimeMillis,
                threadId, taskName, status, null, Boolean.TRUE, this.sequence.incrementAndGet());
        addHistoryStatus(threadId, threadHistory);
    }

    /**
     * @param threadId
     * @return
     */
    private List<ThreadHistory> getHistory(Long threadId) {
        history.putIfAbsent(threadId, new CopyOnWriteArrayList<ThreadHistory>());
        List<ThreadHistory> l = history.get(threadId);
        return l;
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
     * @return a copy of history of this thread. null if no information
     *         available.
     */
    public List<ThreadHistory> getThreadHistoryCopy(Long threadId) {
        return history.containsKey(threadId)?new ArrayList<ThreadHistory>(history.get(threadId)):null;
    }
}
