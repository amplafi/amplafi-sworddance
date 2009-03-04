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

package com.sworddance.taskcontrol;

import java.util.concurrent.Future;

/**
 * implementors are called when a future is set.
 * @author patmoore
 * @param <T>
 *
 */
public interface FutureListener<T> {
    public <P extends Future<T>> void futureSet(P future, T value);
    public <P extends Future<T>> void futureSetException(P future, Throwable throwable);
}
