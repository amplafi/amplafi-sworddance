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

import java.util.Map;

/**
 * @author patmoore
 * @param <V>
 *
 */
public abstract class AbstractParameterizedCallableImpl<V> implements ParameterizedCallable<V> {

    /**
     * @see java.util.concurrent.Callable#call()
     */
    public V call() throws Exception {
        return this.executeCall();
    }

    protected <K> Map<K, V>getMapFromParameters(Object...parameters) {
        Map<K, V>map = CUtilities.get(parameters, 0);
        ApplicationNullPointerException.notNull(map, "no map supplied");
        return map;
    }
    protected <K> K getKeyFromParameters(Object...parameters) {
        K key = CUtilities.get(parameters, 1);
        ApplicationNullPointerException.notNull(key, "no key supplied");
        return key;
    }
}
