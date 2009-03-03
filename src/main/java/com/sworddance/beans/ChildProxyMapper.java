/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

import java.util.List;
import java.util.Map;

/**
 * @author patmoore
 * @param <I>
 * @param <O>
 *
 */
public class ChildProxyMapper<I,O extends I> extends ProxyMapper<I,O> {

    private RootProxyMapper<?,?> rootProxyMapper;
    public ChildProxyMapper(String basePropertyPath, RootProxyMapper<?,?> rootProxyMapper, O realObject, Class<O> realClass, List<String> propertyChains) {
        super(basePropertyPath, realObject, realClass, propertyChains);
        this.rootProxyMapper = rootProxyMapper;
    }

    /**
     * @param rootProxyMapper the rootProxyMapper to set
     */
    protected void setRootProxyMapper(RootProxyMapper<?,?> rootProxyMapper) {
        this.rootProxyMapper = rootProxyMapper;
    }

    /**
     * @return the rootProxyMapper
     */
    protected RootProxyMapper<?,?> getRootProxyMapper() {
        return rootProxyMapper;
    }
    /**
     * @param property
     * @param result
     */
    @Override
    protected void putOriginalValues(String propertyName, Object result) {
        this.getRootProxyMapper().putOriginalValues(getTruePropertyName(propertyName), result);
    }
    @Override
    protected void putNewValues(String propertyName, Object result) {
        this.getRootProxyMapper().putNewValues(getTruePropertyName(propertyName), result);
    }
    @Override
    public Object getCachedValue(String propertyName) {
        return this.getRootProxyMapper().getCachedValue(getTruePropertyName(propertyName));
    }
    @Override
    public boolean containsKey(String propertyName) {
        return this.getRootProxyMapper().containsKey(getTruePropertyName(propertyName));
    }

    @Override
    public Map<String, Object> getNewValues() {
        return this.getRootProxyMapper().getNewValues();
    }
    @Override
    public Map<String, Object> getOriginalValues() {
        return this.getRootProxyMapper().getOriginalValues();
    }
    @Override
    public ProxyBehavior getProxyBehavior() {
        return this.getRootProxyMapper().getProxyBehavior();
    }

    @Override
    protected <CI, CO extends CI> ProxyMapper<CI, CO> getChildProxyMapper(String propertyName, PropertyAdaptor propertyAdaptor, Object base) {
        return this.getRootProxyMapper().getChildProxyMapper(this, getTruePropertyName(propertyName), propertyAdaptor, base);
    }
    /**
     * @return the proxyLoader
     */
    @Override
    public ProxyLoader getProxyLoader() {
        if ( super.getProxyLoader() == null && this.getRootProxyMapper() != null) {
            return this.getRootProxyMapper().getProxyLoader();
        } else {
            return super.getProxyLoader();
        }
    }

}
