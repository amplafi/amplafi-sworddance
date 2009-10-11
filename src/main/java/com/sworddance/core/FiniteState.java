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

import java.util.Collection;

/**
 * implementers are classes that represent a finite state ( usually enums ) with allowed transitions.
 * @author patmoore
 * @param <T> the missing parameter
 *
 */
public interface FiniteState<T extends FiniteState<T>> {
    /**
     *
     * @return true if no transitions from this state is possible
     */
    boolean isTerminalState();
    /**
     *
     * @param newFiniteState
     * @return true if the tranistion to newFiniteState is permitted.
     */
    boolean isAllowedTransition(T newFiniteState);
    /**
     *
     * @param newFiniteState
     * @return newFiniteState if {@link #isAllowedTransition(FiniteState)} otherwise return the current setting.
     */
    T checkToChange(T newFiniteState);
    Collection<T> getAllowedTransitions();

    public class FiniteStateChecker<T extends FiniteState<T>> {
        public boolean isAllowedTransition(T oldFiniteState, T newFiniteState) {
            if ( oldFiniteState == null) {
                return true;
            } else if ( newFiniteState == null ) {
                return false;
            } else {
                return oldFiniteState.isAllowedTransition(newFiniteState);
            }
        }

        public T checkToChange(T oldFiniteState, T newFiniteState) {
            if ( isAllowedTransition(oldFiniteState, newFiniteState)) {
                return newFiniteState;
            } else {
                return oldFiniteState;
            }
        }
    }
}
