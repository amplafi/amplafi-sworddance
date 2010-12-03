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

package com.sworddance.beans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.testng.annotations.Test;

import static org.testng.Assert.*;
/**
 * @author patmoore
 *
 */
public class TestProxyMapper {

    @Test
    public void testSimpleGetSet() {
        Interface1Impl child1 = new Interface1Impl(2, true, null);
        Interface1Impl child = new Interface1Impl(1, false, child1);
        Interface1Impl impl = new Interface1Impl(0, true, child);
        Interface1 interface1 = ProxyFactoryImpl.INSTANCE.getProxy(impl, "goo", "child.goo");
        // make sure we are not looking at the original objects
        testProxyUnique(interface1, child1, child, impl);

        Interface1 returnedChild = interface1.getChild();
        testProxyUnique(returnedChild, interface1, child1, child, impl);
        assertFalse(returnedChild.isGoo());

        Interface1 returnedChild1 = returnedChild.getChild();
        testProxyUnique(returnedChild1, returnedChild, interface1, child1, child, impl);
        Interface1 realObject = ProxyFactoryImpl.INSTANCE.getRealObject(returnedChild1);
        assertSame(child1, realObject);
        assertTrue(returnedChild1.isGoo());

        assertNull(returnedChild1.getChild(), returnedChild1.getChild()+" should be null");
    }

    private void testProxyUnique(Object value, Object...others) {
        assertNotNull(value);
        assertTrue(Proxy.isProxyClass(value.getClass()));
        for (Object other: others) {
            assertNotSame(value, other);
        }
    }

    /**
     * Make sure that when a proxy mapper is serialize that it does not serialize the realObject.
     * Check to make sure that when deserialized it cannot access uninitialized property paths.
     * @throws Exception
     */
    @Test
    public void testSerializable() throws Exception {
        Interface1Impl child1 = new Interface1Impl(2, true, null);
        Interface1Impl child = new Interface1Impl(1, false, child1);
        Interface1Impl impl = new Interface1Impl(0, true, child);
        Interface1 interface1 = ProxyFactoryImpl.INSTANCE.getProxy(impl, ProxyBehavior.nullValue, Arrays.asList("goo", "child.goo"));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream);
        out.writeObject(interface1);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Interface1 restoredObject = (Interface1) objectInputStream.readObject();
        ProxyFactoryImpl.INSTANCE.initProxyMapper(restoredObject);
        testProxyUnique(restoredObject, interface1, child1, child, impl);
        assertTrue(restoredObject.isGoo());

        Interface1 returnedChild = restoredObject.getChild();
        testProxyUnique(returnedChild, restoredObject, interface1, child1, child, impl);
        assertFalse(returnedChild.isGoo());

        Interface1 returnedChild1 = returnedChild.getChild();
        assertNull(returnedChild1);
    }

    /**
     * test for delayed applying of changes for root.
     */
    @Test
    public void testApplyChanges() {
        Interface1Impl child1 = new Interface1Impl(2, true, null);
        Interface1Impl child = new Interface1Impl(1, false, child1);
        Interface1Impl impl = new Interface1Impl(0, true, child);
        Interface1 interface1 = ProxyFactoryImpl.INSTANCE.getProxy(impl, "goo", "child.goo", "child.child.goo");

        assertFalse(interface1.getChild().isGoo());
        interface1.getChild().setGoo(true);
        assertTrue(interface1.getChild().isGoo());
        assertFalse(child.isGoo());
        ProxyMapper<Interface1, ? extends Interface1> rootProxyMapper = ProxyFactoryImpl.INSTANCE.getProxyMapper(interface1);
        rootProxyMapper.applyToRealObject();
        assertTrue(child.isGoo());
        // applying from child proxy does not work.
//        ProxyMapper<Object, Object> childProxyMapper = ProxyMapper.getProxyMapper(interface1.getChild());
//        childProxyMapper.applyToRealObject();
//        assertTrue(child.isGoo());
    }

    public static interface Interface1 {
        public boolean isGoo();
        public void setGoo(boolean goo);
        public Interface1 getChild();
        public void setChild(Interface1 child);

    }
    public static class Interface1Impl implements Interface1 {
        private Interface1 child;
        private boolean goo;
        private int lvl;
        public Interface1Impl(int lvl, boolean goo, Interface1Impl child) {
            this.goo = goo;
            this.child = child;
            this.setLvl(lvl);
        }
        /**
         *
         */
        public Interface1Impl() {
        }
        /**
         * @param child the child to set
         */
        public void setChild(Interface1 child) {
            this.child = child;
        }
        /**
         * @return the child
         */
        public Interface1 getChild() {
            return child;
        }
        /**
         * @param goo the goo to set
         */
        public void setGoo(boolean goo) {
            this.goo = goo;
        }
        /**
         * @return the goo
         */
        public boolean isGoo() {
            return goo;
        }
        /**
         * @param lvl the lvl to set
         */
        public void setLvl(int lvl) {
            this.lvl = lvl;
        }
        /**
         * @return the lvl
         */
        public int getLvl() {
            return lvl;
        }
    }
}
