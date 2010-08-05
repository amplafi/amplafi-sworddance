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

import org.testng.annotations.Test;
import static org.testng.Assert.*;
/**
 * @author patmoore
 *
 */
public class TestComparableComparator {

    @Test
    public void testMin() {
        Integer result = ComparableComparator.INSTANCE.min(1,4, 2, null);
        assertTrue(new Integer(1).equals(result), "result="+result);
    }

    @Test
    public void testMax() {
        Integer result = ComparableComparator.INSTANCE.max(1,4, 2, null);
        assertTrue(new Integer(4).equals(result), "result="+result);
    }
}
