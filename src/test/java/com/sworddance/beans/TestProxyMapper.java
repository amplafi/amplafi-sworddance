/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
/**
 * @author patmoore
 *
 */
public class TestProxyMapper {

    @Test(enabled=false)
    public void testSimpleGetSet() {
        Interface1Impl child = new Interface1Impl(false, null);
        Interface1Impl impl = new Interface1Impl(true, child);
        Interface1 interface1 = ProxyMapper.getProxy(impl, "goo", "child.goo");
        Interface1 returnedChild = interface1.getChild();
        assertNotNull(returnedChild);
        assertNotSame(child, returnedChild);
        assertFalse(returnedChild.isGoo());
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
        public Interface1Impl(boolean goo, Interface1Impl child) {
            this.goo = goo;
            this.child = child;
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
    }
}
