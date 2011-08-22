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

import java.io.Serializable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.sworddance.util.map.MapKeyed;

/**
 * add some convenience to the {@link FutureTask} class.
 *
 * TODO:
 * FutureResult should have 3 possible callables:
 *
 *     * call on success
 *     * call on failure
 *     * call when result (success or failure)
 *
 * In this way when the future has a result it is able to proactive trigger the next action.
 * @param <T> type of value returned by this {@link Future}.
 * @author Patrick Moore
 */
public interface FutureResult<T> extends Future<T>, MapKeyed<Serializable>, FutureListenerNotifier {

    public Throwable getException();
    /**
     *
     * @return {@link #get()} if {@link #isSuccessful()} is true
     */
    public T poll();

    /**
     * Note that ! isFailed() != {@link #isSuccessful()} because the request may not be done.
     * @return {@link #isDone()} && ! {@link #isCancelled()} &&
     *  {@link FutureResult#getException()}==null
     */
    public boolean isSuccessful();

    /**
     * Note that ! isFailed() != {@link #isSuccessful()} because the request may not be done.
     * @return {@link #isDone()} && (! {@link #isCancelled()} ||
     *  {@link FutureResult#getException()}!=null)
     */
    public boolean isFailed();

    /**
     * TODO: still working through how to chain in a meaningful way FutureResult -> Task -> TaskGroup -> TaskControl -> ? needs to be assigned to a thread.
     * Would like the get to fail if this chain breaks.
     * @return true if some object has claimed that it will set this FutureResult.
     *
     * false means {@link #get()} or {@link #get(long, java.util.concurrent.TimeUnit)}  IllegalStateException will be thrown
     * if {@link #isOwned()} == false and !{@link #isDone()} then
     *
     */
//    public boolean isOwned();

}
