package com.sworddance.taskcontrol;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * All tasks in TaskControl should extend or emulate this class's behavior.
 *
 * @author pmoore
 *
 */
public class DefaultPrioritizedTask implements PrioritizedTask, Callable {
    private TaskGroup<?> taskGroup;

    private final CountDownLatch shouldRun = new CountDownLatch(1);

    protected final Runnable wrappedRunnable;

    protected NotificationObject notification;

    private int sequenceId;

    private final Callable<?> wrappedCallable;

    private final Integer priority;

    private String status;

    private FutureResult<Object> result = new FutureResult<Object>();

    private String name;

    private long startTime;

    private long endTime;

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

    private static SimpleDateFormat FORMATTER = new SimpleDateFormat(
            "HH:mm:ss.SSS");

    public DefaultPrioritizedTask() {
        this((Runnable) null);
    }

    public DefaultPrioritizedTask(Runnable wrapped) {
        super();
        wrappedRunnable = wrapped;
        wrappedCallable = null;
        priority = new Integer(Thread.NORM_PRIORITY);
        initResourceLocker(wrapped);
    }

    public DefaultPrioritizedTask(Callable<?> callable) {
        this(callable.getClass().getName(), callable);
    }
    public DefaultPrioritizedTask(String name, Callable<?> callable) {
        super();
        wrappedCallable = callable;
        wrappedRunnable = null;
        priority = new Integer(Thread.NORM_PRIORITY);
        initResourceLocker(callable);
        setName(name);
    }


    public DefaultPrioritizedTask(Runnable wrapped, int priority) {
        super();
        wrappedRunnable = wrapped;
        wrappedCallable = null;
        this.priority = new Integer(priority);
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
            return shouldRun.await(0, MILLISECONDS) && !isSuccessful();
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

    protected void setSuccess() {
        if ( !this.result.isDone()) {
            this.result.set(null);
        }
    }

    protected String getTimingString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getElapsed());
        sb.append("ms elapsed. Start=");
        sb.append(FORMATTER.format(new Date(startTime)));
        sb.append(" End=");
        sb.append(FORMATTER.format(new Date(endTime > 0 ? endTime : System.currentTimeMillis())));
        return sb.toString();
    }

    public long getElapsed() {
        return endTime - startTime;
    }

    public Object call() throws Exception {
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
            setError(e);
            throw e;
        } catch (Error e) {
            setError(e);
            throw e;
        } catch (Throwable e) {
            setError(e);
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setName(threadName);
            finishTiming();
            if (!isSuccessful()) {
                setStatus("exception thrown: " + getError());
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
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
    }

    protected void finishTiming() {
        if (endTime == 0) {
            endTime = System.currentTimeMillis();
        }
    }

    /**
     * subclasses should override this method.
     *
     */
    protected Object callBody() throws Exception {
        Object returnResult;
        if (wrappedCallable != null) {
            returnResult = wrappedCallable.call();
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

    protected void setError(Throwable e) {
        result.setException(e);
    }

    public Throwable getError() {
        Throwable exception = result.getException();
        return exception == null ? null : exception;
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

    public Object getResult() {
        try {
            return result.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }

    /**
     * @return either the wrapped Runnable or wrapped Callable
     */
    protected Object getWrapped() {
        if (wrappedCallable != null) {
            return wrappedCallable;
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
            value = new Integer(lockTypeUsed);
        } else {
            value = new Integer(value.intValue() | lockTypeUsed);
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
}
