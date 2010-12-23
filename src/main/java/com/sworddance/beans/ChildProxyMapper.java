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

import com.sworddance.beans.ProxyLoader.ChildObjectNotLoadableException;

/**
 * ProxyMapper that is dependent on another {@link ProxyMapper} ( {@link RootProxyMapper} ) to get values.
 * @author patmoore
 * @param <I>
 * @param <O>
 *
 */
public class ChildProxyMapper<I,O extends I> extends ProxyMapperImpl<I,O> {

    private RootProxyMapper<?,?> rootProxyMapper;
    private ProxyMapper<?,?> baseProxyMapper;
    // TODO: need a way to set this after deserialization.
    private transient PropertyAdaptor propertyAdaptor;
    @SuppressWarnings("unchecked")
    public ChildProxyMapper(String basePropertyPath, ProxyMapperImplementor<?,?> baseProxyMapper, O realObject, PropertyAdaptor propertyAdaptor, List<String> propertyChains) {
        super(basePropertyPath, realObject, (Class<O>)propertyAdaptor.getReturnType(), (Class<I>)propertyAdaptor.getReturnType(), baseProxyMapper.getProxyLoader(), propertyChains);
        this.rootProxyMapper = baseProxyMapper.getRootProxyMapper();
        this.setBaseProxyMapper(baseProxyMapper);
        this.propertyAdaptor = propertyAdaptor;
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
<<<<<<< HEAD
=======
    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public RootProxyMapper<?,?> getRootProxyMapper() {
        return rootProxyMapper;
    }
    @Override
<<<<<<< HEAD
	public O applyToRealObject() {
=======
    public O applyToRealObject() {
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        throw new UnsupportedOperationException("cannot applyToRealObject() from childProxyMapper (yet)");
    }
    /**
     * @param propertyName
     * @param result
     */
    @Override
<<<<<<< HEAD
	protected void putOriginalValues(String propertyName, Object result) {
        this.getRootProxyMapper().putOriginalValues(getTruePropertyName(propertyName), result);
    }
    @Override
	protected void putNewValues(String propertyName, Object result) {
        this.getRootProxyMapper().putNewValues(getTruePropertyName(propertyName), result);
    }
    public Object getCachedValue(String propertyName) {
        return this.getRootProxyMapper().getCachedValue(getTruePropertyName(propertyName));
    }
    public boolean containsKey(Object propertyName) {
        return this.getRootProxyMapper().containsKey(getTruePropertyName(propertyName));
    }
    public Map<String, Object> getNewValues() {
        return this.getRootProxyMapper().getNewValues(getBasePropertyPath());
    }
    public Map<String, Object> getOriginalValues() {
        return this.getRootProxyMapper().getOriginalValues(getBasePropertyPath());
    }
=======
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
    public boolean containsKey(Object propertyName) {
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
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public ProxyBehavior getProxyBehavior() {
        return this.getRootProxyMapper().getProxyBehavior();
    }
    @Override
<<<<<<< HEAD
	protected PropertyMethodChain getPropertyMethodChain(Class<?> clazz, String propertyName) {
        return this.getRootProxyMapper().getPropertyMethodChain(clazz, getTruePropertyName(propertyName));
    }
    @Override
	@SuppressWarnings("hiding")
=======
    protected PropertyMethodChain getPropertyMethodChain(Class<?> clazz, String propertyName) {
        return this.getRootProxyMapper().getPropertyMethodChain(clazz, getTruePropertyName(propertyName));
    }

    @Override
    @SuppressWarnings("hiding")
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    protected <CI, CO extends CI> ProxyMapperImplementor<CI, CO> getChildProxyMapper(String propertyName, PropertyAdaptor propertyAdaptor, Object base, ProxyMapperImplementor<?, ?> baseProxyMapper) {
        return this.getRootProxyMapper().getChildProxyMapper(getTruePropertyName(propertyName), propertyAdaptor, base, baseProxyMapper);
    }
    /**
     * @return the proxyLoader
     */
    @Override
<<<<<<< HEAD
	public ProxyLoader getProxyLoader() {
=======
    public ProxyLoader getProxyLoader() {
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        if ( super.getProxyLoader() == null && this.getRootProxyMapper() != null) {
            return this.getRootProxyMapper().getProxyLoader();
        } else {
            return super.getProxyLoader();
        }
    }
<<<<<<< HEAD
    @Override
	public ProxyMethodHelper getProxyMethodHelper() {
=======

    @Override
    public ProxyMethodHelper getProxyMethodHelper() {
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        if ( super.getProxyLoader() == null && this.getRootProxyMapper() != null) {
            return this.getRootProxyMapper().getProxyMethodHelper();
        } else {
            return super.getProxyMethodHelper();
        }

    }

<<<<<<< HEAD
    @Override
	@SuppressWarnings("unchecked")
    public O getRealObject(boolean mustBeNotNull, Object...messages) throws ChildObjectNotLoadableException {
        O actualObject;
        try {
            actualObject = super.getRealObject(mustBeNotNull, messages);
        } catch (ChildObjectNotLoadableException e) {
            Object baseRealObject = this.getBaseProxyMapper().getRealObject(mustBeNotNull, messages);
            if ( baseRealObject != null) {
	            actualObject = (O) this.propertyAdaptor.read(baseRealObject);
	            setRealObject(actualObject);
	        } else {
	        	actualObject = null;
	        }
=======
    @SuppressWarnings("unchecked")
    @Override
    public O getRealObject() throws ChildObjectNotLoadableException {
        O actualObject;
        try {
            actualObject = super.getRealObject();
        } catch (ChildObjectNotLoadableException e) {
            Object baseRealObject = this.getBaseProxyMapper().getRealObject();
            actualObject = (O) this.propertyAdaptor.read(baseRealObject);
            setRealObject(actualObject);
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        }
        return actualObject;
    }

    /**
     * @param baseProxyMapper the baseProxyMapper to set
     */
    protected void setBaseProxyMapper(ProxyMapper<?,?> baseProxyMapper) {
        this.baseProxyMapper = baseProxyMapper;
    }

    /**
     * @return the baseProxyMapper
     */
<<<<<<< HEAD
=======
    @Override
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    public ProxyMapper<?,?> getBaseProxyMapper() {
        return baseProxyMapper;
    }

}
