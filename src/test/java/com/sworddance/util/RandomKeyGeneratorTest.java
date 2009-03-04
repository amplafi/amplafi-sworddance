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
