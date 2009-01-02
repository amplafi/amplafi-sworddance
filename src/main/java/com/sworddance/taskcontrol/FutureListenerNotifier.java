/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.taskcontrol;

/**
 * implementations collect {@link FutureListener}s for later (or immediate notification).
 *
 * @author patmoore
 * @param <T>
 *
 */
public interface FutureListenerNotifier<T> {
    /**
     * TODO -- making sure that futureListeners can still be run.
     * The futureListener should be only weakly held so that the {@link FutureListenerNotifier} will not prevent GC.
     * @param futureListener
     */
    public void addFutureListener(FutureListener<T> futureListener);
}
