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

package com.sworddance.core;

import java.util.Comparator;

/**
 * Implementers can be displayed as a list.
 * @author Patrick Moore
 */
public interface Listable {
    /**
     *
     */
    public static final String LIST_DISPLAY_VALUE = "listDisplayValue";
    public static final ListableComparator COMPARATOR = new ListableComparator();
    /**
     * @return the string that is suitable when implementer is displayed in a list of similar items.
     */
    public String getListDisplayValue();
    /**
     * Use in tapestry-For's keyExpression attribute.
     * @return a key for tapestry for-expressions.
     */
    public Object getKeyExpression();

    public boolean hasKey(Object key);

<<<<<<< HEAD
    public static class ListableComparator implements Comparator<Listable> {
=======
    public static class ListableComparator implements Comparator<Listable> {
        @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        public int compare(Listable o1, Listable o2) {
            return o1.getListDisplayValue().compareTo(o2.getListDisplayValue());
        }

    }
}
