/**
 *
 */
package com.sworddance.taskcontrol;

import java.util.Collection;

/**
 *
 */
public interface DependentPrioritizedTask extends PrioritizedTask {
    /**
     *
     * @param task
     * @return may be dependent on task
     */
    public boolean isDependentOn(PrioritizedTask task);

    /**
     * @param task
     * @return if this determination of success is dependent on
     */
    public boolean isSuccessDependentOn(PrioritizedTask task);

    /**
     * @param task
     * @return true if this is dependent on task.
     */
    public boolean isAlwaysDependentOn(PrioritizedTask task);

    /**
     * @param task
     */
    public void addAlwaysDependency(PrioritizedTask task);

    /**
     *
     * @param dependencies
     */
    public void addAlwaysDependencies(Collection<? extends PrioritizedTask> dependencies);

    /**
     * @param task
     */
    public void addDependency(PrioritizedTask task);

    /**
     * @param dependencies
     */
    public void addDependencies(Collection<? extends PrioritizedTask> dependencies);

    /**
     * @return parent task.
     */
    public DependentPrioritizedTask getParentTask();

    /**
     *
     * @param ignoreFailure
     */
    public void setIgnoreTaskGroupFailure(boolean ignoreFailure);
}
