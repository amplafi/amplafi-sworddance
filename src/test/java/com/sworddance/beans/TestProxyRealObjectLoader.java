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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author patmoore
 *
 */
public class TestProxyRealObjectLoader {

    /**
     * Test to see if a child far removed from the rootProxyMapper can get to its real object.
     *
     * Use case ProxyMappers were created in one transaction and now need to be used in another ( the initial realObject has been discard as part of tx clean up)
     * @param greatGrandparent
     * @param loadedGreatGrandparent
     */
    @Test(dataProvider="greatGrandparents")
    public void testChildLoading(GreatGrandparent greatGrandparent, GreatGrandparent loadedGreatGrandparent) {
        Grandparent grandparent = greatGrandparent.getGrandparent();
        Parent parent = grandparent.getParent();
        Child child = parent.getChild();

        ProxyLoaderImpl proxyLoader = new ProxyLoaderImpl(loadedGreatGrandparent);

        ProxyFactoryImpl proxyFactoryImpl = new ProxyFactoryImpl(proxyLoader, BaseProxyMethodHelperImpl.INSTANCE);
        GreatGrandparent proxiedGreatGrandparent = proxyFactoryImpl.getProxy(greatGrandparent, "grandparent", "grandparent.parent");
        assertNotSame(grandparent, proxiedGreatGrandparent);
        ProxyMapper<GreatGrandparent, GreatGrandparent> proxiedGreatGrandparentProxyMapper = proxyFactoryImpl.getProxyMapper(proxiedGreatGrandparent);
        assertNotNull(proxiedGreatGrandparentProxyMapper.getProxyLoader());
        assertNotNull(proxiedGreatGrandparentProxyMapper.getProxyMethodHelper());
        assertTrue(proxiedGreatGrandparentProxyMapper.isRealObjectSet());
<<<<<<< HEAD
        proxiedGreatGrandparentProxyMapper.clearCached();
=======
        proxiedGreatGrandparentProxyMapper.clear();
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970

        Grandparent proxiedGrandparent = proxiedGreatGrandparent.getGrandparent();
        assertNotSame(grandparent, proxiedGrandparent);
        ProxyMapperImplementor<Grandparent, Grandparent> proxiedGrandparentProxyMapper = proxyFactoryImpl.getProxyMapper(proxiedGrandparent);
        assertFalse(proxiedGrandparentProxyMapper.isRealObjectSet());
        assertEquals(proxiedGrandparentProxyMapper.getBaseProxyMapper(), proxiedGreatGrandparentProxyMapper);
        assertEquals(proxiedGrandparentProxyMapper.getRootProxyMapper(), proxiedGreatGrandparentProxyMapper);

        Parent proxiedParent = proxiedGrandparent.getParent();
        assertNotSame(parent, proxiedParent);
        assertFalse(proxiedGrandparentProxyMapper.isRealObjectSet());
        ProxyMapperImplementor<Parent, Parent> proxiedParentProxyMapper = proxyFactoryImpl.getProxyMapper(proxiedParent);
        assertFalse(proxiedParentProxyMapper.isRealObjectSet());
        assertEquals(proxiedParentProxyMapper.getBaseProxyMapper(), proxiedGrandparentProxyMapper);
        assertEquals(proxiedParentProxyMapper.getRootProxyMapper(), proxiedGreatGrandparentProxyMapper);

        Child proxiedChild = proxiedParent.getChild();
        assertNotSame(child, proxiedChild);

        ProxyMapperImplementor<Child, Child> proxiedChildProxyMapper = proxyFactoryImpl.getProxyMapper(proxiedChild);
        assertEquals(proxiedChildProxyMapper.getBaseProxyMapper(), proxiedParentProxyMapper);
        assertEquals(proxiedChildProxyMapper.getRootProxyMapper(), proxiedGreatGrandparentProxyMapper);
        assertEquals(proxyLoader.getLoadCalled(),3, "should have been called once for parent, grandparent and greatgrandparent");
        assertTrue(proxiedGreatGrandparentProxyMapper.isRealObjectSet());
        assertTrue(proxiedGrandparentProxyMapper.isRealObjectSet());
        assertTrue(proxiedParentProxyMapper.isRealObjectSet());
        assertTrue(proxiedChildProxyMapper.isRealObjectSet());
        if ( greatGrandparent.getClass() == loadedGreatGrandparent.getClass()) {
<<<<<<< HEAD
            assertSame(child, proxiedChildProxyMapper.getRealObject(true));
            assertEquals(proxiedGreatGrandparentProxyMapper.getRealClass(), loadedGreatGrandparent.getClass());
        } else {
            // expecting different classes across transaction boundaries.
            assertNotSame(child, proxiedChildProxyMapper.getRealObject(true));
=======
            assertSame(child, proxiedChildProxyMapper.getRealObject());
            assertEquals(proxiedGreatGrandparentProxyMapper.getRealClass(), loadedGreatGrandparent.getClass());
        } else {
            // expecting different classes across transaction boundaries.
            assertNotSame(child, proxiedChildProxyMapper.getRealObject());
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
            assertFalse(proxiedGreatGrandparentProxyMapper.getRealClass().equals(loadedGreatGrandparent.getClass()));
        }

    }

    @DataProvider(name="greatGrandparents")
    public Object[][] getGreatGrandparents() {
        String value = "the_value!";
        ChildImpl child = new ChildImpl(value);
        ParentImpl parent = new ParentImpl(child);
        GrandparentImpl grandparent = new GrandparentImpl(parent);
        GreatGrandparent greatGrandparent = new GreatGrandparentImpl(grandparent);

        Child hibernateImplementationOfChild = new HibernateImplementationOfChild(value);
        Parent hibernateImplementationOfParent = new HibernateImplementationOfParent(hibernateImplementationOfChild);
        Grandparent hibernateImplementationOfGrandparent = new HibernateImplementationOfGrandparent(hibernateImplementationOfParent);
        GreatGrandparent hibernateImplementationOfGreatGrandparent = new HibernateImplementationOfGreatGrandparent(hibernateImplementationOfGrandparent);
        return new Object[][] {
            new Object[] { greatGrandparent, greatGrandparent },
            // Hibernate has returned its own implementation of GreatGrandparent
            new Object[] { greatGrandparent, hibernateImplementationOfGreatGrandparent }
        };
    }

    public interface GreatGrandparent {
        Grandparent getGrandparent();
        void setGrandparent(Grandparent grandparent);
    }
    public interface Grandparent {
        Parent getParent();
        void setParent(Parent parent);
    }
    public interface Parent {
        Child getChild();
        void setChild(Child child);
    }
    public interface Child {

    }

    @Proxy(proxyClass=GreatGrandparent.class)
    public static class GreatGrandparentImpl implements GreatGrandparent {
        private Grandparent grandparent;

        public GreatGrandparentImpl(Grandparent grandparent) {
            this.grandparent = grandparent;
        }

        /**
         * @param grandparent the grandparent to set
         */
        public void setGrandparent(Grandparent grandparent) {
            this.grandparent = grandparent;
        }

        /**
         * @return the grandparent
         */
        public Grandparent getGrandparent() {
            return grandparent;
        }
    }
    public static class GrandparentImpl implements Grandparent {
        private Parent parent;

        public GrandparentImpl(Parent parent) {
            super();
            this.parent = parent;
        }

        /**
         * @param parent the parent to set
         */
        public void setParent(Parent parent) {
            this.parent = parent;
        }

        /**
         * @return the parent
         */
        public Parent getParent() {
            return parent;
        }
    }

    public static class ParentImpl implements Parent {

        private  Child child;

        public ParentImpl(Child child) {
            this.child = child;
        }
        /**
         * @param child the child to set
         */
        public void setChild(Child child) {
            this.child = child;
        }

        /**
         * @return the child
         */
        public Child getChild() {
            return child;
        }

    }

    public static class ChildImpl implements Child {
        public ChildImpl(String value) {
            this.value = value;
        }

        private String value;

        /**
         * @param value the value to set
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

    }

    public static class HibernateImplementationOfGreatGrandparent implements GreatGrandparent {
        private Grandparent grandparent;

        public HibernateImplementationOfGreatGrandparent(Grandparent grandparent) {
            this.grandparent = grandparent;
        }

        /**
         * @param grandparent the grandparent to set
         */
        public void setGrandparent(Grandparent grandparent) {
            this.grandparent = grandparent;
        }

        /**
         * @return the grandparent
         */
        public Grandparent getGrandparent() {
            return grandparent;
        }
    }

    public static class HibernateImplementationOfGrandparent extends GrandparentImpl {

        /**
         * @param parent
         */
        public HibernateImplementationOfGrandparent(Parent parent) {
            super(parent);
        }

    }

    public static class HibernateImplementationOfParent extends ParentImpl {

        public HibernateImplementationOfParent(Child child) {
            super(child);
        }

    }

    public static class HibernateImplementationOfChild implements Child {

        private String value;
        public HibernateImplementationOfChild(String value) {
            this.value = value;
        }


        /**
         * @param value the value to set
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }

    public static class ProxyLoaderImpl extends BaseProxyLoaderImpl {
        private GreatGrandparent greatGrandparent;
        private int loadCalled;
        public ProxyLoaderImpl(GreatGrandparent greatGrandparent) {
            this.greatGrandparent = greatGrandparent;
        }
        /**
         * @see com.sworddance.beans.ProxyLoader#getRealObject(com.sworddance.beans.ProxyMapper)
         */
        @SuppressWarnings("unchecked")
<<<<<<< HEAD
=======
        @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        public <I, O extends I> O getRealObject(ProxyMapper<I, O> proxyMapper) throws ChildObjectNotLoadableException {
            Class<? extends Object> realClass = proxyMapper.getRealClass();
            loadCalled++;
            if (GreatGrandparent.class.isAssignableFrom(realClass)) {
                return (O) this.greatGrandparent;
            } else {
                throw new ChildObjectNotLoadableException();
            }
        }
        /**
         * @param loadCalled the loadCalled to set
         */
        public void setLoadCalled(int loadCalled) {
            this.loadCalled = loadCalled;
        }
        /**
         * @return the loadCalled
         */
        public int getLoadCalled() {
            return loadCalled;
        }
        /**
         * @see com.sworddance.beans.ProxyLoader#getProxyClassFromClass(java.lang.Class)
         */
<<<<<<< HEAD
=======
        @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        public <I> Class<? extends I> getProxyClassFromClass(Class<? extends I> clazz) {
            assertTrue(GreatGrandparent.class.isAssignableFrom(clazz), "should only be called for the RootProxyMapper objects");
            Class<? extends I> returnClass = super.getProxyClassFromClass(clazz);
            if ( returnClass != null ) {
                return returnClass;

            } else {
                throw new UnsupportedOperationException("should have been handled by super class");
            }
        }

    }
}
