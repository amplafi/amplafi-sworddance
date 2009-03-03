/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

import java.util.Arrays;
import java.util.List;

/**
 * @author patmoore
 *
 */
public class ProxyFactoryImpl implements ProxyFactory {

    public <I,O extends I> I getProxy(O realObject, String...propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(Class<O> realClass, ProxyBehavior proxyBehavior, String... propertyChains) {
        return getProxy(realClass, proxyBehavior, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(O realObject, ProxyBehavior proxyBehavior, String...propertyChains) {
        return getProxy(realObject, proxyBehavior, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(O realObject, List<String>propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, propertyChains);
    }

    @SuppressWarnings("unchecked")
    public <I,O extends I> I getProxy(O realObject, ProxyBehavior proxyBehavior, List<String>propertyChains) {
        Class realClass = realObject.getClass();
        return (I) getProxy(realObject, realClass, proxyBehavior, propertyChains);
    }
    public <I,O extends I> I getProxy(Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        ProxyMapper<I, O> handler = new RootProxyMapper<I, O>(realClass, proxyBehavior, propertyChains);
        return handler.getExternalFacingProxy();
    }
    /**
     * @param <O>
     * @param <I>
     * @param realObject
     * @param realClass
     * @param proxyBehavior
     * @param propertyChains
     * @return the proxy
     */
    public <I,O extends I> I getProxy(O realObject, Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        ProxyMapper<I, O> handler = new RootProxyMapper<I, O>(realObject, realClass, proxyBehavior, propertyChains);
        return handler.getExternalFacingProxy();
    }
}
