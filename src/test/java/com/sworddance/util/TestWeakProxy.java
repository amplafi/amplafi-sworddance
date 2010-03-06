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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
/**
 * @author patmoore
 *
 */
public class TestWeakProxy {

    @Test
    @SuppressWarnings("unused")
    public void testWeakProxy() {
        // make sure can derive Foo interface from a foo object
        Foo foo = WeakProxy.newProxyInstance(new Foo() {});

        try {
            List<?> list = WeakProxy.newProxyInstance(new ArrayList<String>(), Collection.class);
            fail("should be returning a collection");
        } catch ( ClassCastException e) {

        }
        Collection<?> list = WeakProxy.newProxyInstance(new ArrayList<String>(), Collection.class);
        Bar o = new TestObject();
        Foo bar = WeakProxy.newProxyInstance(o);
    }
    interface Foo {

    }
    interface Bar {

    }
    class TestObject implements Foo, Bar {

    }
}
