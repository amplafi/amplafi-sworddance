package com.sworddance.util;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import com.sworddance.util.RandomKeyGenerator;

/**
 * Test for {@link RandomKeyGenerator}
 *
 */
public class RandomKeyGeneratorTest {

    /**
     * Generate 1000 random keys and check length of them
     *
     */
    @Test
    public void testGeneration() {
        RandomKeyGenerator rnd = new RandomKeyGenerator();
        for (int i = 0; i < 1000; i++) {
            String nextKey = rnd.nextKey();
            assertNotNull(nextKey);
            assertEquals(nextKey.length(), 7);
        }
        assertEquals(rnd.nextKey(100).length(), 100);
    }

}
