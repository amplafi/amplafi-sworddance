/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.util;

import java.lang.ref.Reference;

import org.apache.commons.collections.Transformer;

/**
 * unpeels all the wrapping {@link Reference}.
 * @author patmoore
 *
 */
public class ReferenceTransformer implements Transformer {
    public static final ReferenceTransformer INSTANCE = new ReferenceTransformer();
    /**
     * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
     */
    @Override
    public Object transform(Object input) {
        Object v = input;
        while(v instanceof Reference) {
            v= ((Reference<?>)v).get();
        }
        return v;
    }

}
