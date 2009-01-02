/**
 *
 */
package com.sworddance.taskcontrol;

import java.util.Collection;

/**
 * Implementers indicate that they may lock resources.
 */
public interface ResourceLocker {

    /**
     * @return locks needed.
     */
    public Collection<ResourceLock> getResourceLocksNeeded();

    public void addLock(ResourceLock lock);

    /**
     *
     * @return false if this task has no locks.
     */
    public boolean hasLocks();
}
