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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author patmoore
 *
 */
public class TestConcurrentInitializedMap {

    @Test
    public void testSimpleInitialization() {
        ConcurrentInitializedMap<String, String> concurrentInitializedMap = new ConcurrentInitializedMap<String, String>(String.class);

        assertFalse(concurrentInitializedMap.containsKey("foo"));
        // automatically initialized.
        assertEquals("", concurrentInitializedMap.get("foo"));
    }
    @Test
    public void testCallableInitialization() {
        ConcurrentInitializedMap<String, String> concurrentInitializedMap = new ConcurrentInitializedMap<String, String>(new Callable<String>() {
            AtomicInteger atomicInteger = new AtomicInteger(0);
            public String call() throws Exception {
                if ( !atomicInteger.compareAndSet(0, 1)) {
                    throw new IllegalStateException("initializer called more than once!");
                }
                return "secret value";
            }

        });

        assertFalse(concurrentInitializedMap.containsKey("foo"));
        // automatically initialized. (only once)
        assertEquals("secret value", concurrentInitializedMap.get("foo"));
        assertEquals("secret value", concurrentInitializedMap.get("foo"));
    }
    @Test
    public void testParameterizedCallableInitialization() {
        final ConcurrentInitializedMap<String, Object> concurrentInitializedMap = new ConcurrentInitializedMap<String, Object>(new AbstractParameterizedCallableImpl<Object>() {
            ConcurrentMap<String, AtomicInteger> atomicIntegers =  new ConcurrentInitializedMap<String, AtomicInteger>(AtomicInteger.class);
            public Object executeCall(Object...parameters) throws Exception {

                if ( !atomicIntegers.get(parameters[1]).compareAndSet(0, 1)) {
                    throw new IllegalStateException("initializer called more than once! for "+parameters[1]);
                }
                assertTrue(parameters[0] instanceof ConcurrentMap<?,?>, "class is "+parameters[0].getClass());
                return "secret value"+parameters[1];
            }

        });

        assertFalse(concurrentInitializedMap.containsKey("foo"));
        // automatically initialized.
        assertEquals("secret value"+"foo", concurrentInitializedMap.get("foo"));
        // ... only once
        assertEquals("secret value"+"foo", concurrentInitializedMap.get("foo"));
        assertEquals("secret value"+"bar", concurrentInitializedMap.get("bar"));
        assertEquals("secret value"+"bar", concurrentInitializedMap.get("bar"));
    }
}
