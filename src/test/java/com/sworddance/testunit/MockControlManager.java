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
package com.sworddance.testunit;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createNiceControl;
import static org.easymock.EasyMock.createStrictControl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.easymock.IMocksControl;

/**
 * This provides a central point that can be used to retrieve the current mock control objects
 * for a given thread. This is useful when creating mock services that need to be coordinated
 * with testNG test classes.
 * @author Patrick Moore
 */
public final class MockControlManager {
    private static final Set<ControlSource> ACTIVE_CONTROL_SOURCES = new CopyOnWriteArraySet<ControlSource>();
    private static final ThreadLocal<ControlSource> CONTROL_SOURCE = new ThreadLocal<ControlSource>();
    private MockControlManager() {

    }

    public static ControlSource newStrictControlSource() {
        StrictControlSource control = new StrictControlSource();
        ACTIVE_CONTROL_SOURCES.add(control);
        return control;
    }
    public static ControlSource newControlSource() {
        ControlSource control = new RelaxedControlSource();
        ACTIVE_CONTROL_SOURCES.add(control);
        return control;
    }

	public static ControlSource newNiceControlSource() {
		ControlSource control = new NiceControlSource();
        ACTIVE_CONTROL_SOURCES.add(control);
        return control;
	}

    public static void assignThreadControlSource(ControlSource controlSource) {
        if ( ACTIVE_CONTROL_SOURCES.contains(controlSource)) {
            CONTROL_SOURCE.set(controlSource);
        } else {
            throw new IllegalStateException("mocksControl passed is not in active set.");
        }
    }
    /**
     * should be called by every test as part of its end-of-test cleanup.
     *
     */
    public static void unassignThreadControlSource() {
        CONTROL_SOURCE.remove();
    }
    /**
     * should be called for each instance of a test class when that instance is done being
     * used, and the controlSource should be discarded.
     * @param controlSource
     */
    public static void removeControlSource(ControlSource controlSource) {
        ACTIVE_CONTROL_SOURCES.remove(controlSource);
    }
    public static IMocksControl getMockControl() {
        ControlSource controlSource = CONTROL_SOURCE.get();
        if ( controlSource == null ) {
            throw new IllegalStateException("This thread does not have a controlSource registered.");
        } else if ( ACTIVE_CONTROL_SOURCES.contains(controlSource)) {
            IMocksControl mocksControl = controlSource.get();
            return mocksControl;
        } else {
            throw new IllegalStateException("controlSource passed is not in active set.");
        }
    }
    /**
     * base class for the different kinds of ControlSources which represent {@link IMocksControl}.
     * @author Patrick Moore
     */
    public abstract static class ControlSource extends ThreadLocal<IMocksControl> {
        @Override
        protected abstract IMocksControl initialValue();
    }
    /**
     * this ControlSource does not care about the call order.
     * @author Patrick Moore
     */
    private static class RelaxedControlSource extends ControlSource {
        @Override
        protected IMocksControl initialValue() {
            return createControl();
        }
    }

    /**
     * cares about the mock call order.
     * @author Patrick Moore
     */
    private static class StrictControlSource extends ControlSource {
        @Override
        protected IMocksControl initialValue() {
            return createStrictControl();
        }
    }

    /**
     * Doesn't cares about call count and order.
     * @author Konstantin Burov
     */
    private static class NiceControlSource extends ControlSource {
        @Override
        protected IMocksControl initialValue() {
            return createNiceControl();
        }
    }
}
