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
package com.sworddance.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
/**
 * @author patmoore
 *
 */
public class TestCreateTracking {

    @Test(dataProvider="settings")
    public void testCreateTracking(String systemProperty, boolean dummy1, boolean dummy2) {
        CreateTracking.initialize(systemProperty);
        Dummy1 dummy1_1 = new Dummy1();
        if ( dummy1 ) {
            assertNotNull(dummy1_1.createTracking);
        } else {
            assertNull(dummy1_1.createTracking);
        }
        Dummy2 dummy2_1 = new Dummy2();
        if ( dummy2 ) {
            assertNotNull(dummy2_1.createTracking);
        } else {
            assertNull(dummy2_1.createTracking);
        }
    }

    @DataProvider(name="settings")
    protected Object[][] getSettings() {
        return new Object[][] {
            new Object[] { "true", true, true },
            new Object[] { "Dummy1", true, false },
            new Object[] { "Dummy2", false, true },
            new Object[] { "Dummy.*", true, true },
        };
    }
    private static class Dummy1 {
        CreateTracking createTracking = CreateTracking.getInstance(null, Dummy1.class);
    }

    private static class Dummy2 {
        CreateTracking createTracking = CreateTracking.getInstance(null, Dummy2.class);
    }
}
