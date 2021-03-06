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
import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationIllegalStateException;

/**
 * Holds a FiniteState for an object.
 * FiniteStateHolders are expected to be somewhat immutable.
 *
 * The somewhat nature is still being worked out in the propetitary code. Right now there are transitions between a FS1 -> FS2
 * which require a new FiniteStateHolder. The usual example is that FS1 is an auditable event ( something being posted public for example)
 * and we want to preserve that the posting was public.
 *
 * Other transitions may not require such audit records, for example, "unread" -> "read" transitions, (assuming no audit log is expected on read actions).
 *
 * Note that this "auditness" is defined in the transition note in the FiniteState itself. ( there can't be a FiniteState.isNewFiniteStateHolderNeeded() )
 *
 * @author patmoore
 * @param <FS>
 */
public interface FiniteStateHolder<FS extends FiniteState<FS>> {

    /**
     * If {@link #getNextFiniteState()} is set then record is in a transitioning state.
     *
     * @return true if this has not null {@link #getNextFiniteState()}
     */
    public boolean isTransitioning();

    /**
     * @return the FiniteState that has most recently been completed.
     */
    public FS getFiniteState();

    /**
     * @return the FiniteState being transitioned to (if any).
     */
    public FS getNextFiniteState();

    /**
     * TODO: better name
     * @return {@link #getNextFiniteState()} if {@link #isTransitioning()} otherwise {@link #getFiniteState()}
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
    public boolean isNewFiniteStateHolderNeeded(FS nextFiniteState);

    /**
     * Move {@link #getNextFiniteState()} to the {@link #getFiniteState()} and clear {@link #getNextFiniteState()}
     * @return true if a transition did need to be completed
     */
    public boolean completeTransitionIfNeeded();

    /**
     * sets the nextFiniteState.
     *
     * @param <TR>
     * @param nextFiniteState
     * @return this or possibly new FiniteStateHolder.
     */
    public <TR extends FiniteStateHolder<FS>> TR initTransition(FS nextFiniteState);

    /**
     * sets the next finite state if allowed, returns this object silently otherwise.
     * 
     * @param nextFiniteState
     * @return this or possibly new FiniteStateHolder.
     */
    public <TR extends FiniteStateHolder<FS>> TR initTransitionIfAllowed(FS nextFiniteState);
    
    /**
     * Cancels current transition.
     */
    public void cancelTransition();

    public abstract class AbstractFiniteStateHolder<FS extends FiniteState<FS>> implements FiniteStateHolder<FS> {

        protected FS finiteState;
        private boolean transitioning;

        /**
         * @see com.sworddance.core.FiniteStateHolder#getCurrentFiniteState()
         */
        public FS getCurrentFiniteState() {
            return isTransitioning()? this.getNextFiniteState(): this.getFiniteState();
        }

        /**
         * @see com.sworddance.core.FiniteStateHolder#getFiniteState()
         */
        public FS getFiniteState() {
            return this.finiteState;
        }

        /**
         * @see com.sworddance.core.FiniteStateHolder#initTransition(com.sworddance.core.FiniteState)
         */
        protected void setFiniteState(FS finiteState) {
            this.finiteState = finiteState;
        }

        /**
         * @see com.sworddance.core.FiniteStateHolder#isAllowedTransition(com.sworddance.core.FiniteState)
         */
        public boolean isAllowedTransition(FS nextFiniteState) {
            if ( this.finiteState == null || this.finiteState.isAllowedTransition(nextFiniteState)) {
                // testing the starting finiteState to avoid mysterious 'jumps' between FiniteStates that are normally not legal.
                if ( isTransitioning()) {
                    FS currentNextFiniteState = this.getNextFiniteState();
                    return currentNextFiniteState == null || currentNextFiniteState.isAllowedTransition(nextFiniteState);
                } else {
                    // no current state at all
                    return true;
                }
            } else {
                return false;
            }
        }
        /**
         * @see com.sworddance.core.FiniteStateHolder#isTransitionNeeded(com.sworddance.core.FiniteState)
         */
        public boolean isTransitionNeeded(FS nextFiniteState) {
            FS currentFiniteState = this.getCurrentFiniteState();
            return currentFiniteState != nextFiniteState && currentFiniteState.isAllowedTransition(nextFiniteState);
        }

        /**
         * @see com.sworddance.core.FiniteStateHolder#isTransitioning()
         */
        public boolean isTransitioning() {
            return this.transitioning;
        }

        /**
         * @see com.sworddance.core.FiniteStateHolder#initTransition(com.sworddance.core.FiniteState)
         */
        @SuppressWarnings("unchecked")
        public <TR extends FiniteStateHolder<FS>> TR initTransition(FS nextFiniteState) {
            ApplicationIllegalArgumentException.valid(!isNewFiniteStateHolderNeeded(nextFiniteState), "Default behavior cannot handle creating new FSH for ", this," transitioning to ", nextFiniteState);
            return (TR) doInitTransition(nextFiniteState);
        }
        @SuppressWarnings("unchecked")
        public <TR extends FiniteStateHolder<FS>> TR initTransitionIfAllowed(FS nextFiniteState) {
            if (isAllowedTransition(nextFiniteState)) {
                return initTransition(nextFiniteState);
            }
            return (TR) this;
        }
        /**
         * Note that null nextFiniteState is treated like a state that must be transitioned to.
         * @param nextFiniteState
         * @return this
         */
        @SuppressWarnings("unchecked")
        protected <TR extends FiniteStateHolder<FS>> TR doInitTransition(FS nextFiniteState) {
            ApplicationIllegalStateException.checkState(isAllowedTransition(nextFiniteState), getCurrentFiniteState()," to ",nextFiniteState, " not allowed.");

            if ( this.getFiniteState() == nextFiniteState){
                this.cancelTransition();
            } else {
                this.setNextFiniteState(nextFiniteState);
                this.transitioning = true;
            }
            return (TR) this;
        }
        protected abstract void setNextFiniteState(FS nextFiniteState);

        protected void resetTransitioning() {
            this.transitioning = false;
        }
    }
    /**
     * @author patmoore
     * @param <FS> FiniteState implementing Class
     *
     */
    public class TransitioningFiniteStateHolderContainer<FS extends FiniteState<FS>> extends AbstractFiniteStateHolder<FS> {

        private FS currentNextFiniteState;
        public TransitioningFiniteStateHolderContainer() {

        }
        public TransitioningFiniteStateHolderContainer(FS finiteState) {
            this.finiteState = finiteState;
        }
        /**
         * @see com.sworddance.core.FiniteStateHolder#cancelTransition()
         */
        public void cancelTransition() {
            this.currentNextFiniteState = null;
            resetTransitioning();
        }

        /**
         * @see com.sworddance.core.FiniteStateHolder#completeTransitionIfNeeded()
         */
        public boolean completeTransitionIfNeeded() {
            if ( this.isTransitioning() ) {
                this.finiteState = this.currentNextFiniteState;
                this.currentNextFiniteState = null;
                resetTransitioning();
                return true;
            } else {
                return false;
            }
        }
        /**
         * @see com.sworddance.core.FiniteStateHolder#getNextFiniteState()
         */
        public FS getNextFiniteState() {
            return this.currentNextFiniteState;
        }

        /**
         * @see com.sworddance.core.FiniteStateHolder#initTransition(com.sworddance.core.FiniteState)
         */
        @Override
        protected void setNextFiniteState(FS nextFiniteState) {
            this.currentNextFiniteState = nextFiniteState;
        }

        /**
         * Subclasses can do their own check if this method returns true
         * @return false if nextFiniteState == null or nextFiniteState == {@link #getCurrentFiniteState()} or ! {@link #isAllowedTransition(FiniteState)}
         * @see com.sworddance.core.FiniteStateHolder#isNewFiniteStateHolderNeeded(com.sworddance.core.FiniteState)
         */
        protected boolean isIfDifferentThenNewFiniteStateHolderNeeded(FS nextFiniteState) {
            if ( this.getFiniteState() == null || nextFiniteState == null ) {
                // initial State. or canceling prior transition
                return false;
            } else if ( this.getCurrentFiniteState() == nextFiniteState) {
                // a NOP
                return false;
            } else if ( !this.isAllowedTransition(nextFiniteState)) {
                // illegal transition
                return false;
            } else {
                return true;
            }

        }

        /**
         * Always returns false;
         * @see com.sworddance.core.FiniteStateHolder#isNewFiniteStateHolderNeeded(com.sworddance.core.FiniteState)
         */
        public boolean isNewFiniteStateHolderNeeded(FS nextFiniteState) {
            return false;
        }
        @Override
        public String toString() {
            return "\"current\":\""+this.getFiniteState()+"\"; \"next\":\""+this.getNextFiniteState()+"\"; \"transitioning\":"+this.isTransitioning();
        }
    }

    /**
     * Useful when the transition should happen as a result of the db transaction completing
     * @author patmoore
     *
     * @param <FS>
     */
    public class InstantTransitionFiniteStateHolder<FS extends FiniteState<FS>> extends AbstractFiniteStateHolder<FS> {

        public FS getNextFiniteState() {
            return isTransitioning()?getFiniteState():null;
        }

        public boolean isNewFiniteStateHolderNeeded(FS nextFiniteState) {
            return false;
        }

        public boolean completeTransitionIfNeeded() {
            if ( this.isTransitioning() ) {
                resetTransitioning();
                return true;
            } else {
                return false;
            }
        }

        public void cancelTransition() {
            resetTransitioning();
        }

        @Override
        protected void setNextFiniteState(FS nextFiniteState) {
            this.setFiniteState(nextFiniteState);
        }

    }
}
