package com.sworddance.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.sworddance.util.BaseIterableIterator;
import com.sworddance.util.CurrentIterator;

/**
 * a list of methods that called in sequence will result in either setting or getting a value.
 * @author patmoore
 *
 */
public class PropertyMethodChain implements Iterable<PropertyAdaptor>{
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

    public Object getValue(Object base, Iterator<PropertyAdaptor> iterator) {
        Object result = invoke(base, null, false, iterator);
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
    public Object setValue(Object base, Object value, Iterator<PropertyAdaptor> iterator) {
        Object result = invoke(base, value, true, iterator);
        return result;
    }
    public Object invoke(Object base, Object value, boolean set) {
        BaseIterableIterator<PropertyAdaptor> iterator = this.iterator();
        return invoke(base, value, set, iterator);
    }
    public Object invoke(Object base, Object value, boolean set, Iterator<PropertyAdaptor> iterator) {
        Object result;
        if ( iterator instanceof CurrentIterator && ((CurrentIterator<PropertyAdaptor>) iterator).current() != null) {
            result = invoke(base, value, set, iterator, ((CurrentIterator<PropertyAdaptor>) iterator).current());
        } else {
            result = base;
        }
        for(; result != null && iterator.hasNext();) {
            PropertyAdaptor propertyMethods =iterator.next();
            result = invoke(result, value, set, iterator, propertyMethods);
        }
        return result;
    }

    /**
     * @param result
     * @param value
     * @param set
     * @param iterator
     * @param propertyAdaptor
     * @return result of invocation
     */
    private Object invoke(Object target, Object value, boolean set, Iterator<PropertyAdaptor> iterator, PropertyAdaptor propertyAdaptor) {
        if (!set || iterator.hasNext()) {
            return target == null? null: propertyAdaptor.read(target);
        } else {
            return propertyAdaptor.write(target, value);
        }
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

    /**
     * @return iterator
     * @see java.util.List#iterator()
     */
    public BaseIterableIterator<PropertyAdaptor> iterator() {
        return new BaseIterableIterator<PropertyAdaptor>(propertyMethodList.iterator());
    }


}