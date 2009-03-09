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
 * @author patmoore
 * @param <I>
 * @param <O>
 *
 */
public class ChildProxyMapper<I,O extends I> extends ProxyMapper<I,O> {

    private RootProxyMapper<?,?> rootProxyMapper;
    public ChildProxyMapper(String basePropertyPath, RootProxyMapper<?,?> rootProxyMapper, O realObject, Class<O> realClass, List<String> propertyChains) {
        super(basePropertyPath, realObject, realClass, propertyChains);
        this.rootProxyMapper = rootProxyMapper;
    }

    /**
     * @param rootProxyMapper the rootProxyMapper to set
     */
    protected void setRootProxyMapper(RootProxyMapper<?,?> rootProxyMapper) {
        this.rootProxyMapper = rootProxyMapper;
    }

    /**
     * @return the rootProxyMapper
     */
    protected RootProxyMapper<?,?> getRootProxyMapper() {
        return rootProxyMapper;
    }
    @Override
    public O applyToRealObject() {
        throw new UnsupportedOperationException("cannot applyToRealObject() from childProxyMapper (yet)");
    }
    /**
     * @param property
     * @param result
     */
    @Override
    protected void putOriginalValues(String propertyName, Object result) {
        this.getRootProxyMapper().putOriginalValues(getTruePropertyName(propertyName), result);
    }
    @Override
    protected void putNewValues(String propertyName, Object result) {
        this.getRootProxyMapper().putNewValues(getTruePropertyName(propertyName), result);
    }
    @Override
    public Object getCachedValue(String propertyName) {
        return this.getRootProxyMapper().getCachedValue(getTruePropertyName(propertyName));
    }
    @Override
    public boolean containsKey(String propertyName) {
        return this.getRootProxyMapper().containsKey(getTruePropertyName(propertyName));
    }

    @Override
    public Map<String, Object> getNewValues() {
        return this.getRootProxyMapper().getNewValues(getBasePropertyPath());
    }
    @Override
    public Map<String, Object> getOriginalValues() {
        return this.getRootProxyMapper().getOriginalValues(getBasePropertyPath());
    }
    @Override
    public ProxyBehavior getProxyBehavior() {
        return this.getRootProxyMapper().getProxyBehavior();
    }
    @Override
    protected PropertyMethodChain getPropertyMethodChain(Class<?> clazz, String propertyName) {
        return this.getRootProxyMapper().getPropertyMethodChain(clazz, getTruePropertyName(propertyName));
    }

    @Override
    protected <CI, CO extends CI> ProxyMapper<CI, CO> getChildProxyMapper(String propertyName, PropertyAdaptor propertyAdaptor, Object base) {
        return this.getRootProxyMapper().getChildProxyMapper(this, getTruePropertyName(propertyName), propertyAdaptor, base);
    }
    /**
     * @return the proxyLoader
     */
    @Override
    public ProxyLoader getProxyLoader() {
        if ( super.getProxyLoader() == null && this.getRootProxyMapper() != null) {
            return this.getRootProxyMapper().getProxyLoader();
        } else {
            return super.getProxyLoader();
        }
    }

}
