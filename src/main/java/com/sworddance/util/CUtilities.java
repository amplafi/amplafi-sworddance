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
     * @param object {@link Map}, {@link Collection}, Array, {@link CharSequence}
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
            } else if (object.getClass().isArray()) {
                total = Array.getLength(object);
            } else if (object instanceof CharSequence) {
                total = ((CharSequence)object).length();
            } else if (object instanceof Iterable) {
                Iterator it = ((Iterable) object).iterator();
                while (it.hasNext()) {
                    total++;
                    it.next();
                }
            } else {
                throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
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
     * @return the index-th item in the collection if collection is a Map, the index-th Map.Entry element is returned.
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
        } else if (collection.getClass().isArray()) {
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
    public static boolean isNotEmpty(Object... objects) {
        return !isEmpty(objects);
    }

    /**
     *
     * @param <K>
     * @param <T>
     * @param map
     * @param key
     * @param value if null then {@link Map#remove(Object)} is called, otherwise
     * @return map.{@link Map#put(Object, Object)}
     */
    public static <K, T> T put(Map<K,T> map, K key, T value) {
        if ( map != null ) {
            if ( value == null ) {
                return map.remove(key);
            } else {
                return map.put(key, value);
            }
        } else {
            return null;
        }
    }

    /**
     * Used when value extends {@link MapKeyed} to add to a map.
     * @param <K>
     * @param <V>
     * @param map may be null.
     * @param value if null then nothing happens ( key to remove is not known )
     * @return {@link #put(Map, Object, Object)}
     */
    public static <K, V extends MapKeyed<K>> V put(Map<K,V> map, V value) {
        if (map == null || value == null) {
            return null;
        } else {
            return put(map, value.getMapKey(), value);
        }
    }
    /**
     * Adds any number of MapKeyed<V> to the map.
     * @param <K>
     * @param <V>
     * @param map map be null.
     * @param values extends V but can't be enforced because generics don't allow for multiple extends bounds when compiler can't enforce that
     * there is only one class specified. (ie. <T extends V & MapKeyed<K>> is not permitted )
     */
    public static <K, V extends MapKeyed<K>> void putAll(Map<K,?> map, Object... values) {
        if (map != null) {
            for(Object value : values ) {
                put((Map<K,V>)map, (V) value);
            }
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
            V callValue;
            try {
                callValue = defaultValue.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if ( map instanceof ConcurrentMap) {
                ((ConcurrentMap<K,V>)map).putIfAbsent(key, callValue);
            } else {
                map.put(key, callValue);
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
