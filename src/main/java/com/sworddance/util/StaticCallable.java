/**
 * Copyright 2006-2011 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.util;

import java.util.concurrent.Callable;

/**
 * A Callable that returns a fixed value
 * @author patmoore
 *
 */
public class StaticCallable<V> implements Callable<V> {

    private final V value;
    public StaticCallable(V value) {
        this.value = value;
    }
    /**
     * @see java.util.concurrent.Callable#call()
     */
    public V call() throws Exception {
        return value;
    }

}
