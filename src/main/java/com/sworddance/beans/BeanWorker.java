/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package com.sworddance.beans;

import java.lang.reflect.InvocationTargetException;
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
import static org.apache.commons.lang.StringUtils.*;

/**
 * Provides some general utility methods so that bean operations can be used more easily.
 * @author patmoore
 *
 */
public class BeanWorker {

    private static final Pattern PROPERTY_METHOD_PATTERN = Pattern.compile("(is|set|get)(([A-Z])(\\w*))$");
    private static final Pattern GET_METHOD_PATTERN = Pattern.compile("(is|get)(([A-Z])(\\w*))$");
    private static final Pattern SET_METHOD_PATTERN = Pattern.compile("(set)(([A-Z])(\\w*))$");
    private List<String> propertyNames;
    // key = class, key = propertyNames value = chain of methods to get to value.
    private static final MapByClass<Map<String,List<PropertyMethods>>> methodsMap = new MapByClass<Map<String,List<PropertyMethods>>>();
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
            List<PropertyMethods> methodChain = getMethods(base.getClass(), property);
            if ( methodChain != null ) {
                for(PropertyMethods propertyMethods : methodChain) {
                    Method method = propertyMethods.getter;
                    try {
                        result = method.invoke(result);
                        if ( result == null ) {
                            break;
                        }
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(method.toGenericString(), e);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(method.toGenericString(), e);
                    } catch (InvocationTargetException e) {
                        throw new IllegalArgumentException(method.toGenericString(), e);
                    }
                }
            }
        }
        return (T) result;
    }
    protected List<PropertyMethods> getMethods(Class<?> clazz, String property) {
        Map<String, List<PropertyMethods>> classMethodMap = getMethodMap(clazz);
        List<PropertyMethods> methodChain = classMethodMap.get(property);
        return methodChain;
    }
    protected Map<String, List<PropertyMethods>> getMethodMap(Class<?> clazz) {
        Map<String, List<PropertyMethods>> propMap;
        if ( !methodsMap.containsKey(clazz)) {
            propMap = new ConcurrentHashMap<String, List<PropertyMethods>>();

            for(String property: propertyNames) {
                String[] splitProps = property.split("\\.");
                propMap.put(property, getMethods(clazz, splitProps));
            }
        } else {
            propMap = methodsMap.get(clazz);
        }
        return propMap;
    }
    /**
     * collects a chain of property methods that are called sequentially to get the final result.
     * @param clazz
     * @param propertyNamesList
     * @return the chain of methods.
     */
    protected List<PropertyMethods> getMethods(Class<?> clazz, String[] propertyNamesList) {
        Class<?>[] parameterTypes = new Class<?>[0];
        List<PropertyMethods> methodArray = new ArrayList<PropertyMethods>();
        for(String propertyName: propertyNamesList) {
            PropertyMethods propertyMethods = new PropertyMethods();
            propertyMethods.getter = getMethod(clazz, propertyName, parameterTypes);
            clazz= propertyMethods.getter.getReturnType();
            methodArray.add(propertyMethods);
        }
        return methodArray;
    }
    /**
     * Get a the Getter method with the given parameter types (usually only a single parameter)
     * @param clazz
     * @param propertyName
     * @param parameterTypes
     * @return the getter method.
     */
    private Method getMethod(Class<?> clazz, String propertyName, Class<?>... parameterTypes) {
        if (propertyName == null ) {
            throw new IllegalArgumentException("propertyName cannot be null");
        }
        String capitalize = capitalize(propertyName);
        for (String methodName: Arrays.asList(propertyName, "get"+capitalize, "is"+capitalize)) {
            try {
                return clazz.getMethod(methodName, parameterTypes);
            } catch (SecurityException e) {
//                throw new IllegalArgumentException(clazz+"."+propertyName+ " " + StringUtils.join(parameterTypes), e);
            } catch (NoSuchMethodException e) {
//                throw new IllegalArgumentException(clazz+"."+propertyName+ " " + StringUtils.join(parameterTypes), e);
            }
        }
        throw new IllegalArgumentException(clazz+"."+propertyName+ " " + join(parameterTypes));
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
    protected static class PropertyMethods {
        protected Method getter;
        protected Method setter;
    }
}
