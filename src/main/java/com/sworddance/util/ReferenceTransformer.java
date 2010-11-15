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

import org.apache.commons.collections.Transformer;

/**
 * unpeels all the wrapping {@link Reference}.
 * @author patmoore
 *
 */
public class ReferenceTransformer implements Transformer {
    public static final ReferenceTransformer INSTANCE = new ReferenceTransformer();
    /**
     * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
     */
    public Object transform(Object input) {
        Object v = input;
        while(v instanceof Reference<?>) {
            v= ((Reference<?>)v).get();
        }
        return v;
    }

}
