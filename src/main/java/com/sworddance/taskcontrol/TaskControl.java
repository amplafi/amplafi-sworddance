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

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;

/**
 * manages a collection of {@link TaskGroup}s that contain tasks that need to
 * be executed. Currently only 1 TaskGroup can be assigned. this will change in
 * the near future.
 *
 *
 */
public class TaskControl implements Runnable {
    private final ThreadFactory threadFactory;

    private final BlockingQueue<PrioritizedTask> eligibleTasks;

    private final ThreadPoolExecutor executor;

    /**
     * Lock used to signal when new jobs are ready
     * or the TaskControl's state has changed, for example, it is being shutdown.
     */
    private final Lock stateChangeNotificator;

    private final Condition newTasks;

    private boolean dumpTaskGroupStats;
    private Runnable processQueue = new ProcessQueue();

    private AtomicInteger currentTaskGroup = new AtomicInteger(0);

    private CountDownLatch shutDown = new CountDownLatch(1);

    /*
     * task groups that have jobs that should be chosen from
     */
    private List<TaskGroup<?>> taskGroups = new CopyOnWriteArrayList<TaskGroup<?>>();

    private List<PrioritizedTask> runningTaskList = new CopyOnWriteArrayList<PrioritizedTask>();

    // only needed because no way of finding out from PooledExecutor if there
    // are jobs still running
    private final AtomicInteger runningTasks;

    private Log log;

    private boolean privateThreadFactory;

    public TaskControl(Comparator<PrioritizedTask> activeComparator, int maxThreads) {
        this(activeComparator, maxThreads, new ThreadFactoryImpl());
        this.privateThreadFactory = true;
    }
    @SuppressWarnings("unchecked")
    public TaskControl(Comparator<PrioritizedTask> activeComparator, int maxThreads, ThreadFactory threadFactory) {
        eligibleTasks = new PriorityBlockingQueue<PrioritizedTask>(20,
                activeComparator);
        stateChangeNotificator = new ReentrantLock();
        newTasks = stateChangeNotificator.newCondition();
        runningTasks = new AtomicInteger(0);
        this.threadFactory = threadFactory;
        // stupid java rules
        executor = new ThreadPoolExecutor(1, maxThreads, 10,
                MICROSECONDS, (BlockingQueue) eligibleTasks, threadFactory);
        this.stayActive = true;
    }

    /**
     *
     */
    public TaskControl(Log log) {
        this(new PriorityEligibleWorkItemComparator(), 5);
        setLog(log);
    }


    @SuppressWarnings("unchecked")
    public TaskGroup<?> newTaskGroup(String name) {
        TaskGroup<?> taskGroup = new TaskGroup(name);
        taskGroup.setLog(getLog());
        taskGroup.setTaskControl(this);
        return taskGroup;
    }

    /**
     * Add {@link TaskGroup} to TaskControl. If there are no tasks in the TaskGroup, the TaskGroup will
     * immediately have it's result set to null and not actually be added.
     * @param taskGroup
     */
    public void addTaskGroup(TaskGroup<?> taskGroup) {
        if ( !taskGroup.prepareToRun() ) {
            return;
        }
        taskGroup.setTaskControl(this);
        taskGroups.add(taskGroup);
        stateChanged();
    }

    /**
     * entered by external thread to notify the TaskControl of taskCompletion.
     * @param task
     *
     */
    private void taskComplete(PrioritizedTask task) {
        if (!runningTaskList.remove(task)) {
            getLog().debug("removing task that was not on running list");
        }
        runningTasks.decrementAndGet();
        stateChanged();
    }

    /**
     * Used to notify TaskControl that it should wake up because there may be
     * changes to the state of things. This can happen if TaskGroups get new
     * tasks added to them.
     *
     */
    public void stateChanged() {
        stateChangeNotificator.lock();
        try {
            newTasks.signal();
        } finally {
            stateChangeNotificator.unlock();
        }
    }

    public void run() {
        try {
            prepareToRun();
            while (stillRunning()) {
                stateChangeNotificator.lock();
                try {
                    if ( !isTaskReady() ) {
                        newTasks.await(60, SECONDS);
                    }
                } finally {
                    stateChangeNotificator.unlock();
                }
                processQueue.run();
            }
        } catch (Exception e) {
            getLog().warn("TaskControl ending with exception", e);
        } finally {
            shutdownNow();
        }
    }

    /**
     * all tasks should be done at this point, but if they are not they probably
     * should be killed.
     */
    public void shutdownNow() {
        shutDown.countDown();
        // do graceful shutdown... otherwise there is a slight race condition
        // with the Worker threads as they wrap up processing the last of the
        // jobs. (case: jobs has decremented runningTasks, but worker thread not
        // yet complete.) Don't want to use shutdownNow because that does
        // interrupt

        executor.shutdown();
        try {
            // executor.awaitTerminationAfterShutdown() - don't use race
            // condition where
            // poolsize ended up == 0 but still in wait
            executor.awaitTermination(1000L, MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if ( this.privateThreadFactory ) {
            ((ThreadFactoryImpl) threadFactory).shutDownNow();
        }
        for (TaskGroup<?> taskGroup: taskGroups) {
            taskGroup.shutdownNow(dumpTaskGroupStats);
        }
        // if calling from outside - wake up main thread.
        stateChanged();
    }

    protected void prepareToRun() {
    }

    /**
     * Determine if there are any tasks that need to be moved from the possible
     * Queue to the eligibleTask queue. It is possible that there are possible
     * jobs but no eligible jobs.
     *
     * Note the deadlock potential here as this method is called from within 2
     * other sync blocks Don't make public and monitor usage.
     * TODO race conditions if adding taskGroups
     * @return true if any possible task can be run.
     */
    private boolean isTaskReady() {
        TaskGroup<?> group = getCurrentTaskGroup();
        if ( group != null && group.isTaskReady() ) {
            return true;
        }
        int originalIndex = currentTaskGroup.get();
        ArrayList<TaskGroup<?>> copy = new ArrayList<TaskGroup<?>>(taskGroups);

        for(int index = originalIndex+1; index < copy.size(); index++) {
            group = copy.get(index);
            if ( group != null && group.isTaskReady() ) {
                currentTaskGroup.compareAndSet(originalIndex, index);
                return true;
            }
        }
        for (int index = 0; index < originalIndex; index++) {
            group = copy.get(index);
            if (group != null && group.isTaskReady()) {
                currentTaskGroup.compareAndSet(originalIndex, index);
                return true;
            }
        }
        return false;
    }

    private TaskGroup<?> getCurrentTaskGroup() {
        TaskGroup<?> taskGroup;
        do {
            int index = currentTaskGroup.get();
            try {
                taskGroup = taskGroups.get(index);
                return taskGroup;
            } catch (IndexOutOfBoundsException e) {
                if (index == 0) {
                    // no taskGroups left
                    return null;
                } else {
                    currentTaskGroup.set(0);
                    continue;
                }
            }
        } while (true);
    }

    public void setStayActive(boolean stayActive) {
        this.stayActive = stayActive;
        stateChanged();
    }

    public boolean isStayActive() {
        return stayActive;
    }

    // variables for debugging hung TaskControl
    private int lastRunningTaskSize = Integer.MIN_VALUE;

    private int lastEligibleTasksSize = Integer.MIN_VALUE;

    private boolean lastIsTaskNOTReady;

    /**
     * do not automatically shut down if there are no jobs.
     */
    private boolean stayActive;

    /**
     * if the thread has not been interrupted and the eligibleTask list is not
     * empty or there are possible jobs to move from to the eligibleTask queue
     * then keep running.
     *
     * @return
     * @throws InterruptedException
     */
    private boolean stillRunning() {
        try {
            if (Thread.currentThread().isInterrupted()
                    || shutDown.await(0, MILLISECONDS)) {
                getLog().debug("TaskControl interrupted");
                return false;
            }
        } catch (InterruptedException e) {
            return false;
        }
        if ( !stayActive ) {
            // make sure that the only eligible task isn't being passed to a worker
            // thread when there are no other running tasks
            lastRunningTaskSize = runningTasks.get();
            lastEligibleTasksSize = eligibleTasks.size();
            lastIsTaskNOTReady = !isTaskReady();
            if (lastRunningTaskSize == 0
                    && lastEligibleTasksSize == 0
                    && lastIsTaskNOTReady) {
                getLog().debug("TaskControl ending -- nothing left to run");
                return false;
            }
        }
        return true;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    /**
     * @param dumpTaskGroupStats the dumpTaskGroupStats to set
     */
    public void setDumpTaskGroupStats(boolean dumpTaskGroupStats) {
        this.dumpTaskGroupStats = dumpTaskGroupStats;
    }

    /**
     * @return the dumpTaskGroupStats
     */
    public boolean isDumpTaskGroupStats() {
        return dumpTaskGroupStats;
    }

    /**
     * this class actually moves the eligible tasks from the eligible
     * list to the {@link PriorityBlockingQueue} the the current task executor
     * reads from to pick up new tasks to execute.
     * @author Patrick Moore
     */
    private class ProcessQueue implements Runnable {
        /**
         * Called from when new possible tasks are added or when a running task
         * completes its execution. Responsible for determining which of the
         * possible tasks should be transfered to the eligibleTasks queue that
         * is used by the PooledExecutor to chose the next task.
         */
        public void run() {
            while (isTaskReady()) {
                // note that there is no guarentee that the task is the same as
                // this one
                final PrioritizedTask nextTask = nextTaskFromCurrentGroup();

                // verify that we have retrieved a valid object.
                if (!nextTask.isReadyToRun()) {
                    throw new RuntimeException(
                            "PrioritizedTask contract violation, "
                                    + "or Comparator violation - why is this object not ready to run");
                }
                // wrap so that there is no reliance on the task doing the
                // correct notification.
                executor.execute(new TaskWrapper(nextTask) {
                    public void run() {
                        try {
                            getWrappedTask().run();
                        } finally {
                            taskComplete(getWrappedTask());
                        }
                    }
                });
                // we tell the executor to create additional threads because
                // otherwise
                // if min>#threads>max and a thread is waiting additional
                // threads will
                // not be created even if that means that there are waiting
                // tasks to be
                // run (12/07/2005).
                executor.prestartCoreThread();
                /*
                 * Incrementing runningTasks must occur here, not by a call from
                 * nextTask.run(). The reason is that otherwise there is a race
                 * condition: 1. eligibleTasks isEmpty 2. nextTask added to
                 * eligibleTasks 3. the worker thread pulls off nextTask from
                 * eligibleTasks (eligibleTasks empty again) 4.
                 * TaskControl.stillRunning executes and determines it should
                 * shutdown because the eligibleTask queue is empty and there
                 * are no more tasks that can run. 5. worker thread tries to run
                 * nextTask.
                 */
                runningTasks.incrementAndGet();
                runningTaskList.add(nextTask);
            }
        }

        private PrioritizedTask nextTaskFromCurrentGroup() {
            PrioritizedTask nextTask = null;
            do {
                TaskGroup<?> taskGroup = getCurrentTaskGroup();
                nextTask = taskGroup.nextTask();
            } while (nextTask == null);
            currentTaskGroup.incrementAndGet();
            return nextTask;
        }
    }
}
