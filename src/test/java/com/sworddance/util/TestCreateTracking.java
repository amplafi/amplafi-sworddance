/**
 * Copyright 2006-2011 by Amplafi. All rights reserved.
 * Confidential.
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
