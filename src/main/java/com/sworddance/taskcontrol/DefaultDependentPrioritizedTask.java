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
import java.util.Set;

import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.sworddance.util.CUtilities.*;

public class DefaultDependentPrioritizedTask<R> extends DefaultPrioritizedTask<R> implements DependentPrioritizedTask {
    protected final Set<PrioritizedTask> dependencyTasks = new CopyOnWriteArraySet<PrioritizedTask>();

    protected final Set<PrioritizedTask> cleanUpAfterTasks = new CopyOnWriteArraySet<PrioritizedTask>();

    protected String completionMsg = "Successful.";

    private DependentPrioritizedTask parentTask;

    private boolean ignoreTaskGroupFailure;

    public DefaultDependentPrioritizedTask() {
        super();
    }

    public DefaultDependentPrioritizedTask(Runnable wrapped) {
        super(wrapped);
        this.initTaskAware(wrapped);
    }

    public DefaultDependentPrioritizedTask(Callable<? extends R> callable) {
        this(callable.getClass().getName(), callable);
    }

    /**
     * @param priority the importance of this task. Higher is better.
     */
    public DefaultDependentPrioritizedTask(int priority) {
        super(null, priority);
    }

    public DefaultDependentPrioritizedTask(String name, Callable<? extends R> callable) {
        super(name, callable);
        this.initTaskAware(callable);
    }

    private void initTaskAware(Object wrapped) {
        if (wrapped instanceof TaskAware) {
            ((TaskAware) wrapped).setDependentPrioritizedTask(this);
        }
<<<<<<< HEAD
    }
=======
    }

    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public void setTaskGroup(TaskGroup<?> taskGroup) {
        for (PrioritizedTask element : this.dependencyTasks) {
            // this avoids circular dependencies
            if (element.getTaskGroup() == null) {
                throw new IllegalStateException( this.getName() + ": has dependent task ("+element.getName()+") that has not been assigned to a task group");
            }
        }
        super.setTaskGroup(taskGroup);
    }

    public boolean isDependentOn(PrioritizedTask task) {
        if (this.isSuccessDependentOn(task)) {
            return true;
        }
        return this.isAlwaysDependentOn(task);
    }

    public boolean isSuccessDependentOn(PrioritizedTask task) {
        return this.dependencyTasks.contains(task);
    }

    public boolean isAlwaysDependentOn(PrioritizedTask task) {
        return this.cleanUpAfterTasks.contains(task);
    }

    private boolean doDependencyCheck() {
        if (!this.ignoreTaskGroupFailure && this.getTaskGroup() != null && this.getTaskGroup().getError() != null) {
            this.setError(new RuntimeException("TaskGroup in error", this.getTaskGroup().getError()));
            this.getTaskGroup().debug(this.getName() + ": TaskGroup in error " + this.getTaskGroup().getError().getClass());
            return false;
        }
        for (PrioritizedTask dependency : this.dependencyTasks) {
            if (!dependency.isSuccessful()) {
                if (dependency.isDone()) {
                    // dependency failed ... this task will never be run
                    Throwable error = dependency.getError();
                    this.setError(new RuntimeException("Dependency "+ dependency.getName() + " failed.", error));
                    this.getTaskGroup().warning( this.getName() + "Dependency " + dependency.getName() + " failed. "+ error);
                }
                return false;
            }
        }
        for (PrioritizedTask dependency : this.cleanUpAfterTasks) {
            if (!dependency.isDone()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Once this method returns true, the method is not allowed to return false.
     * Because of this calling this method cause dependencies not to be able to
     * be added
<<<<<<< HEAD
     */
=======
     */
    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public boolean isReadyToRun() {
        // this task already has some result.
        if (super.isDone()) {
            return false;
        }
        if (super.isReadyToRun()) {
            return true;
        }
        boolean depCheck = this.doDependencyCheck();
        if (!depCheck) {
            return false;
        }
        // all dependencies satisfied ... make sure that the answer does not
        // change.
        super.releaseToRun();
        return true;
<<<<<<< HEAD
    }
=======
    }

    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public boolean isNeverEligibleToRun() {
        if (super.isDone()) {
            // it did run...
            return false;
        }
        boolean depCheck = this.doDependencyCheck();
        if (depCheck) {
            // next time isReadyToRun happens this task maybe ready to run.
            return false;
        }
        return this.isDone();
<<<<<<< HEAD
    }
=======
    }

    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public void releaseToRun() {
        // do nothing
    }

    public boolean isDependencyAddable() {
        return !super.isReadyToRun();
    }

    /**
     * Indicated that this task is dependent on another task's successful
     * execution. All dependencies should be added before being added to the
     * TaskControl. It is possible to add more dependencies if there
     * {@link #isReadyToRun()} has never returned true (there has always been at
     * least one unsatisfied dependency) but this is not advised.
     *
     * @param dependency
     *            The task that must be successfully completed before this task
     *            can execute.
     */
    public void addDependency(PrioritizedTask dependency) {
        this.checkCanAddDependency(dependency);
        add(this.dependencyTasks, dependency);
    }

    /**
     * @param dependentTasks
     */
    public void addDependencies(Collection<? extends PrioritizedTask> dependentTasks) {
        if (dependentTasks != null) {
            for (PrioritizedTask task : dependentTasks) {
                this.addDependency(task);
            }
        }
    }

    public void addDependencies(DefaultDependentPrioritizedTask task) {
        this.addDependencies(task.dependencyTasks);
    }

    private void checkCanAddDependency(PrioritizedTask dependency) {
        if ( dependency == null) {
            return;
        }
        if (dependency == this) {
            throw new IllegalStateException(this.getName() + ": Cannot depend on itself.");
        }
        if (!this.isDependencyAddable()) {
            throw new IllegalStateException(this.getName() + ":Already released to run. Dependency to " + dependency.getName() + " cannot be added.");
        }
        if (dependency instanceof DefaultDependentPrioritizedTask<?> && ((DefaultDependentPrioritizedTask<?>) dependency).isDependentOn(this)) {
            throw new IllegalStateException(this.getName() + ":Circular Dependency. " + dependency.getName() + " depends on " + this.getName());
        }
    }

    /**
     * Indicated that this task is dependent on another task being run. The
     * dependency is satisfied even if dependency does not complete
     * successfully.
     *
     * All dependencies should be added before being added to the TaskControl.
     * It is possible to add more dependencies if there {@link #isReadyToRun()}
     * has never returned true (there has always been at least one unsatisfied
     * dependency) but this is not advised.
     *
     * @param dependency
     *            The task that must be successfully completed before this task
     *            can execute.
     */
    public void addAlwaysDependency(PrioritizedTask dependency) {
        this.checkCanAddDependency(dependency);
        add(this.cleanUpAfterTasks, dependency);
    }

    public void addAlwaysDependencies(Collection<? extends PrioritizedTask> dependencies) {
        if (dependencies != null) {
            for (PrioritizedTask element : dependencies) {
                DependentPrioritizedTask task = (DependentPrioritizedTask) element;
                this.addAlwaysDependency(task);
            }
        }
    }

    public void addAlwaysDependencies(DefaultDependentPrioritizedTask task) {
        this.addAlwaysDependencies(task.cleanUpAfterTasks);
    }

    /**
     * @param sb
     */
    public void showUnsatisfiedDependencies(StringBuilder sb) {
        for (PrioritizedTask dependency : this.dependencyTasks) {
            if (!dependency.isSuccessful()) {
                sb.append('"').append(dependency.getName()).append('"').append(' ');
            }
        }
        for (PrioritizedTask dependency : this.cleanUpAfterTasks) {
            if (!dependency.isDone()) {
                sb.append('"').append(dependency.getName()).append('"').append(' ');
            }
        }
<<<<<<< HEAD
    }
=======
    }

    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public void setSuccessStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.completionMsg).append(" ").append(super.getTimingString()).append(" ");
        sb.append(this.getDependenciesStr());
        this.setStatus(sb.toString());
    }

    public String getDependenciesStr() {
        StringBuilder sb = new StringBuilder();
        if (!this.dependencyTasks.isEmpty()) {
            sb.append("Dependent Tasks: ");
            for (PrioritizedTask dependency : this.dependencyTasks) {
                sb.append('"').append(dependency.getName()).append('"').append(' ');
            }
        }
        if (!this.cleanUpAfterTasks.isEmpty()) {
            sb.append(" CleanUp after Tasks: ");
            for (PrioritizedTask dependency : this.cleanUpAfterTasks) {
                sb.append('"').append(dependency.getName()).append('"').append(' ');
            }
        }
        return sb.toString();
    }

    public DependentPrioritizedTask getParentTask() {
        return this.parentTask;
    }

    public void setParentTask(DependentPrioritizedTask parentTask) {
        this.parentTask = parentTask;
    }

    public void setIgnoreTaskGroupFailure(boolean ignoreTaskGroupFailure) {
        this.ignoreTaskGroupFailure = ignoreTaskGroupFailure;
    }

    public boolean isIgnoreTaskGroupFailure() {
        return this.ignoreTaskGroupFailure;
    }
}
