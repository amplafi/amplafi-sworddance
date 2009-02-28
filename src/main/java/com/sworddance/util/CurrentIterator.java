/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.util;

import java.util.Iterator;

/**
 * Iterator that remembers the last value next() (or prev() ) returned.
 * @author patmoore
 * @param <E> value returned
 *
 */
public interface CurrentIterator<E> extends Iterator<E> {
    /**
     *
     * @return value last returned by {@link #next()}, null if #next() has not been called yet
     */
    public E current();
}
