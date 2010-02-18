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

import org.apache.commons.collections.keyvalue.MultiKey;

/**
 * @author patmoore
 *
 */
public class NamespaceMapKeyImpl extends MultiKey implements NamespaceMapKey {

    /**
     * name space this key is in.
     * effectively final (but can't be because we need to load from db).
     */
    protected String namespace;
    /**
     * effectively final (but can't be because we need to load from db).
     */
    protected String key;

    public NamespaceMapKeyImpl(Object key1, Object key2, Object key3, Object key4, Object key5) {
        super(key1, key2, key3, key4, key5);
    }

    public NamespaceMapKeyImpl(Object key1, Object key2, Object key3, Object key4) {
        super(key1, key2, key3, key4);
    }

    public NamespaceMapKeyImpl(Object key1, Object key2, Object key3) {
        super(key1, key2, key3);
    }

    public NamespaceMapKeyImpl(Object key1, Object key2) {
        super(key1, key2);
    }

    public NamespaceMapKeyImpl(Object[] keys, boolean makeClone) {
        super(keys, makeClone);
    }

    public NamespaceMapKeyImpl(Object[] keys) {
        super(keys);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getKey() {
        return key;
    }

}
