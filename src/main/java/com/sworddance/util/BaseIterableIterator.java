/*
 * Created on Oct 31, 2007
 * Copyright 2006 by Amplafi, Inc.
 */
package com.sworddance.util;

import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple implementation of {@link IterableIterator}.
 * @param <T> type returned by iteration.
 * @author Patrick Moore
 */
public class BaseIterableIterator<T> implements IterableIterator<T>, CurrentIterator<T> {

    private Iterator<T> iter;
    // TODO: should this have a weak reference?
    private T current;
    public BaseIterableIterator() {
    }
    public BaseIterableIterator(Iterator<T> iter) {
        this.iter = iter;
    }
    public BaseIterableIterator(Iterable<T> iter) {
        setIterator(iter.iterator());
    }
    public BaseIterableIterator(T... objects) {
        setIterator(Arrays.asList(objects).iterator());
    }
    public BaseIterableIterator(Map<?,?> map) {
        setIterator(map.entrySet().iterator());
    }
    public <K> BaseIterableIterator(Reference<K> ref) {
        setIterator(ref);
    }
    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    protected void setIterator(Object k) {
        this.iter = extractIterator(k);
    }

    @SuppressWarnings("unchecked")
    protected Iterator<T> extractIterator(Object k) {
        while ( k instanceof Reference) {
            k = ((Reference)k).get();
        }
        if ( k == null ) {
            return (Iterator<T>) Collections.emptyList().iterator();
        } else if ( k instanceof Iterator) {
            return (Iterator<T>)k;
        } else if ( k instanceof Iterable) {
            return ((Iterable<T>)k).iterator();
        } else if ( k instanceof Map ) {
            return ((Map)k).entrySet().iterator();
        } else if ( k.getClass().isArray()) {
            return Arrays.asList((T[])k).iterator();
        } else {
            return Arrays.asList((T)k).iterator();
        }

    }
    @Override
    public T next() {
        return current = iter.next();
    }

    public T current() {
        return current;
    }
    @Override
    public void remove() {
        iter.remove();
        // don't null current that way old value can be retrieved.
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

}
