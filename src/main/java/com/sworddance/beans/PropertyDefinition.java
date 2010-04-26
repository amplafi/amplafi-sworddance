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

/**
 * This provides more detail than is available via reflection. Specifically, what is the exact class of the keys and values in {@link java.util.Map}s or the elements of a {@link java.util.List}.
 * @author patmoore
 *
 */
public interface PropertyDefinition {

    /**
     * @param keyPropertyDefinition the keyPropertyDefinition to set
     */
    void setKeyPropertyDefinition(PropertyDefinition keyPropertyDefinition);

    /**
     * @return the keyPropertyDefinition
     */
    PropertyDefinition getKeyPropertyDefinition();

    boolean isKeyPropertyDefinitionSet();

    /**
     * @param elementPropertyDefinition the elementPropertyDefinition to set
     */
    void setElementPropertyDefinition(PropertyDefinition elementPropertyDefinition);

    /**
     * @return the elementPropertyDefinition
     */
    PropertyDefinition getElementPropertyDefinition();

    boolean isElementPropertyDefinitionSet();

    /**
     * @param propertyClass the propertyClass to set
     */
    void setPropertyClass(Class<?> propertyClass);

    /**
     * @return the propertyClass
     */
    Class<?> getPropertyClass();

    boolean isPropertyClassDefined();

    boolean isSameDataClass(PropertyDefinition propertyDefinition);

    boolean isAssignableFrom(PropertyDefinition propertyDefinition);

}