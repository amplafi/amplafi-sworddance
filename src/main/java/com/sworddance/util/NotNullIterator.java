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

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.Map;

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

    /**
     *
     */
    public NotNullIterator() {
        super();
    }

    /**
     * Handles {@link java.lang.Iterable}<{@link java.lang.ref.Reference}<T>> case
     * @param object
     */
    public NotNullIterator(Object object) {
        setIterator(object);
    }
    /**
     * @param iter
     */
    public NotNullIterator(Iterable<T> iter) {
        super(iter);
    }
    /**
     * @param iter
     */
    public NotNullIterator(Iterator<T> iter) {
        super(iter);
    }
    /**
     * @param map
     */
    public NotNullIterator(Map<?, ?> map) {
        super(map);
    }
    /**
     * @param <K>
     * @param ref
     */
    public <K> NotNullIterator(Reference<K> ref) {
        super(ref);
    }
    /**
     * @param first
     * @param objects
     */
    public NotNullIterator(Object first, T... objects) {
        super((T)first, objects);
    }
    @Override
    protected void setIterator(Object iterator) {
        Iterator<?> iter = extractIterator(iterator);
        TransformIterator transformIterator = new TransformIterator(iter, ReferenceTransformer.INSTANCE);
        super.setIterator(new FilterIterator(transformIterator, NotNullKeyValuePredicate.INSTANCE));
    }
}
