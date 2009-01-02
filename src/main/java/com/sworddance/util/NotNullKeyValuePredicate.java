/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.util;

import java.lang.ref.Reference;
import java.util.Map;
import org.apache.commons.collections.Predicate;

/**
 * Make sure the evaluated object is not null.
 * If the object is a {@link Reference} the object is dereferenced  and if the object is a Map.Entry then
 * make sure that the key and the value held by the entry are also not null.
 * @author patmoore
 *
 */
public class NotNullKeyValuePredicate implements Predicate {
    public static final NotNullKeyValuePredicate INSTANCE = new NotNullKeyValuePredicate();
    /**
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    @Override
    public boolean evaluate(Object object) {
        Object v = object;
        while ( v instanceof Reference) {
            v = ((Reference<?>)v).get();
        }
        if ( v == null) {
            return false;
        }
        if ( v instanceof Map.Entry) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) v;
            if ( entry.getKey() == null || entry.getValue() == null) {
                return false;
            }
        }
        return true;
    }

}
