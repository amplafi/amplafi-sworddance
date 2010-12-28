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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
/**
 * test TaskControl.
 * @author Patrick Moore
 */
@Test
public class TestTaskControl {
    @SuppressWarnings("unchecked")
    @DataProvider(name="testObjects")
    protected Object[][] getTestObjects() {
        TaskGroup taskGroup = new TaskGroup("taskGroup");
        taskGroup.setLog(LogFactory.getLog(this.getClass()));
        return new Object[][] {
                new Object[] {
                        taskGroup,
                        new OrderedOut(),
                        LogFactory.getLog(this.getClass())
                }
        };
    }
    /**
     * make sure that default in/out is FIFO.
     * @param taskGroup
     * @param orderOut
     * @param log
     * @throws Exception
     *
     */
    @Test(dataProvider="testObjects")
    public void testTestOrdering(TaskGroup<?> taskGroup, OrderedOut orderOut, Log log) throws Exception {
        TaskControl taskControl = new TaskControl(new TestPriorityComparator(), 1, log);
        for (int i = 0; i < 200; i++) {
            TestTask task = new TestTask(i, orderOut);
            taskGroup.addTask(task);
        }
        startTaskControl(taskControl, taskGroup);
        assertEquals(orderOut.expected, 200);
        orderOut.printErrorMessage();
    }

    private void startTaskControl(TaskControl taskControl, TaskGroup<?> taskGroup) throws InterruptedException {
        // additional test that addTaskGroup after TaskControl start will work.
        taskControl.addTaskGroup(taskGroup);
        Thread t = new Thread(taskControl);
        taskControl.setStayActive(false);
        t.setName("TaskControl");
        t.start();
        t.join();
        assertFalse(t.isAlive());
    }

    @Test(dataProvider="testObjects")
    public void testBadDependencies(TaskGroup<?> taskGroup, OrderedOut orderOut, Log log) {
        taskGroup.setLog(log);
        DefaultDependentPrioritizedTask blocking = new DefaultDependentPrioritizedTask();
        blocking.setName("blocking");
        // add some dead tasks.
        int i = 0;
        {
            TestTask task = new TestTask(i, orderOut);
            task.addDependency(blocking);
            try {
                taskGroup.addTask(task);
                fail("adding dependent tasks need to be added to TaskControl before dependencies");
            } catch (IllegalStateException e) {
                // good
            }
        }
        i++;
        {
            TestTask task = new TestTask(i, orderOut);
            try {
                task.addDependency(task);
                fail("cannot depend on self");
            } catch (IllegalStateException e) {
                // good
            }
        }
    }

    @Test(dataProvider="testObjects")
    public void testBadJobs(TaskGroup<?> taskGroup, OrderedOut orderOut, Log log) throws Exception {
        TaskControl taskControl = new TaskControl(new TestPriorityComparator(), 1, log);
        List<TestTask> list = new ArrayList<TestTask>();
        DefaultPrioritizedTask<Object> blocking = new DefaultPrioritizedTask<Object>();
        blocking.setName("blocking");
        taskGroup.addTask(blocking);
        // add some dead tasks.
        int i = 0;
        {
            TestTask task = new TestTask(i, orderOut);
            task.addDependency(blocking);
            list.add(task);
            taskGroup.addTask(task);
        }
        i++;
        {
            TestTask task = new TestTask(i, orderOut);
            list.add(task);
            task.setSuccess();
            try {
                taskGroup.addTask(task);
                fail("adding completedjobs should fail");
            } catch (IllegalStateException e) {
                // good
            }
        }
        i++;
        {
            // task depending on itself
            TestTask task = new TestTask(i, orderOut);
            list.add(task);
            task.setSuccess();
            try {
                task.addDependency(task);
                fail("tasks can't depend on themselves");
            } catch (IllegalStateException e) {
                // good
            }
        }
        i++;
        {
            // task depending on itself
            TestTask task = new TestTask(i, orderOut);
            list.add(task);
            task.setSuccess();
            try {
                task.addAlwaysDependency(task);
                fail("tasks can't depend on themselves");
            } catch (IllegalStateException e) {
                // good
            }
        }
        i++;
        {
            TestTask task = new TestTask(i, orderOut);
            list.add(task);
            taskGroup.addTask(task);
            // now make it bad...
            task.setSuccess();
        }
        startTaskControl(taskControl, taskGroup);
        orderOut.printErrorMessage();
        assertEquals(orderOut.expected, 0);
        for (int k = 0; k < list.size(); k++) {
            TestTask task = list.get(k);
            assertFalse(task.callBodyCalled, "Task #" + k + " should never be run");
        }
    }

    /**
     * test that if the first task at the head of a chain fails the other
     * dependent tasks fail out so that AlwaysDependencies behave as expected.
     * @param log
     * @param taskGroup
     * @param orderOut
     * @throws Exception
     */
    @Test(dataProvider="testObjects")
    public void testChainFailure1(TaskGroup<?> taskGroup, OrderedOut orderOut, Log log) throws Exception {
        int i = 0;
        TaskControl taskControl = new TaskControl(new TestPriorityComparator(), 1, log);
        TestTask task1 = new FailTestTask(i++, orderOut);
        taskGroup.addTask(task1);
        TestTask task2 = new TestTask(i++, orderOut);
        task2.addDependency(task1);
        taskGroup.addTask(task2);

        startTaskControl(taskControl, taskGroup);
        assertTrue(task1.callBodyCalled,
                "Task #" + task1.id + " should be run");
        assertFalse(task2.callBodyCalled,
                "Task #" + task2.id + " should not be run");
    }

    /**
     * test that if the first task at the head of a chain fails the other
     * dependent tasks fail out so that AlwaysDependencies behave as expected.
     * @param log
     * @param taskGroup
     * @param orderOut
     * @throws Exception
     */
    @Test(dataProvider="testObjects")
    public void testChainFailure2(TaskGroup<?> taskGroup, OrderedOut orderOut, Log log) throws Exception {
        int i = 0;
        TaskControl taskControl = new TaskControl(new TestPriorityComparator(), 1, log);

        TestTask task1 = new FailTestTask(i++, orderOut);
        taskGroup.addTask(task1);
        TestTask task2 = new TestTask(i++, orderOut);
        task2.addDependency(task1);
        taskGroup.addTask(task2);
        TestTask task3 = new TestTask(i++, orderOut);
        task3.setIgnoreTaskGroupFailure(true);
        task3.addAlwaysDependency(task2);
        taskGroup.addTask(task3);

        startTaskControl(taskControl, taskGroup);
        assertTrue(task1.callBodyCalled, "Task #" + task1.id + " should be run");
        Throwable error = task1.getError();
        assertNotNull(error);
        assertFalse(task2.callBodyCalled, "Task #" + task2.id + " should not be run");
        assertNotNull(task2.getError());
        assertTrue(task3.callBodyCalled,
                "Task #" + task3.id + " should be run");
        assertNull(task3.getError());
    }

    /**
     * test that if the first task at the head of a chain fails the other
     * dependent tasks fail out so that AlwaysDependencies behave as expected.
     * @param log
     * @param taskGroup
     * @param orderOut
     * @throws Exception
     */
    @Test(dataProvider="testObjects")
    public void testChainFailure3(TaskGroup<?> taskGroup, OrderedOut orderOut, Log log) throws Exception {
        int i = 0;
        TaskControl taskControl = new TaskControl(new TestPriorityComparator(), 1, log);

        TestTask task1 = new FailTestTask(i++, orderOut);
        taskGroup.addTask(task1);
        TestTask task2 = new TestTask(i++, orderOut);
        task2.setIgnoreTaskGroupFailure(true);
        task2.addAlwaysDependency(task1);
        taskGroup.addTask(task2);

        startTaskControl(taskControl, taskGroup);
        assertTrue(task1.callBodyCalled, "Task #" + task1.id + " should be run");
        assertTrue(task2.callBodyCalled, "Task #" + task2.id + " should be run");
    }

    @Test(dataProvider="testObjects")
    public void testSimpleDependency(TaskGroup<?> taskGroup, OrderedOut orderOut, Log log) throws Exception {
        TaskControl taskControl = new TaskControl(new TestPriorityComparator(), 1, log);
        orderOut.down = true;
        List<TestTask> list = new ArrayList<TestTask>();
        // add dependencies
        int i;
        for (i = 0; i < 20; i++) {
            TestTask task = new TestTask(i, orderOut);
            list.add(task);
            orderOut.expected = i;
        }
        for (int k = 0; k < list.size() - 1; k++) {
            // create a dependency that will reverse the normal FIFO
            TestTask task = list.get(k);
            task.addDependency(list.get(k + 1));
        }
        for (int k = list.size() - 1; k >= 0; k--) {
            // create a dependency that will reverse the normal FIFO
            TestTask task = list.get(k);
            taskGroup.addTask(task);
        }
        startTaskControl(taskControl, taskGroup);
        orderOut.printErrorMessage();
        assertEquals(orderOut.expected, -1);
        for (int k = 0; k < list.size(); k++) {
            TestTask task = list.get(k);
            assertTrue(task.callBodyCalled, "Task #" + k + " should be run");
        }
    }

    @Test(dataProvider="testObjects")
    public void testComplexDependency(TaskGroup<?> taskGroup, OrderedOut orderOut, Log log) throws Exception {
        TaskControl taskControl = new TaskControl(new TestPriorityComparator(), 5, log);
        orderOut.down = true;
        List<TestTask> list = new ArrayList<TestTask>();
        // add dependencies
        int i;
        for (i = 0; i < 20; i++) {
            TestTask task = new TestTask(i, orderOut);
            list.add(task);
            orderOut.expected = i;
        }
        for (int k = 0; k < list.size(); k++) {
            // create a dependency that will reverse the normal FIFO
            TestTask task = list.get(k);
            for (int m = 1; m < 4 && m + k < list.size(); m++) {
                task.addDependency(list.get(k + m));
            }
        }
        for (int k = list.size() - 1; k >= 0; k--) {
            // create a dependency that will reverse the normal FIFO
            TestTask task = list.get(k);
            taskGroup.addTask(task);
        }
        startTaskControl(taskControl, taskGroup);
        orderOut.printErrorMessage();
        for (int k = 0; k < list.size(); k++) {
            TestTask task = list.get(k);
            assertTrue(task.callBodyCalled,
                    "Task #" + k + " should be run");
        }
        assertEquals(orderOut.expected, -1);
    }

    /**
     * make sure an empty taskgroup immediately reports that it is done (especially with result)
     * @throws Exception
     *
     */
    @SuppressWarnings("unchecked")
    public void testEmptyTaskGroup() throws Exception {
        TaskControl taskControl = new TaskControl(new TestPriorityComparator(), 1, LogFactory.getLog(this.getClass()));
        TaskGroup taskGroup = taskControl.newTaskGroup("empty");
        startTaskControl(taskControl, taskGroup);
        FutureResult result = taskGroup.getResult();

        // should immediately return result.
        result.get(1L, TimeUnit.NANOSECONDS);

    }
    /**
     * a test implementation of {@link DefaultDependentPrioritizedTask}.
     * @author Patrick Moore
     */
    private class TestTask extends DefaultDependentPrioritizedTask {
        private int id;

        boolean callBodyCalled;

        private OrderedOut orderOut;

        public TestTask(int id, OrderedOut orderOut) {
            setName("Test Task #" + id);
            this.id = id;
            this.orderOut = orderOut;
        }

        @Override
        protected Object callBody() throws Exception {
            callBodyCalled = true;
            if (orderOut != null) {
                orderOut.ran(id);
            }
            long sleepTime = (long) (Math.random() * 15L);
            Thread.sleep(sleepTime);
            return null;
        }

        @Override
        protected void setSuccess() {
            super.setSuccess();
        }
    }

    /**
     * a task that always fails.
     * @author Patrick Moore
     */
    private class FailTestTask extends TestTask {
        /**
         * @param i
         * @param orderOut
         */
        public FailTestTask(int i, OrderedOut orderOut) {
            super(i, orderOut);
        }

        @Override
        protected Object callBody() throws Exception {
            super.callBody();
            throw new Exception("EXPECTED");
        }
    }

    /**
     * used to track that the order of task execution is correct.
     * @author Patrick Moore
     */
    private static class OrderedOut {
        private int expected;

        private boolean down;

        private int lastExpected = Integer.MIN_VALUE;

        private int lastActual = Integer.MIN_VALUE;

        public void ran(int id) {
            if (expected != id) {
                lastExpected = expected;
                lastActual = id;
            }
            if (down) {
                expected--;
            } else {
                expected++;
            }
        }

        public void printErrorMessage() {
            assertEquals(lastActual, lastExpected);
        }
    }

    /**
     * a test comparator.
     * @author Patrick Moore
     */
    private static class TestPriorityComparator implements Comparator<PrioritizedTask> {

        public int compare(PrioritizedTask o1, PrioritizedTask o2) {
            TestTask t1 = (TestTask) ((TaskWrapper) o1).getBaseWrappedTask();
            TestTask t2 = (TestTask) ((TaskWrapper) o2).getBaseWrappedTask();
            return t1.id - t2.id;
        }

    }
}
