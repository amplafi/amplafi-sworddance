/**
 *
 */
package com.sworddance.taskcontrol;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;

import static java.util.concurrent.TimeUnit.*;

/**
 * defines a collection of tasks that are interdependent. This class is
 * responsible for supplying the next task from this group that should be run.
 * The determination of which task should be next is made by the
 * {@link java.util.Comparator} that is supplied to the constructor.
 * @param <T> the type of the results object.
 */
public class TaskGroup<T> implements NotificationObject {
    private TaskControl taskControl;

    private ResourceLockManager resourceManager = new ResourceLockManager();

    private final ThreadHistoryTracker threadHistoryTracker;

    private final String name;

    private final AtomicInteger taskSequence;

    private final List<PrioritizedTask> tasksToBeRun;

    /**
     * set to indicate that TaskGroup has been told to shutdown. No more tasks
     * can be added and all pending tasks are discarded.
     */
    private CountDownLatch shutdownTaskGroup = new CountDownLatch(1);

    /**
     * tasks that do not use the resource locking mechanism.
     */
    private final List<PrioritizedTask> locklessTasks;

    private String statsFileDirectory;

    private boolean debugEnabled;
    private static final SimpleDateFormat FORMATER = new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss");

    /**
     * These are tasks that can never run. This list is used as a holding bin
     * for later reporting.
     */
    private final List<PrioritizedTask> deadTasks;

    private final Comparator<PrioritizedTask> taskComparator;

    private Log log;

    private Semaphore groupLevelLock = new Semaphore(1);

    /**
     * names, start, stop times of completed tasks.
     */
    private List<String> tasksCompletedInfo = new CopyOnWriteArrayList<String>();

    /*
     * Used to indicate when all the tasks in this TaskGroup have completed. And
     * what the overall state of the Tasks run within the TaskGroup is.
     */
    private final FutureResult<T> result;

    /**
     * used to aid in debugging. This will hold a reference to the last task
     * that caused {@link #isTaskReady()} to return true
     */
    @SuppressWarnings("unused")
    private WeakReference<PrioritizedTask> lastEligibleTask;

    private Set<PrioritizedTask> runningTasks = Collections.newSetFromMap(new ConcurrentHashMap<PrioritizedTask, Boolean>());
    private String latestStatsFilename;

    public TaskGroup(String name, Comparator<PrioritizedTask> taskComparator, FutureResult<T> result) {
        this.name = name;
        this.taskComparator = taskComparator;
        this.result = result;
        taskSequence = new AtomicInteger(0);
        tasksToBeRun = new ArrayList<PrioritizedTask>();
        locklessTasks = new ArrayList<PrioritizedTask>();
        deadTasks = new ArrayList<PrioritizedTask>();
        threadHistoryTracker = new ThreadHistoryTracker();
    }

    /**
     * Use default Comparator. Currently {@link PossibleWorkItemComparator}
     *
     * @param name
     */
    public TaskGroup(String name) {
        this(name, new PossibleWorkItemComparator(), new FutureResult<T>());
    }
    public TaskGroup(String name, FutureResult<T> result) {
        this(name, new PossibleWorkItemComparator(), result);
    }

    /**
     * @return taskGroup's result exception.
     */
    public Throwable getError() {
        Throwable exception = result.getException();
        return exception == null ? null : exception;
    }

    public FutureResult<T> getResult() {
        return result;
    }

    private void taskStart(PrioritizedTask task) {
        String id = getTaskId(task);
        if (task instanceof DefaultDependentPrioritizedTask) {
            threadHistoryTracker.addStartHistory(id, "starting",
                    ((DefaultDependentPrioritizedTask) task)
                            .getDependenciesStr());
        } else {
            threadHistoryTracker.addStartHistory(id, "starting", null);
        }
    }

    private String getTaskId(PrioritizedTask task) {
        return task.getName() != null ? task.getName() : task.getClass()
                .getName()
                + ":" + System.identityHashCode(task);
    }

    /**
     * called by each of the TaskGroup tasks as it completes execution.
     * @param task
     */
    private void taskComplete(PrioritizedTask task) {
        runningTasks.remove(task);
        String id = getTaskId(task);
        resourceManager.releaseTaskLocks(task);
        if (task.getError() != null) {
            synchronized (result) {
                // save only the first error
                if (result.getException() == null) {
                    result.setException(task.getError());
                }
            }
            if (task instanceof DefaultPrioritizedTask) {
                threadHistoryTracker.addStopHistory(id,
                        task.getError().toString(),
                        ((DefaultPrioritizedTask) task).getElapsed()
                        + "ms",
                        ((DefaultPrioritizedTask) task).getElapsed());
            } else {
                threadHistoryTracker.addStopHistory(id,
                        task.getError().toString(), null, 0);
            }
        } else {
            if (task instanceof DefaultPrioritizedTask) {
                threadHistoryTracker.addStopHistory(id, "completed",
                        ((DefaultPrioritizedTask) task).getElapsed() + "ms",
                        ((DefaultPrioritizedTask) task).getElapsed());
            } else {
                threadHistoryTracker.addStopHistory(id, "completed", null, 0);
            }
        }
        String taskStatus = id + ":" + task.getStatus();
        debug("Task Completed:" + taskStatus);
        tasksCompletedInfo.add(taskStatus);

        if ( isTaskGroupTasksComplete()) {
            if ( !result.isDone()) {
                // no value yet in the result object
                // supply a result so that threads waiting on results will get notified.
                result.set(null);
            }
        }
    }

    public void addTaskStatus(PrioritizedTask task, String status) {
        String id = getTaskId(task);
        threadHistoryTracker.addHistoryStatus(id, status);
    }

    /**
     * used to notify taskControl that is actively taking jobs from this
     * TaskGroup that while before there may not have been a task available, the
     * situation has changed.
     *
     * @see NotificationObject#stateChanged()
     */
    public void stateChanged() {
        if (getTaskControl() != null) {
            getTaskControl().stateChanged();
        }
    }

    public int getSequence() {
        return taskSequence.incrementAndGet();
    }

    public void debug(String msg) {
        getALogger().debug(msg);
    }

    private Log getALogger() {
        if (getLog() == null && getTaskControl() != null) {
            return getTaskControl().getLog();
        } else {
            return getLog();
        }
    }

    public void warning(String object) {
        getALogger().warn(object);
    }

    /**
     * This is the method that all tasks must be added to the tasksToBeRun
     * collection.
     *
     * @param task
     */
    public void addTask(PrioritizedTask task) {
        this.addTask(task, null);
    }

    /**
     * This is the method that all tasks must be added to the tasksToBeRun
     * collection.
     * @param insertionPoint
     *
     * @param task
     */
    public void addTask(PrioritizedTask task, Comparator<ResourceLock> insertionPoint) {
        // this method is the only place where tasksToBeRun is to be updated.
        if (task.isDone()) {
            throw new IllegalStateException(task
                    + ": Task already has a result.");
        }
        task.setNotification(this);
        synchronized (tasksToBeRun) {
            if (!this.isShutdown()) {
                tasksToBeRun.add(task);
                task.setTaskGroup(this);
                if (!task.hasLocks()) {
                    synchronized (locklessTasks) {
                        locklessTasks.add(task);
                    }
                } else if (insertionPoint == null) {
                    this.resourceManager.addTaskLocks(task);
                } else {
                    this.resourceManager.addTaskLocks(task, insertionPoint);
                }
            }
        }
        stateChanged();
    }

    @SuppressWarnings("unchecked")
    public String getUnrunTasksListStr() {
        StringBuilder sb = new StringBuilder();
        List<PrioritizedTask> remainingTasksToBeRun;
        synchronized (tasksToBeRun) {
            remainingTasksToBeRun = new ArrayList<PrioritizedTask>(tasksToBeRun);
        }
        if (taskComparator instanceof PossibleWorkItemComparator) {
            PossibleWorkItemComparator comp = ((PossibleWorkItemComparator) taskComparator).clone();
            comp.setCompleteSort(true);
            Collections.sort(remainingTasksToBeRun, comp);
        } else {
            Collections.sort(remainingTasksToBeRun, taskComparator);
        }
        for (PrioritizedTask task : remainingTasksToBeRun) {
            if (task instanceof DefaultDependentPrioritizedTask) {
                sb.append(task.getName()).append('[');
                sb.append("resource={");
                Collection l = this.resourceManager.getDependentTasks(
                        (DependentPrioritizedTask) task, true);
                for (DependentPrioritizedTask dependency: (Collection<DependentPrioritizedTask>) l) {
                    if (!dependency.isDone()) {
                        sb.append(dependency.getName()).append(' ');
                    }
                }
                sb.append("} direct={");
                ((DefaultDependentPrioritizedTask) task)
                        .showUnsatisfiedDependencies(sb);
                sb.append("}]\n");
            } else {
                sb.append("readyToRun = ").append(task.isReadyToRun()).append(
                        '\n');
            }
        }
        return sb.toString();
    }

    public String getDeadTaskStr() {
        StringBuilder sb = new StringBuilder();
        synchronized (tasksToBeRun) {
            if (taskComparator instanceof PossibleWorkItemComparator) {
                PossibleWorkItemComparator comp = ((PossibleWorkItemComparator) taskComparator).clone();
                comp.setCompleteSort(true);
                Collections.sort(deadTasks, comp);
            } else {
                Collections.sort(deadTasks, taskComparator);
            }
            for (PrioritizedTask task : deadTasks) {
                if (task instanceof DefaultDependentPrioritizedTask) {
                    sb.append(task.getName()).append("[dep=");
                    ((DefaultDependentPrioritizedTask) task)
                            .showUnsatisfiedDependencies(sb);
                    sb.append("]\n");
                } else {
                    sb.append("readyToRun = ").append(task.isReadyToRun());
                }
            }
            return sb.toString();
        }
    }

    /**
     * @return true if there is a task in this TaskGroup that is ready to run
     */
    @SuppressWarnings("unchecked")
    public boolean isTaskReady() {
        if (isShutdown()) {
            return false;
        }
        synchronized (tasksToBeRun) {
            List<PrioritizedTask> eligibleTasks = resourceManager.getUnblockedTasks();
            eligibleTasks.addAll(locklessTasks);
            boolean newDeadTasks;
            // go through this loop until there are no more deadTasks found.
            // this also makes sure that tasks that have 'always' run dependency
            // on a task will be in the correct state.
            // unless we repeat on finding a dead task we may not be if the
            // dependent
            // task occurs later in tasksToBeRun list
            do {
                newDeadTasks = false;
                for (PrioritizedTask task : eligibleTasks) {
                    if (task == null) {
                        // should never happen
                        tasksToBeRun.remove(null);
                    }
                    // skip tasks that may be running (and thus have not release
                    // their locks)
                    else if (!tasksToBeRun.contains(task)) {
                        continue;
                    } else if (task.isReadyToRun()) {
                        this.lastEligibleTask = new WeakReference<PrioritizedTask>(task);
                        // exiting here may mean that tasks that are now "neverEligible" to
                        // be run don't get a chance to set their "neverEligible" state. .. but
                        // that should be o.k.
                        return true;
                    } else if (task.isNeverEligibleToRun()) {
                        deadTasks.add(task);
                        warning(task.getName()
                                + ": Declaring itself never eligible to run. Moved to dead pool");
                        newDeadTasks = true;
                        tasksToBeRun.remove(task);
                    } else if (task.isDone()) {
                        // has a result without being run.
                        // most such cases should be caught by above code.
                        deadTasks.add(task);
                        newDeadTasks = true;
                        warning(task.getName()
                                + ": has result but has never run. Moved to dead pile.");
                        tasksToBeRun.remove(task);
                    }
                }
            } while (newDeadTasks);
            return false;
        }
    }

    public boolean isTaskGroupTasksComplete() {
        boolean complete = this.runningTasks.isEmpty();
        if ( complete ) {
            synchronized (tasksToBeRun) {
                if (!isTaskReady()) {
                    complete = tasksToBeRun.isEmpty();
                } else {
                    complete = false;
                }
            }
        }
        return complete;
    }

    /**
     * @return a task that is ready to run.
     */
    @SuppressWarnings("unchecked")
    public PrioritizedTask nextTask() {
        if (isShutdown()) {
            throw new IllegalStateException("TaskGroup has been shutdown");
        }
        List<PrioritizedTask> eligibleTasks = resourceManager.getUnblockedTasks();
        eligibleTasks.addAll(locklessTasks);
        synchronized (tasksToBeRun) {
            Collections.sort(eligibleTasks, taskComparator);
            PrioritizedTask nextTask = null;
            // skip tasks that may be running (and thus have not release their
            // locks)
            for (int i = 0; i < eligibleTasks.size(); i++) {
                nextTask = eligibleTasks.get(i);
                if (tasksToBeRun.contains(nextTask)) {
                    break;
                }
            }
            // Respect the order of the next two lines -- we never want either collection to both be empty
            // if there is still tasks to be run.
            runningTasks.add(nextTask);
            tasksToBeRun.remove(nextTask);
            synchronized (locklessTasks) {
                locklessTasks.remove(nextTask);
            }
            return new TaskWrapper(nextTask) {
                public void run() {
                    taskStart(getWrappedTask());
                    try {
                        getWrappedTask().run();
                    } finally {
                        taskComplete(getWrappedTask());
                    }
                }
            };
        }
    }

    public void setTaskControl(TaskControl taskControl) {
        // this is a potential threading problem if not checked for.
        if (this.taskControl != null && taskControl != this.taskControl) {
            throw new IllegalStateException("TaskGroup: '" + getName()
                    + "' already assigned to '" + taskControl.toString() + "'");
        }
        this.taskControl = taskControl;
    }

    public TaskControl getTaskControl() {
        return taskControl;
    }

    /**
     * Called when the task group is being prepared to be run. All last minute
     * initializations should occur here (for example getting database
     * connections)
     * @return true if the taskGroup can run, false if the TaskGroup should not be run.
     */
    public boolean prepareToRun() {
        if ( this.tasksToBeRun.isEmpty()) {
            this.result.set(null);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Called when no more tasks from this TaskGroup will be executed.
     * @param dumpStats should the TaskGroup execution information be dumped?
     *
     */
    public void shutdownNow(boolean dumpStats) {
        if (isShutdown()) {
            // already been told to shutdown
            return;
        }

        synchronized (tasksToBeRun) {
            this.shutdownTaskGroup.countDown();
            if (deadTasks.size() != 0) {
                String msg = getDeadTaskStr();
                warning(deadTasks.size() + " tasks were DOA :\n" + msg);
            }
            if (tasksToBeRun.size() != 0) {
                String msg = getUnrunTasksListStr();
                warning(tasksToBeRun.size() + " tasks were never run :\n" + msg);
                if (this.result.getException() == null) {
                    this.result.setException(new IllegalStateException(
                            tasksToBeRun.size() + " tasks were never run"));
                }
            }
            tasksToBeRun.clear();
        }
        if (isDebugEnabled()) {
            debug(this.tasksCompletedInfo.size()
                    + " completed tasks. Statuses:");
            for (String info: tasksCompletedInfo) {
                debug(info);
            }
        }
        if ( dumpStats ) {
            dumpStats();
        }
    }

    /**
     * @return
     */
    private boolean isShutdown() {
        synchronized (tasksToBeRun) {
            try {
                return this.shutdownTaskGroup.await(0, MILLISECONDS);
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    public void dumpStats() {
        File latestStatsFile = null;
        File postRunResourceMapFile = null;
        String dateStr = FORMATER.format(new Date());
        String exitStatus = getError() == null ? "-success" : "-fail";
        latestStatsFilename = getName() + exitStatus + "-stats-" + dateStr
                + ".csv";
        String postRunResourceMapFilename = getName() + "-post-resource-map-"
                + dateStr + ".csv";
        if (statsFileDirectory != null) {
            File directory = new File(statsFileDirectory);
            if (directory.isDirectory() && directory.canWrite()) {
                latestStatsFile = new File(directory, latestStatsFilename);
                if (latestStatsFile.exists() && !latestStatsFile.canWrite()) {
                    warning("can't write dumpfile "
                            + latestStatsFile.getAbsolutePath());
                    latestStatsFile = null;
                } else {
                    latestStatsFilename = latestStatsFile.getAbsolutePath();
                }

                postRunResourceMapFile = new File(directory,
                        postRunResourceMapFilename);
                if (postRunResourceMapFile.exists()
                        && !postRunResourceMapFile.canWrite()) {
                    warning("can't write dumpfile "
                            + postRunResourceMapFile.getAbsolutePath());
                    postRunResourceMapFile = null;
                } else {
                    postRunResourceMapFilename =
                        postRunResourceMapFile.getAbsolutePath();
                }
            }
        }
        if (latestStatsFile == null) {
            latestStatsFile = new File(latestStatsFilename);
        }
        if (postRunResourceMapFile == null) {
            postRunResourceMapFile = new File(postRunResourceMapFilename);
        }

        dumpLockList(postRunResourceMapFile);
        dumpStats(latestStatsFilename);
    }

    public void dumpLockList() {
        File postRunResourceMapFile = null;
        String dateStr = FORMATER.format(new Date());
        String postRunResourceMapFilename = getName() + "-resource-map-"
                + dateStr + ".csv";
        if (statsFileDirectory != null) {
            File directory = new File(statsFileDirectory);
            if (directory.isDirectory() && directory.canWrite()) {
                postRunResourceMapFile = new File(directory,
                        postRunResourceMapFilename);
                if (postRunResourceMapFile.exists()
                        && !postRunResourceMapFile.canWrite()) {
                    warning("can't write dumpfile "
                            + postRunResourceMapFile.getAbsolutePath());
                    postRunResourceMapFile = null;
                } else {
                    postRunResourceMapFilename = postRunResourceMapFile
                            .getAbsolutePath();
                }
            }
        }
        if (postRunResourceMapFile == null) {
            postRunResourceMapFile = new File(postRunResourceMapFilename);
        }

        dumpLockList(postRunResourceMapFile);
    }

    public boolean hasPreviousResourceLockOfType(PrioritizedTask task,
            String resourceName, int lockTypeLookingFor) {
        return this.resourceManager.hasPreviousResourceLockOfType(task,
                resourceName, lockTypeLookingFor);
    }

    public void dumpLockList(File file) {
        resourceManager.dumpLockList(file);
    }

    /**
     *
     * @return including directory information
     */
    public String getLatestStatsFilename() {
        return latestStatsFilename;
    }

    public String getRunTasksListStr() {
        StringBuffer sb = new StringBuffer();
        if (tasksCompletedInfo.size() > 0) {
            sb.append("Completed:\n");
            for (String info: tasksCompletedInfo) {
                sb.append(info);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    public String getName() {
        return name;
    }

    /**
     * add a task that will be dependent on all tasks added via
     * addPossibleTask() or addLinearTask(). This is useful for resulting in
     * cleaning up of data operations. cleanUpTask is considered a linear task.
     * cleanUpTask does not need to be the last thing added. It could simply be
     * away of making sure that all tasks added to this point have executed (or
     * errored out) before this task is run.
     *
     * @param cleanUpTask
     * @param alwaysRunTask
     *            always run this task even if the dependent tasks
     */
    public void addSerialTask(DependentPrioritizedTask cleanUpTask,
            boolean alwaysRunTask) {
        addTaskDependencies(cleanUpTask, alwaysRunTask);
        cleanUpTask.addLock(ResourceLockManager
                .createGlobalExclusiveResourceLock());
        addTask(cleanUpTask);
    }

    /**
     * used by tasks that are created by other tasks to make sure that all tasks
     * dependent on parent are dependent on subtask as well.
     *
     * @param parent
     * @param task
     */
    public void addSubtask(DependentPrioritizedTask parent,
            DependentPrioritizedTask task) {
        inheritDependentsOnParent(parent, task);
        addTask(task);
    }

    private void inheritDependentsOnParent(DependentPrioritizedTask parent,
            DependentPrioritizedTask task) {
        synchronized (tasksToBeRun) {
            for (Object element : tasksToBeRun) {
                DependentPrioritizedTask depTask =
                    (DependentPrioritizedTask) element;
                if (depTask.isSuccessDependentOn(parent)) {
                    depTask.addDependency(task);
                } else if (depTask.isAlwaysDependentOn(parent)) {
                    depTask.addAlwaysDependency(task);
                }
            }
        }
    }

    /**
     * Create a dependency to all tasks currently registered with this
     * TaskGroup.
     *
     * @param task
     * @param alwaysDependency
     */
    protected void addTaskDependencies(DependentPrioritizedTask task,
            boolean alwaysDependency) {
        synchronized (tasksToBeRun) {
            for (PrioritizedTask depTask : tasksToBeRun) {
                if (alwaysDependency) {
                    task.addAlwaysDependency(depTask);
                } else {
                    task.addDependency(depTask);
                }
            }
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * @param debugEnabled the debugEnabled to set
     */
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    /**
     * @return the debugEnabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * @param fileName output filename.
     */
    public void dumpStats(String fileName) {
        new CSVThreadHistoryTrackerFormatter(this.threadHistoryTracker)
                .dumpToFile(fileName);
    }

    /**
     * @return the semaphore to use when modifying global data for this
     *         taskGroup
     */
    public Semaphore getGroupLevelLock() {
        return groupLevelLock;
    }

    public void setStatsFileDirectory(String statsFileDirectory) {
        this.statsFileDirectory = statsFileDirectory;
    }

    public String getStatsFileDirectory() {
        return statsFileDirectory;
    }
}
