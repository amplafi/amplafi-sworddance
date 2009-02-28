/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

import static org.testng.Assert.*;

import java.lang.reflect.Proxy;

import org.testng.annotations.Test;
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
        Interface1 interface1 = ProxyFactoryImpl.getProxy(impl, "goo", "child.goo");
        // make sure we are not looking at the original objects
        testProxyUnique(interface1, child1, child, impl);

        Interface1 returnedChild = interface1.getChild();
        testProxyUnique(returnedChild, interface1, child1, child, impl);
        assertFalse(returnedChild.isGoo());

        Interface1 returnedChild1 = returnedChild.getChild();
        testProxyUnique(returnedChild1, returnedChild, interface1, child1, child, impl);
        assertSame(child1, ProxyMapper.getRealObject(returnedChild1));
        assertTrue(returnedChild1.isGoo());
    }

    private void testProxyUnique(Object value, Object...others) {
        assertNotNull(value);
        assertTrue(Proxy.isProxyClass(value.getClass()));
        for (Object other: others) {
            assertNotSame(value, other);
        }
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
