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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Used to provide create a List when initialization is required.
 * @author patmoore
 * @param <V>
 *
 */
public class InitializeWithList<V> implements Callable<List<V>> {
    public static final InitializeWithList INSTANCE = new InitializeWithList();
    /**
     * @see java.util.concurrent.Callable#call()
     */
    @SuppressWarnings("unused")
    @Override
    public List<V> call() throws Exception {
        return new ArrayList<V>();
    }

}
