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
@Test
public class TestUtilities {
    public void testGetFirst() {
        assertNull(getFirst(null));
        assertEquals(getFirst(new String[]{"a"}), "a");
        assertEquals(getFirst(new String[]{"a", "b"}), "a");
        assertEquals(getFirst(Arrays.asList("a", "b")), "a");
        assertEquals(getFirst("a"), "a");
        assertEquals(getFirst(new TreeSet<String>(Arrays.asList("a", "b"))), "a");
    }

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

    /**
     * test to make sure that get does not alter the map,
     * callables works right
     */
    public void testGet() {
        // use concurrent map because it does not allow null keys or values
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
        assertNull(get(map, "foo"));
        assertFalse(map.containsKey("foo"));

        assertNull(get(map, "foo", new Callable<String>() {

            @Override
            public String call() throws Exception {
                return null;
            }

        }));
        assertFalse(map.containsKey("foo"));
        assertEquals(get(map, "foo", new Callable<String>() {

            @Override
            public String call() throws Exception {
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
    }

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
}
