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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author patmoore
 *
 */
public class TestNotNullIterator {

    @Test
    public void test() {
        int i = 0;
        List<String> l = Arrays.asList(null, "foo", null, "fee");
        for (String s: NotNullIterator.<String>newNotNullIterator(l)) {
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
        for(Map.Entry<String, Object>entry: NotNullIterator.<Map.Entry<String, Object>>newNotNullIterator(m)) {
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
    @SuppressWarnings("unused")
    public void testReference() {
        List<Reference<String>> list = new ArrayList<Reference<String>>();
        String[] objects = new String[2];
        for(int i = 0; i < 4; i++) {
            String o = new String("foo"+i);
            objects[i/2] = o;
            list.add(new WeakReference<String>(o));
        }
        WeakReference<List<Reference<String>>> referList = new WeakReference<List<Reference<String>>>(list);
        // to compel all the weak ref objects to be gc'ed
        System.gc();
        int j = 0;
        for(String str: NotNullIterator.<String>newNotNullIterator(referList)) {
            j++;
        }
        assertEquals(j, 2);
    }
    /**
     * Test to see that factory methods reduce NotNullIterator creation
     */
    @Test
    public void testEmpty() {
        assertSame(NotNullIterator.<String>newNotNullIterator(null), NotNullIterator.EMPTY);
        assertSame(NotNullIterator.<String>newNotNullIterator( new WeakReference<List<Reference<String>>>(null)), NotNullIterator.EMPTY);
        assertSame(NotNullIterator.<String>newNotNullIterator( new ArrayList<Reference<String>>()), NotNullIterator.EMPTY);
        assertSame(NotNullIterator.<String>newNotNullIterator( new HashMap<String, String>()), NotNullIterator.EMPTY);
        assertNotSame(NotNullIterator.<String>newNotNullIterator(Arrays.asList(null, null)), NotNullIterator.EMPTY);
    }
}
