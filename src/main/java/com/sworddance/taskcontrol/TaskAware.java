/**
 * 
 */
package com.sworddance.taskcontrol;

/**
 * @author pmoore
 * 
 */
public interface TaskAware {

    /**
     * @param task
     */
    public void setDependentPrioritizedTask(DependentPrioritizedTask task);

}
