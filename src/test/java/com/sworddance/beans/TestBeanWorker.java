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
import java.lang.reflect.Field;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author patmoore
 *
 */
public class TestBeanWorker {

    /**
     * test to make sure we can understand bean names correctly.
     */
    @Test
    public void testGetterSetter() {
        BeanWorker beanWorker = new BeanWorker();
        String f = beanWorker.getGetterPropertyName("isFoo");
        assertEquals(f, "foo");
        f = beanWorker.getGetterPropertyName("getFoo");
        assertEquals(f, "foo");
        f = beanWorker.getGetterPropertyName("geTFoo");
        assertNull(f);

        f = beanWorker.getSetterPropertyName("setFoo");
        assertEquals(f, "foo");
        f = beanWorker.getSetterPropertyName("seTFoo");
        assertNull(f);
        f = beanWorker.getSetterPropertyName("equals");
        assertNull(f);
    }

    @Test
    public void testCanGetAccessToIntermediateProperties() {

    }

    /**
     * Test to make sure that only the requested properties are mapped.
     * Also test to make sure that a restricted set of mappings does not screw up another (different set of mappings)
     * @throws Exception
     */
    @Test
    public void testMethodMap() throws Exception {
        BeanWorker beanWorker = new BeanWorker("goo", "testClass.delete");
        Map<String, PropertyMethodChain> map = beanWorker.getMethodMap(TestClass.class);
        assertNull(map.get("delete"));
        PropertyMethodChain list = map.get("goo");
        assertNotNull(list);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getGetter(), TestClass.class.getMethod("getGoo", new Class[0]));

        assertNull(map.get("testClass"));
        list = map.get("testClass.delete");
        assertNotNull(list);

        assertNull(list.get(0).getSetter());
        assertEquals(list.get(1).getGetter(), TestClass.class.getMethod("isDelete", new Class[0]));
        assertEquals(list.get(1).getSetter(), TestClass.class.getMethod("setDelete", new Class[] {boolean.class}));

        // test different set of mappings
        BeanWorker beanWorker1 = new BeanWorker(false, "goo", "delete");
        TestClass testClass1 = new TestClass();
        testClass1.setDelete(true);
        boolean delete = (Boolean) beanWorker1.getValue(testClass1, "delete");
        assertTrue(delete);
    }

    @Test
    public void testPropertyType() {
        BeanWorker beanWorker = new BeanWorker("goo", "testClass.delete");
        Class<?> gooClass = beanWorker.getPropertyType(TestClass.class, "goo");
        assertEquals(gooClass, String.class);
        Class<?> deleteClass = beanWorker.getPropertyType(TestClass.class, "testClass.delete");
        assertEquals(deleteClass, boolean.class);
        Class<?> testClass = beanWorker.getPropertyType(TestClass.class, "testClass");
        assertEquals(testClass, TestClass.class);
    }
    /**
     * tests get/set using the same BeanWorker ( this tests duck-typing ) on 2 different classes that are not related to each other.
     * @param beanWorker
     * @param testClass
     * @param childTestClass
     * @throws Exception
     */
    @Test(dataProvider="testClassInstances")
    public void testPropertyGetSet(BeanWorker beanWorker, Object testClass, Object childTestClass) throws Exception {
        Field deleteField = childTestClass.getClass().getField("delete");
        assertEquals(deleteField.get(childTestClass), true);
        boolean childDelete = (Boolean) beanWorker.getValue(testClass, "testClass.delete");
        assertTrue(childDelete);
        beanWorker.setValue(testClass, "testClass.delete", false);
        childDelete =  (Boolean) beanWorker.getValue(testClass, "testClass.delete");
        assertFalse(childDelete);
        // extra checks to make sure that childTestClass is still assigned to testClass.testClass;
        assertEquals(deleteField.get(childTestClass), false);
        assertSame(testClass.getClass().getField("testClass").get(testClass), childTestClass);
    }
    @DataProvider(name="testClassInstances")
    public Object[][] getTestClassInstances() {
        BeanWorker beanWorker = new BeanWorker(false, "goo", "testClass.delete");
        TestClass testClass = new TestClass();
        testClass.goo = "goo1";
        testClass.testClass = new TestClass();
        TestClass childTestClass = testClass.testClass;
        childTestClass.delete = true;

        TestClass1 testClass1 = new TestClass1();
        testClass.goo = "goo1";
        testClass1.testClass = new TestClass1();
        TestClass1 childTestClass1 = testClass1.testClass;
        childTestClass1.delete = true;
        return new Object[][] {
             new Object[] { beanWorker, testClass, childTestClass },
             new Object[] { beanWorker, testClass1, childTestClass1 }
        };
    }

    public static class TestClass {
        public String goo;
        public boolean delete;
        public TestClass testClass;

        /**
         * @param goo the goo to set
         */
        public void setGoo(String goo) {
            this.goo = goo;
        }

        /**
         * @return the goo
         */
        public String getGoo() {
            return goo;
        }

        /**
         * @param delete the delete to set
         */
        public void setDelete(boolean delete) {
            this.delete = delete;
        }

        /**
         * @return the delete
         */
        public boolean isDelete() {
            return delete;
        }

        /**
         * @param testClass the testClass to set
         */
        public void setTestClass(TestClass testClass) {
            this.testClass = testClass;
        }

        /**
         * @return the testClass
         */
        public TestClass getTestClass() {
            return testClass;
        }
    }
    public static class TestClass1 {
        public String goo;
        public boolean delete;
        public TestClass1 testClass;

        /**
         * @param goo the goo to set
         */
        public void setGoo(String goo) {
            this.goo = goo;
        }

        /**
         * @return the goo
         */
        public String getGoo() {
            return goo;
        }

        /**
         * @param delete the delete to set
         */
        public void setDelete(boolean delete) {
            this.delete = delete;
        }

        /**
         * @return the delete
         */
        public boolean isDelete() {
            return delete;
        }

        /**
         * @param testClass the testClass to set
         */
        public void setTestClass(TestClass1 testClass) {
            this.testClass = testClass;
        }

        /**
         * @return the testClass
         */
        public TestClass1 getTestClass() {
            return testClass;
        }
    }
}
