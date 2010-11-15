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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author patmoore
 *
 */
public class ProxyFactoryImpl implements ProxyFactory {

    public static final ProxyFactoryImpl INSTANCE = new ProxyFactoryImpl(BaseProxyLoaderImpl.INSTANCE, BaseProxyMethodHelperImpl.INSTANCE);
    private ProxyLoader defaultProxyLoader;
    private ProxyMethodHelper defaultProxyMethodHelper;


    public ProxyFactoryImpl(ProxyLoader defaultProxyLoader, ProxyMethodHelper defaultProxyMethodHelper) {
        this.defaultProxyLoader = defaultProxyLoader;
        this.defaultProxyMethodHelper = defaultProxyMethodHelper;
    }

    public <I,O extends I> I getProxy(O realObject, String...propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(O realObject, Class<O>realClass,  String...propertyChains) {
        return getProxy(realObject, realClass, ProxyBehavior.leafStrict, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(Class<O> realClass, ProxyBehavior proxyBehavior, String... propertyChains) {
        return getProxy(realClass, proxyBehavior, Arrays.asList(propertyChains));
    }

    public <I,O extends I> I getProxy(O realObject, List<String>propertyChains) {
        return getProxy(realObject, ProxyBehavior.leafStrict, propertyChains);
    }

    @SuppressWarnings("unchecked")
    public <I,O extends I> I getProxy(O realObject, ProxyBehavior proxyBehavior, List<String>propertyChains) {
        Class realClass = this.getDefaultProxyLoader().getRealClass(realObject);
        return (I) getProxy(realObject, realClass, proxyBehavior, propertyChains);
    }
    public <I,O extends I> I getProxy(Class<? extends O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        return getProxy(null, realClass, proxyBehavior, propertyChains);
    }
    /**
     * The method that does the real work.
     * @param <O>
     * @param <I>
     * @param realObject
     * @param realClass
     * @param proxyBehavior
     * @param propertyChains
     * @return the proxy
     */
    public <I,O extends I> I getProxy(O realObject, Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        Class<? extends I> proxyClass = this.getDefaultProxyLoader().getProxyClassFromClass(realClass);
        Map<String, Object> newValues = null;
        Map<String, Object> originalValues = null;
        RootProxyMapper<I, O> proxyMapper = new RootProxyMapper<I, O>(realObject, realClass, proxyClass, proxyBehavior, defaultProxyLoader, propertyChains, originalValues, newValues );
        initProxyMapper(proxyMapper);
        return proxyMapper.getExternalFacingProxy();
    }

    /**
     * @see com.sworddance.beans.ProxyFactory#initProxyMapper(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public <I, R extends ProxyMapper<I, ? extends I>> R initProxyMapper(I proxy) {
        R proxyMapper = (R) getProxyMapper(proxy);
        if ( proxyMapper instanceof ProxyMapperImplementor<?, ?>) {
            initProxyMapper(proxyMapper);
        }
        return proxyMapper;
    }

    /**
     * gets the real object when the leaf node is being proxied
     * @param <I>
     * @param proxy
     * @return proxy or the real object if proxy is a ProxyMapper
     */
    public <I> I getRealObject(I proxy) {
        ProxyMapper<I, ? extends I> proxyMapper = getProxyMapper(proxy);
        if ( proxyMapper != null) {
            return proxyMapper.getRealObject();
        }
        return proxy;
    }
    @SuppressWarnings("unchecked")
    public <I, R extends ProxyMapper<I, ? extends I>> R getProxyMapper(I proxy) {
        if ( proxy != null && Proxy.isProxyClass(proxy.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            if ( handler instanceof ProxyMapper<?,?>) {
                return (R) handler;
            }
        }
        return null;
    }
    /**
     * @param <I>
     * @param <O>
     * @param proxyMapper
     */
    private <I, O extends I> void initProxyMapper(RootProxyMapper<I, O> proxyMapper) {
        if ( proxyMapper.getProxyLoader() == null) {
            proxyMapper.setProxyLoader(defaultProxyLoader);
        }
        if ( proxyMapper.getProxyMethodHelper()==null) {
            proxyMapper.setProxyMethodHelper(defaultProxyMethodHelper);
        }
    }

    /**
     * @param defaultProxyLoader the defaultProxyLoader to set
     */
    public void setDefaultProxyLoader(ProxyLoader defaultProxyLoader) {
        this.defaultProxyLoader = defaultProxyLoader;
    }

    /**
     * @return the defaultProxyLoader
     */
    public ProxyLoader getDefaultProxyLoader() {
        return defaultProxyLoader;
    }

    /**
     * @param defaultProxyMethodHelper the defaultProxyMethodHelper to set
     */
    public void setDefaultProxyMethodHelper(ProxyMethodHelper defaultProxyMethodHelper) {
        this.defaultProxyMethodHelper = defaultProxyMethodHelper;
    }

    /**
     * @return the defaultProxyMethodHelper
     */
    public ProxyMethodHelper getDefaultProxyMethodHelper() {
        return defaultProxyMethodHelper;
    }

}
