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
<<<<<<< HEAD
    }
=======
    }

    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    protected Object callBody() {
        try {
            getTaskGroup().getLog().info(completionMsg);
        } catch (Exception e) {
            // O.K.
        }
        return null;
    }
}
