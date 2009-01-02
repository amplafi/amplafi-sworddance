package com.sworddance.taskcontrol;

import java.util.Comparator;

/**
 * Prioritize the work items that could be moved on to the active queue.
 * 
 * The prioritization rules when comparing Task t1 to Task t2 are as follows ('<'
 * means comes before).
 * 
 * @author pmoore
 * 
 */
public class PriorityEligibleWorkItemComparator implements Comparator<PrioritizedTask> {

    public PriorityEligibleWorkItemComparator() {
        super();
    }

    /**
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(PrioritizedTask left, PrioritizedTask right) {
        if (left.getPriority() != right.getPriority()) {
            return right.getPriority() - left.getPriority();
        } else {
            return left.getSequence() - right.getSequence();
        }
    }

}
