/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

/**
 * Used by ProxyMapper to retrieve realObect when it doesn't have the real backing object
 * already.
 *
 * @author patmoore
 *
 */
public interface ProxyLoader {
    public <I, O extends I> I get(ProxyMapper<I,O> proxyMapper);
}
