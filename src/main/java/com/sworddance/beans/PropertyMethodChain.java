/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.sworddance.beans;

import java.util.ArrayList;
import java.util.Collections;
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

    private final List<PropertyAdaptor> propertyMethodList;
    private final Class<?> clazz;
    private final boolean readOnly;
    private final String property;

    public PropertyMethodChain(Class<?> clazz, String property, boolean readOnly, List<PropertyAdaptor> propertyMethodList) {
        this.propertyMethodList = Collections.unmodifiableList(new ArrayList<PropertyAdaptor>(propertyMethodList));
        this.property = property;
        this.readOnly = readOnly;
        this.clazz = clazz;
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

    /**
     * @return the clazz
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * @return the readOnly
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @return the property
     */
    public String getProperty() {
        return property;
    }


}