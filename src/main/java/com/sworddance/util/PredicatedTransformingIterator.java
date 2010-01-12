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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NOPTransformer;
import org.apache.commons.collections.functors.TruePredicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.commons.collections.iterators.TransformIterator;

/**
 * filter a iterator and then transform it. The transformation
 * can result in a iterable object which is returned one ata time.
 * resulting in a flattening of a transformation that results in a 1-n transformation.
 * @author Patrick Moore
 * @param <E>
 */
public class PredicatedTransformingIterator<E> implements IterableIterator<E> {
    private Iterator<?> iterator;
    private Predicate predicate;
    private Transformer transformer;
    private Iterator<E> nestedIterator;
    private TransformIterator baseIterator;

    public PredicatedTransformingIterator(Collection<?> baseCollection) {
        this(baseCollection.iterator());
    }
    public PredicatedTransformingIterator(Collection<?> baseCollection, Predicate predicate, Transformer transformer) {
        this(baseCollection.iterator(), predicate, transformer);
    }
    public PredicatedTransformingIterator(Iterator<?> iterator) {
        this.iterator = iterator;
    }
    public PredicatedTransformingIterator(Iterator<?> iterator, Predicate predicate, Transformer transformer) {
        this.iterator = iterator;
        this.predicate = predicate;
        this.transformer = transformer;
    }
    public PredicatedTransformingIterator(Collection<?> baseCollection, Transformer transformer) {
        this(baseCollection.iterator(), null, transformer);
    }
    public boolean hasNext() {
        initIfNeeded();
        findNextNestedIterator();
        return baseIterator.hasNext() || (nestedIterator != null && nestedIterator.hasNext());
    }

    private void initIfNeeded() {
        if ( baseIterator == null ) {
            FilterIterator filtering = new FilterIterator(iterator);
            filtering.setPredicate(predicate == null? TruePredicate.INSTANCE: predicate);
            baseIterator = new TransformIterator(filtering);
            baseIterator.setTransformer(transformer==null? NOPTransformer.INSTANCE:transformer);
        }
    }

    public E next() {
        initIfNeeded();
        findNextNestedIterator();
        return nestedIterator.next();
    }
    @SuppressWarnings("unchecked")
    private void findNextNestedIterator() {
        while((nestedIterator == null || !nestedIterator.hasNext())
                && baseIterator.hasNext()) {
            Object nextObj = baseIterator.next();
            if ( nextObj == null ) {
                // TODO : not strictly correct?
                continue;
            }
            if ( nextObj instanceof Iterable) {
                nestedIterator = ((Iterable<E>)nextObj).iterator();
            } else {
                nestedIterator = new SingletonIterator(nextObj);
            }
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Auto generated");
    }

    /**
     * @param iterator the iterator to set
     */
    public void setIterator(Iterator<?> iterator) {
        this.iterator = iterator;
    }

    /**
     * @return the iterator
     */
    public Iterator<?> getIterator() {
        return iterator;
    }

    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    /**
     * @return the predicate
     */
    public Predicate getPredicate() {
        return predicate;
    }

    /**
     * @param transformer the transformer to set
     */
    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * @return the transformer
     */
    public Transformer getTransformer() {
        return transformer;
    }

    public Iterator<E> iterator() {
        return this;
    }

}
