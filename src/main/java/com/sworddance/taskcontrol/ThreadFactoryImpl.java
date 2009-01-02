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
