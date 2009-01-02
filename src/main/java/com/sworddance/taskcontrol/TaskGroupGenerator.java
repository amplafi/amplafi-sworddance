/**
 * 
 */
package com.sworddance.taskcontrol;

/**
 * @author pmoore
 * 
 */
public abstract class TaskGroupGenerator implements Runnable {
    protected TaskControl taskControl;

    /**
     * @param taskControl
     */
    public void setTaskControl(TaskControl taskControl) {
        this.taskControl = taskControl;
    }

}
