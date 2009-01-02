/**
 *
 */
package com.sworddance.taskcontrol;

import java.util.Collection;

/**
 * used by TaskControl and TaskGroup tasks to provide initialization and clean
 * up functionality.
 *
 * @author Patrick Moore
 *
 */
public abstract class TaskWrapper implements PrioritizedTask {
    private final PrioritizedTask wrappedTask;

    /**
     * @param nextTask
     */
    protected TaskWrapper(PrioritizedTask nextTask) {
        this.wrappedTask = nextTask;
    }

    /**
     * @see PrioritizedTask#isReadyToRun()
     */
    public boolean isReadyToRun() {
        return this.getWrappedTask().isReadyToRun();
    }

    /**
     * @see PrioritizedTask#releaseToRun()
     */
    public void releaseToRun() {
        this.releaseToRun();
    }

    /**
     * @see PrioritizedTask#isSuccessful()
     */
    public boolean isSuccessful() {
        return getWrappedTask().isSuccessful();
    }

    /**
     * @see PrioritizedTask#isNeverEligibleToRun()
     */
    public boolean isNeverEligibleToRun() {
        return getWrappedTask().isNeverEligibleToRun();
    }

    /**
     * @see PrioritizedTask#setNotification(NotificationObject)
     */
    public void setNotification(NotificationObject notification) {
        getWrappedTask().setNotification(notification);
    }

    /**
     * @see PrioritizedTask#getSequence()
     */
    public int getSequence() {
        return getWrappedTask().getSequence();
    }

    /**
     * @see PrioritizedTask#getPriority()
     */
    public int getPriority() {
        return getWrappedTask().getPriority();
    }

    /**
     * @see PrioritizedTask#getStatus()
     */
    public String getStatus() {
        return getWrappedTask().getStatus();
    }

    /**
     * @see PrioritizedTask#getName()
     */
    public String getName() {
        return getWrappedTask().getName();
    }

    /**
     * @see PrioritizedTask#isDone()
     */
    public boolean isDone() {
        return getWrappedTask().isDone();
    }

    /**
     * @see PrioritizedTask#getError()
     */
    public Throwable getError() {
        return getWrappedTask().getError();
    }

    /**
     * @see PrioritizedTask#getResult()
     */
    public Object getResult() {
        return getWrappedTask().getResult();
    }

    /**
     * @see TaskGroupAware#setTaskGroup(TaskGroup)
     */
    public void setTaskGroup(TaskGroup<?> taskGroup) throws IllegalStateException {
        getWrappedTask().setTaskGroup(taskGroup);
    }

    /**
     * @see TaskGroupAware#getTaskGroup()
     */
    public TaskGroup<?> getTaskGroup() {
        return getWrappedTask().getTaskGroup();
    }

    public Collection<ResourceLock> getResourceLocksNeeded() {
        return getWrappedTask().getResourceLocksNeeded();
    }

    public void addLock(ResourceLock lock) {
        getWrappedTask().addLock(lock);
    }

    public boolean hasLocks() {
        return getWrappedTask().hasLocks();
    }

    public PrioritizedTask getWrappedTask() {
        return wrappedTask;
    }

    public PrioritizedTask getBaseWrappedTask() {
        return wrappedTask instanceof TaskWrapper ?
                (((TaskWrapper) wrappedTask).getWrappedTask())
                : wrappedTask;
    }

    @Override
    public String toString() {
        return "TaskWrapper around:"+this.getBaseWrappedTask();
    }
}
