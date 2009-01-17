package com.sworddance.taskcontrol;

/**
 * Expected behavior is that a task can only run once. So once 'hasResult()' is
 * true it can never be false Also once a task is ready to run then then it may
 * never be false. If for some reason a task determines while running that it
 * shouldn't be running, then a copy of that task should be created and the
 * original task terminated.
 *
 * Both of these conditions are necessary to avoid hard-to-find race conditions.
 *
 * @author pmoore
 *
 */
public interface PrioritizedTask extends Runnable, TaskGroupAware,
        ResourceLocker {

    /**
     * @return true if ready to run
     */
    public boolean isReadyToRun();

    public void releaseToRun();

    public boolean isSuccessful();

    /**
     * Indicates that the task has determined that it will never be eligible to
     * run and should be removed from any pending task list. This can happen for
     * example with dependent tasks that have one of their dependencies fail.
     * Once this method returns true it can never return false;
     *
     * @return true if the task has determined that it is never eligible.
     */
    public boolean isNeverEligibleToRun();

    public void setNotification(NotificationObject notification);

    public int getSequence();

    /**
     * @return priority in the range Thread.MIN_PRIORITY .. Thread.MAX_PRIORITY. May be used as
     * actual Thread priority so do not change the range.
     */
    public int getPriority();

    public String getStatus();

    public String getName();

    /**
     * @return
     * the task has completed with or without error or the
     * {@link #isNeverEligibleToRun()} has been set.
     *
     */
    public boolean isDone();

    public Throwable getError();

    public Object getResult();
}