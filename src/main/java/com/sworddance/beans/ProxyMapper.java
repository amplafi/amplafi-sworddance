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
 * This class takes an interface and a real object.
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
        }
        return proxy;
    }
    private Object getExistingProxy(String propertyName) {
        if ( this.parent != null ) {
            return this.parent.getExistingProxy(this.basePropertyPath+"."+propertyName);
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
        Class<?>[] interfaces = realObject.getClass().getInterfaces();
        InvocationHandler handler = new ProxyMapper<I, O>(realObject, proxyBehavior, propertyChains);
        return (I) Proxy.newProxyInstance(realObject.getClass().getClassLoader(), interfaces, handler);
    }
    public static <I,O extends I> I getProxy(O realObject, List<String>propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, propertyChains);
    }
    public static <I,O extends I> I getProxy(O realObject, ProxyBehavior proxyBehavior, String...propertyChains) {
        return getProxy(realObject, proxyBehavior, Arrays.asList(propertyChains));
    }
    public static <I,O extends I> I getProxy(O realObject, String...propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, Arrays.asList(propertyChains));
    }
}
