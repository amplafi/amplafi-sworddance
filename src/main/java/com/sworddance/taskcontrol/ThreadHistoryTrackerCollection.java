/**
 * 
 */
package com.sworddance.taskcontrol;

import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * holds past histories for display by a direct action or export to csv file.
 * 
 * @author pmoore
 * 
 */
public class ThreadHistoryTrackerCollection {
    private List<ThreadHistoryTracker> history = new CopyOnWriteArrayList<ThreadHistoryTracker>();

    public void addHistory(ThreadHistoryTracker tracker) {
        history.add(0, tracker);
    }
}
