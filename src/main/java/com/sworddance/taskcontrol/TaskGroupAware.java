/**
 *
 */
package com.sworddance.taskcontrol;

/**
 * @author pmoore
 *
 */
public interface TaskGroupAware {
    /**
     * set the taskGroup for this object (usually a task). If implementer should
     * not be added to the taskGroup
     *
     * @param taskGroup
     * @throws IllegalStateException
     *             task should not be added to this taskGroup
     */
    public void setTaskGroup(TaskGroup<?> taskGroup) throws IllegalStateException;

    public TaskGroup<?> getTaskGroup();
}
