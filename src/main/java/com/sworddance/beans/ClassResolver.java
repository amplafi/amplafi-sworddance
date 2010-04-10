/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

/**
 * Handles proxy issues where the "real" class is not the class as reported by {@link Object#getClass()}.
 *
 * This happens most notably with hibernate.
 * @author patmoore
 *
 */
public interface ClassResolver {
    public <I> Class<? extends I> getRealClass(I possibleProxy);
}
