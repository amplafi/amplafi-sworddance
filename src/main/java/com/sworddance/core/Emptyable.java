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

package com.sworddance.core;

/**
 * Just a generically useful question.
 * @author patmoore
 *
 */
public interface Emptyable {
    /**
     *
     * @return if the implementor is "empty" -- (as defined by the other parts of the implementor's spec )
     */
    boolean isEmpty();
    /**
     * make the implementor empty so next call to {@link #isEmpty()} returns true.
     * @throws UnsupportedOperationException clear() operation is not supported.
     */
    void clear() throws UnsupportedOperationException;
}
