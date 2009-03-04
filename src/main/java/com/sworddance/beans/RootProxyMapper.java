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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sworddance.util.ApplicationIllegalArgumentException;

/**
 * @author patmoore
 * @param <I>
 * @param <O>
 */
public class RootProxyMapper<I, O extends I> extends ProxyMapper<I, O> {
    private ProxyBehavior proxyBehavior;

    private ConcurrentMap<String, Object> originalValues;

    private ConcurrentMap<String, Object> newValues;

    private ConcurrentMap<String, ProxyMapper<?,?>> childProxies;

    /**
     * @param realObject
     * @param proxyBehavior
     * @param propertyChains first property is used to determine equality
     */
    @SuppressWarnings("unchecked")
    public RootProxyMapper(O realObject, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        this(realObject, (Class<O>) realObject.getClass(), proxyBehavior, propertyChains);
    }

    public RootProxyMapper(Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        this(null, realClass, proxyBehavior, propertyChains);
    }

    public RootProxyMapper(O realObject, List<String> propertyChains) {
        this(realObject, ProxyBehavior.strict, propertyChains);
    }

    public RootProxyMapper(O realObject, ProxyBehavior proxyBehavior, String... propertyChains) {
        this(realObject, proxyBehavior, Arrays.asList(propertyChains));
    }

    public RootProxyMapper(O realObject, String... propertyChains) {
        this(realObject, ProxyBehavior.strict, Arrays.asList(propertyChains));
    }

    public RootProxyMapper(O realObject, Class<O> realClass, ProxyBehavior proxyBehavior, List<String> propertyChains) {
        super(null, realObject, realClass, propertyChains);
        this.setProxyBehavior(proxyBehavior);
        initValuesMap(realObject, propertyChains);
    }

    public void initValuesMap(O base, List<String> propertyChains) {
        originalValues = new ConcurrentHashMap<String, Object>();
        newValues = new ConcurrentHashMap<String, Object>();
        if (base != null) {
            for (String property : propertyChains) {
                initValue(base, property);
            }
        }
    }
    @Override
    public boolean containsKey(String propertyName) {
        return this.getNewValues().containsKey(propertyName) || this.getOriginalValues().containsKey(propertyName) || this.childProxies.containsKey(propertyName);
    }
    /**
     * @param propertyName
     * @param result
     */
    @Override
    protected void putOriginalValues(String propertyName, Object result) {
        if (propertyName == null) {
            throw new ApplicationIllegalArgumentException( "propertyName cannot be null");
        }
        this.getOriginalValues().put(propertyName, result==null?NullObject:result);
    }
    @Override
    protected void putNewValues(String propertyName, Object result) {
        if (propertyName == null) {
            throw new ApplicationIllegalArgumentException( "propertyName cannot be null");
        }
        this.getNewValues().put(propertyName, result==null?NullObject:result);
    }
    @Override
    public Object getCachedValue(String propertyName) {
        Object o;
        if (this.getNewValues().containsKey(propertyName)) {
            o = this.getNewValues().get(propertyName);
        } else if (this.getOriginalValues().containsKey(propertyName)){
            o = this.getOriginalValues().get(propertyName);
        } else {
            ProxyMapper<?,?> childProxy = this.childProxies.get(propertyName);
            o = childProxy.getExternalFacingProxy();
        }
        if ( o == NullObject) {
            o = null;
        }
        return o;
    }

    @Override
    public void clear() {
        this.setRealObject(null);
        for(ProxyMapper<?, ?>proxyMapper: this.childProxies.values()) {
            proxyMapper.clear();
        }
    }
    /**
     * Child proxies are used when 'this' has been asked for a property that is partial path to leaf properties.
     * <p>For example, a ProxyMapper is managing properties:
     * <ul><li>foo.bar</li>
     * <li>foo.goo</li>
     * <li>bee</li>
     * </ul>
     * The ProxyMapper is asked for the "foo" property. The ProxyMapper will return a child ProxyMapper "foo" that has properties:
     * <ul><li>bar (mapped to parent "foo.bar")</li>
     * <li>goo (mapped to parent "foo.goo")</li>
     * </ul>
     * This allows the ProxyMapper usage to be less visible to called utility code.</p>
     * @param <CI>
     * @param <CO>
     * @param propertyName
     * @return existing child proxy
     */
    @SuppressWarnings("unchecked")
    public <CI, CO extends CI> ProxyMapper<CI, CO> getExistingChildProxy(String propertyName) {
        if (this.childProxies != null){
            return (ProxyMapper<CI, CO>) this.childProxies.get(propertyName);
        } else {
            return null;
        }
    }
    /**
     * @param propertyName
     * @param proxy
     */
    private void setChildProxy(String propertyName, ProxyMapper<?,?> proxy) {
        if (this.childProxies == null){
            this.childProxies = new ConcurrentHashMap<String, ProxyMapper<?,?>>();
        }
        this.childProxies.putIfAbsent(propertyName, proxy);
    }
    @Override
    public <CI, CO extends CI> ProxyMapper<CI, CO> getChildProxyMapper(String propertyName, PropertyAdaptor propertyAdaptor, Object base) {
        return this.getChildProxyMapper(this, propertyName, propertyAdaptor, base);
    }
    @SuppressWarnings("unchecked")
    protected <CI, CO extends CI> ProxyMapper<CI, CO> getChildProxyMapper(ProxyMapper<?, ?> baseProxyMapper, String propertyName, PropertyAdaptor propertyAdaptor, Object base) {
        ProxyMapper<CI,CO> childProxy = getExistingChildProxy(propertyName);
        // do not want to eagerly get the propValue unnecessarily because this may trigger expensive operations (for example hibernate db operation )
        CO propValue;
        if ( base != null && (childProxy == null || !childProxy.isRealObjectSet() )) {
            propValue = (CO) propertyAdaptor.read(base);
        } else {
            propValue = null;
        }
        if ( childProxy != null ) {
            if (!childProxy.isRealObjectSet()) {
                childProxy.setRealObject(propValue);
            }
            return childProxy;
        } else if ( propValue != null ) {
            childProxy = new ChildProxyMapper<CI,CO>(propertyName, this, propValue, (Class<CO>)propertyAdaptor.getReturnType(), new ArrayList<String>());
            setChildProxy(propertyName, childProxy);
            // multi-thread environment may mean that the object this thread created was not the
            // one actually inserted. (see use of ConcurrentMap#putIfAbsent() )
            childProxy = getExistingChildProxy(propertyName);
        }
        return childProxy;
    }

    /**
     * @param proxyBehavior the proxyBehavior to set
     */
    public void setProxyBehavior(ProxyBehavior proxyBehavior) {
        this.proxyBehavior = proxyBehavior;
    }

    /**
     * @return the proxyBehavior
     */
    @Override
    public ProxyBehavior getProxyBehavior() {
        return proxyBehavior;
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#getNewValues()
     */
    @Override
    public Map<String, Object> getNewValues() {
        return this.newValues;
    }
    @Override
    public Map<String, Object> getOriginalValues() {
        return this.originalValues;
    }
}
