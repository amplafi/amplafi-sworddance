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

import java.util.List;
import java.util.Map;

/**
 * This provides more detail than is available via reflection. Specifically, what is the exact class of the keys and values in {@link Map}s or the elements of a {@link List}.
 * @author patmoore
 *
 */
public class PropertyDefinition {

    private PropertyDefinition keyPropertyDefinition;
    private PropertyDefinition elementPropertyDefinition;
    private Class<?> propertyClass;
    public PropertyDefinition() {

    }
    public PropertyDefinition(Class<?> propertyClass, PropertyDefinition keyPropertyDefinition, PropertyDefinition elementPropertyDefinition) {
        this.propertyClass = propertyClass;
        this.keyPropertyDefinition = keyPropertyDefinition;
        this.elementPropertyDefinition = elementPropertyDefinition;
    }
    /**
     * @param keyPropertyDefinition the keyPropertyDefinition to set
     */
    public void setKeyPropertyDefinition(PropertyDefinition keyPropertyDefinition) {
        this.keyPropertyDefinition = keyPropertyDefinition;
    }
    /**
     * @return the keyPropertyDefinition
     */
    public PropertyDefinition getKeyPropertyDefinition() {
        return keyPropertyDefinition;
    }
    public boolean isKeyPropertyDefinitionSet() {
        return this.keyPropertyDefinition != null;
    }
    /**
     * @param elementPropertyDefinition the elementPropertyDefinition to set
     */
    public void setElementPropertyDefinition(PropertyDefinition elementPropertyDefinition) {
        this.elementPropertyDefinition = elementPropertyDefinition;
    }
    /**
     * @return the elementPropertyDefinition
     */
    public PropertyDefinition getElementPropertyDefinition() {
        return elementPropertyDefinition;
    }
    public boolean isElementPropertyDefinitionSet() {
        return this.elementPropertyDefinition != null;
    }
    /**
     * @param propertyClass the propertyClass to set
     */
    public void setPropertyClass(Class<?> propertyClass) {
        this.propertyClass = propertyClass;
    }
    /**
     * @return the propertyClass
     */
    public Class<?> getPropertyClass() {
        return propertyClass;
    }
    public boolean isPropertyClassDefined() {
        return propertyClass != null;
    }

    public boolean isSameDataClass(PropertyDefinition propertyDefinition) {
        return propertyDefinition != null && this.propertyClass == propertyDefinition.propertyClass;
    }

    public boolean isAssignableFrom(PropertyDefinition propertyDefinition) {
        return propertyClass.isAssignableFrom(propertyDefinition.propertyClass);
    }

}
