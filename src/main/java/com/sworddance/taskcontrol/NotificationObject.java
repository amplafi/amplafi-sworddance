/**
 *
 */
package com.sworddance.taskcontrol;

/**
 * @author pmoore
 *
 */
public interface NotificationObject {
    public void stateChanged();

    public int getSequence();

    public void debug(String msg);

    /**
     * @param msg
     */
    public void warning(String msg);
}
