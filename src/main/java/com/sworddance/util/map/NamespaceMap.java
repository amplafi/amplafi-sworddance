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
//    SortedMap<? extends NamespaceMapKey, String> getNamespaceMap(Object namespace);

    // Conflicts with static import of isEmpty
//    public boolean isEmpty(Object namespace);

}
