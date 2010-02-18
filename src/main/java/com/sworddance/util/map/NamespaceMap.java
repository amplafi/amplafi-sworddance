/*
 * Created on Aug 18, 2007 Copyright 2006-2008 by Amplafi
 */
package com.sworddance.util.map;

import java.util.Map;
import java.util.Set;

import com.sworddance.core.Emptyable;

/**
 * implementers have a 2 level map. namespace retrieves a map that is then used look up in with key.
 *
 * @author Patrick Moore
 */
public interface NamespaceMap extends NamespaceMapProvider, Emptyable {
    /**
     * @param namespace the key to get the map.
     * @param key the key of the map
     * @return the value indexed by (namespace,key)
     */
    public String get(Object namespace, Object key);

    // TODO : should return a subMap so that the returned map can be modified.
    public Map<String, String> getAsStringMap(Object namespace);

    public String remove(Object namespace, Object key);

    public String put(Object namespace, Object key, Object value);

    public void putAll(Object namespace, Map<?, ?> values);

    public void removeAll(Object namespace);

    public boolean containsKey(Object namespace, Object key);

    Set<? extends NamespaceMapKey> keySet();

    // Conflicts with static import of isEmpty
//    public boolean isEmpty(Object namespace);

}
