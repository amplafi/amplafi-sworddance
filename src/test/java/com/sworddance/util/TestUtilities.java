package com.sworddance.util;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import static com.sworddance.util.CUtilities.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
/**
 * Tests for {@link CUtilities}.
 */
public class TestUtilities {
    @Test
    public void testGetFirst() {
        assertNull(getFirst(null));
        assertEquals(getFirst(new String[]{"a"}), "a");
        assertEquals(getFirst(new String[]{"a", "b"}), "a");
        assertEquals(getFirst(Arrays.asList("a", "b")), "a");
        assertEquals(getFirst("a"), "a");
        assertEquals(getFirst(new TreeSet<String>(Arrays.asList("a", "b"))), "a");
    }

    @Test
    public void testCloneArrayList() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        List<String> list2 = cloneCollection(list);
        assertEquals(list2.getClass(), ArrayList.class);
        assertEquals(list2.size(), 2);
        assertEquals(list2.get(0), "a");
        assertEquals(list2.get(1), "b");
    }

    @Test
    public void testCloneTreeSet() {
        Set<String> set = new TreeSet<String>();
        set.add("a");
        set.add("b");
        Set<String> set2 = cloneCollection(set);
        assertEquals(set2.getClass(), TreeSet.class);
        assertEquals(set2.size(), 2);
        Iterator<String> iterator = set2.iterator();
        assertEquals(iterator.next(), "a");
        assertEquals(iterator.next(), "b");
    }

    @Test
    public void testCombine() {
        String[] checkNullCase  = combine(null, null, new String[] { null, null });
        assertTrue(Arrays.deepEquals( checkNullCase, new String[] { null,null,null,null} ));
        // all nulls needs an explicit class supplied.
        checkNullCase  = combineToSpecifiedClass(String.class, (String)null, null, null, null );
        assertTrue(Arrays.deepEquals( checkNullCase, new String[] { null,null,null,null} ));
        String[] result = combine("foo", "bar", new String[] {"nested1", "nested2"});
        assertTrue(Arrays.deepEquals(result, new String[] {"foo", "bar", "nested1", "nested2"}));

        Object[] args = new Object[] {null, "bar", new String[] {"nested1", "nested2"}};
        result = combine(args);
        assertTrue(Arrays.deepEquals(result, new String[] {null, "bar", "nested1", "nested2"}), Arrays.asList(result).toString());
    }

    /**
     * test to make sure that get does not alter the map,
     * callables works right
     */
    @Test
    public void testGet() {
        // use concurrent map because it does not allow null keys or values
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
        assertNull(get(map, "foo"));
        assertFalse(map.containsKey("foo"));

        assertNull(get(map, "foo", new Callable<String>() {

            @Override
            public String call() {
                return null;
            }

        }));
        assertFalse(map.containsKey("foo"));
        assertEquals(get(map, "foo", new Callable<String>() {

            @Override
            public String call() {
                return "bar";
            }

        }), "bar");
        assertTrue(map.containsKey("foo"));
        assertEquals(get(map, "foo", new Callable<String>() {

            @Override
            public String call() throws Exception {
                throw new IllegalStateException("Should not have called this!");
            }

        }), "bar");

        // test returning of default value even if map or key is null
        Integer i = get(null, null, 1);
        assertEquals(i, Integer.valueOf(1));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(isEmpty(null));
        assertTrue(isEmpty(""));
        assertTrue(isEmpty(new HashMap<Object, Object>()));
        assertTrue(isEmpty(new ArrayList<Object>()));
        assertTrue(isEmpty(new Object[0]));
        assertTrue(isEmpty(new String[0]));
        assertFalse(isEmpty("foo"));
        assertFalse(isEmpty(new StringBuilder("foo")));

        assertTrue(isEmpty(new StringBuilder("")));
        List<Object> l = new ArrayList<Object>();
        l.add(new Object());
        assertFalse(isEmpty(l));
        Map<Object, Object> m = new HashMap<Object, Object>();
        m.put("ff", new Object());
        assertFalse(isEmpty(m));
        assertFalse(isEmpty(new Object[] { new Object()}));
    }

    @Test
    public void testGetClassSafely() {
        Class<?> clazz = getClassSafely(null, "foo");
        assertEquals(clazz, String.class);
        clazz = getClassSafely(new Object(), "foo");
        assertEquals(clazz, Object.class);
        clazz = getClassSafely();
        assertEquals(clazz, null);
        clazz = getClassSafely(null);
        assertEquals(clazz, null);
    }
}
