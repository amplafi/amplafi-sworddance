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

package com.sworddance.util;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of {@link IterableIterator}.
 * @param <T> type returned by iteration.
 * @author Patrick Moore
 */
public class BaseIterableIterator<T> implements IterableIterator<T>, CurrentIterator<T> {
    private int index = -1;

    private Iterator<T> iter;
    // TODO: should this have a weak reference?
    private T current;
    public BaseIterableIterator() {
<<<<<<< HEAD
        this.setIterator(null);
    }
    public BaseIterableIterator(Iterator<?> iter) {
        this.setIterator(iter);
    }
    public BaseIterableIterator(Iterable<?> iter) {
=======
    }
    public BaseIterableIterator(Iterator<T> iter) {
        this.setIterator(iter);
    }
    public BaseIterableIterator(Iterable<T> iter) {
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        setIterator(iter!=null?iter.iterator():null);
    }
    public BaseIterableIterator(T... objects) {
        setIterator(Arrays.asList(objects).iterator());
    }
<<<<<<< HEAD
    /**
     * There is a compile reason to have 2 separate constructors .. but forgot exact reason.
     * @param first
     * @param objects
     */
=======
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public BaseIterableIterator(T first, T... objects) {
        List<T> list = new ArrayList<T>();
        list.add(first);
        list.addAll(Arrays.asList(objects));
        setIterator(list.iterator());
    }
    public BaseIterableIterator(Map<?,?> map) {
        setIterator(map !=null?map.entrySet().iterator():null);
    }
    public <K> BaseIterableIterator(Reference<K> ref) {
        setIterator(ref);
<<<<<<< HEAD
    }
=======
    }
    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public boolean hasNext() {
        return iter.hasNext();
    }

    protected void setIterator(Object k) {
        this.iter = extractIterator(k);
    }

    @SuppressWarnings("unchecked")
<<<<<<< HEAD
    protected static <T> Iterator<T> extractIterator(Object k) {
=======
    protected Iterator<T> extractIterator(Object k) {
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
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

<<<<<<< HEAD
    }
=======
    }
    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public T next() {
        current = iter.next();
        index++;
        return current;
    }

    public T current() {
        return current;
<<<<<<< HEAD
    }
=======
    }
    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public void remove() {
        iter.remove();
        // don't null current that way old value can be retrieved.
        index--;
<<<<<<< HEAD
    }
=======
    }

    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public Iterator<T> iterator() {
        return this;
    }
    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

}
