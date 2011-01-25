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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of {@link IterableIterator}.
 * @param <T> type returned by iteration.
 * @author Patrick Moore
 */
public class BaseIterableIterator<T> implements IterableIterator<T>, CurrentIterator<T>, Enumeration<T> {
    private int index = -1;

    private Iterator<T> iter;
    // TODO: should this have a weak reference?
    private T current;
    public BaseIterableIterator() {
        this.setIterator(null);
    }
    public BaseIterableIterator(Iterator<?> iter) {
        this.setIterator(iter);
    }
    public BaseIterableIterator(Iterable<?> iter) {
        setIterator(iter!=null?iter.iterator():null);
    }
    public BaseIterableIterator(Enumeration<?> iter) {

        setIterator(iter!=null?new IterEnum<T>(iter):null);
    }
    public BaseIterableIterator(T... objects) {
        setIterator(Arrays.asList(objects).iterator());
    }
    /**
     * There is a compile reason to have 2 separate constructors .. but forgot exact reason.
     * @param first
     * @param objects
     */
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
    }
    public boolean hasNext() {
        return iter.hasNext();
    }

    protected void setIterator(Object k) {
        this.iter = extractIterator(k);
    }

    @SuppressWarnings("unchecked")
    protected static <T> Iterator<T> extractIterator(Object k) {
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
    public T next() {
        current = iter.next();
        index++;
        return current;
    }

    public T current() {
        return current;
    }
    public void remove() {
        iter.remove();
        // don't null current that way old value can be retrieved.
        index--;
    }
    public Iterator<T> iterator() {
        return this;
    }
    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }
	public boolean hasMoreElements() {
		return this.hasNext();
	}
	public T nextElement() {
		return this.next();
	}
	private static class IterEnum<T> implements Iterator<T> {
		private Enumeration<?> enumeration;

		public IterEnum(Enumeration<?> enumeration) {
			this.enumeration = enumeration;
		}

		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}

		public T next() {
			return (T) this.enumeration.nextElement();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
