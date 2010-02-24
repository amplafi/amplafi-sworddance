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

package com.sworddance.util.perf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sworddance.scheduling.TimeServer;
import com.sworddance.scheduling.TimeServerImpl;
import com.sworddance.util.ConcurrentInitializedMap;
import com.sworddance.util.InitializeWithList;

/**
 * Stores the ThreadHistory objects by {@link Thread#getId()}
 *
 */
public class ThreadHistoryTracker implements Serializable {
    /**
     * Map<Thread.getId(), List ( most recent first )>
     * Should use Stack but stack add to end, will need to reverse order when copying before can use stack
     *
     * Stack SHOULD be used as the list because:
     * stack is synchronized, usually only one thread is accessing the stack ( to add to it )
     */
    private ConcurrentMap<Long, List<ThreadHistory>> history = new ConcurrentInitializedMap<Long, List<ThreadHistory>>(new InitializeWithList<ThreadHistory>());

    private AtomicInteger sequence = new AtomicInteger(0);

    private transient TimeServer timeServer;

    /**
     *
     */
    public ThreadHistoryTracker() {
        this(new TimeServerImpl());
    }
    public ThreadHistoryTracker(TimeServer timeServer) {
        this.timeServer = timeServer;
    }

    public void addStartHistory(String taskName, String status, String note) {
        Thread t = Thread.currentThread();
        // TODO: use System.identityHashCode() getId() may be reused.
        Long threadId = t.getId();
        long currentTimeMillis = getTimeInMilliseconds();
        addStartHistory(currentTimeMillis, threadId, taskName, status, note);
    }

    /**
     * @return
     */
    private long getTimeInMilliseconds() {
        return timeServer.currentTimeMillis();
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
        long currentTimeMillis = getTimeInMilliseconds();
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

    public void addHistoryStatus(String taskName, String status, String note) {
        Thread t = Thread.currentThread();
        Long threadId = t.getId();
        long currentTimeMillis = getTimeInMilliseconds();
        addHistoryStatus(currentTimeMillis, threadId, taskName, status, note);
    }

    /**
     * add a time entry not for this thread. Should only be used at significant
     * points. Use primarily to help justify any effort to break up a task into
     * smaller pieces.
     * @param currentTimeMillis
     * @param threadId
     * @param taskName
     * @param status
     * @param note TODO
     */
    public void addHistoryStatus(long currentTimeMillis, Long threadId,
            String taskName, String status, String note) {
        ThreadHistory threadHistory = new ThreadHistory(currentTimeMillis,
                threadId, taskName, status, null, Boolean.TRUE, this.sequence.incrementAndGet());
        addHistoryStatus(threadId, threadHistory);
    }

    /**
     * @param threadId
     * @return
     */
    private List<ThreadHistory> getHistory(Long threadId) {
        List<ThreadHistory> l = history.get(threadId);
        return l;
    }

    /**
     * @return the set of thread Ids with ThreadHistory
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
        if( history.containsKey(threadId)) {
            List<ThreadHistory> list = history.get(threadId);
            synchronized (list) {
                return new ArrayList<ThreadHistory>(list);
            }
        } else {
            return null;
        }
    }
}
