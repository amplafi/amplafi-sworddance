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
package com.sworddance.core;

/**
 * Utility class that allows wiring up existing services (from a dependency injection framework such as hivemind
 * registry) into a given object.
 *
 * @author andyhot
 */
public interface ServicesSetter {

    public void wire(Object obj);

    public void wire(Object obj, String... excludedProperties);

    /**
     * wire object in question to hivemind services.
     *
     * Exclude:
     * <ul>
     * <li>properties listed,</li>
     * <li>properties whose class is annotated with @NotService,</li>
     * <li>properties whose class,</li>
     * is in a package starting with "java",</li>
     * <li>primitive properties</li>
     * <li>properties that are not both readable and writable</li>
     * <li>properties that are already set (getter returns non-null)</li>
     * </ul>
     *
     * If there is only one service that matches the property type then it is wired no problem.
     * If there is multiple services then if the service name matches the Property name
     * (ignoring the capitalization of the first letter) then the service with the matching
     * property name is injected.
     * @param obj
     * @param excludedProperties
     */
    public void wire(Object obj, Iterable<String> excludedProperties);

    /**
     * @param propertyType
     * @return true class can be wired up as a service
     */
    public boolean isWireableClass(Class<?> propertyType);
}
