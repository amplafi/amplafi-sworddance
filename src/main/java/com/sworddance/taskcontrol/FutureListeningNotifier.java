/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.taskcontrol;

/**
 * Unified interface for a {@link FutureListener} that notifies other {@link FutureListener} (who are also probably expecting a different class
 * than the value the triggering Future contains.)
 * @author patmoore
 * @param <L> the type being listened for
 * @param <N> the type that attached listeners expect.
 *
 */
public interface FutureListeningNotifier<L, N> extends FutureListener<L>, FutureListenerNotifier<N> {

}
