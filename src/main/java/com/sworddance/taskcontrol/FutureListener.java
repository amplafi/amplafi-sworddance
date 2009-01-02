/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.taskcontrol;

import java.util.concurrent.Future;

/**
 * implementors are called when a future is set.
 * @author patmoore
 * @param <T>
 *
 */
public interface FutureListener<T> {
    public <P extends Future<T>> void futureSet(P future, T value);
    public <P extends Future<T>> void futureSetException(P future, Throwable throwable);
}
