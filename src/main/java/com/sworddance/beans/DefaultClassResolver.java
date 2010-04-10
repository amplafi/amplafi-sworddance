/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;


/**
 * @author patmoore
 *
 */
public class DefaultClassResolver implements ClassResolver {

    public static final ClassResolver INSTANCE = new DefaultClassResolver();

    /**
     * @see com.sworddance.beans.ClassResolver#getRealClass(java.lang.Object)
     */
    @Override
    public Class<?> getRealClass(Object possibleProxy) {
        return possibleProxy != null? possibleProxy.getClass(): null;
    }

}
