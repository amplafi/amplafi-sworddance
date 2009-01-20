package com.sworddance.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.StringUtils.*;

/**
 * a list of methods that called in sequence will result in either setting or getting a value.
 * @author patmoore
 *
 */
public class PropertyMethodChain {
    /**
    *
    */
    static final String PROPERTY_SEP = ".";
    private List<PropertyMethods> propertyMethodList;

    /**
     * @param clazz
     * @param property
     * @param readOnly readonly property
     */
    public PropertyMethodChain(Class<?> clazz, String property, boolean readOnly) {
        String[] splitProps = property.split("\\" + PROPERTY_SEP);
        this.propertyMethodList = getMethods(clazz, splitProps, readOnly);
    }

    /**
     * @param propertyMethods
     */
    public void add(PropertyMethods propertyMethods) {
        propertyMethodList.add(propertyMethods);
    }

    /**
     * @param base
     * @return the property's value at the end of the PropertyMethodChain
     */
    public Object getValue(Object base) {
        Object result = invoke(base, null, false);
        return result;
    }
    public PropertyMethods get(int index) {
        return this.propertyMethodList.get(index);
    }

    /**
     * set the property's value at the end of the PropertyMethodChain so long as no null values
     * are encountered when trying to get to the property.
     * @param base
     * @param value
     * @return the value (if any) returned by the set.
     */
    public Object setValue(Object base, Object value) {
        Object result = invoke(base, value, true);
        return result;
    }

    @SuppressWarnings("null")
    public Object invoke(Object base, Object value, boolean set) {
        Object result = base;
        for(int i = 0; result != null && i < propertyMethodList.size(); i++) {
            PropertyMethodChain.PropertyMethods propertyMethods = propertyMethodList.get(i);
            Method method = null;
            try {
                if (!set || i < propertyMethodList.size()-1) {
                    method = propertyMethods.getGetter();
                    result = method.invoke(result);
                } else {
                    method = propertyMethods.getSetter();
                    result = method.invoke(result, value);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(method.toGenericString(), e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(method.toGenericString(), e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(method.toGenericString(), e);
            }
        }
        return result;
    }

    public int size() {
        return this.propertyMethodList.size();
    }
    /**
     * collects a chain of property methods that are called sequentially to get the final result.
     * @param clazz
     * @param propertyNamesList
     * @return the chain of methods.
     */
    protected List<PropertyMethods> getMethods(Class<?> clazz, String[] propertyNamesList, boolean readOnly) {
        Class<?>[] parameterTypes = new Class<?>[0];
        List<PropertyMethods> propertyMethodChain = new ArrayList<PropertyMethods>();
        for(Iterator<String> iter = Arrays.asList(propertyNamesList).iterator(); iter.hasNext();) {
            String propertyName = iter.next();
            PropertyMethods propertyMethods = new PropertyMethods();
            propertyMethods.setGetter(getMethod(clazz, propertyName, parameterTypes));
            if ( !iter.hasNext() && !readOnly) {
                // only get the setter on the last iteration because PropertyMethodChain is only allowed to set the property at the
                // end of the chain. No other property along the way can be set.
                try {
                    propertyMethods.setSetter(clazz.getMethod("set"+capitalize(propertyName), propertyMethods.getReturnType()));
                } catch (SecurityException e) {
                    // oh well..
                } catch (NoSuchMethodException e) {
                    // oh well..
                }
            }
            clazz = propertyMethods.getReturnType();
            propertyMethodChain.add(propertyMethods);
        }
        return propertyMethodChain;
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
//                    throw new IllegalArgumentException(clazz+"."+propertyName+ " " + StringUtils.join(parameterTypes), e);
            } catch (NoSuchMethodException e) {
//                    throw new IllegalArgumentException(clazz+"."+propertyName+ " " + StringUtils.join(parameterTypes), e);
            }
        }
        throw new IllegalArgumentException(clazz+PROPERTY_SEP+propertyName+ " " + join(parameterTypes));
    }
    /**
     * @return last type returned
     */
    public Class<?> getReturnType() {
        return get(this.size()-1).getReturnType();
    }
    protected static class PropertyMethods {
        private Method getter;
        private Method setter;
        /**
         * @return the returnType
         */
        public Class<?> getReturnType() {
            return getGetter().getReturnType();
        }
        /**
         * @param getter the getter to set
         */
        public void setGetter(Method getter) {
            this.getter = getter;
        }
        /**
         * @return the getter
         */
        public Method getGetter() {
            return getter;
        }
        /**
         * @param setter the setter to set
         */
        public void setSetter(Method setter) {
            this.setter = setter;
        }
        /**
         * @return the setter
         */
        public Method getSetter() {
            return setter;
        }
    }

}