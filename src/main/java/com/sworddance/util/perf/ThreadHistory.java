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
package com.sworddance.util.perf;

/**
 * a record of a thread status change.
 * @author Patrick Moore
 */
public class ThreadHistory {
    private long timestampInMillis;

    private Long threadId;

    private String taskName;

    private String status;

    private String note;

    /**
     *  null -- no change in status, false = task stopping.
     */
    private Boolean threadInUse;

    private final int sequenceId;

    private long sequentialTime;

    ThreadHistory(long timestampInMillis, Long threadId, String taskName,
            String status, String note, Boolean threadInUse, int sequenceId) {
        this.timestampInMillis = timestampInMillis;
        this.threadId = threadId;
        this.taskName = taskName;
        this.status = status;
        this.note = note;
        this.threadInUse = threadInUse;
        this.sequenceId = sequenceId;
    }
    ThreadHistory(long timestampInMillis, Long threadId, String taskName,
        String status, String note, Boolean threadInUse, long sequentialTime, int sequenceId) {
        this.timestampInMillis = timestampInMillis;
        this.threadId = threadId;
        this.taskName = taskName;
        this.status = status;
        this.note = note;
        this.threadInUse = threadInUse;
        this.sequenceId = sequenceId;
        this.sequentialTime = sequentialTime;
    }

    /**
     * @return the sequentialTime
     */
    public long getSequentialTime() {
        return sequentialTime;
    }

    /**
     * @return the sequence
     */
    public int getSequenceId() {
        return sequenceId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(40);
        sb.append(timestampInMillis).append(":").append(threadId).append(taskName)
                .append(status);
        return sb.toString();
    }

    /**
     * @return the timestampInMillis
     */
    public long getTimestampInMillis() {
        return timestampInMillis;
    }

    /**
     * @return the threadId
     */
    public Long getThreadId() {
        return threadId;
    }

    /**
     * @return the taskName
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @return the threadInUse
     */
    public Boolean getThreadInUse() {
        return threadInUse;
    }
}