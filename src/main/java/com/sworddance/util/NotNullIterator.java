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

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.TransformIterator;

/**
 * This iterator should be used to filter out null values in a collection or array.
 * This saves the element != null check.
 *
 * Use example:
 *
 * for(ElementType element : new NotNullIterator<ElementType>(elements))
 * @author patmoore
 * @param <T>
 *
 */
public class NotNullIterator<T> extends BaseIterableIterator<T> {
    public NotNullIterator(Object object) {
        setIterator(object);
    }
    public NotNullIterator(T... objects) {
        setIterator(Arrays.asList(objects).iterator());
    }
    @Override
    protected void setIterator(Object iterator) {
        Iterator<?> iter = extractIterator(iterator);
        TransformIterator transformIterator = new TransformIterator(iter, ReferenceTransformer.INSTANCE);
        super.setIterator(new FilterIterator(transformIterator, NotNullKeyValuePredicate.INSTANCE));
    }
}
