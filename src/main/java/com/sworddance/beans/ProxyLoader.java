/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

/**
 * @author patmoore
 *
 */
public interface ProxyLoader {
    public <T> T get(Class<T> clazz, Object id);
}
