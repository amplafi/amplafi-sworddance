package com.sworddance.core;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.sworddance.core.FiniteStateHolder.TransitioningFiniteStateHolderContainer;
import com.sworddance.core.FiniteStateHolder.InstantTransitionFiniteStateHolder;

import static org.testng.Assert.*;

/**
 * Test the various behaviours of FiniteState and {@link FiniteStateHolder}
 * @author patmoore
 *
 */
public class TestFiniteState {

    @Test
    public void testAllowedTransitions() {
        assertTrue(FakeFiniteState.start.isAllowedTransition(FakeFiniteState.terminal));
        assertTrue(FakeFiniteState.terminal.isAllowedTransition(FakeFiniteState.terminal));
        assertFalse(FakeFiniteState.terminal.isAllowedTransition(null));
        assertFalse(FakeFiniteState.start.isAllowedTransition(null));
        assertTrue(FakeFiniteState.backToNull.isAllowedTransition(null));
    }
    @Test
    public void testTerminalState() {
        assertTrue(FakeFiniteState.terminal.isTerminalState());
        assertFalse(FakeFiniteState.start.isTerminalState());
    }

    @Test
    public void testTransitions() {
        FiniteStateHolder<FakeFiniteState> current = new TransitioningFiniteStateHolderContainer<FakeFiniteState>();
        FakeFiniteState lastFakeFiniteState = null;
        for(FakeFiniteState nextFiniteState : Arrays.<FakeFiniteState>asList( FakeFiniteState.start, FakeFiniteState.backToNull, null, FakeFiniteState.start, FakeFiniteState.backToNull, FakeFiniteState.start, FakeFiniteState.terminal)) {
            current.initTransition(nextFiniteState);
            assertEquals(current.getFiniteState(), lastFakeFiniteState);
            assertTrue(current.isTransitioning());
            current.completeTransitionIfNeeded();
            assertEquals(current.getCurrentFiniteState(), nextFiniteState);
            lastFakeFiniteState = nextFiniteState;
        }
        assertEquals(current.getCurrentFiniteState(), FakeFiniteState.terminal);
    }

    @Test
    public void testInstantTransitions() {
        FiniteStateHolder<FakeFiniteState> current = new InstantTransitionFiniteStateHolder<FakeFiniteState>();
        for(FakeFiniteState nextFiniteState : Arrays.<FakeFiniteState>asList( FakeFiniteState.start, FakeFiniteState.backToNull, null, FakeFiniteState.start, FakeFiniteState.backToNull, FakeFiniteState.start, FakeFiniteState.terminal)) {
            current.initTransition(nextFiniteState);
            assertEquals(current.getFiniteState(), nextFiniteState);
            assertTrue(current.isTransitioning());
            current.completeTransitionIfNeeded();
            assertEquals(current.getCurrentFiniteState(), nextFiniteState);
        }
        assertEquals(current.getCurrentFiniteState(), FakeFiniteState.terminal);

    }
    public enum FakeFiniteState implements FiniteState<FakeFiniteState> {
        start, backToNull, terminal;

        static {
            start.setValidNextStatus(terminal, backToNull);
            backToNull.setValidNextStatus(start, null);
        }
        private List<FakeFiniteState> allowedTransitions;

        public static final FiniteStateChecker<FakeFiniteState> STATE_CHECKER = new FiniteStateChecker<FakeFiniteState>();


        private void setValidNextStatus(FakeFiniteState...validNextStatus) {
            this.allowedTransitions = Arrays.asList(validNextStatus);
        }
        /**
         * @see com.sworddance.core.FiniteState#checkToChange(com.sworddance.core.FiniteState)
         */
        public FakeFiniteState checkToChange(FakeFiniteState newFiniteState) {
            return STATE_CHECKER.checkToChange(this, newFiniteState);
        }

        /**
         * @see com.sworddance.core.FiniteState#isAllowedTransition(com.sworddance.core.FiniteState)
         */
        public boolean isAllowedTransition(FakeFiniteState next) {
            return STATE_CHECKER.isAllowedTransition(this, next);
        }

        public boolean isTerminalState() {
            return STATE_CHECKER.isTerminalState(this);
        }

        /**
         * @see com.sworddance.core.FiniteState#getAllowedTransitions()
         */
        public List<FakeFiniteState> getAllowedTransitions() {
            return allowedTransitions;
        }
    }
}
