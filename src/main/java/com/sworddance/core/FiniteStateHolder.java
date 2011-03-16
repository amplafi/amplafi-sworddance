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

import com.sworddance.core.FiniteState;

/**
 * Holds the current FiniteState for an object.
 * @author patmoore
 */
public interface FiniteStateHolder<FS extends FiniteState<FS>> {

    /**
     * If {@link #getNextFiniteState()} is set then record is in a transitioning state.
     *
     * @return true if this has not null {@link #getNextFiniteState()}
     */
    public boolean isTransitioning();

    /**
     * @return the messagePointAction that has most recently been completed.
     */
    public FS getFiniteState();

    /**
     * @return the messagePointAction being transitioned to (if any).
     */
    public FS getNextFiniteState();

    /**
     * @return {@link #getNextFiniteState()} if not null otherwise {@link #getFiniteState()}
     */
    public FS getCurrentFiniteState();

    public boolean isAllowedTransition(FS nextFiniteState);

    /**
     * @param nextFiniteState
     * @return true if {@link #isAllowedTransition(FiniteState)} and
     *         {@link #getCurrentFiniteState()} != nextFiniteState
     */
    public boolean isTransitionNeeded(FS nextFiniteState);

    /**
     * assumed that {@link #isAllowedTransition(FiniteState)} has been called.
     *
     * @param nextFiniteState
     * @return true if calling {@link #initTransition(FiniteState)} with nextFiniteState will result in a new FiniteStateHolder
     */
    public boolean isNewTransitionableResourceNeeded(FS nextFiniteState);

    /**
     * Move {@link #getNextFiniteState()} to the {@link #getFiniteState()} and clear {@link #getNextFiniteState()}
     */
    public void completeTransitionIfNeeded();

    /**
     * sets the nextFiniteState.
     *
     * @param <TR>
     * @param nextFiniteState
     * @return this or possibly new FiniteStateHolder.
     */
    public <TR extends FiniteStateHolder<FS>> TR initTransition(FS nextFiniteState);

    /**
     * Cancels current transition.
     *
     * @param <TR>
     * @return this or possibly new FiniteStateHolder.
     */
    public void cancelTransition();
}
