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

<<<<<<< HEAD
    public static NotNullIterator<?>EMPTY = new NotNullIterator<Object>();
=======
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    /**
     *
     */
    public NotNullIterator() {
        super();
    }

    /**
<<<<<<< HEAD
     * @param iter
     */
    public NotNullIterator(Iterable<?> iter) {
=======
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
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        super(iter);
    }
    /**
     * @param iter
     */
<<<<<<< HEAD
    public NotNullIterator(Iterator<?> iter) {
=======
    public NotNullIterator(Iterator<T> iter) {
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
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
<<<<<<< HEAD

    /**
     * Generic factory constructor returns constant if nothing to iterate over.
     * @param <T>
     * @param iterable
     * @return a {@link NotNullIterator}
     */
    public static <T> NotNullIterator<T> newNotNullIterator(Object iterable) {
        return newNotNullIterator(extractIterator(iterable));
    }
    @SuppressWarnings("unchecked")
    public static <T> NotNullIterator<T> newNotNullIterator(Iterator<?> iterator) {
        if ( iterator == null || !iterator.hasNext()) {
            return (NotNullIterator<T>) EMPTY;
        } else {
            return new NotNullIterator<T>(iterator);
        }

    }
=======
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
}
