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
<<<<<<< HEAD
=======
    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public Class<?> getRealClass(Object possibleProxy) {
        return possibleProxy != null? possibleProxy.getClass(): null;
    }

}
