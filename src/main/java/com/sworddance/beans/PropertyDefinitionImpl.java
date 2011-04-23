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

import com.sworddance.util.ApplicationIllegalArgumentException;

/**
 * This provides more detail than is available via reflection. Specifically, what is the exact class of the keys and values in {@link Map}s or the elements of a {@link List}.
 * @author patmoore
 *
 */
public class PropertyDefinitionImpl implements PropertyDefinition {

    private PropertyDefinition keyPropertyDefinition;
    private PropertyDefinition elementPropertyDefinition;
    private Class<?> propertyClass;
    public PropertyDefinitionImpl() {

    }

    public PropertyDefinitionImpl(Class<?> propertyClass, PropertyDefinition keyPropertyDefinition, PropertyDefinition elementPropertyDefinition) {
        this.propertyClass = propertyClass;
        this.keyPropertyDefinition = keyPropertyDefinition;
        this.elementPropertyDefinition = elementPropertyDefinition;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#setKeyPropertyDefinition(com.sworddance.beans.PropertyDefinition)
     */
    public void setKeyPropertyDefinition(PropertyDefinition keyPropertyDefinition) {
        this.keyPropertyDefinition = keyPropertyDefinition;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#getKeyPropertyDefinition()
     */
    public PropertyDefinition getKeyPropertyDefinition() {
        return keyPropertyDefinition;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#isKeyPropertyDefinitionSet()
     */
    public boolean isKeyPropertyDefinitionSet() {
        return this.keyPropertyDefinition != null;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#setElementPropertyDefinition(com.sworddance.beans.PropertyDefinition)
     */
    public void setElementPropertyDefinition(PropertyDefinition elementPropertyDefinition) {
        this.elementPropertyDefinition = elementPropertyDefinition;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#getElementPropertyDefinition()
     */
    public PropertyDefinition getElementPropertyDefinition() {
        return elementPropertyDefinition;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#isElementPropertyDefinitionSet()
     */
    public boolean isElementPropertyDefinitionSet() {
        return this.elementPropertyDefinition != null;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#setPropertyClass(java.lang.Class)
     */
    public void setPropertyClass(Class<?> propertyClass) {
        this.propertyClass = propertyClass;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#getPropertyClass()
     */
    public Class<?> getPropertyClass() {
        return propertyClass;
    }
    /**
     * @see com.sworddance.beans.PropertyDefinition#isPropertyClassDefined()
     */
    public boolean isPropertyClassDefined() {
        return propertyClass != null;
    }

    /**
     * @see com.sworddance.beans.PropertyDefinition#isSameDataClass(com.sworddance.beans.PropertyDefinition)
     */
    public boolean isSameDataClass(PropertyDefinition propertyDefinition) {
        return propertyDefinition != null && this.propertyClass == propertyDefinition.getPropertyClass();
    }

    /**
     * @see com.sworddance.beans.PropertyDefinition#isAssignableFrom(com.sworddance.beans.PropertyDefinition)
     */
    public boolean isAssignableFrom(PropertyDefinition propertyDefinition) {
        return propertyClass.isAssignableFrom(propertyDefinition.getPropertyClass());
    }

    @Override
    public Object clone() {
        PropertyDefinitionImpl clone;
        try {
            clone = (PropertyDefinitionImpl)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ApplicationIllegalArgumentException(e);
        }
        if ( this.isKeyPropertyDefinitionSet()) {
            clone.setKeyPropertyDefinition((PropertyDefinition) this.keyPropertyDefinition.clone());
        }
        if ( this.isElementPropertyDefinitionSet()) {
            clone.setElementPropertyDefinition((PropertyDefinition) this.elementPropertyDefinition.clone());
        }
        return clone;
    }

}
