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

/**
 * Compare to {@link Comparable}s that allows 1 comparable to be
 * @author patmoore
 *
 */
public class ComparableComparator extends AbstractComparator<Comparable<?>> {

    public static final ComparableComparator INSTANCE = new ComparableComparator();
    /**
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * @return if comparison cannot be done then 0 for equals is returned.
     */
    @Override
    public int compare(Comparable<?> o1, Comparable<?> o2) {
        Integer result = doCompare(o1, o2);
        return result == null?0:result;
    }

    public int compare(int o1, Number o2) {
        return compare(new Integer(o1), o2);
    }

    public int compare(Number o1, int o2) {
        return compare(o1, new Integer(o2));
    }
    public int compare(int o1, int o2) {
        return o1-o2;
    }

    public boolean less(int o1, Number o2) {
        Integer result = doCompare(new Integer(o1), o2);
        return result != null && result < 0;
    }

    public boolean less(Number o1, int o2) {
        Integer result = doCompare(o1, new Integer(o2));
        return result != null && result < 0;
    }
    public boolean less(int o1, int o2) {
        return compare(o1, o2) < 0;
    }
    public boolean less(Comparable<?> o1, Comparable<?>  o2) {
        Integer result = doCompare(o1, o2);
        return result != null && result < 0;
    }
    public boolean greater(int o1, Number o2) {
        Integer result = doCompare(new Integer(o1), o2);
        return result != null && result > 0;
    }

    public boolean greater(Number o1, int o2) {
        Integer result = doCompare(o1, new Integer(o2));
        return result != null && result > 0;
    }
    
    public boolean greater(int o1, int o2) {
        return compare(o1, o2) > 0;
    }
    public boolean greater(Comparable<?> o1, Comparable<?>  o2) {
        Integer result = doCompare(o1, o2);
        return result != null && result > 0;
    }
    public boolean equals(Comparable<?> o1, Comparable<?>  o2) {
        Integer result = doCompare(o1, o2);
        return result != null && result == 0;
    }
    public boolean equalsIgnoreCase(CharSequence o1, CharSequence o2) {
        Integer result = doCompare(o1, o2);
        return result != null && result == 0;
    }
}
