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

import java.util.Iterator;

/**
 * Iterator that remembers the last value next() (or prev() ) returned.
 * @author patmoore
 * @param <E> value returned
 *
 */
public interface CurrentIterator<E> extends Iterator<E> {
    /**
     *
     * @return value last returned by {@link #next()}, null if #next() has not been called yet
     */
    public E current();
    /**
     * remove() will reduce this number.
     * @return index of last item returned by the Iterator
     */
    int getIndex();
}
