package com.sworddance.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Copyright 2007 by Amplafi. All right reserved. Date: Aug 26, 2007 Time: 10:19:37 PM
 */
@Test(enabled = false)
public class TestClassCapture extends Assert {
    public void testCapturing() {
        ClassCapture<List> cc = new ClassCapture();
        assertEquals(cc.getCapturedClass(), List.class);
    }
}
