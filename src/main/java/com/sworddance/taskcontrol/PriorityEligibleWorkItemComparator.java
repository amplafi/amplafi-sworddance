/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
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

    public int compare(PrioritizedTask left, PrioritizedTask right) {
        if (left.getPriority() != right.getPriority()) {
            return right.getPriority() - left.getPriority();
        } else {
            return left.getSequence() - right.getSequence();
        }
    }

}
