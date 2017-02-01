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

import static com.sworddance.util.NotNullIterator.newNotNullIterator;
import static org.apache.commons.lang.StringUtils.join;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.sworddance.util.map.MapKeyed;

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
                throw new ApplicationIllegalArgumentException("Unsupported object type: " + object.getClass().getName());
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
     * Add a anotherCollection to a collection provided both the collection and the anotherCollection are not null.
     * @param <T>
     * @param collection
     * @param anotherCollection
     * @return true if the value was added
     */
    public static <T> boolean addAll(Collection<T> collection, Collection<T> anotherCollection) {
        return anotherCollection != null && collection != null && collection.addAll(anotherCollection);
    }
    public static <T> boolean addIfNotContains(Collection<T> collection, T value) {
        if (  collection != null && value != null && !collection.contains(value)) {
            return collection.add(value);
        } else {
            return false;
        }
    }
    /**
     * filters out null values
     * @param collection if null then return false
     * @param values
     * @return true collection changed
     */
    public static <T> boolean addAllIfNotContains(Collection<T> collection, T... values) {
        boolean collectionChanged = false;
        if ( collection != null && values != null) {
            collectionChanged = addAllIfNotContains(collection, Arrays.asList(values));
        }
        return collectionChanged;
    }
    public static <T> boolean addAllIfNotContains(Collection<T> collection, Collection<T> values) {
        boolean collectionChanged = false;
        if ( collection != null) {
            for(T value : NotNullIterator.<T>newNotNullIterator(values)) {
                if ( !collection.contains(value)) {
                    collectionChanged |= collection.add(value);
                }
            }
        }
        return collectionChanged;
    }

    /**
     * @param <T>
     * @param collection
     * @param newValues
     * @return true collection changed
     */
    public static <T> boolean addAllNotNull(Collection<T> collection, T... newValues) {
        if ( collection != null && newValues != null) {
            return addAllNotNull(collection, Arrays.asList(newValues));
        } else {
            return false;
        }
    }
    public static <T> boolean addAllNotNull(Collection<T> collection, Collection<T> newValues) {
        boolean collectionChanged = false;
        if ( collection != null ) {
            for(T newValue: NotNullIterator.<T>newNotNullIterator(newValues)) {
                collectionChanged |= collection.add(newValue);
            }
        }
        return collectionChanged;
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
    public static <T> T getFirstNonNull(Object... collection) {
        Object collections;
        switch(collection.length) {
        case 0:
            return null;
        case 1:
            collections = collection[0];
            break;
        default:
            collections = collection;
            break;
        }
        T result = (T)getFirst(collections);
        if ( result == null) {
            int size = size(collections);
            for(int i = 1; i < size; i++) {
                result = (T)get(collections, i);
                if ( result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * a universal isEmpty check that can handle arrays, {@link Collection}s, {@link Map}s, {@link CharSequence} or objects.
     * @param object Array, {@link Collection}, {@link Map}s, {@link CharSequence} or object.
     * @return true if the passed object is an array, {@link Collection}, {@link Map}, or {@link CharSequence} that is null or contains no elements.
     * For other objects return true if the object is not null.
     * if the objects is a single object (that is not an array, collection, map) then true is return if the object is null.
     * TODO (Any or All? which is better?)
     */
    public static boolean isEmpty(Object object) {
        if (object == null ) {
            return true;
        } else if ( object instanceof Map<?, ?> ) {
            return ((Map<?,?>)object).isEmpty();
        } else if ( object instanceof Collection<?> ) {
            return ((Collection<?>)object).isEmpty();
        } else if ( object.getClass().isArray()) {
            return ((Object[])object).length == 0;
        } else if ( object instanceof CharSequence) {
            return ((CharSequence)object).length() == 0;
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

    public static boolean isAllEmpty(Object... objects) {
        for(Object object: NotNullIterator.newNotNullIterator(objects)) {
            boolean result = isEmpty(object);
            if (!result ) {
                return result;
            }
        }
        return true;
    }
    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    /**
     * Combine multiple arrays into a single array.
     * A ClassCastException will probably result on the return if there is not at least 1 non-null object or array in the objects parameter list.
     * use {@link #combineToSpecifiedClass(Class, Object...)} and supply the expected class for a more certain result.
     * @param <T>
     * @param objects objects of <T> or arrays of <T> objects.
     * @return a single array of <T> objects. nulls are preserved.
     */
    public static <T> T[] combine(Object...objects) {
        Class<T> componentType = null;
        return combineToSpecifiedClass(componentType, objects);
    }

    /**
     * TODO: really should return a List<> but need to know the component type for the toArray to be proper.
     * Example:
     * [ x, [y, x], m] combines to [x,y,x,m]
     * @param <T>
     * @param componentType if null then the component type is attempted to be determined.
     * @param objects objects of <T> or arrays of <T> objects.
     * @return a single array of <T> objects. nulls are preserved.
     */
    public static <T> T[] combineToSpecifiedClass(Class<T> componentType, Object... objects) {
        List<T> list = new ArrayList<T>();
        if ( objects != null) {
            if ( componentType == null && objects.getClass().getComponentType() != Object.class) {
                componentType = (Class<T>) objects.getClass().getComponentType();
            }

            if (componentType == null) {
                componentType = guessComponentType(objects);
            }

            for(Object object:objects) {
                if (object != null && object.getClass().isArray()) {
                    T[] array = combineToSpecifiedClass(componentType, (Object[])object);
                    if ( array != null) {
                        list.addAll(Arrays.asList(array));
                    }
                } else {
                    list.add((T)object);
                }
            }
        }
        if ( componentType == null ) {
            // probably will always fail?
            return (T[]) list.toArray();
        } else {
            T[] newArray = (T[]) Array.newInstance(componentType, list.size());
            return list.toArray(newArray);
        }
    }

    private static <T> Class<T> guessComponentType(Object... objects) {
        Class<T> type = null;
        for (Object object : newNotNullIterator(objects)) {
            Class<T> prevType = type;
            if (object.getClass().isArray()) {
                type = (Class<T>) object.getClass().getComponentType();
            } else {
                type = (Class<T>) object.getClass();
            }
            if (prevType != null && !type.isAssignableFrom(prevType)) {
                type = (Class<T>) Object.class;
                break;
            }
        }
        return type;
    }

    /**
     * This is a safe put when using {@link java.util.concurrent.ConcurrentMap} which throw exceptions if key or value is null
     * @param <K>
     * @param <T>
     * @param map if null then nothing happens
     * @param key if null then nothing happens
     * @param value if null then {@link Map#remove(Object)} is called, otherwise
     * @return map.{@link Map#put(Object, Object)}
     */
    public static <K, T> T put(Map<K,T> map, K key, T value) {
        if ( map != null && key != null) {
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
    @SuppressWarnings("unchecked")
    public static <K, V extends MapKeyed<K>> void putAll(Map<K,?> map, Object... values) {
        if (map != null) {
            for(Object value : values ) {
                put((Map<K,V>)map, (V) value);
            }
        }
    }
    /**
     * @see #get(Map, Object, Callable)
     * @param <K>
     * @param <T>
     * @param map maybe null
     * @param key maybe null
     * @return null if map or key is null
     */
    public static <K, T> T get(Map<K,T> map, Object key) {
        return get(map, key, (Callable<T>)null);
    }

    /**
     * Get a value from a map. If the value returned is null, then if defaultValue is provided, the {@link Callable#call()} is made and that value is set.
     * If map is {@link ConcurrentMap} then the value supplied by default is set using {@link ConcurrentMap#putIfAbsent(Object, Object)}. Otherwise
     * {@link Map#put(Object, Object)} call is made and the defaultValue-supplied value is returned (and any synchronization issues are handled by the caller).
     * @param <K> key type in map
     * @param <V> value type in map
     * @param map if null then null is returned
     * @param key if null then null is returned
     * @param defaultValue if a {@link ParameterizedCallable} then map and key are passed to {@link ParameterizedCallable#executeCall(Object...)}(map,key)
     * see also {@link com.sworddance.util.AbstractParameterizedCallableImpl}
     * @return the value in the map.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> V get(Map<K,V> map, Object key, Callable<V> defaultValue) {
        V value;
        if ( map != null && key != null) {
            value = map.get(key);
        } else {
            value = null;
        }
        if ( value == null && defaultValue != null) {
            V callValue;
            try {
                if ( defaultValue instanceof ParameterizedCallable<?>) {
                    callValue = ((ParameterizedCallable<V>)defaultValue).executeCall(map, key);
                } else {
                    callValue = defaultValue.call();
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ApplicationGeneralException(e);
            }
            if ( callValue != null && map != null && key != null ) {
                if ( map instanceof ConcurrentMap<?, ?>) {
                    ((ConcurrentMap<K,V>)map).putIfAbsent((K)key, callValue);
                } else {
                    map.put((K)key, callValue);
                }
                // another thread may beat us to assigning the value.
                value = map.get(key);
            } else {
                value = callValue;
            }
        }
        return value;
    }

    /**
     * Same as  {@link #get(Map, Object, Callable)} but handles creating a {@link Callable} to wrap the defaultValue.
     * @param <K>
     * @param <V>
     * @param map
     * @param key
     * @param defaultValue
     * @return the value in the map.
     */
    public static <K, V> V get(Map<K,V> map, K key, final V defaultValue) {
        return get(map, key, new Callable<V>() {
            public V call() {
                return defaultValue;
            }
        });
    }

    public static <T> Set<T> asSet(T... values) {
        LinkedHashSet<T> set = new LinkedHashSet<T>();
        addAllNotNull(set, values);
        return set;
    }

    /**
     * Converts object to a list.
     *
     * @param <T>
     * @param object
     * @return null if object is null, object if object is list, new list if object is another collection, list of Map.Entry if object is a map.
     * other wise result of Arrays.asList
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> convertToList(Object object) {
        List<T> result;
        if (object == null) {
            result = null;
        } else if (object instanceof List) {
            result = (List) object;
        } else if (object.getClass().isArray()) {
            result = Arrays.asList((T[]) object);
        } else if ( object instanceof Collection) {
            return new ArrayList<T>((Collection)object);
        } else if ( object instanceof Map) {
            return new ArrayList<T>(((Map)object).entrySet());
        } else {
            result = Arrays.asList((T) object);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Collection<?>> T cloneCollection(T cloned) {
        Collection result = null;
        if (cloned != null) {
            try {
                result = cloned.getClass().newInstance();
            } catch (InstantiationException e) {
                throw new ApplicationGeneralException(e);
            } catch (IllegalAccessException e) {
                throw new ApplicationGeneralException(e);
            }
            result.addAll(cloned);
        }
        return (T) result;
    }

    /**
     * @param newValues
     * @param <T>
     * @return an new {@link List} populated with the non-null values in newValues
     */
    public static <T> List<T> newList(T...newValues) {
        List<T> l = new ArrayList<T>();
        addAllNotNull(l, newValues);
        return l;
    }

    /**
     * Create a map from alternating keys and values. if a key is null then it (and its
     * corresponding value) are not placed in the map.
     *
     * @param <K>
     * @param <V>
     * @param keysAndValues
     * @return a Map<K,V> of the values.
     */
    public static <K, V> Map<K, V> createMap(Object... keysAndValues) {
        return createMap(false, keysAndValues);
    }

    /**
     * Same as {@link #createMap(Object...)} but also skips pairs with null values.
     *
     * @param <K>
     * @param <V>
     * @param keysAndValues
     * @return map with no null keys or values.
     */
    public static <K, V> Map<K, V> createMapSkipNullValues(Object... keysAndValues) {
        return createMap(true, keysAndValues);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> createMap(boolean skipNullValues, Object... keysAndValues) {
        Map<K, V> map = new LinkedHashMap<K, V>();
        if (keysAndValues != null && keysAndValues.length != 0) {
            if (keysAndValues.length % 2 != 0) {
                throw new ApplicationIllegalStateException("Non-even number of parameters to createMap. Need matched set of keys and values. got=",
                    join(keysAndValues, ","));
            }
            for (int i = 0; i < keysAndValues.length; i += 2) {
                if (keysAndValues[i] != null && (!skipNullValues || keysAndValues[i + 1] != null)) {
                    map.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
                }
            }
        }
        return map;
    }


    /**
     * conceptually equivalent to masterCollection.clear(); masterCollection.addAll(newValues);
     *
     * except that the masterCollection is only modified to the extent needed to bring it into compliance.
     * Useful for avoiding unnecessary db operations.
     * compare the values in the newValues Collection to the masterCollection.
     * @param <T>
     * @param masterCollection
     * @param newValues
     * @return true if a change was made
     */
    public static <T> boolean updateCollectionAsNeeded(Collection<T> masterCollection, Collection<T> newValues) {
        boolean changed = false;
        // avoid updating with self.
        if ( masterCollection != newValues) {
            if ( isEmpty(newValues)) {
                if ( isNotEmpty(masterCollection)) {
                    masterCollection.clear();
                    changed = true;
                }
            } else {
                // may be copying directly from another envelope
                List<T> remainingValues = new ArrayList<T>(newValues);
                // remove topics that are no longer present.
                for(Iterator<T> iterator = masterCollection.iterator(); iterator.hasNext(); ) {
                    T existingObjectInMasterCollection = iterator.next();
                    if(!remainingValues.contains(existingObjectInMasterCollection)) {
                        iterator.remove();
                        changed = true;
                    } else {
                        remainingValues.remove(existingObjectInMasterCollection);
                    }
                }
                // add any remaining objects that are actually new.
                masterCollection.addAll(remainingValues);
                changed = changed || !remainingValues.isEmpty();
            }
        }
        return changed;
    }
    /**
     * because {@link Collections#reverse(List)} does not return the list.
     * @param <T>
     * @param list
     * @return list
     */
    public static <T extends List<?>> T reverse(T list) {
        Collections.reverse(list);
        return list;
    }
    public static <T> List<T> reverse(T... elements) {
        List<T> list = Arrays.asList(elements);
        Collections.reverse(list);
        return list;
    }

    public static <T> Class<? extends T> getClassIfPossible(String className) {
        Class<? extends T> clazz = null;
        try {
            clazz = (Class<? extends T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            // we are quiet on purpose - may be should log
        }
        return clazz;
    }
    public static <T> void removeIfNotPresent(Collection<T> collection, Collection<T> permitted) {
        for(Iterator<T> iter = collection.iterator(); iter.hasNext(); ) {
            T element = iter.next();
            if ( !permitted.contains(element)) {
                iter.remove();
            }
        }
    }

    public static Class<?> getClassSafely(Object...objects) {
        if ( objects != null) {
            for(Object object: objects) {
                if (object != null) {
                    return object.getClass();
                }
            }
        }
        return null;
    }
    public static Pattern onlyPattern(String regex) {
        return Pattern.compile("^"+regex+"$", Pattern.CASE_INSENSITIVE);
    }
    public static Pattern withinPattern(String regex) {
        // terminator is reluctant so that non-alphanumerics that end regex will still get matched.
        return Pattern.compile("\\b"+regex+"\\b??", Pattern.CASE_INSENSITIVE);
    }
    /**
     * create pattern to search for equivalent javascript. Specifically,
     * <ul><li>All punctuation characters are escaped and leading/trailing spaces are allowed
     * <li>Alnum whitespace Alnum is change to require only a single whitespace ( handles cases like the space between var and ga in : "var ga = []; ")
     * </ul>
     * @param jsScriptStr
     * @return string to supply to {@link Pattern#compile(String)}
     */
    public static String jsQuoteForPattern(String jsScriptStr) {
        String escapeAllPunctuationChars = jsScriptStr.replaceAll("(?:\\s*([\\p{Punct}])\\s*)", "\\\\s*\\\\$1\\\\s*");

        String requireAtLeast1WsBetweenWords = escapeAllPunctuationChars.replaceAll("(\\p{Alnum})\\s+(\\p{Alnum})", "$1\\\\s+$2");
        String simplifyWsMatching = requireAtLeast1WsBetweenWords.replaceAll("(?:\\Q\\s*\\E)+", "\\\\s*");
        return simplifyWsMatching;
    }

    /**
     * Create a search path list containing:
     * [ fileName, /fileName, /{eachdir}/fileName, /META-INF/fileName, /META-INF/{eachdir}/fileName ]
     * @return a list of locations to look for the file supplied.
     */
    public static List<String> createSearchPath(String fileName, String...alternateDirectories) {
        List<String> searchPath = new ArrayList<String>();
        searchPath.add(fileName);
        String adjustedFilename;
        if (!fileName.startsWith("/")) {
            searchPath.add("/"+fileName);
            adjustedFilename = fileName;
        } else {
            adjustedFilename = fileName.substring(1);
        }
        List<String> workingDirectories = new ArrayList<String>();
        if ( isNotEmpty(alternateDirectories)) {
            workingDirectories.addAll(Arrays.asList(alternateDirectories));
        }
        List<String> alternates = new ArrayList<String>();
        for(String alternateRoot : new String[]{"", "META-INF/" }) {
            alternates.add(alternateRoot);
            for(String a: workingDirectories) {
                String full;
                if ( a.endsWith("/")) {
                    full = alternateRoot+a;
                } else {
                    full = alternateRoot+a+"/";
                }
                alternates.add(full);
            }
        }
        for(String alternateDirectory: alternates) {
            if (!adjustedFilename.startsWith(alternateDirectory)) {
                searchPath.add("/"+alternateDirectory+adjustedFilename);
            }
        }
        return searchPath;
    }
    public static InputStream getResourceAsStream(Object searchRoot, String fileName, String...alternateDirectories) {
        return getResourceAsStream(searchRoot, fileName, false, alternateDirectories);
    }
    public static InputStream getResourceAsStream(Object searchRoot, String fileName, boolean optional, String...alternateDirectories) {
        List<String> searchPaths = createSearchPath(fileName, alternateDirectories);
        return getResourceAsStream(searchRoot, fileName, optional, searchPaths);
    }

    public static InputStream getResourceAsStream(Object searchRoot, boolean optional, List<String> searchPaths) {
        return getResourceAsStream(searchRoot, null, optional, searchPaths);
    }
    private static InputStream getResourceAsStream(Object searchRoot, String fileName, boolean optional, List<String> searchPaths) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for(String searchPath: searchPaths) {
            InputStream resource = null;
            if ( searchRoot != null ) {
                resource = searchRoot.getClass().getResourceAsStream(searchPath);
            }

            if (resource == null && contextClassLoader!=null) {
                resource = contextClassLoader.getResourceAsStream( searchPath );
            }
            if (resource == null ) {
                resource = ClassLoader.getSystemResourceAsStream(searchPath);
            }
            if ( resource != null) {
                return resource;
            }
        }
        if ( !optional) {
            if ( fileName != null) {
                throw new ApplicationNullPointerException(fileName, " not found in ", join(searchPaths, ","),
                    " java.class.path=",System.getProperty("java.class.path"),
                    " java.library.path=",System.getProperty("java.library.path"), " searchRoot =", getClassSafely(searchRoot));
            } else {
                throw new ApplicationNullPointerException("No listed file found ", join(searchPaths, ","),
                    " java.class.path=",System.getProperty("java.class.path"),
                    " java.library.path=",System.getProperty("java.library.path"), " searchRoot =", getClassSafely(searchRoot));
            }
        } else {
            return null;
        }
    }
    public static Collection<URL> getResources(Object searchRoot, String fileName, String...alternateDirectories) {
        return getResources(searchRoot, fileName, false, alternateDirectories);
    }
    public static Collection<URL> getResources(Object searchRoot, String fileName, boolean optional, String...alternateDirectories) {
        List<String> searchPaths = createSearchPath(fileName, alternateDirectories);
        return getResources(searchRoot, fileName, optional, searchPaths);
    }

    public static Collection<URL> getResources(Object searchRoot, boolean optional, List<String> searchPaths) {
        return getResources(searchRoot, null, optional, searchPaths);
    }
    /**
     * @param searchRoot
     * @param fileName
     * @param optional
     * @param searchPaths
     * @return a de-duped Enumeration<URL> never returns null.
     */
    private static Collection<URL> getResources(Object searchRoot, String fileName, boolean optional, List<String> searchPaths) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader searchRootClassLoader = searchRoot != null?searchRoot.getClass().getClassLoader():null;
        HashMap<String, URL> results = new HashMap<String, URL>();
        for(String searchPath: searchPaths) {
            Enumeration<URL> resource = null;
            if ( searchRootClassLoader != null ) {
                try {
                    resource = searchRootClassLoader.getResources(searchPath);
                    for(URL url : NotNullIterator.<URL>newNotNullIterator(resource)) {
                        results.put(url.toString(), url);
                    }
                } catch (IOException e) {
                    // TODO what?
                }
            }
            if (contextClassLoader!=null) {
                try {
                    resource = contextClassLoader.getResources( searchPath );
                    for(URL url : NotNullIterator.<URL>newNotNullIterator(resource)) {
                        results.put(url.toString(), url);
                    }
                } catch (IOException e) {
                    // TODO what?
                }
            }
            if (resource == null ) {
                try {
                    resource = ClassLoader.getSystemResources(searchPath);
                    for(URL url : NotNullIterator.<URL>newNotNullIterator(resource)) {
                        results.put(url.toString(), url);
                    }
                } catch (IOException e) {
                    // TODO what?
                }
            }
        }
        if ( isEmpty(results) && !optional) {
            if ( fileName != null) {
                throw new ApplicationNullPointerException(fileName, " not found in ", join(searchPaths, ","),
                    " java.class.path=",System.getProperty("java.class.path"),
                    " java.library.path=",System.getProperty("java.library.path"), " searchRoot =", getClassSafely(searchRoot));
            } else {
                throw new ApplicationNullPointerException("No listed file found ", join(searchPaths, ","),
                    " java.class.path=",System.getProperty("java.class.path"),
                    " java.library.path=",System.getProperty("java.library.path"), " searchRoot =", getClassSafely(searchRoot));
            }
        } else {
            return results.values();
        }
    }
    
    public static <K, V extends Collection<W>, W> Map<K, V> merge(Map<K, V> first, Map<K, V> second) {
        Map<K, V> result = new HashMap<K, V>();
        if ( isNotEmpty(first) || isNotEmpty(second)) {
            if ( isEmpty(first)) {
                result.putAll(second);
            } else if(isEmpty(second)) {
                result.putAll(first);
            } else {
                result.putAll(first);
                Set<Map.Entry<K, V>> secondMapEntrySet = second.entrySet();
                for (Map.Entry<K, V> entry : secondMapEntrySet) {
                    K key = entry.getKey();
                    if (result.containsKey(key)) {
                        V resultValue = result.get(key);
                        resultValue.addAll(entry.getValue());
                    } else {
                        result.put(key, entry.getValue());
                    }
                }
            }
        }
        return result;
    }
}
