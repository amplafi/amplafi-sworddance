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
import java.util.Map;
import org.apache.commons.collections.Predicate;

/**
 * Make sure the evaluated object is not null.
 * If the object is a {@link Reference} the object is dereferenced  and if the object is a Map.Entry then
 * make sure that the key and the value held by the entry are also not null.
 * @author patmoore
 *
 */
public class NotNullKeyValuePredicate implements Predicate {
    public static final NotNullKeyValuePredicate INSTANCE = new NotNullKeyValuePredicate();
    /**
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    @Override
    public boolean evaluate(Object object) {
        Object v = object;
        while ( v instanceof Reference<?>) {
            v = ((Reference<?>)v).get();
        }
        if ( v == null) {
            return false;
        }
        if ( v instanceof Map.Entry<?,?>) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) v;
            if ( entry.getKey() == null || entry.getValue() == null) {
                return false;
            }
        }
        return true;
    }

}
