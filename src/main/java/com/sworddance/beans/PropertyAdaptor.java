// Copyright 2004, 2005 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.sworddance.beans;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.sworddance.util.ApplicationGeneralException;

import static org.apache.commons.lang.StringUtils.*;

/**
 * Used to manage dynamic access to a property of a specific class.
 *
 * @author Howard Lewis Ship
 * @author patmoore
 */
public class PropertyAdaptor {
    private String propertyName;

    private Method getter;

    private Method setter;

    public PropertyAdaptor(String propertyName) {
        this.propertyName = propertyName;
    }
    public PropertyAdaptor(String propertyName, Method readMethod, Method writeMethod) {
        this.propertyName = propertyName;
        this.getter = readMethod;
        this.setter = writeMethod;
    }

    /**
     * @return the name of the method used to read the property, or null if the property is not
     * readable.
     */
    public String getReadMethodName() {
        return getter == null ? null : getter.getName();
    }

    /**
     * @return the name of the method used to write the property, or null if the property is not
     * writable.
     */
    public String getWriteMethodName() {
        return setter == null ? null : setter.getName();
    }

    public String getPropertyName() {
        return propertyName;
    }


    /**
     * Updates the property of the target object.
     *
     * @param target the object to update
     * @param value the value to be stored into the target object property
     */
    public void write(Object target, Object value) {
        if (setter == null) {
            throw new ApplicationGeneralException("No set"+this.propertyName+"()");
        }

        try {
            setter.invoke(target, new Object[] { value });

        } catch (Exception ex) {
            throw new ApplicationGeneralException(ex);
        }
    }

    public void smartWrite(Object target, String value) {
        Object convertedValue = convertValueForAssignment(target, value);

        write(target, convertedValue);
    }

    /** @since 1.1 */
    private Object convertValueForAssignment(Object target, String value) {
        if (value == null || getReturnType().isInstance(value)) {
            return value;
        }

        PropertyEditor e = PropertyEditorManager.findEditor(getReturnType());

        if (e == null) {
            Object convertedValue = instantiateViaStringConstructor(value);

            if (convertedValue != null) {
                return convertedValue;
            }

            throw new ApplicationGeneralException("noPropertyEditor("+propertyName+"+)"+target.getClass());
        }

        try {
            e.setAsText(value);

            return e.getValue();
        } catch (Exception ex) {
            throw new ApplicationGeneralException("unableToConvert("+value+", "+getReturnType()+", "+propertyName+", "+target, ex);
        }
    }

    /**
     * Checks to see if this adaptor's property type has a public constructor that takes a single
     * String argument.
     */
    private Object instantiateViaStringConstructor(String value) {
        try {
            Constructor<?> c = getReturnType().getConstructor(new Class[] { String.class });

            return c.newInstance(new Object[] { value });
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @return true if there's a write method for the property.
     */
    public boolean isWritable() {
        return setter != null;
    }

    /**
     * Reads the property of the target object.
     *
     * @param target the object to read a property from
     * @return property's value
     */
    public Object read(Object target) {
        if (getter == null) {
            throw new ApplicationGeneralException("no get"+propertyName+"()");
        }

        try {
            return getter.invoke(target);

        } catch (Exception ex) {
            throw new ApplicationGeneralException(ex);
        }
    }

    /**
     * @return true if there's a read method for the property.
     */
    public boolean isReadable() {
        return getter != null;
    }

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
    public void setGetter(Class<?> clazz, Class<?>... parameterTypes) {
        setGetter(getMethod(clazz, parameterTypes));
    }
    /**
     * Get a the Getter method with the given parameter types (usually only a single parameter)
     * @param clazz
     * @param propertyName
     * @param parameterTypes
     * @return the getter method.
     */
    private Method getMethod(Class<?> clazz, Class<?>... parameterTypes) {
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
        throw new IllegalArgumentException(clazz+"."+propertyName+ " " + join(parameterTypes));
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
     * when constructing a PropertyMethodChain, we don't always want the setter to be available
     * because only the 'leaf' property should be writable.
     * @param clazz
     * @return true if setter was found.
     */
    public boolean initSetter(Class<?>clazz) {
        try {
            setSetter(clazz.getMethod("set"+capitalize(propertyName), this.getReturnType()));
            return true;
        } catch (SecurityException e) {
            // oh well..
            return false;
        } catch (NoSuchMethodException e) {
            // oh well..
            return false;
        }
    }
    /**
     * @return the setter
     */
    public Method getSetter() {
        return setter;
    }

    @Override
    public String toString() {
        return getter.getName();
    }
}
