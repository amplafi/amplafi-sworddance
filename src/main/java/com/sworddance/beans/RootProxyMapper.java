/**
 * Copyright 2006-2008 by Amplafi. All rights reserved. Confidential.
 */
package com.sworddance.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author patmoore
 * @param <I>
 * @param <O>
 */
public class RootProxyMapper<I, O extends I> extends ProxyMapper<I, O> {
    private ProxyBehavior proxyBehavior;

    private ConcurrentMap<String, Object> originalValues;

    private ConcurrentMap<String, Object> newValues;

    private ConcurrentMap<String, ProxyMapper<?,?>> childProxies;
    /**
     * @param realObject
     * @param proxyBehavior
     * @param propertyChains first property is used to determine equality
     */
    @SuppressWarnings("unchecked")
    public RootProxyMapper(O realObject, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        this(realObject, (Class<O>) realObject.getClass(), proxyBehavior, propertyChains);
    }

    public RootProxyMapper(Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        this(null, realClass, proxyBehavior, propertyChains);
    }

    public RootProxyMapper(O realObject, List<String> propertyChains) {
        this(realObject, ProxyBehavior.strict, propertyChains);
    }

    public RootProxyMapper(O realObject, ProxyBehavior proxyBehavior, String... propertyChains) {
        this(realObject, proxyBehavior, Arrays.asList(propertyChains));
    }

    public RootProxyMapper(O realObject, String... propertyChains) {
        this(realObject, ProxyBehavior.strict, Arrays.asList(propertyChains));
    }

    public RootProxyMapper(O realObject, Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        super(null, realObject, realClass, propertyChains);
        this.setProxyBehavior(proxyBehavior);
        initValuesMap(realObject, propertyChains);
    }

    public void initValuesMap(O base, List<String> propertyChains) {
        originalValues = new ConcurrentHashMap<String, Object>();
        newValues = new ConcurrentHashMap<String, Object>();
        if (base != null) {
            for (String property : propertyChains) {
                initValue(base, property);
            }
        }
    }
    @Override
    public boolean containsKey(String propertyName) {
        return this.getNewValues().containsKey(propertyName) || this.originalValues.containsKey(propertyName);
    }
    /**
     * @param property
     * @param result
     */
    @Override
    protected void putOriginalValues(String property, Object result) {
        this.originalValues.put(property, result);
    }
    @Override
    protected void putNewValues(String property, Object result) {
        this.getNewValues().put(property, result);
    }
    @Override
    public Object getCachedValues(String propertyName) {
        if (this.getNewValues().containsKey(propertyName)) {
            return this.getNewValues().get(propertyName);
        } else {
            return this.originalValues.get(propertyName);
        }
    }
    /**
     * Child proxies are used when 'this' has been asked for a property that is partial path to leaf properties.
     * <p>For example, a ProxyMapper is managing properties:
     * <ul><li>foo.bar</li>
     * <li>foo.goo</li>
     * <li>bee</li>
     * </ul>
     * The ProxyMapper is asked for the "foo" property. The ProxyMapper will return a child ProxyMapper "foo" that has properties:
     * <ul><li>bar (mapped to parent "foo.bar")</li>
     * <li>goo (mapped to parent "foo.goo")</li>
     * </ul>
     * This allows the ProxyMapper usage to be less visible to called utility code.</p>
     * @param propertyName
     * @return existing child proxy
     */
    public ProxyMapper<?,?> getExistingChildProxy(String propertyName) {
        if (this.childProxies != null){
            return this.childProxies.get(propertyName);
        } else {
            return null;
        }
    }
    /**
     * @param propertyName
     * @param proxy
     */
    private void setChildProxy(String propertyName, ProxyMapper<?,?> proxy) {
        if (this.childProxies == null){
            this.childProxies = new ConcurrentHashMap<String, ProxyMapper<?,?>>();
        }
        this.childProxies.putIfAbsent(propertyName, proxy);
    }
    @Override
    public ProxyMapper<?, ?> getChildProxyMapper(String propertyName) {
        return this.getChildProxyMapper(this, propertyName);
    }
    protected ProxyMapper<?,?> getChildProxyMapper(ProxyMapper<?, ?> base, String propertyName) {
        ProxyMapper<?,?> childProxy = getExistingChildProxy(propertyName);
        if ( childProxy == null ) {
            Class<?> propertyType = this.getPropertyType(base.getRealClass(), propertyName);
            childProxy = new ChildProxyMapper(propertyName, this, null, propertyType, new ArrayList<String>());
            setChildProxy(propertyName, childProxy);
            // multi-thread environment may mean that the object this thread created was not the
            // one actually inserted. (see use of ConcurrentMap#putIfAbsent() )
            childProxy = getExistingChildProxy(propertyName);
        }
        return childProxy;
    }

    /**
     * @param proxyBehavior the proxyBehavior to set
     */
    public void setProxyBehavior(ProxyBehavior proxyBehavior) {
        this.proxyBehavior = proxyBehavior;
    }

    /**
     * @return the proxyBehavior
     */
    @Override
    public ProxyBehavior getProxyBehavior() {
        return proxyBehavior;
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#getNewValues()
     */
    @Override
    public Map<String, Object> getNewValues() {
        return this.newValues;
    }
    @Override
    public Map<String, Object> getOriginalValues() {
        return this.newValues;
    }
}
