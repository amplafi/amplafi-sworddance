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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sworddance.util.ApplicationGeneralException;

/**
 * Manages the lock lists for each resource.
 */
public class ResourceLockManager {
    private ConcurrentMap<String, LinkedList<ResourceLock>> lockLists = new ConcurrentHashMap<String, LinkedList<ResourceLock>>();

    private Set<PrioritizedTask> tasksAdded = new HashSet<PrioritizedTask>();

    public static final String GLOBALRESOURCE = "$GLOBAL$";

    /**
     * Add the locks that this tasks indicates that it needs to the end of the
     * lock list for that resource. New resources and their accompanying lock
     * list are created automatically. Tasks are only allowed to add their locks
     * once.
     *
     * If a lock is exclusive then all following locks on that resource are
     * blocked. If a lock is nonexclusive then all following exclusive loads are
     * blocked.
     *
     * If a task does not have an explicit lock on the {@link #GLOBALRESOURCE}
     * resource, then an nonexclusive lock is created.
     *
     * If a task has a global ({@link #GLOBALRESOURCE}), exclusive lock as
     * part of its list then there is additional behavior. All existing
     * resources have a exclusive lock created for 'task' and inserted at the
     * end of the resource's lock list. If any new resources are created, then
     * the tasks with global, exclusive locks have additional exclusive locks
     * created on those new resources.
     *
     * @param task
     */
    public void addTaskLocks(PrioritizedTask task) {
        addTaskLocks(task, new DefaultInsertionPoint());
    }

    public synchronized void addTaskLocks(PrioritizedTask task,
            Comparator<ResourceLock> insertionComparator) {
        synchronized (tasksAdded) {
            // avoid possible circular dependency
            if (tasksAdded.contains(task)) {
                // TODO if error when adding a task. then how to
                // unravel the mess?
                throw new RuntimeException(task.getName()
                        + ":task already has added locks");
            }
            tasksAdded.add(task);
        }
        ResourceLock globalLock = null;
        // make a copy to avoid ConcurrentModification when looping through this
        // list
        ArrayList<ResourceLock> taskLocks = new ArrayList<ResourceLock>(task.getResourceLocksNeeded());
        // make sure the locks do not have assignments already
        for (ResourceLock lock: taskLocks) {
            lock.setTask(task);
        }
        for (ResourceLock lock : taskLocks) {
            if (GLOBALRESOURCE.equals(lock.getResourceName())) {
                globalLock = lock;
                // an explicit global lock is being added, so existing resources
                // need to have
                // placeholder locks added. -- bug if there are 2 global locks
                // for the task
                if (globalLock.isExclusiveLock()
                        && !globalLock.isLockReleased()) {
                    for (Map.Entry<?, ?> entry: lockLists.entrySet()) {
                        String resourceName = entry.getKey().toString();
                        if (!GLOBALRESOURCE.equals(resourceName)) {
                            ResourceLock newLock = new ResourceLock(
                                    resourceName, globalLock.getLockType());
                            addGeneratedLock(task, newLock, insertionComparator);
                        }
                    }
                }
            }
            addLock(lock, insertionComparator);
        }

        // if explicit global lock not specified then create a nop lock that
        // will be use to preserve relative ordering when global exclusive locks
        // are added.
        if (globalLock == null) {
            globalLock = createGlobalResourceNonexclusiveLock();
            addGeneratedLock(task, globalLock, insertionComparator);
        }
    }

    public static ResourceLock createGlobalResourceNonexclusiveLock() {
        return new ResourceLock(GLOBALRESOURCE, ResourceLock.NONEXCLUSIVE);
    }

    private void addGeneratedLock(PrioritizedTask task, ResourceLock newLock,
            Comparator<ResourceLock> insertionComparator) {
        newLock.setGenerated();
        newLock.setTask(task);
        task.getResourceLocksNeeded().add(newLock);
        addLock(newLock, insertionComparator);
    }

    /**
     * add a single lock. There are 2 general cases handled by this method.
     *
     * @param lock
     * @param insertionComparator
     */
    private void addLock(ResourceLock lock, Comparator<ResourceLock> insertionComparator) {
        // TODO get rid of this cast
        PrioritizedTask task = (PrioritizedTask) lock.getTask();
        if (!(task instanceof DependentPrioritizedTask)
                || ((DependentPrioritizedTask) task).getParentTask() == null) {
            addNonsubtaskLock(lock);
            return;
        } else {
            addSubtaskLock(lock, insertionComparator);
        }
    }

    /**
     * Insert a lock for a subtask. NOtice that it is very dangerous to add a
     * subtask lock
     *
     * @param lock
     * @param insertionComparator
     */
    private void addSubtaskLock(ResourceLock lock,
            Comparator<ResourceLock> insertionComparator) {
        // TODO get rid of this cast
        PrioritizedTask task = (PrioritizedTask) lock.getTask();
        List<ResourceLock> lockList = getResourceLockList(lock
                .getResourceName());
        // find insertion point of lock for subtasks
        // non-subtasks are always add to the end.
        // first find parent.
        ListIterator<ResourceLock> iter;
        ResourceLock currentLock = null;
        boolean foundInsertPoint = false;
        for (iter = lockList.listIterator(); iter.hasNext();) {
            currentLock = iter.next();
            // TODO get rid of this cast
            PrioritizedTask currentTask = (PrioritizedTask) currentLock.getTask();
            if (currentTask == task) {
                combineLocks(iter, lock, currentLock);
                return;
            }
            // thread safety note: task could change its parent task.
            else if (((DependentPrioritizedTask) task).getParentTask() == currentTask) {
                // found starting potential insertion point
                foundInsertPoint = true;
                break;
            }
        }
        // subtasks are not allowed to have any more locks or stronger locks
        // than
        // the parent. Nor are they allowed to have locks on any resource that a
        // parent
        // does not have a lock on. This is
        if (!foundInsertPoint) {
            throw new RuntimeException(lock + ":could not find a parent lock");
        }
        if (!lock.isSubset(currentLock)) {
            throw new RuntimeException(lock
                    + ": subtask not a subset of parent lock " + currentLock);
        }
        if (iter.hasNext()) {
            // there are more locks after the parent. let the comparator search
            // for the correct insertion point
            for (; iter.hasNext();) {
                currentLock = iter.next();
                int compareValue = insertionComparator.compare(lock,
                        currentLock);
                if (compareValue == 0) {
                    combineLocks(iter, lock, currentLock);
                    return;
                } else if (compareValue < 0) {
                    // went too far go back and exit.
                    iter.previous();
                    break;
                }
            }
        }
        iter.add(lock);
    }

    /**
     * This is used to add the task lock to the end of the list.
     *
     * @param lockList
     *
     */
    private void addNonsubtaskLock(ResourceLock lock) {
        List<ResourceLock> lockList = getResourceLockList(lock.getResourceName());
        // TODO get rid of this cast
        PrioritizedTask task = (PrioritizedTask) lock.getTask();
        // the only place that there should be a lock that is found by this loop
        // is at the
        // end of the list. The code does this extra work to make sure that this
        // condition is held.
        for (ListIterator<ResourceLock> iter = lockList.listIterator(); iter.hasNext();) {
            ResourceLock currentLock = iter.next();
            if (currentLock.getTask() == task) {
                combineLocks(iter, lock, currentLock);
                if (iter.hasNext()) {
                    throw new RuntimeException(
                            "Should only be modifying lock at the end of the list");
                }
                return;
            }
        }
        lockList.add(lock);
    }

    /**
     * Each task is allowed one and only one lock per resource. Two locks need
     * to be combined. This method combines them. The surviving lock is inserted
     * into the current position into iter.
     *
     * @param iter
     * @param lock
     *            lock that is attempting to be added.
     * @param currentLock
     *            link in the list being iterated through by iter
     */
    private void combineLocks(ListIterator<ResourceLock> iter, ResourceLock lock,
            ResourceLock currentLock) {
        // all locks for same task get combined at the same point.
        // locks may be created by task or added by ResourceLockManager in
        // response to
        // global locks
        ResourceLock successorLock = currentLock.combine(lock);
        ResourceLocker task = lock.getTask();
        if (successorLock == lock) {
            task.getResourceLocksNeeded().remove(currentLock);
            iter.remove();
            iter.add(lock);
        } else {
            task.getResourceLocksNeeded().remove(lock);
            if (task.getResourceLocksNeeded().contains(lock)) {
                throw new RuntimeException("Tried to combine lock " + lock
                        + "(id=" + System.identityHashCode(lock)
                        + ") but actually removed something else");
            }
        }
    }

    /**
     * called when a task has completed.
     *
     * @param task
     */
    public void releaseTaskLocks(ResourceLocker task) {
        Collection<ResourceLock> taskLocks = task.getResourceLocksNeeded();
        if (taskLocks != null) {
            for (ResourceLock lock : taskLocks) {
                lock.releaseLock();
            }
        }
    }

    /**
     *
     * @param task
     * @return true if the task has any locks in front of it that will block the
     * task from running.
     */
    public boolean isTaskLocksAvailable(ResourceLocker task) {
        Collection<ResourceLock> taskLocks = task.getResourceLocksNeeded();
        if (taskLocks == null) {
            return true;
        }
        for (ResourceLock lock: taskLocks) {
            List<ResourceLock> lockList = getResourceLockListCopy(lock.getResourceName(),
                    true);
            // race condition: because lock maybe released by another thread,
            // need to check after retrieving lockList.
            if (lock.isLockReleased()) {
                continue;
            }
            ResourceLock current = null;
            for (Iterator<ResourceLock> iter0 = lockList.iterator(); iter0.hasNext();) {
                current = iter0.next();
                if (current == lock) {
                    break;
                } else if (lock.isExclusiveLock()) {
                    return false;
                } else if (current.isExclusiveLock()) {
                    return false;
                }
            }
            if (current != lock) {
                throw new RuntimeException("Did not find lock");
            }
        }
        return true;
    }

    /**
     * return the list of locks for the given resource. copy over exclusive,
     * unreleased locks from the global list. This will allow the tasks with
     * global exclusive locks to be able to create subtasks that insert
     * selective locks. If released locks are not pruned then the actual list is
     * returned. This is important for combineLocks. combineLocks should be
     * changed to remove this dependency.
     *
     * @param resourceName
     * @return actual list
     */
    private List<ResourceLock> getResourceLockList(String resourceName) {
        // TODO : remove sync block.
        synchronized (lockLists) {
            LinkedList<ResourceLock> list = lockLists.get(resourceName);
            if (list == null) {
                list = new LinkedList<ResourceLock>();
                lockLists.put(resourceName, list);

                LinkedList<ResourceLock> globalList = lockLists.get(GLOBALRESOURCE);
                if (globalList != null) {
                    for (ListIterator<ResourceLock> iter = globalList.listIterator(); iter.hasNext();) {
                        ResourceLock lock = iter.next();
                        if (lock.isExclusiveLock()) {
                            ResourceLock newLock = new ResourceLock(resourceName, lock.getLockType());
                            ResourceLocker task = lock.getTask();
                            newLock.setTask(task);
                            // if the lock is released then a released lock is
                            // added so the resource map has the complete history.
                            if (lock.isLockReleased()) {
                                newLock.releaseLock();
                            }
                            task.addLock(newLock);
                            list.add(newLock);
                        }
                    }
                }
            }
            return list;
        }
    }

    private List<ResourceLock> getResourceLockListCopy(String resourceName,
            boolean pruneReleasedLocks) {
        List<ResourceLock> result = new ArrayList<ResourceLock>(getResourceLockList(resourceName));
        if (pruneReleasedLocks) {
            for (Iterator<ResourceLock> iter = result.iterator(); iter.hasNext();) {
                ResourceLock lock = iter.next();
                if (lock.isLockReleased()) {
                    iter.remove();
                }
            }
        }
        return result;
    }

    /**
     *
     * @param task
     * @param pruneReleased
     * @return the dependencies if this task was to add its lock requests.
     */
    public synchronized Collection<ResourceLocker> getDependentTasks(
            DependentPrioritizedTask task, boolean pruneReleased) {
        Set<ResourceLocker> dependencies = new HashSet<ResourceLocker>();
        Collection<?> taskLocks = task.getResourceLocksNeeded();
        if (taskLocks != null) {
            for (Object name : taskLocks) {
                ResourceLock lock = (ResourceLock) name;
                List<ResourceLock> lockList = getResourceLockListCopy(lock.getResourceName(), pruneReleased);
                // race condition: if the lock is released it is not going to be
                // on the
                // list so check after retrieving the list.
                if (pruneReleased && lock.isLockReleased()) {
                    continue;
                }
                ResourceLock current = null;
                for (Iterator<ResourceLock> iter0 = lockList.iterator(); iter0.hasNext();) {
                    current = iter0.next();
                    if (current == lock) {
                        break;
                    } else if (current.getTask() == lock.getTask()) {
                        // TODO should never happen. each task is only allowed
                        // one lock.
                        throw new RuntimeException(
                                "Task has multiple locks on resource "
                                        + lock.getResourceName());
                    }
                    // the current lock is an exclusive lock that is not shared
                    // or has a different dependent
                    // community.
                    else if (lock.isSharedLock() && current.isSharedLock()) {
                        if (!isLockBeingShared(lock, current)) {
                            dependencies.add(current.getTask());
                        }
                    } else if (lock.isExclusiveLock()) {
                        dependencies.add(current.getTask());
                    } else if (current.isExclusiveLock()) {
                        dependencies.add(current.getTask());
                    }
                }
                if (current != lock) {
                    throw new ApplicationGeneralException(task + ": Did not find lock "
                            + lock.getLockStr() + " on list for resource="
                            + lock.getResourceName());
                }
            }
        }
        return dependencies;
    }

    /**
     * @param lock
     * @param current
     * @return
     */
    private boolean isLockBeingShared(ResourceLock lock, ResourceLock current) {
        if (!lock.isSharedLock() || !current.isSharedLock()) {
            return false;
        } else {
            DependentPrioritizedTask task = (DependentPrioritizedTask) lock.getTask();
            DependentPrioritizedTask currentTask = (DependentPrioritizedTask) current.getTask();
            // shared lock but is it being shared with 'current'?
            return task.getParentTask() != null
                    && (task.getParentTask() == currentTask.getParentTask()
                            || task.getParentTask() == currentTask);
        }
    }

    /**
     * @return a global exclusive lock.
     */
    public static ResourceLock createGlobalExclusiveResourceLock() {
        return new ResourceLock(GLOBALRESOURCE, ResourceLock.GLOBALLOCKTYPE);
    }

    /**
     * determines which tasks are eligible to be considered for running based on
     * the locks. Notice that this is not necessarily a complete list possible
     * tasks as there maybe some tasks which have no locks at all and thus the
     * ResourceLockManager is not aware of them.
     *
     * When scanning the list, the locks being examined may be released.
     *
     * @return tasks available to run.
     */
    public synchronized List getUnblockedTasks() {
        List<ResourceLock> globalLocks = getResourceLockListCopy(GLOBALRESOURCE, true);
        // has first unreleased lock been found?
        boolean foundOne = false;
        List<Ordering> list = new ArrayList<Ordering>();
        Set<ResourceLocker> emptyResourceLockers = Collections.emptySet();
        ELIGIBLES:
        for (ListIterator<ResourceLock> iter = globalLocks.listIterator(); iter.hasNext();) {
            ResourceLock currentGlobalLock = iter.next();
            ResourceLocker currentTask = currentGlobalLock.getTask();
            if (currentGlobalLock.isExclusiveLock()) {
                if (foundOne) {
                    // after a bunch of nonexclusive global lock, lock is now at
                    // a exclusive lock which will stop the process.
                    break;
                } else {
                    // first unreleased lock is an exclusive lock
                    list.add(new Ordering(currentGlobalLock.getTask(), Integer.MAX_VALUE, emptyResourceLockers));
                    break;
                }
            } else {
                foundOne = true;
                Set<ResourceLocker> blockedSet = new HashSet<ResourceLocker>();
                int blockedExclusives = 0;
                // look at current task's nonglobal locks and see if the locks
                // are available
                for (ResourceLock currentTaskLock : currentTask.getResourceLocksNeeded()) {
                    if (GLOBALRESOURCE.equals(currentTaskLock.getResourceName())) {
                        continue;
                    } else if (currentTaskLock.isLockReleased()) {
                        continue;
                    } else {
                        // determine if this particular lock that the task is
                        // requesting causes
                        // the task not to be eligible to run. Note that there
                        // may be other locks or reasons
                        // that the task is blocked by.
                        List<ResourceLock> otherResources = getResourceLockListCopy(currentTaskLock.getResourceName(), true);
                        if (currentTaskLock.isLockReleased()) {
                            // Check for race condition: the lock was just
                            // released.
                            // The otherResources list probably doesn't have
                            // currentTaskLock in it.
                            continue;
                        }
                        ResourceLock otherTaskResourcesLock = null;
                        ListIterator<ResourceLock> iter1;
                        for (iter1 = otherResources.listIterator(); iter1.hasNext();) {
                            otherTaskResourcesLock = iter1.next();
                            ResourceLocker otherTask = otherTaskResourcesLock.getTask();
                            if (otherTaskResourcesLock == currentTaskLock) {
                                // found the lock for the current task we are
                                // currently checking
                                // there are no locks on this resource blocking
                                // this task from being run
                                break;
                            } else if (otherTaskResourcesLock.getTask() == currentTaskLock.getTask()) {
                                // TODO should never happen. each task is only
                                // allowed one lock.
                                throw new RuntimeException(currentGlobalLock.getTask() + ":Task has multiple locks on resource "
                                        + currentGlobalLock.getResourceName());
                            } else if (otherTaskResourcesLock.isExclusiveLock()) {
                                // found another task's exclusive lock on a
                                // resources
                                // needed by this task.
                                if (otherTaskResourcesLock.isSharedLock()
                                        && currentTaskLock.isSharedLock()
                                        && (otherTask instanceof DependentPrioritizedTask)
                                        && (currentTask instanceof DependentPrioritizedTask)
                                        && ((DependentPrioritizedTask) otherTask).getParentTask() == ((DependentPrioritizedTask) currentTask)
                                                .getParentTask()) {
                                    // they are sharing access.
                                    continue;
                                } else {
                                    // currentTask is blocked by otherTask
                                    continue ELIGIBLES;
                                }
                            } else if (currentTaskLock.isExclusiveLock()) {
                                // found another task's nonexclusive lock.
                                // The lock
                                // currently looking for is an exclusive lock.
                                // This task is not eligible
                                continue ELIGIBLES;
                            }
                        }
                        // make sure lock was found on list.
                        if (otherTaskResourcesLock != currentTaskLock) {
                            throw new RuntimeException(currentTaskLock + ": lock not found on resource manager list - was it added correctly?");
                        }
                        if (!currentTaskLock.isExclusiveLock()) {
                            // non-exclusive locks block only starting at the
                            // next
                            // exclusive lock.
                            for (; iter1.hasNext();) {
                                ResourceLock l = iter1.next();
                                if (l.isExclusiveLock()) {
                                    iter1.previous();
                                    break;
                                }
                            }
                        }
                        // exclusive locks block everyone that follows
                        for (; iter1.hasNext();) {
                            ResourceLock l = iter1.next();
                            if (l.isExclusiveLock()) {
                                blockedExclusives++;
                            }
                            blockedSet.add(l.getTask());
                        }
                    }
                }
                // no blocking tasks
                list.add(new Ordering(currentGlobalLock.getTask(), blockedExclusives, blockedSet));
            }
        }
        Collections.sort(list, new CompareOrdering());
        List<ResourceLocker> returnList = new ArrayList<ResourceLocker>();
        for (Ordering ordering: list) {
            returnList.add(ordering.task);
        }
        return returnList;
    }

    public boolean hasPreviousResourceLockOfType(PrioritizedTask task,
            String resourceName, int lockTypeLookingFor) {
        resourceName = resourceName.toUpperCase();
        List<ResourceLock> resourceList = getResourceLockListCopy(resourceName, false);
        for (ResourceLock lock : resourceList) {
            if (lock.getTask() == task) {
                return false;
            } else if (lock.isLock(lockTypeLookingFor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return
     * a list of csv strings representing all the lock lists currently
     * managed by this ResourceLockManager.
     */
    public List<String> generateLockMatrix() {
        List<List<?>> lists = new ArrayList<List<?>>();
        List<String> locksLines = new ArrayList<String>();
        StringBuffer sb = new StringBuffer("Task Name,Global,");
        for (Map.Entry<?, ?> entry: lockLists.entrySet()) {
            if (!GLOBALRESOURCE.equalsIgnoreCase((String) entry.getKey())) {
                sb.append(entry.getKey().toString()).append(',');
                List<?> l = (List<?>) entry.getValue();
                lists.add(l);
            }
        }
        int[] indexes = new int[lists.size()];
        locksLines.add(sb.toString());
        List<?> globalList = lockLists.get(GLOBALRESOURCE);
        if (globalList != null) {
            for (Object name : globalList) {
                ResourceLock lock = (ResourceLock) name;
                sb.setLength(0);
                sb.append(lock.getTask()).append(',').append(lock.getLockStr())
                        .append(',');
                for (int i = 0; i < lists.size(); i++) {
                    List<?> list = lists.get(i);
                    if (indexes[i] < list.size()) {
                        ResourceLock l = (ResourceLock) list.get(indexes[i]);
                        if (l.getTask() == lock.getTask()) {
                            sb.append(l.getLockStr()).append(',');
                            indexes[i]++;
                        } else {
                            sb.append("-,");
                        }
                    } else {
                        sb.append("-,");
                    }
                }
                locksLines.add(sb.toString());
            }
        }
        return locksLines;
    }

    /**
     * only called after the iterator is at a valid insertion point. Default
     * behavior is that the lock is inserted immediately the last previous
     * subtask for this parent.
     *
     */
    private static class DefaultInsertionPoint implements Comparator<ResourceLock> {
        // TODO for now always assume that l1 is the lock to be added
        /**
         * Sort 2 resource locks. t1 is the parent of t2 then t1 needs to come
         * before t2 t1 == t2 then so indicate (locks should be combined) t1 has
         * explicit dependency on t2 then t1 comes after t2. t2 has explicit
         * dependency on t1 then t2 comes after t1. t1's parent == t2's parent
         * then put t1 after t2
         * @param l1
         * @param l2
         *
         * @return comparison order between l1 and l2
         */
        public int compare(ResourceLock l1, ResourceLock l2) {
            PrioritizedTask t1 = (PrioritizedTask) l1.getTask();
            PrioritizedTask t2 = (PrioritizedTask) l2.getTask();
            if (t1 == t2) {
                return 0;
            } else if (!(t1 instanceof DependentPrioritizedTask)
                    || !(t2 instanceof DependentPrioritizedTask)) {
            	// HACK: bad because order is randomly determined by the argument ordering.
                return -1;
            }
            DependentPrioritizedTask task1 = (DependentPrioritizedTask) l1.getTask();
            DependentPrioritizedTask task2 = (DependentPrioritizedTask) l2.getTask();
            if (task1 == task2.getParentTask()) {
                return -1;
            } else if (task1.getParentTask() == task2) {
                return 1;
            } else if (task1.isDependentOn(task2)) {
                return 1;
            } else if (task2.isDependentOn(task1)) {
                return -1;
            } else if (task1.getParentTask() == task2
                    .getParentTask()) {
                return 1;
            } else if (task1.getParentTask() != null) {
                return -1;
            } else if (task2.getParentTask() != null) {
                return 1;
            } else {
                // TODO bad violates Comparator contract
                // say that t1 will be put at the end of the list.
                return -1;
            }
        }
    }

    /**
     * Holds the information showing which other tasks a given task is blocking.
     */
    private static class Ordering {
        final ResourceLocker task;

        final Set<ResourceLocker> blockedSet;

        final int blockedExclusives;

        public Ordering(ResourceLocker task, int blockedExclusives,
                Set<ResourceLocker> blockedSet) {
            this.task = task;
            this.blockedExclusives = blockedExclusives;
            this.blockedSet = blockedSet;
        }
    }

    /**
     * used to sort the possible tasks that are eligible. Sort order is the
     * number of tasks that the given task is blocking by itself. For example if
     * a task with a read lock is the last lock before an exclusive lock, then
     * it is considered blocking. If 2 read tasks are blocking an exclusive
     * task, then that doesn't count.
     *
     */
    private static class CompareOrdering implements Comparator<Ordering> {
        public int compare(Ordering o1, Ordering o2) {
            if (o1.task == o2.task) {
                return 0;
            } else if (o1.blockedExclusives != o2.blockedExclusives) {
                return o2.blockedExclusives - o1.blockedExclusives;
            } else {
                return o2.blockedSet.size() - o2.blockedSet.size();
            }
        }
    }

    /**
     * @param postRunResourceMapFile
     */
    public void dumpLockList(File postRunResourceMapFile) {
        FileWriter fos = null;
        try {
            fos = new FileWriter(postRunResourceMapFile);
            for (String line: generateLockMatrix()) {
                fos.write(line);
                fos.write('\n');
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
