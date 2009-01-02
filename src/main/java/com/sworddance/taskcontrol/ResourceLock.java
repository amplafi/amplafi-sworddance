/**
 *
 */
package com.sworddance.taskcontrol;

import java.util.Collections;
import java.util.Set;

import java.util.concurrent.CountDownLatch;
import static java.util.concurrent.TimeUnit.*;

/**
 * Represents a lock on a resource. LockType property indicates how strong the
 * lock is.
 */
public class ResourceLock implements Cloneable {
    private final String resourceName;

    private ResourceLocker task;

    private final int originalLockType;

    private int lockType;

    private final CountDownLatch lockReleased = new CountDownLatch(1);

    private boolean generatedLock;

    public static final int NONEXCLUSIVE = 0;

    public static final int EXCLUSIVE = 1 << 1;

    static public final int READ = 1 << 2;

    static public final int WRITE = 1 << 3;

    static public final int SHARE_SIBLING = 1 << 4;

    static public final int SHARE_ONLY_CHILDREN = 1 << 5;

    // TODO don't really like this way of inserting CreateIndexTasks before
    // PostProcessingTasks.
    public static final int POST_INDEX_LOCK = 1 << 6;

    private static final int LOAD = 1 << 7;

    public static final int READNX = READ | NONEXCLUSIVE;

    /**
     * An exclusive read operation that does not block subtasks with the same
     * parent task. This is used for operations like drop/create indexes, or
     * drop/create mat views. In these cases other create indexes operations can
     * be happening but not other operations. Notice that combining dissimiliar
     * operations could result in trouble (create-index-on-table with
     * create-mat-view-of-table)
     */
    public static final int READSS = READ | EXCLUSIVE | SHARE_SIBLING;

    public static final int WRITEX = WRITE | READ | EXCLUSIVE;

    /**
     * A write lock that will allow only children to share this lock.
     */
    public static final int WRITESC = WRITEX | SHARE_SIBLING
            | SHARE_ONLY_CHILDREN;

    /**
     *
     */
    public static final int LOADX = WRITEX | LOAD;

    public static final int GLOBALLOCKTYPE = SHARE_SIBLING
            | SHARE_ONLY_CHILDREN | WRITE | READ | EXCLUSIVE;

    public static final int GLOBALLOCKTYPE_NOWRITE = SHARE_SIBLING
            | SHARE_ONLY_CHILDREN | READ | EXCLUSIVE;

    public ResourceLock(String resourceName, int lockType) {
        this.resourceName = resourceName.toUpperCase();
        originalLockType = this.lockType = lockType;
    }

    public boolean isExclusiveLock() {
        return (lockType & EXCLUSIVE) != 0;
    }

    public String getResourceName() {
        return resourceName;
    }

    public ResourceLocker getTask() {
        return task;
    }

    /**
     * Can only be called once per lock. Otherwise possible circular
     * dependencies are created.
     *
     * @param task
     */
    final void setTask(ResourceLocker task) {
        if (this.task != null && this.task != task) {
            throw new RuntimeException("Can only set task once");
        }
        if (task == null) {
            throw new RuntimeException("can't set task to null");
        }
        this.task = task;
    }

    public void releaseLock() {
        lockReleased.countDown();
    }

    public boolean isLockReleased() {
        try {
            return lockReleased.await(0, MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * @return the bit pattern representing the lockType.
     */
    public int getLockType() {
        return lockType;
    }

    public boolean isSubset(ResourceLock lock) {
        return (lock != null) && (lock.lockType & lockType) == lockType;
    }

    /**
     * very dangerous need alternative way to combine locks when a task has
     * multiple locks for same resource.
     *
     * @param lockType
     */
    void setLockType(int lockType) {
        this.lockType = lockType;
    }

    /**
     * downgrade the lock.
     *
     * @param lockTypeMask
     *            subset of current lockType setting.
     */
    public void downGrade(int lockTypeMask) {
        // TODO verify had the lock that was claimed to be used
        this.lockType &= lockTypeMask;
    }

    /**
     * do not allow equals() to be overriden because ResourceLockManager
     * manipulates collections of locks and if the lock changed or removed is
     * different than the lock expected because of a bad equals then bad stuff
     * can happen!
     */
    @Override
    public final boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * see comment on equals().
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * notice that this is not a unique enough comparison to be using as
     * equals. Especially with regards to collections.
     *
     * @param o
     * @return o is the same as this.
     */
    public boolean sameAs(Object o) {
        if (o instanceof ResourceLock) {
            ResourceLock l = (ResourceLock) o;
            return l.task == task && l.lockType == lockType;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('[');

        if (task != null) {
            sb.append(task.toString());
        }
        sb.append(':').append(resourceName).append(':').append(getLockStr())
                .append(']');
        return sb.toString();
    }

    public String getLockStr() {
        StringBuffer sb = new StringBuffer();
        if (generatedLock) {
            sb.append('(');
        }
        // lock has been downgraded.
        if (originalLockType != lockType) {
            appendLockTypeStr(sb, originalLockType);
            sb.append("->");
        }
        appendLockTypeStr(sb, lockType);
        if (generatedLock) {
            sb.append(')');
        }
        if (isLockReleased()) {
            sb.append('/');
        }
        return sb.toString();
    }

    private void appendLockTypeStr(StringBuffer sb, int lockTypeVal) {
        if (lockTypeVal == NONEXCLUSIVE) {
            // only show the 'N' if there are no other details about this lock
            // this will reduce the noise in the output.
            sb.append('N');
        } else {
            if ((lockTypeVal & READ) == READ) {
                sb.append('R');
            }
            if ((lockTypeVal & LOAD) == LOAD) {
                sb.append('L');
            } else if ((lockTypeVal & WRITE) == WRITE) {
                sb.append('W');
            }
            if ((lockTypeVal & EXCLUSIVE) == EXCLUSIVE) {
                sb.append('X');
            }
            if ((lockTypeVal & SHARE_SIBLING) == SHARE_SIBLING) {
                sb.append('S');
            }
            if ((lockTypeVal & POST_INDEX_LOCK) == POST_INDEX_LOCK) {
                sb.append('P');
            }
        }
    }

    /**
     * Between which tasks the lock can be shared with is beyond the scope of
     * ResourceLock.
     *
     * @return true if this lock can be shared.
     */
    public boolean isSharedLock() {
        return (lockType & SHARE_SIBLING) == SHARE_SIBLING;
    }

    public boolean isOnlySharingWithChildren() {
        return (lockType & SHARE_ONLY_CHILDREN) == SHARE_ONLY_CHILDREN;
    }

    /**
     *
     */
    public void setGenerated() {
        this.generatedLock = true;
    }

    /**
     * @param lock
     * @return successor lock
     */
    ResourceLock combine(ResourceLock lock) {
        if (!lock.generatedLock) {
            if (this.generatedLock) {
                return lock;
            } else {
                this.lockType = lock.getLockType() | this.getLockType();
                return this;
            }
        } else if (!this.generatedLock) {
            return this;
        } else {
            this.lockType = lock.getLockType() | this.getLockType();
            return this;
        }
    }

    public Set<?> getExpandedLocks() {
        return Collections.emptySet();
    }

    @Override
    public Object clone() {
        ResourceLock newLock = new ResourceLock(resourceName, lockType);
        copyInto(newLock);
        return newLock;
    }

    /**
     * @param newLock
     */
    protected void copyInto(ResourceLock newLock) {
        newLock.generatedLock = generatedLock;
    }

    public boolean isLock(int lockTypeLookingFor) {
        return (lockType & lockTypeLookingFor) == lockTypeLookingFor;
    }
}
