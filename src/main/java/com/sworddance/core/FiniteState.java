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
import com.sworddance.util.ApplicationIllegalStateException;

import static com.sworddance.util.CUtilities.*;

/**
 * implementers are classes that represent a finite state ( usually enums ) with allowed transitions.
 *
 * Standard Template: (replace 'ManagerState' with actual enum name )
 *
 * <pre>
        private List<ManagerState> allowedTransitions;

        public static final FiniteStateChecker<ManagerState> STATE_CHECKER = new FiniteStateChecker<ManagerState>();

        @Override
        public ManagerState checkToChange(ManagerState newFiniteState) {
            return STATE_CHECKER.checkToChange(this, newFiniteState);
        }

        @Override
        public Collection<ManagerState> getAllowedTransitions() {
            return this.allowedTransitions;
        }

        @Override
        public boolean isAllowedTransition(ManagerState newFiniteState) {
            return STATE_CHECKER.isAllowedTransition(this, newFiniteState);
        }

        @Override
        public boolean isTerminalState() {
            return STATE_CHECKER.isTerminalState(this);
        }
 * </pre>
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
     * @return true if the transition to newFiniteState is permitted.
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
            if ( oldFiniteState == null || oldFiniteState == newFiniteState) {
                return true;
            } else if ( newFiniteState == null || isTerminalState(oldFiniteState)) {
                return false;
            } else {
                return oldFiniteState.getAllowedTransitions().contains(newFiniteState);
            }
        }

        /**
         * @param finiteState
         * @return true if finiteState != null and there are no allowed transitions.
         */
        public boolean isTerminalState(T finiteState) {
            return finiteState != null && (isEmpty(finiteState.getAllowedTransitions()));
        }

        /**
         *
         * @param oldFiniteState
         * @param newFiniteState
         * @return oldFiniteState if !oldFiniteState.{@link FiniteState#isAllowedTransition(FiniteState)} otherwise newFiniteState.
         */
        public T checkToChange(T oldFiniteState, T newFiniteState) {
            if ( isAllowedTransition(oldFiniteState, newFiniteState)) {
                return newFiniteState;
            } else {
                return oldFiniteState;
            }
        }

        /**
         * throws exception if !oldFiniteState.{@link FiniteState#isAllowedTransition(FiniteState)}
         * @param oldFiniteState
         * @param newFiniteState
         * @return newFiniteState
         * @throws ApplicationIllegalStateException if the transition is not permitted.
         */
        public T checkAllowed(T oldFiniteState, T newFiniteState) throws ApplicationIllegalStateException {
            ApplicationIllegalStateException.checkState(isAllowedTransition(oldFiniteState, newFiniteState),
                "cannot go from ", oldFiniteState," to ", newFiniteState);
            return newFiniteState;
        }
    }
}
