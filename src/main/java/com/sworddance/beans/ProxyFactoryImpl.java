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

import java.util.Arrays;
import java.util.List;

/**
 * @author patmoore
 *
 */
public class ProxyFactoryImpl implements ProxyFactory {

    public <I,O extends I> I getProxy(O realObject, String...propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(O realObject, Class<O>realClass,  String...propertyChains) {
        return getProxy(realObject, realClass, ProxyBehavior.leafStrict, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(Class<O> realClass, ProxyBehavior proxyBehavior, String... propertyChains) {
        return getProxy(realClass, proxyBehavior, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(O realObject, ProxyBehavior proxyBehavior, String...propertyChains) {
        return getProxy(realObject, proxyBehavior, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(O realObject, List<String>propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, propertyChains);
    }

    @SuppressWarnings("unchecked")
    public <I,O extends I> I getProxy(O realObject, ProxyBehavior proxyBehavior, List<String>propertyChains) {
        Class realClass = realObject.getClass();
        return (I) getProxy(realObject, realClass, proxyBehavior, propertyChains);
    }
    public <I,O extends I> I getProxy(Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        ProxyMapper<I, O> handler = new RootProxyMapper<I, O>(realClass, proxyBehavior, propertyChains);
        return handler.getExternalFacingProxy();
    }
    /**
     * @param <O>
     * @param <I>
     * @param realObject
     * @param realClass
     * @param proxyBehavior
     * @param propertyChains
     * @return the proxy
     */
    public <I,O extends I> I getProxy(O realObject, Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        ProxyMapper<I, O> handler = new RootProxyMapper<I, O>(realObject, realClass, proxyBehavior, propertyChains);
        return handler.getExternalFacingProxy();
    }
}
