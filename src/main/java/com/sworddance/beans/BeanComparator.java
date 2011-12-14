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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Compares and provides equal test using the specified properties
 * of the given beans - even objects of different type can be compared.
 * <p/>
 * This class is threadsafe.
 * @param <T>
 */
public class BeanComparator<T> extends BeanWorker {

    private Map<String, BeanComparator<?>> subComparators = new HashMap<String, BeanComparator<?>>();
    /**
     * @param propertyNames A list of properties that this BeanComparator will operate on.
     * Dot separated property paths are allowed, i.e. 'user.name'
     */
    public BeanComparator(String...propertyNames) {
        super(propertyNames);
    }
    public BeanComparator(Collection<String> propertyNames) {
        super(propertyNames);
    }

    public void addSubComparator(String propertyName, BeanComparator<?> beanComparator) {
    	this.subComparators.put(propertyName, beanComparator);
    	this.addPropertyNames(propertyName);
    }

    /**
     * Checks if the given objects' specified properties are equal.
     * @param one The first object to compare.
     * @param two The second object to compare.
     * @return True if the objects match at all the property paths that this
     * BeanComparator was configured.
     */
    public boolean areEqual(T one, T two) {
        return this.compareToBase(one, two).isEmpty();
    }

    public Set<String> compareTo(Object base, Object derived) {
    	return this.compareToBase((T)base, (T)derived);
    }
    /**
     * Compares derived to base and returns the properties that are different.
     * @param base
     * @param derived
     * @return empty list if either base or derived are null.
     */
    public Set<String> compareToBase(T base, T derived) {
        if (base==null || derived == null) {
            return Collections.emptySet();
        }
        Set<String> differences = new HashSet<String>();
        for (String property:getPropertyNames()) {
        	Object oneValue = this.getValue(base, property);
        	Object twoValue = this.getValue(derived, property);
        	if ( oneValue != twoValue) {
            	if( oneValue == null || twoValue == null) {
            	    differences.add(property);
            	} else if (this.subComparators.containsKey(property)) {
            		for(String subPropertyName: this.subComparators.get(property).compareTo(oneValue, twoValue)) {
            			differences.add(property+"."+subPropertyName);
            		}
    			} else if ( ! oneValue.equals(twoValue) ) {
    			    if ( oneValue instanceof Iterable && twoValue instanceof Iterable) {
                        Iterator<Object> iter1 = ((Iterable<Object>)oneValue).iterator();
                        Iterator<Object> iter2 = ((Iterable<Object>)twoValue).iterator();
    			        for(;iter1.hasNext()&&iter2.hasNext();) {
    			            Object sub1 = iter1.next();
    			            Object sub2 = iter2.next();
    			            if (!compareMore(sub1, sub2)) {
    			                differences.add(property);
    			                break;
    			            }
    			        }
    			        if ( iter1.hasNext() != iter2.hasNext()) {
                            differences.add(property);
    			        }
    			    } else {
    			        differences.add(property);
    			    }
            	}
            }
        }
        return differences;
    }
    private boolean compareMore(Object o1, Object o2) {
        return bothNull(o1, o2) || (o1 != null && o1.equals(o2));
    }

    private boolean bothNull(Object one, Object two) {
        return one==null && two==null;
    }
}
