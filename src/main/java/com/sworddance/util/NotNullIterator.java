/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
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
