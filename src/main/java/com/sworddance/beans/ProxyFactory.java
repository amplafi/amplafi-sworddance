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
 * @author patmoore
 *
 */
public interface ProxyFactory {

    public <I,O extends I> I getProxy(O realObject, String...propertyChains);
    public <I,O extends I> I getProxy(O realObject, Class<O> realClass, String...propertyChains);
}