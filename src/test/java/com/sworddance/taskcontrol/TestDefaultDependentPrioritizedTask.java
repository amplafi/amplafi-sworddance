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

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
/**
 * Test the behavior of the {@link DefaultDependentPrioritizedTask} class.
 *
 */
public class TestDefaultDependentPrioritizedTask {

    @Test
    public void testExceptionsPropagated() throws Exception {
        DefaultDependentPrioritizedTask task = new DefaultDependentPrioritizedTask(
                new ExceptionGenerator());
        try {
            task.call();
            fail("should have thrown an exception");
        } catch(AssertionError e) {
            throw e;
        } catch (Throwable t) {
            // correct behavior
        }
        // now test within taskControl
        task = new DefaultDependentPrioritizedTask(new ExceptionGenerator());
        TaskGroup<?> taskGroup = new TaskGroup<Object>("Test");
        Log logger = LogFactory.getLog(this.getClass());
        taskGroup.setLog(logger);
        TaskControl taskControl = new TaskControl(logger);
        taskControl.setStayActive(false);
        taskControl.setLog(logger);
        taskGroup.addTask(task);
        taskControl.addTaskGroup(taskGroup);
        Thread t = new Thread(taskControl);
        t.start();
        t.join();
        assertNotNull(taskGroup.getException(), "Should throw Exception");
    }

    /**
     * generate a exception for the test.
     */
    public static class ExceptionGenerator implements Callable<Object> {
        public Object call() throws Exception {
            throw new UnsupportedOperationException("TODO");
        }

    }
}
