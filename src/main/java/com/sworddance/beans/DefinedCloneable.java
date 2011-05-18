/**
 * Copyright 2006-2011 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;


/**
 * A Cloneable that does not throw a java.lang.CloneNotSupportedException!
 *
 * Ideally <C> would be part of the interface definition.
 *
 * @author patmoore
 *
 */
public interface DefinedCloneable extends Cloneable {
    /**
     *
     * Implementation note: java.lang.Object#clone() should be called first by the implementor.
     * The fields that need to be cloned themselves should be cloned and replaced on the clone returned from java.lang.Object#clone()
     * @return the cloned object ( sadly can not use generic casting because of protected {@link Object#clone()} signature.
     */
    Object clone();
}
