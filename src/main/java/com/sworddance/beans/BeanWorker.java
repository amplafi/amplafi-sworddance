/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.collections.CollectionUtils.*;

/**
 * Provides some general utility methods so that bean operations can be used more easily.
 *
 * Note that an instance of a BeanWorker is not linked to a class. This allows "duck-typing" operations.
 * @author patmoore
 *
 */
public class BeanWorker {

    private static final Pattern PROPERTY_METHOD_PATTERN = Pattern.compile("(is|set|get)(([A-Z])(\\w*))$");
    private static final Pattern GET_METHOD_PATTERN = Pattern.compile("(is|get)(([A-Z])(\\w*))$");
    private static final Pattern SET_METHOD_PATTERN = Pattern.compile("(set)(([A-Z])(\\w*))$");

    /**
     * This list of property names is the list of the only properties that the BeanWorker is allowed to modify.
     * Specifically, "foo.goo" does not mean the BeanWorker is allowed to modify the "foo" property - only "foo"'s "goo" property can be modified.
     */
    private List<String> propertyNames;
    // key = class, key = (each element in) propertyNames value = chain of methods to get to value.
    private static final MapByClass<Map<String,PropertyMethodChain>> methodsMap = new MapByClass<Map<String,PropertyMethodChain>>();
    public BeanWorker() {

    }
    public BeanWorker(String... propertyNames) {
        this.propertyNames = new ArrayList<String>( Arrays.asList(propertyNames));
    }
    /**
     * @param propertyNames
     */
    public BeanWorker(Collection<String> propertyNames) {
        this.propertyNames = new ArrayList<String>( propertyNames);
    }
    /**
     * @param propertyNames the propertyNames to set
     */
    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }
    /**
     * @return the propertyNames
     */
    public List<String> getPropertyNames() {
        return propertyNames;
    }
    public String getPropertyName(int index) {
        return isNotEmpty(this.propertyNames) && index < this.propertyNames.size()?this.propertyNames.get(index) : null;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getValue(Object base, String property) {
        Object result = base;
        if ( base != null && property != null ) {
            PropertyMethodChain methodChain = getPropertyMethodChain(base.getClass(), property);
            if ( methodChain != null ) {
                result = methodChain.getValue(result);
            }
        }
        return (T) result;
    }

    protected void setValue(Object base, String property, Object value) {
        Object result = base;
        if ( base != null && property != null ) {
            PropertyMethodChain methodChain = getPropertyMethodChain(base.getClass(), property);
            if ( methodChain != null ) {
                result = methodChain.setValue(result, value);
            }
        }
    }
    protected PropertyMethodChain getPropertyMethodChain(Class<?> clazz, String property) {
        Map<String, PropertyMethodChain> classMethodMap = getMethodMap(clazz);
        PropertyMethodChain methodChain = classMethodMap.get(property);
        return methodChain;
    }

    public Class<?> getPropertyType(Class<?> clazz, String property) {
        PropertyMethodChain chain = getPropertyMethodChain(clazz, property);
        if ( chain == null) {
            chain = new PropertyMethodChain(clazz, property, true);
            // TODO should put in the methodChain
        }
        return chain.getReturnType();
    }
    /**
     * Each
     * @param clazz
     * @return
     */
    protected Map<String, PropertyMethodChain> getMethodMap(Class<?> clazz) {
        Map<String, PropertyMethodChain> propMap;
        if ( !methodsMap.containsKey(clazz)) {
            propMap = new ConcurrentHashMap<String, PropertyMethodChain>();
            for(String property: propertyNames) {
                propMap.put(property, new PropertyMethodChain(clazz, property, false));
            }
            methodsMap.putIfAbsent(clazz, propMap);
        }
        propMap = methodsMap.get(clazz);
        return propMap;
    }

    protected String getPropertyName(Method method) {
        String methodName = method.getName();
        return this.getPropertyName(methodName);
    }
    protected String getPropertyName(String methodName) {
        Matcher matcher = PROPERTY_METHOD_PATTERN.matcher(methodName);
        String propertyName;
        if (matcher.find()) {
            propertyName = matcher.group(3).toLowerCase()+matcher.group(4);
        } else {
            propertyName = null;
        }
        return propertyName;
    }
    protected String getGetterPropertyName(String methodName) {
        Matcher matcher = GET_METHOD_PATTERN.matcher(methodName);
        String propertyName;
        if (matcher.find()) {
            propertyName = matcher.group(3).toLowerCase()+matcher.group(4);
        } else {
            propertyName = null;
        }
        return propertyName;
    }
    protected String getSetterPropertyName(String methodName) {
        Matcher matcher = SET_METHOD_PATTERN.matcher(methodName);
        String propertyName;
        if (matcher.find()) {
            propertyName = matcher.group(3).toLowerCase()+matcher.group(4);
        } else {
            propertyName = null;
        }
        return propertyName;
    }
}
