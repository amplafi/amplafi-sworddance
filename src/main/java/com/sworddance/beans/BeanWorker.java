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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sworddance.util.ApplicationIllegalArgumentException;

import static com.sworddance.util.CUtilities.*;

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
    private List<String> propertyNames = new ArrayList<String>();
    // key = class, key = (each element in) propertyNames value = chain of methods to get to value.
    // TODO in future cache into a second map.
    private final MapByClass<ConcurrentMap<String,PropertyMethodChain>> methodsMap = new MapByClass<ConcurrentMap<String,PropertyMethodChain>>();
    public BeanWorker() {

    }
    public BeanWorker(String... propertyNames) {
        this( Arrays.asList(propertyNames));
    }
    /**
     * @param propertyNames
     */
    public BeanWorker(Collection<String> propertyNames) {
        if (isNotEmpty(propertyNames)) {
            this.propertyNames.addAll(propertyNames);
        }
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

    /**
     * Follows the propertyPath starting at base until null or until the end.
     * @param <T>
     * @param base
     * @param property
     * @return null or the property
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Object base, String property) {
        T result = null;
        if ( base != null && property != null ) {
            PropertyMethodChain methodChain = getPropertyMethodChain(base.getClass(), property);
            if ( methodChain != null ) {
                result = (T) methodChain.getValue(base);
            }
        }
        return result;
    }

    public void setValue(Object base, String property, Object value) {
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
    /**
     * For example, "grandparent.parent.child" will return a Method
     * chain of length 3 ( "getGrandparent().getParent().getChild()" )
     *
     * @param clazz
     * @param property "grandparent.parent.child"
     * @param readOnly
     * @return a chain of {@link Method}s that when sequentially called will return a result.
     */
    protected PropertyMethodChain getPropertyMethodChainAddIfAbsent(Class<?> clazz, String property, boolean readOnly) {
        ConcurrentMap<String, PropertyMethodChain> classMethodMap = getMethodMap(clazz);
        PropertyMethodChain methodChain = addPropertyMethodChainIfAbsent(clazz, classMethodMap, property, readOnly);
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
     * Each class has its own version of the PropertyMethodChain map.
     * @param clazz
     * @return PropertyMethodChain map for the passed class.
     */
    protected ConcurrentMap<String, PropertyMethodChain> getMethodMap(Class<?> clazz) {
        ConcurrentMap<String, PropertyMethodChain> propMap;
        if ( !methodsMap.containsKey(clazz)) {
            propMap = new ConcurrentHashMap<String, PropertyMethodChain>();
            methodsMap.putIfAbsent(clazz, propMap);
        }
        propMap = methodsMap.get(clazz);

        for(String property: propertyNames) {
            addPropertyMethodChainIfAbsent(clazz, propMap, property, false);
        }
        return propMap;
    }
    /**
     * @param clazz
     * @param propMap
     * @param propertyName
     * @param readOnly if true and if propertyMethodChain has not been found then only the get method is searched for.
     * @return the propertyMethodChain
     * @throws ApplicationIllegalArgumentException if the propertyName is not actually a property.
     */
    protected PropertyMethodChain addPropertyMethodChainIfAbsent(Class<?> clazz, ConcurrentMap<String, PropertyMethodChain> propMap, String propertyName, boolean readOnly)
        throws ApplicationIllegalArgumentException {
        if (!propMap.containsKey(propertyName)) {
            PropertyMethodChain propertyMethodChain = newPropertyMethodChain(clazz, propertyName, readOnly);
            if ( propertyMethodChain == null) {
                throw new ApplicationIllegalArgumentException(clazz, " has no property named '",propertyName,"'");
            }
            propMap.putIfAbsent(propertyName, propertyMethodChain);
        }
        return propMap.get(propertyName);
    }


    /**
     * @param clazz
     * @param property
     * @param readOnly
     * @return the propertyMethodChain
     */
    protected PropertyMethodChain newPropertyMethodChain(Class<?> clazz, String property, boolean readOnly) {
        try {
            return new PropertyMethodChain(clazz, property, readOnly);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
