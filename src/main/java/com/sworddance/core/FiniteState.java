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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

        private void setValidNextStatus(ManagerState...validNextStatus) {
            this.allowedTransitions = Arrays.asList(validNextStatus);
        }

        public ManagerState checkToChange(ManagerState newFiniteState) {
            return STATE_CHECKER.checkToChange(this, newFiniteState);
        }
        public Collection<ManagerState> getAllowedTransitions() {
            return this.allowedTransitions;
        }
        public boolean isAllowedTransition(ManagerState newFiniteState) {
            return STATE_CHECKER.isAllowedTransition(this, newFiniteState);
        }
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

    /**
     * Methods in this class are expected to be called only by {@link FiniteStateHolder}s and {@link FiniteState} methods.
     * This class and its methods are provided to ensure a consistent behavior across {@link FiniteStateHolder}s.
     *
     * @param <T>
     */
    public class FiniteStateChecker<T extends FiniteState<T>> {
        /**
         * States that are always allowed to transition to any other state.
         */
        private final List<T> alwaysAllowedTransitions;
        /**
         * @param alwaysAllowedTransitions states that can transition to any other state.
         */
        public FiniteStateChecker(T... alwaysAllowedTransitions) {
            this.alwaysAllowedTransitions = Arrays.asList(alwaysAllowedTransitions);
        }

        /**
         * Only intended to be called by {@link FiniteState#isAllowedTransition(FiniteState)}
         * true is intended to be definitive for subclasses. false could be changed to true by subclasses. So this default
         * method is intentionally conservative.
         *
         * @param oldFiniteState
         * @param newFiniteState maybe null. Null is checked for like any other state.
         * @return true if oldFiniteState == (null | newFiniteState ) or newFiniteState in ( alwaysAllowed | old.allowed set )
         * false the subclasses can do additional checks.
         */
        public boolean isAllowedTransition(T oldFiniteState, T newFiniteState) {
            if ( oldFiniteState == null || oldFiniteState == newFiniteState) {
                return true;
            } else if ( this.alwaysAllowedTransitions.contains(oldFiniteState)) {
                // the old state is always allowed to transition from...
                return true;
            } else if ( isTerminalState(oldFiniteState)) {
                return false;
            } else {
                return oldFiniteState.getAllowedTransitions().contains(newFiniteState);
            }
        }

        public boolean isAllowedTransition(FiniteStateHolder<T> finiteStateHolder, T nextFiniteState) {
            // do not call isAllowedTransition(FiniteState,FiniteState) directly. want to allow the FiniteState to
            // have its own coding.
            T currentNextFiniteState = finiteStateHolder.getNextFiniteState();
            T finiteState = finiteStateHolder.getFiniteState();
            if ( currentNextFiniteState != null) {
                return currentNextFiniteState.isAllowedTransition(nextFiniteState)
                && ( finiteState == null || finiteState.isAllowedTransition(nextFiniteState));
            } else if ( finiteState != null ) {
                return finiteState.isAllowedTransition(nextFiniteState);
            } else {
                // no current state at all
                return true;
            }
        }
        /**
         * @param finiteState
         * @return true if finiteState != null and there are no allowed transitions.
         */
        public boolean isTerminalState(T finiteState) {
            return finiteState != null && (isEmpty(finiteState.getAllowedTransitions()));
        }

        public T getCurrentFiniteState(FiniteStateHolder<T> finiteStateHolder) {
            return finiteStateHolder.getNextFiniteState() != null? finiteStateHolder.getNextFiniteState(): finiteStateHolder.getFiniteState();
        }

        /**
         * Also checks to make sure the transition is allowed.
         * @param finiteStateHolder
         * @param nextFiniteState
         * @return true if currentFiniteState != nextFiniteState and currentFiniteState-to-nextFiniteState is allowed.
         */
        public boolean isTransitionNeeded(FiniteStateHolder<T> finiteStateHolder, T nextFiniteState) {
            T currentFiniteState = finiteStateHolder.getCurrentFiniteState();
            return currentFiniteState != nextFiniteState && currentFiniteState.isAllowedTransition(nextFiniteState);
        }
        /**
         *
         * @param oldFiniteState
         * @param newFiniteState
         * @return oldFiniteState if !oldFiniteState.{@link FiniteState#isAllowedTransition(FiniteState)} otherwise newFiniteState.
         */
        public T checkToChange(T oldFiniteState, T newFiniteState) {
            // we want to go through the enum's isAllowedTransition() just in case it has extended behavior.
            if ( oldFiniteState == null || oldFiniteState.isAllowedTransition(newFiniteState)) {
                return newFiniteState;
            } else {
                return oldFiniteState;
            }
        }

        /**
         * throws exception if !oldFiniteState.{@link FiniteState#isAllowedTransition(FiniteState)}
         * @param oldFiniteState
         * @param newFiniteState
         * @param messages
         * @return newFiniteState
         * @throws ApplicationIllegalStateException if the transition is not permitted.
         */
        public T checkAllowed(T oldFiniteState, T newFiniteState, Object...messages) throws ApplicationIllegalStateException {
            ApplicationIllegalStateException.checkState(isAllowedTransition(oldFiniteState, newFiniteState),
                getClassSafely(oldFiniteState,newFiniteState), ": Finite state cannot transition from ", oldFiniteState," to ", newFiniteState, " : ", messages);
            return newFiniteState;
        }
    }
}
