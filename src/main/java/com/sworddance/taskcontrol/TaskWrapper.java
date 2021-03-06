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

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * used by TaskControl and TaskGroup tasks to provide initialization and clean
 * up functionality.
 *
 * @author Patrick Moore
 *
 */
public abstract class TaskWrapper implements PrioritizedTask {
    private final PrioritizedTask wrappedTask;

    /**
     * @param nextTask
     */
    protected TaskWrapper(PrioritizedTask nextTask) {
        this.wrappedTask = nextTask;
    }

    /**
     * @see PrioritizedTask#isReadyToRun()
     */
    public boolean isReadyToRun() {
        return this.getWrappedTask().isReadyToRun();
    }


    /**
     * @see com.sworddance.taskcontrol.PrioritizedTask#getWrappedCallable()
     */
    @SuppressWarnings("unchecked")
    public <R> Callable<? extends R> getWrappedCallable() {
        return (Callable<? extends R>) wrappedTask.getWrappedCallable();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see PrioritizedTask#releaseToRun()
     */
    public void releaseToRun() {
        getWrappedTask().releaseToRun();
    }

    /**
     * @see PrioritizedTask#isSuccessful()
     */
    public boolean isSuccessful() {
        return getWrappedTask().isSuccessful();
    }

    /**
     * @see PrioritizedTask#isNeverEligibleToRun()
     */
    public boolean isNeverEligibleToRun() {
        return getWrappedTask().isNeverEligibleToRun();
    }

    /**
     * @see PrioritizedTask#setNotification(NotificationObject)
     */
    public void setNotification(NotificationObject notification) {
        getWrappedTask().setNotification(notification);
    }

    /**
     * @see PrioritizedTask#getSequence()
     */
    public int getSequence() {
        return getWrappedTask().getSequence();
    }

    /**
     * @see PrioritizedTask#getPriority()
     */
    public int getPriority() {
        return getWrappedTask().getPriority();
    }

    /**
     * @see PrioritizedTask#getStatus()
     */
    public String getStatus() {
        return getWrappedTask().getStatus();
    }

    /**
     * @see PrioritizedTask#getName()
     */
    public String getName() {
        return getWrappedTask().getName();
    }

    /**
     * @see PrioritizedTask#isDone()
     */
    public boolean isDone() {
        return getWrappedTask().isDone();
    }

    /**
     * @see PrioritizedTask#getException()
     */
    public Throwable getException() {
        return getWrappedTask().getException();
    }

    /**
     * @see PrioritizedTask#get()
     */
    public Object get() {
        return getWrappedTask().get();
    }

    public Object poll() {
        return getWrappedTask().poll();
    }
    /**
     * @see TaskGroupAware#setTaskGroup(TaskGroup)
     */
    public void setTaskGroup(TaskGroup<?> taskGroup) throws IllegalStateException {
        getWrappedTask().setTaskGroup(taskGroup);
    }

    /**
     * @see TaskGroupAware#getTaskGroup()
     */
    public TaskGroup<?> getTaskGroup() {
        return getWrappedTask().getTaskGroup();
    }

    public Collection<ResourceLock> getResourceLocksNeeded() {
        return getWrappedTask().getResourceLocksNeeded();
    }

    public void addLock(ResourceLock lock) {
        getWrappedTask().addLock(lock);
    }

    public boolean hasLocks() {
        return getWrappedTask().hasLocks();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.getWrappedTask().cancel(mayInterruptIfRunning);
    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.getWrappedTask().get(timeout, unit);
    }

    public boolean isCancelled() {
        return this.getWrappedTask().isCancelled();
    }

    public void addFutureListener(FutureListener futureListener) {
        this.getWrappedTask().addFutureListener(futureListener);
    }

    public boolean isFailed() {
        return this.getWrappedTask().isFailed();
    }

    public PrioritizedTask getWrappedTask() {
        return wrappedTask;
    }

    public PrioritizedTask getBaseWrappedTask() {
        return wrappedTask instanceof TaskWrapper ?
                (((TaskWrapper) wrappedTask).getWrappedTask())
                : wrappedTask;
    }

    public Serializable getMapKey() {
        return (Serializable) this.getWrappedTask().getMapKey();
    }
    @Override
    public String toString() {
        return "TaskWrapper around:"+this.getBaseWrappedTask();
    }
}
