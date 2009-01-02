/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

/**
 * @author patmoore
 *
 */
public enum ProxyBehavior {
    /**
     * throw exception if the property is not cached
     */
    strict,
    /**
     * return null if the property is not cached and print warning
     */
    nullValue,
    /**
     * load the real object and read the value.
     */
    readThrough;
}
