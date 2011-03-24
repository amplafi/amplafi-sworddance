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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sworddance.util.perf.LapTimer;

import static java.util.concurrent.TimeUnit.*;

/**
 * All tasks in TaskControl should extend or emulate this class's behavior.
 *
 * @author pmoore
 * @param <R> callable result type
 *
 */
public class DefaultPrioritizedTask<R> implements PrioritizedTask, Callable<R> {
    private TaskGroup<?> taskGroup;

    private final CountDownLatch shouldRun = new CountDownLatch(1);

    private final Runnable wrappedRunnable;

    protected NotificationObject notification;

    private int sequenceId;

    private final Callable<? extends R> wrappedCallable;

    private final Integer priority;

    private String status;

    private FutureResultImplementor<R> result = new FutureResultImpl<R>();

    private String name;

    // should not create until in thread that will be running the task. (cannot create on construction)
    private LapTimer lapTimer;

    private Set<ResourceLock> resourceLocksNeeded = new CopyOnWriteArraySet<ResourceLock>();

    /**
     * This is used to signal which resourceLocks where actually used. This is
     * used to signal that the task was overly greeding in asking for locks. So
     * tasks that follow later that perhaps run only if a resource is actually
     * modified.
     *
     * do not access directly some subclasses delegate to wrapped task.
     */
    private Map<String, Integer> resourceLocksUsed = new HashMap<String, Integer>();

    private boolean lockDowngradeEnabled;

    public DefaultPrioritizedTask() {
        this((Runnable) null);
    }

    public DefaultPrioritizedTask(Runnable wrapped) {
        super();
        wrappedRunnable = wrapped;
        wrappedCallable = null;
        priority = Integer.valueOf(Thread.NORM_PRIORITY);
        initResourceLocker(wrapped);
    }

    public DefaultPrioritizedTask(Callable<? extends R> callable) {
        this(callable.getClass().getName(), callable);
    }
    public DefaultPrioritizedTask(String name, Callable<? extends R> callable) {
        super();
        wrappedCallable = callable;
        wrappedRunnable = null;
        priority = Integer.valueOf(Thread.NORM_PRIORITY);
        initResourceLocker(callable);
        setName(name);
    }


    public DefaultPrioritizedTask(Runnable wrapped, int priority) {
        super();
        wrappedRunnable = wrapped;
        wrappedCallable = null;
        this.priority = Integer.valueOf(priority);
        initResourceLocker(wrapped);
    }

    /**
     * @param name
     * @param runnable
     * @param priority
     */
    public DefaultPrioritizedTask(String name, Runnable runnable, int priority) {
        this(runnable, priority);
        setName(name);
    }

    public synchronized void setNotification(NotificationObject notification) {
        if (this.notification == null) {
            this.notification = notification;
            this.sequenceId = notification.getSequence();
        } else {
            throw new IllegalStateException("already assigned to a TaskControl");
        }
    }

    private void initResourceLocker(Object wrapped) {
        if (wrapped instanceof ResourceLocker) {
            setResourceLocksNeeded(((ResourceLocker) wrapped)
                    .getResourceLocksNeeded());
        }
    }

    public boolean isReadyToRun() {
        try {
            return shouldRun.await(0, MILLISECONDS) && !isDone();
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void releaseToRun() {
        shouldRun.countDown();
    }

    public boolean isSuccessful() {
        return isDone() && this.result.getException() == null;
    }
    // 23 mar 2011 why was this needed. Just for tests?
    protected void setSuccess() {
        if ( !this.result.isDone()) {
            this.result.set(null);
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.result.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return this.result.isCancelled();
    }
    protected String getTimingString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getElapsedInMillis());
        sb.append("ms elapsed. Start=");
        sb.append(this.lapTimer.getStartDateStr());
        sb.append(" End=");
        sb.append(this.lapTimer.getStartDate());
        return sb.toString();
    }

    /**
     *
     * @return total elapsed time in Millis
     */
    public long getElapsedInMillis() {
        return lapTimer.elapsed();
    }

    public R call() throws Exception {
        String threadName = Thread.currentThread().getName();
        startTiming();
        try {
            assertOkToRun();
            if (getName() != null) {
                Thread.currentThread().setName(getName());
            }
            result.set(callBody());
            // let exceptions escape so that is done flag is not set.
            setSuccess();
            return result.get();
        } catch (Exception e) {
            setException(e);
            throw e;
        } catch (Error e) {
            setException(e);
            throw e;
        } catch (Throwable e) {
            setException(e);
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setName(threadName);
            finishTiming();
            if (!isSuccessful()) {
                setStatus("exception thrown: " + getException());
            } else {
                setSuccessStatus();
            }
            downgradeUsedLocks();
        }
    }

    /**
     * cannot add any additional functionality beyond that offered in call().
     *
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        try {
            this.call();
        } catch (Throwable ex) {
            // don't need to do more with this because error
            // attached to the taskGroup.
        }
    }

    protected void startTiming() {
        if (lapTimer == null) {
            this.lapTimer = LapTimer.pushNewStartedThreadTimer();
            lapTimer.start();
        }
    }

    protected void finishTiming() {
        if (lapTimer != null) {
            this.lapTimer.lap("end "+this.getName());
            LapTimer.popThreadTimer(this.lapTimer);
        }
    }

    /**
     * subclasses should override this method.
     *
     */
    protected R callBody() throws Exception {
        R returnResult;
        if (getWrappedCallable() != null) {
            returnResult = getWrappedCallable().call();
        } else if (wrappedRunnable != null) {
            wrappedRunnable.run();
            returnResult = null;
        } else {
            throw new IllegalStateException("No wrapped runnable to run!");
        }
        return returnResult;
    }

    protected void setSuccessStatus() {
        setStatus("Successful.  " + getTimingString());
    }

    /**
     * Called from within call() to verify that the state of this instance still
     * means it should be run. Should be called by all subclasses that override
     * call().
     *
     * @throws InterruptedException
     * @throws IllegalStateException
     *             if this instance is in a non-runnable state could be sign of
     *             sever error.
     */
    protected void assertOkToRun() throws InterruptedException {
        if (!isReadyToRun()) {
            throw new IllegalStateException(
                    "Trying to run but not ready to run. isDone()="
                            + isSuccessful() + " shouldRun="
                            + shouldRun.await(0, MILLISECONDS) + " readyToRun="
                            + isReadyToRun());
        }
    }

    public int getSequence() {
        return sequenceId;
    }

    public int getPriority() {
        return priority == null ? Thread.NORM_PRIORITY : priority.intValue();
    }

    public String getStatus() {
        return status;
    }

    protected void setStatus(String status) {
        this.status = status;
    }

    protected void setException(Throwable e) {
        result.setException(e);
    }

    public Throwable getException() {
        Throwable exception = result.getException();
        return exception == null ? null : exception;
    }

    public void addFutureListener(FutureListener futureListener) {
        this.result.addFutureListener(futureListener);
    }

    public boolean isFailed() {
        return this.result.isFailed();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isDone() {
        return this.result.isDone();
    }

    public Object get() {
        try {
            return result.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }
    public Object get(long timeout, TimeUnit timeUnit) throws TimeoutException, InterruptedException, ExecutionException {
        return result.get(timeout, timeUnit);
    }
    /**
     * @return either the wrapped Runnable or wrapped Callable
     */
    protected Object getWrapped() {
        if (getWrappedCallable() != null) {
            return getWrappedCallable();
        } else if (wrappedRunnable != null) {
            return wrappedRunnable;
        } else {
            return null;
        }
    }

    public void setTaskGroup(TaskGroup<?> taskGroup) {
        if (this.taskGroup == taskGroup) {
            return;
        }
        if (this.taskGroup != null) {
            throw new IllegalStateException(getName()
                    + ": already assigned to taskgroup " + taskGroup.getName());
        }
        Object wrapped = getWrapped();
        if (wrapped instanceof TaskGroupAware) {
            ((TaskGroupAware) wrapped).setTaskGroup(taskGroup);
        }
        this.taskGroup = taskGroup;
    }

    public TaskGroup<?> getTaskGroup() {
        return taskGroup;
    }

    protected void addTaskStatus(String message) {
        this.getTaskGroup().addTaskStatus(this, message);
    }
    public boolean isNeverEligibleToRun() {
        return isDone() && !isReadyToRun();
    }

    /**
     * @param l
     */
    public void setResourceLocksNeeded(Collection<ResourceLock> l) {
        this.resourceLocksNeeded.clear();
        this.resourceLocksNeeded.addAll(l);
    }

    public Collection<ResourceLock> getResourceLocksNeeded() {
        return resourceLocksNeeded;
    }

    public void addLock(ResourceLock lock) {
        this.getResourceLocksNeeded().add(lock);
    }

    public boolean hasLocks() {
        return this.getResourceLocksNeeded() != null
                && !this.getResourceLocksNeeded().isEmpty();
    }

    public void setLockTypeUsed(String resourceName, int lockTypeUsed) {
        resourceName = resourceName.toUpperCase();
        Integer value = resourceLocksUsed.get(resourceName);
        // TODO verify had the lock that was claimed to be used
        if (value == null) {
            value = Integer.valueOf(lockTypeUsed);
        } else {
            value = Integer.valueOf(value.intValue() | lockTypeUsed);
        }
        resourceLocksUsed.put(resourceName, value);
    }

    /**
     * take the resourceLocks and downgrade the locks to the actual level used
     * by this task.
     *
     */
    private void downgradeUsedLocks() {
        if (lockDowngradeEnabled) {
            for (Object element : getResourceLocksNeeded()) {
                ResourceLock lock = (ResourceLock) element;
                Integer value = resourceLocksUsed.get(lock.getResourceName());
                int intvalue = value == null ? 0 : value.intValue();
                lock.downGrade(intvalue);
            }
        }
    }

    public void setLockDowngradeEnabled(boolean lockDowngradeEnabled) {
        this.lockDowngradeEnabled = lockDowngradeEnabled;
    }

    public boolean isLockDowngradeEnabled() {
        return lockDowngradeEnabled;
    }

    /**
     * @param lapTimer the lapTimer to set
     */
    public void setLapTimer(LapTimer lapTimer) {
        this.lapTimer = lapTimer;
    }

    /**
     * @return the lapTimer
     */
    public LapTimer getLapTimer() {
        return lapTimer;
    }

    /**
     * @return the wrappedCallable
     */
    public Callable<? extends R> getWrappedCallable() {
        return wrappedCallable;
    }
}
