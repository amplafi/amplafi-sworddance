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
package com.sworddance.taskcontrol;

import java.util.Iterator;
import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import static java.util.concurrent.TimeUnit.*;

/**
 * A general thread pool that can be used.
 *
 * Maybe replace with {@link java.util.concurrent.Executors}
 *
 * @author pmoore
 *
 */
public class ThreadFactoryImpl implements ThreadFactory {
    private final List<Thread> threads = new CopyOnWriteArrayList<Thread>();

    private CountDownLatch noMoreThreads = new CountDownLatch(1);

    public static final ThreadFactoryImpl POOL = new ThreadFactoryImpl();

    public ThreadFactoryImpl() {
        super();
    }

    /**
     * @param runnable
     * @return a thread that will be only running the passed in task. The
     * runnable will be monitored for it's state.
     */
    public Thread startDedicatedThread(Runnable runnable) {
        Thread t = newThread(runnable);
        t.start();
        return t;
    }

    /**
     * Thread is not started.
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    public Thread newThread(Runnable command) {
        try {
            if (noMoreThreads.await(0, MILLISECONDS)) {
                throw new IllegalStateException(
                        "ThreadPool has been told to stop creating threads");
            }
        } catch (InterruptedException e) {
        }
        Thread t = new Thread(command);
        t.setDaemon(true);
        registerThread(t);
        return t;
    }
    public Thread newThread(String threadName, Runnable command) {
        Thread t = newThread(command);
        t.setName(threadName);
        return t;
    }
    /**
     * @param t
     */
    private void registerThread(Thread t) {
        getThreads().add(t);
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public void removeDeadThreads() {
        for (Iterator<Thread> iter = threads.iterator(); iter.hasNext();) {
            Thread t = iter.next();
            if (!t.isAlive()) {
                iter.remove();
            }
        }
    }

    /**
     *
     */
    public void shutDownNow() {
        noMoreThreads.countDown();
    }
}
