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