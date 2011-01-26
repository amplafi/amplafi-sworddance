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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.StringValueTransformer;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sworddance.util.PredicatedTransformingIterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test()
public class TestPredicatedTransformingIterator {

    private List<Integer> ints;
    private Predicate predicate;

    @BeforeMethod()
    protected void setUp() {
        ints = new ArrayList<Integer>();
        ints.add(0);
        ints.add(1);
        ints.add(2);
        ints.add(3);
        ints.add(4);
        ints.add(5);
        predicate = new Predicate() {
                    public boolean evaluate(Object object) {
                        return ((Integer)object) % 2 == 0;
                    }
                };
    }

    public void testSimple() {
        PredicatedTransformingIterator<Integer> transformingIterator =
            new PredicatedTransformingIterator<Integer>(ints);
        int j = 0;
        for(Integer i : transformingIterator) {
            assertEquals(j++, i.intValue());
        }
        assertEquals(ints.size(), j);
    }

    public void testSimpleTransform() {
        PredicatedTransformingIterator<String> transformingIterator =
            new PredicatedTransformingIterator<String>(ints);
        transformingIterator.setTransformer(StringValueTransformer.INSTANCE);
        int j = 0;
        for(String i : transformingIterator) {
            assertEquals(Integer.toString(j++), i);
        }
        assertEquals(ints.size(), j);
    }

    public void testFiltering() {
        PredicatedTransformingIterator<Integer> transformingIterator =
            new PredicatedTransformingIterator<Integer>(ints);
        transformingIterator.setPredicate(predicate);
        int j = 0;
        int k = 0;
        for(Integer i : transformingIterator) {
            assertEquals(j, i.intValue());
            j+=2;
            k++;
        }
        assertEquals(ints.size()/2, k);
    }

    public void testSetFlattening() {
        PredicatedTransformingIterator<Double> transformingIterator =
            new PredicatedTransformingIterator<Double>(ints);
        Transformer transformer = new Transformer() {

            public Object transform(Object input) {
                Integer i = (Integer)input;
                HashSet<Double> d = new HashSet<Double>();
                for(int k = 0; k < i; k++) {
                    d.add(Double.valueOf(i*10+k));
                }
                return d;
            }

        };
        transformingIterator.setTransformer(transformer);
        transformingIterator.setPredicate(predicate);
        HashSet<Double> expected = new HashSet<Double>();
        for(Integer i : ints) {
            if ( i % 2 == 0) {
                for(int k = 0; k < i; k++) {
                    expected.add(Double.valueOf(i*10+k));
                }
            }
        }
        HashSet<Double> actual = new HashSet<Double>();
        CollectionUtils.addAll(actual, transformingIterator);

        assertTrue(CollectionUtils.disjunction(actual, expected).isEmpty());
    }
}
