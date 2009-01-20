/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * enables a controlled access to an object tree that also supports caching.
 *
 * Sample use case:
 * <ul>
 * <li>a User object has a member object Role.</li>
 * <li>the Role object has its own properties</li>
 * <li>Both Role and User are stored in the database</li>
 * <li>Access to changing properties on either User or Role should be restricted on a dynamic basis</li>
 * </ul>
 *
 * <h3>Alternative rejected solutions</h3>
 * Hibernate caching rejected because:
 * <ul>
 * <li>operates on a per-entity basis not on an object tree basis</li>
 * <li>no mechanism to restrict read-only and write able properties on a per request basis.</li>
 * <li>no serialization mechanism.</l>
 * <li>no graceful way to work with flow code to preserve original state</li>
 * <li>no ability to cache only the parts of the entities needed for the request in question.
 * For example, if the request is allowing an admin to change another user's role, then the request should
 * have no ability through bug or hack attempt to access and change the user's password.</li>
 * </ul>
 *
 * Using apache bean utilities
 * <ul>
 * <li>does not seem to handle tree of objects</li>
 * <li> serialization issues</li>
 * </ul>
 *
 * @author patmoore
 * @param <I> the interface class that the
 * @param <O> extends <I> the class (not interface) that is the concrete class that is wrapped by the ProxyWrapper.
 *
 */
public class ProxyMapper<I,O extends I> extends BeanWorker implements InvocationHandler {
    private ConcurrentMap<String, Object> originalValues;
    private ConcurrentMap<String, Object> newValues;
    private transient O realObject;
    private Class<? extends Object> realClass;
    private ProxyBehavior proxyBehavior;
    private ConcurrentMap<String, Object> childProxies;
    private String basePropertyPath;
    private ProxyMapper parent;
    public ProxyMapper(O realObject, ProxyBehavior proxyBehavior, List<String>propertyChains) {
        super(propertyChains);
        this.realObject = realObject;
        this.realClass = this.realObject.getClass();
        this.proxyBehavior = proxyBehavior;
        initValuesMap(realObject, propertyChains);
    }
    public ProxyMapper(Class<O> realClass, ProxyBehavior proxyBehavior, List<String>propertyChains) {
        super(propertyChains);
        this.realClass = realClass;
        this.proxyBehavior = proxyBehavior;
    }
    public ProxyMapper(O realObject, List<String>propertyChains) {
        this(realObject, ProxyBehavior.strict, propertyChains);
    }
    public ProxyMapper(O realObject, ProxyBehavior proxyBehavior, String...propertyChains) {
        this(realObject, proxyBehavior, Arrays.asList(propertyChains));
    }
    public ProxyMapper(O realObject, String...propertyChains) {
        this(realObject, ProxyBehavior.strict, Arrays.asList(propertyChains));
    }
    /**
     * @param realObject
     * @param propertyChains
     */
    @SuppressWarnings("hiding")
    private void initValuesMap(O realObject, List<String> propertyChains) {
        originalValues = new ConcurrentHashMap<String, Object>();
        newValues = new ConcurrentHashMap<String, Object>();
        for(String property: propertyChains) {
            Object value = getValue(realObject, property);
            originalValues.put(property, value);
            newValues.put(property, value);
        }
    }

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    @SuppressWarnings("unused")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        String propertyName;
        if ( methodName.equals("toString")) {
            return this.getClass()+" "+this.newValues;
        } else if (methodName.equals("equals")) {
            Object key = getValue(args[0], this.getPropertyName(0));
            return this.newValues.get(this.getKeyExpression()).equals(key);
        } else if ( (propertyName = this.getGetterPropertyName(methodName)) != null) {
            if ( this.newValues.containsKey(propertyName)) {
                return this.newValues.get(propertyName);
            } else {
                switch(this.proxyBehavior) {
                case nullValue:
                    return null;
                case readThrough:
                    return method.invoke(getRealObject(), args);
                case leafStrict:
                    return newChildProxy(this, propertyName);
                case strict:
                    throw new IllegalStateException("no cached value with strict proxy behavior");
                }
            }
            return null;
        } else if ((propertyName = this.getSetterPropertyName(methodName)) != null) {
            this.newValues.put(propertyName, args[0]);
            return null;
        } else {
            switch(this.proxyBehavior) {
            case strict:
                throw new IllegalStateException("");
            default:
                return method.invoke(getRealObject(), args);
            }
        }
    }

    /**
     * @param proxyMapper
     * @param propertyName
     * @return
     */
    private Object newChildProxy(ProxyMapper<I, O> proxyMapper, String propertyName) {
        Object proxy = getExistingProxy(propertyName);
        if ( proxy == null ) {
            Class<?> propertyType = this.getPropertyType(realClass, propertyName);
            proxy = getProxy(propertyType, proxyBehavior, propertyName);
            setExistingProxy(propertyName, proxy);
        }
        return proxy;
    }
    /**
     * @param propertyName
     * @param proxy
     */
    private void setExistingProxy(String propertyName, Object proxy) {
        if ( this.parent != null ) {
            this.parent.setExistingProxy(getTruePropertyName(propertyName), proxy);
        } else {
            if (this.childProxies == null){
                this.childProxies = new ConcurrentHashMap<String, Object>();
            }
            this.childProxies.put(getTruePropertyName(propertyName), proxy);
        }
    }
    /**
     * @param propertyName
     * @return
     */
    private String getTruePropertyName(String propertyName) {
        if ( this.basePropertyPath == null) {
            return propertyName;
        } else {
            return this.basePropertyPath+"."+propertyName;
        }
    }
    private Object getExistingProxy(String propertyName) {
        if ( this.parent != null ) {
            return this.parent.getExistingProxy(getTruePropertyName(propertyName));
        } else if (this.childProxies != null){
            return this.childProxies.get(propertyName);
        } else {
            return null;
        }
    }
    /**
     * the real object may no longer be available. This method reloads the realObject if necessary.
     * @return the realObject
     */
    private O getRealObject() {
        if ( this.realObject == null) {
            // TODO
        }
        return this.realObject;
    }
    public Object getKeyExpression() {
        return this.newValues.get(this.getPropertyName(0));
    }

    public O getAppliedValues() {
        O result = getRealObject();
        for(Map.Entry<String, Object> entry : this.newValues.entrySet()) {
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public static <I,O extends I> I getProxy(O realObject, ProxyBehavior proxyBehavior, List<String>propertyChains) {
        Class realClass = realObject.getClass();
        return (I) getProxy(realObject, realClass, proxyBehavior, propertyChains);
    }
    public static <I,O extends I> I getProxy(Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        Class<?>[] interfaces = realClass.getInterfaces();
        InvocationHandler handler = new ProxyMapper<I, O>(realClass, proxyBehavior, propertyChains);
        return (I) Proxy.newProxyInstance(realClass.getClassLoader(), interfaces, handler);
    }
    /**
     * @param <O>
     * @param <I>
     * @param realObject
     * @param realClass
     * @param proxyBehavior
     * @param propertyChains
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <I,O extends I> I getProxy(O realObject, Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        Class<?>[] interfaces = realClass.getInterfaces();
        InvocationHandler handler = new ProxyMapper<I, O>(realObject, proxyBehavior, propertyChains);
        return (I) Proxy.newProxyInstance(realObject.getClass().getClassLoader(), interfaces, handler);
    }
    public static <I,O extends I> I getProxy(O realObject, List<String>propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, propertyChains);
    }
    public static <I,O extends I> I getProxy(O realObject, ProxyBehavior proxyBehavior, String...propertyChains) {
        return getProxy(realObject, proxyBehavior, Arrays.asList(propertyChains));
    }
    public static <I,O extends I> I getProxy(Class<O> realClass, ProxyBehavior proxyBehavior, String... propertyChains) {
        return getProxy(realClass, proxyBehavior, Arrays.asList(propertyChains));
    }
    public static <I,O extends I> I getProxy(O realObject, String...propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, Arrays.asList(propertyChains));
    }
}
