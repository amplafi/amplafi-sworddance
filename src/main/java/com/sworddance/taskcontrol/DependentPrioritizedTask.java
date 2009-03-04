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
