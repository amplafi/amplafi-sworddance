package com.sworddance.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
    private List<PropertyAdaptor> propertyMethodList;

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
    public void add(PropertyAdaptor propertyMethods) {
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
    public PropertyAdaptor get(int index) {
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
            PropertyAdaptor propertyMethods = propertyMethodList.get(i);
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
    protected List<PropertyAdaptor> getMethods(Class<?> clazz, String[] propertyNamesList, boolean readOnly) {
        Class<?>[] parameterTypes = new Class<?>[0];
        List<PropertyAdaptor> propertyMethodChain = new ArrayList<PropertyAdaptor>();
        for(Iterator<String> iter = Arrays.asList(propertyNamesList).iterator(); iter.hasNext();) {
            String propertyName = iter.next();
            PropertyAdaptor propertyMethods = new PropertyAdaptor(propertyName);
            propertyMethods.setGetter(clazz, parameterTypes);
            if ( !iter.hasNext() && !readOnly) {
                // only get the setter on the last iteration because PropertyMethodChain is only allowed to set the property at the
                // end of the chain. No other property along the way can be set.
                propertyMethods.initSetter(clazz);
            }
            clazz = propertyMethods.getReturnType();
            propertyMethodChain.add(propertyMethods);
        }
        return propertyMethodChain;
    }

    /**
     * @return last type returned
     */
    public Class<?> getReturnType() {
        return get(this.size()-1).getReturnType();
    }

    @Override
    public String toString() {
        return this.propertyMethodList.toString();
    }

}