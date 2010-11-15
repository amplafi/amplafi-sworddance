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

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;

/**
 * @author patmoore
 *
 */
public class SingletonTaskControlFactory implements TaskControlFactory {

    private String threadName;
    private Log log;
    private AtomicReference<TaskControl> taskControlRef = new AtomicReference<TaskControl>();
    private WeakReference<Thread> taskControlThreadRef;
    public SingletonTaskControlFactory() {

    }
    public SingletonTaskControlFactory(Log log, String threadName) {
        this.log = log;
        this.threadName = threadName;
    }
    /**
     * @see com.sworddance.taskcontrol.TaskControlFactory#newTaskControl()
     */
    public TaskControl newTaskControl() {
        TaskControl taskControl = getTaskControl();
        if ( taskControl == null ) {
            taskControl = new TaskControl(getLog());
            if ( taskControlRef.compareAndSet(null, taskControl)) {
                Thread taskControlThread = new Thread(taskControl, getThreadName());
                taskControlThread.start();
                taskControlThreadRef = new WeakReference<Thread>(taskControlThread);
            } else {
                // assumes that taskControlRef is never changed once set.
                taskControl = getTaskControl();
            }
        }
        return taskControl;
    }

    /**
     * @return
     */
    private TaskControl getTaskControl() {
        return taskControlRef.get();
    }

    public void shutdown() {
        TaskControl taskControl = getTaskControl();
        if ( taskControl != null ) {
            taskControl.shutdownNow();
        }
        if ( taskControlThreadRef != null ) {
            Thread taskControlThread = this.taskControlThreadRef.get();
            if ( taskControlThread != null) {
                taskControlThread.interrupt();
            }
        }
    }
    /**
     * @param log the log to set
     */
    public void setLog(Log log) {
        this.log = log;
    }
    /**
     * @return the log
     */
    public Log getLog() {
        return log;
    }

    /**
     * @param threadName the threadName to set
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * @return the threadName
     */
    public String getThreadName() {
        return threadName;
    }

}
