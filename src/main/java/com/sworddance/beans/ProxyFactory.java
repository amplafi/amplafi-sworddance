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
 *
 * Creates {@link ProxyMapper} objects.
 * @author patmoore
 *
 */
public interface ProxyFactory {

    /**
     * TODO: realObject should not be constrained because we may be taking data from some source (like a map) that has no hierarchy relationship with I
     * @param <I>
     * @param <O>
     * @param realObject
     * @param propertyChains
     * @return the proxy
     */
    public <I,O extends I> I getProxy(O realObject, String...propertyChains);
    /**
     * Used when the realObject is not available, or if {@link Class#getClass()} may not return the desired class.
     * @param <I>
     * @param <O>
     * @param realObject maybe null
     * @param realClass
     * @param propertyChains
     * @return the proxy
     */
    public <I,O extends I> I getProxy(O realObject, Class<O> realClass, String...propertyChains);
    /**
     * initialize an existing proxy ( that has a ProxyMapper attached to it). Useful for when a proxyMapper needs to have services attached to it.
     *
     * @param <I>
     * @param <R>
     * @param proxy
     * @return the proxyMapper as returned by {@link #getProxyMapper(Object)}(proxy)
     */
    <I, R extends ProxyMapper<I, ? extends I>> R initProxyMapper(I proxy);

    /**
     *
     * @param proxy an object that is possibly already a proxymapper (if so then proxy is returned)
     * @return the proxyMapper
     */
    <I, R extends ProxyMapper<I, ? extends I>> R getProxyMapper(I proxy);
    <I> I getRealObject(I proxy);
}
