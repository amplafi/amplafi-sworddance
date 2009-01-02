/**
 * 
 */
package com.sworddance.taskcontrol;

/**
 * @author pmoore
 * 
 */
public class StatusMessageTask extends DefaultDependentPrioritizedTask {
    /**
     * Do nothing. Print a static message when this task runs.
     * 
     * @param completionMsg
     */
    public StatusMessageTask(final String completionMsg) {
        this("Status Message", completionMsg);
    }

    public StatusMessageTask(final String taskName, final String completionMsg) {
        setName(taskName);
        this.completionMsg = completionMsg;
    }

    @Override
    protected Object callBody() {
        try {
            getTaskGroup().getLog().info(completionMsg);
        } catch (Exception e) {
            // O.K.
        }
        return null;
    }
}
