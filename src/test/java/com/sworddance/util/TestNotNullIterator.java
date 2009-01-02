/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.util;

import static org.testng.Assert.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

/**
 * @author patmoore
 *
 */
public class TestNotNullIterator {

    @Test
    public void test() {
        int i = 0;
        List<String> l = Arrays.asList(null, "foo", null, "fee");
        for (String s: new NotNullIterator<String>(l)) {
            assertNotNull(s);
            i++;
        }
        assertEquals(i, 2);

        Map<String, Object> m = new HashMap<String, Object>();
        m.put(null, "nullv");
        m.put("nv1", null);
        m.put("nnv1", "v");
        m.put("nv2", null);
        m.put("nnv2", "v");
        i = 0;
        for(Map.Entry<String, Object>entry: new NotNullIterator<Map.Entry<String, Object>>(m)) {
            assertNotNull(entry);
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            assertEquals(entry.getValue(), "v");
            i++;
        }
        assertEquals(i, 2);

        i = 0;
        for (String s: new NotNullIterator<String>(null, "foo", null, "fee")) {
            assertNotNull(s);
            i++;
        }
        assertEquals(i, 2);
    }

    /**
     * test filtering of Reference objects
     */
    @Test
    public void testReference() {
        List<Reference<String>> list = new ArrayList<Reference<String>>();
        String[] objects = new String[2];
        for(int i = 0; i < 4; i++) {
            String o = new String("foo"+i);
            objects[i/2] = o;
            list.add(new WeakReference<String>(o));
        }
        WeakReference<List<Reference<String>>> referList = new WeakReference<List<Reference<String>>>(list);
        // to compell all the weak ref objects to be gc'ed
        System.gc();
        int j = 0;
        for(String str: new NotNullIterator<String>(referList)) {
            j++;
        }
        assertEquals(j, 2);
    }
}
