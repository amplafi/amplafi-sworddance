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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A ConcurrentMap with a {@link Callable} initializer that will be used if key used in {@link #get(Object)}
 * has no entry or the entry is null.
 * @author patmoore
 * @param <K>
 * @param <V>
 *
 */
public class ConcurrentInitializedMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    private ConcurrentMap<K, V> map;
    private Callable<V> initializer;

    public ConcurrentInitializedMap(ConcurrentMap<K, V> map, Callable<V> initializer) {
        this.map = map;
        this.initializer = initializer;
    }
    public ConcurrentInitializedMap(Callable<V> initializer) {
        this(new ConcurrentHashMap<K, V>(), initializer);
    }
    /**
     *
     * @see java.util.Map#clear()
     */
    public void clear() {
        map.clear();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /**
     * @see java.util.Map#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    /**
     * if key is not present then the supplied initializer will be used to provide a default value.
     * @see java.util.Map#get(java.lang.Object)
     */
    public V get(Object key) {
        return CUtilities.get(map, key, initializer);
    }

    /**
     * @see java.util.Map#hashCode()
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) {
        return map.put(key, value);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    /**
     * @see java.util.concurrent.ConcurrentMap#putIfAbsent(java.lang.Object, java.lang.Object)
     */
    public V putIfAbsent(K key, V value) {
        return map.putIfAbsent(key, value);
    }

    /**
     * @see java.util.concurrent.ConcurrentMap#remove(java.lang.Object, java.lang.Object)
     */
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {
        return map.remove(key);
    }

    /**
     * @see java.util.concurrent.ConcurrentMap#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public boolean replace(K key, V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    /**
     * @see java.util.concurrent.ConcurrentMap#replace(java.lang.Object, java.lang.Object)
     */
    public V replace(K key, V value) {
        return map.replace(key, value);
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return map.size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<V> values() {
        return map.values();
    }

}