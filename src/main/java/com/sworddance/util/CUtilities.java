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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 * @author patmoore
 *
 */
public class CUtilities {

    /**
     * size of object
     * @param object
     * @return 0 if null
     */
    @SuppressWarnings("unchecked")
    public static int size(Object object) {
        int total = 0;
        if ( object != null ) {
            if (object instanceof Map) {
                total = ((Map) object).size();
            } else if (object instanceof Collection) {
                total = ((Collection) object).size();
            } else if (object instanceof Object[]) {
                total = ((Object[]) object).length;
            } else if (object instanceof Iterator) {
                Iterator it = (Iterator) object;
                while (it.hasNext()) {
                    total++;
                    it.next();
                }
            } else if (object instanceof Enumeration) {
                Enumeration it = (Enumeration) object;
                while (it.hasMoreElements()) {
                    total++;
                    it.nextElement();
                }
            } else if (object instanceof CharSequence) {
                total = ((CharSequence)object).length();
            } else {
                try {
                    total = Array.getLength(object);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
                }
            }
        }
        return total;
    }

    /**
     * Add a value to a collection provided both the collection and the value are not null.
     * @param <T>
     * @param collection
     * @param object
     * @return true if the value was added
     */
    public static <T> boolean add(Collection<T> collection, T object) {
        return object != null && collection != null && collection.add(object);
    }

    /**
     * returns the object at index supplied. returns null if list is null or
     * smaller than the index supplied.
     *
     * @param <T>
     * @param collection
     * @param index
     * @return the index-th item in the collection
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object collection, int index) {
        if (collection == null) {
            return null;
        } else if (collection instanceof List) {
            List<?> list = (List<?>) collection;
            if (list.size() <= index) {
                return null;
            } else {
                return (T) list.get(index);
            }
        } else if (collection instanceof Object[]) {
            Object[] list = (Object[]) collection;
            if (list.length <= index) {
                return null;
            } else {
                return (T) list[index];
            }
        } else if (collection instanceof Iterable) {
            int i = 0;
            for (T result : (Iterable<T>) collection) {
                if (i == index) {
                    return result;
                } else {
                    i++;
                }
            }
            return null;
        } else if (collection instanceof Map) {
            return (T) get(((Map) collection).entrySet(), index);
        } else if (index == 0) {
            return (T) collection;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFirst(Object collection) {
        return (T) get(collection, 0);
    }

    /**
     * a universal isEmpty check that can handle arrays, {@link Collection}s, {@link Map}s, {@link CharSequence} or objects.
     * @param objects zero or more arrays, {@link Collection}s, {@link Map}s, {@link CharSequence} or objects.
     * @return true if ALL of the passed object(s) are an array, {@link Collection}, {@link Map}, or {@link CharSequence} that is null or contains no elements.
     * For other objects return true if the object is not null.
     * if the objects is a single object (that is not an array, collection, map) then true is return if the object is null.
     * TODO (Any or All? which is better?)
     */
    public static boolean isEmpty(Object... objects) {
        if ( objects != null ) {
            for(Object object: objects) {
                if (object != null ) {
                    if ( object instanceof Map ) {
                        if ( !((Map<?,?>)object).isEmpty()) {
                            return false;
                        }
                    } else if ( object instanceof Collection ) {
                        if ( !((Collection<?>)object).isEmpty()) {
                            return false;
                        }
                    } else if ( object.getClass().isArray()) {
                        if ( ((Object[])object).length != 0) {
                            return false;
                        }
                    } else if ( object instanceof CharSequence) {
                        if ( ((CharSequence)object).length() != 0) {
                            return false;
                        }
                    } else {
                        Method empty;
                        try {
                            empty = object.getClass().getMethod("isEmpty", new Class<?>[0]);
                            return (Boolean) empty.invoke(object);
                        } catch (NoSuchMethodException e) {
                        } catch (IllegalArgumentException e) {
                        } catch (IllegalAccessException e) {
                            throw new ApplicationIllegalStateException(e);
                        } catch (InvocationTargetException e) {
                            throw new ApplicationIllegalStateException(e);
                        }
                        // singleton object is always "not-empty"
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static <K, T> T put(Map<K,T> map, K key, T value) {
        if ( value == null ) {
            return map.remove(key);
        } else {
            return map.put(key, value);
        }
    }

    /**
     *
     * @param <K>
     * @param <T>
     * @param map maybe null
     * @param key maybe null
     * @return null if map or key is null
     */
    public static <K, T> T get(Map<K,T> map, K key) {
        return get(map, key, (T)null);
    }

    public static <K, V> V get(Map<K,V> map, K key, Callable<V> defaultValue) {
        if ( map == null || key == null) {
            return null;
        }
        V value = map.get(key);
        if ( value == null && defaultValue != null) {
            try {
                if ( map instanceof ConcurrentMap) {
                    ((ConcurrentMap<K,V>)map).putIfAbsent(key, defaultValue.call());
                } else {
                    map.put(key, defaultValue.call());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // another thread may beat us to assigning the value.
            value = map.get(key);
        }
        return value;
    }

    public static <K, V> V get(Map<K,V> map, K key, final V defaultValue) {
        return get(map, key, new Callable<V>() {
            @Override
            public V call() {
                return defaultValue;
            }
        });
    }

    public static <T> Set<T> asSet(T... values) {
        LinkedHashSet<T> set = new LinkedHashSet<T>();
        if (values != null) {
            for(T element : values) {
                if ( element != null) {
                    set.add(element);
                }
            }
        }
        return set;
    }

}
