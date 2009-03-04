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

/**
 * implementations collect {@link FutureListener}s for later (or immediate notification).
 *
 * @author patmoore
 * @param <T>
 *
 */
public interface FutureListenerNotifier<T> {
    /**
     * TODO -- making sure that futureListeners can still be run.
     * The futureListener should be only weakly held so that the {@link FutureListenerNotifier} will not prevent GC.
     * @param futureListener
     */
    public void addFutureListener(FutureListener<T> futureListener);
}
