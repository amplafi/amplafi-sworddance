/**
 *
 */
package com.sworddance.taskcontrol;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
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
        assertNotNull(taskGroup.getError(), "Should throw Exception");
    }

    /**
     * generate a exception for the test.
     */
    public class ExceptionGenerator implements Callable<Object> {
        public Object call() throws Exception {
            throw new UnsupportedOperationException("TODO");
        }

    }
}
