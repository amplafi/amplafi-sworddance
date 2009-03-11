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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sworddance.util.CurrentIterator;

/**
 * enables a controlled access to an object tree that also supports caching.
 *
 * Sample use case:
 * <ul>
 * <li>a User object has a member object Role.</li>
 * <li>the Role object has its own properties</li>
 * <li>Both Role and User are stored in the database</li>
 * <li>Access to changing properties on either User or Role should be restricted on a dynamic basis</li>
 * </ul>
 *
 * <h3>Alternative rejected solutions</h3>
 * Hibernate caching rejected because:
 * <ul>
 * <li>operates on a per-entity basis not on an object tree basis</li>
 * <li>no mechanism to restrict read-only and write able properties on a per request basis.</li>
 * <li>no serialization mechanism.</l>
 * <li>no graceful way to work with flow code to preserve original state</li>
 * <li>no ability to cache only the parts of the entities needed for the request in question.
 * For example, if the request is allowing an admin to change another user's role, then the request should
 * have no ability through bug or hack attempt to access and change the user's password.</li>
 * </ul>
 *
 * Using apache bean utilities
 * <ul>
 * <li>does not seem to handle tree of objects</li>
 * <li>serialization issues</li>
 * </ul>
 *
 * @author patmoore
 * @param <I> the interface class that the
 * @param <O> extends <I> the class (not interface) that is the concrete class that is wrapped by the ProxyWrapper.
 *
 */
public abstract class ProxyMapper<I,O extends I> extends BeanWorker implements InvocationHandler, Serializable {
    private I externalFacingProxy;
    /**
     * the real value may be null. This can arise if a ProxyMapper was created for a non-null object and then the object was
     * set to null.
     */
    private transient boolean realObjectSet;
    private transient WeakReference<O> realObject;
    private Class<? extends Object> realClass;
    private String basePropertyPath;
    private transient ProxyLoader proxyLoader;

    /**
     * {@link ConcurrentHashMap} does not allow null keys or values.
     */
    protected static final Serializable NullObject = new Serializable() {
        @Override
        public String toString() {
            return "(nullobject)";
        }
    };
    protected ProxyMapper(String basePropertyPath, O realObject, Class<O> realClass, List<String> propertyChains) {
        super(propertyChains);
        this.basePropertyPath = basePropertyPath;
        if (realObject != null) {
            this.setRealObject(realObject);
        }
        this.setRealClass(realClass);
        this.setExternalFacingProxy(createExternalFacingProxy());
    }

    /**
     * used to initialize the ProxyMapper with the cached values
     * @param base
     * @param property
     */
    @SuppressWarnings("unchecked")
    protected Object initValue(O base, String property) {
        Object result;
        if ( base != null && property != null ) {
            result = base;
            StringBuilder builder = new StringBuilder();
            PropertyMethodChain methodChain = getPropertyMethodChainAddIfAbsent(base.getClass(), property, true);
            if ( methodChain != null ) {
                CurrentIterator<PropertyAdaptor> iterator = methodChain.iterator();
                PropertyAdaptor propertyAdaptor = null;
                for (;iterator.hasNext() && result != null;) {
                    propertyAdaptor = iterator.next();
                    // construct the intermediate propertyPath to the passed property name.
                    if ( builder.length() > 0 ) {
                        builder.append(".");
                    }
                    builder.append(propertyAdaptor.getPropertyName());

                    if (propertyAdaptor.getReturnType().isInterface()) {
                        // only interfaces get child proxies
                        // need to do leaf nodes as well because one propertyChain's leaf is another's parent
                        // example: "foo" and "foo.uri"
                        ProxyMapper childProxy = getChildProxyMapper(builder.toString(), propertyAdaptor, result);
                        if ( childProxy == null) {
                            result = null;
                        } else if ( iterator.hasNext()) {
                            // we are still walking the property chain.
                            // result will be the real object for the next iteration through the loop.
                            result = childProxy.getRealObject();
                        } else {
                            // we are going to be done. return the child proxy.
                            result = childProxy.getExternalFacingProxy();
                        }
                    } else {
                        // there will not be any more proxies
                        // now finish out the retrieval of the result.
                        result = methodChain.getValue(result, iterator);
                    }
                }
            }
        } else {
            result = null;
        }
        putOriginalValues(property, result);
        return result;
    }

    /**
     * @param property
     * @param result
     */
    protected abstract void putOriginalValues(String propertyName, Object result);
    protected abstract void putNewValues(String propertyName, Object result);
    public abstract Object getCachedValue(String propertyName);
    public abstract boolean containsKey(String propertyName);
    public abstract Map<String, Object> getNewValues();
    public abstract Map<String, Object> getOriginalValues();
    public abstract ProxyBehavior getProxyBehavior();
    public void clear() {
        this.realObject = null;
        this.realObjectSet = false;
    }
    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    @SuppressWarnings("unused")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        String propertyName;
        if ( methodName.equals("toString")) {
            // TODO doesn't seem right should go through ... ( but real object may not exist... )
            return this.getClass()+" "+this.getNewValues();
        } else if (methodName.equals("equals")) {
            // get the key of the object we are comparing to
            Object key = getValue(args[0], getKeyProperty());
            Boolean eq = this.getKeyExpression().equals(key);
            return eq;
        } else if ( method.getGenericParameterTypes().length == 0 && (propertyName = this.getGetterPropertyName(methodName)) != null) {
            if ( this.containsKey(propertyName)) {
                return this.getCachedValue(propertyName);
            } else {
                switch(this.getProxyBehavior()) {
                case nullValue:
                    return null;
                case readThrough:
                case leafStrict:
                    if ( method.getReturnType() == Void.class || args != null && args.length > 1) {
                        // or more than 1 argument (therefore not java bean property )
                        return method.invoke(getRealObject(), args);
                    } else {
                        return initValue(getRealObject(), propertyName);
                    }
                case strict:
                    throw new IllegalStateException("no cached value with strict proxy behavior");
                }
            }
            return null;
        } else if (method.getGenericParameterTypes().length == 1 &&(propertyName = this.getSetterPropertyName(methodName)) != null) {
            this.putNewValues(propertyName, args[0]);
            return null;
        } else {
            // HACK: how to handle sideeffects? (can't )
            switch(this.getProxyBehavior()) {
            case strict:
                throw new IllegalStateException("");
            default:
                return method.invoke(getRealObject(), args);
            }
        }
    }
    /**
     * @return
     */
    private String getKeyProperty() {
        return this.getPropertyName(0);
    }

    /**
     * @param propertyName
     * @return
     */
    protected String getTruePropertyName(String propertyName) {
        if ( this.basePropertyPath == null) {
            return propertyName;
        } else {
            return this.basePropertyPath+"."+propertyName;
        }
    }

    /**
     * @return the basePropertyPath
     */
    public String getBasePropertyPath() {
        return basePropertyPath;
    }

    /**
     * the real object may no longer be available. This method reloads the realObject if necessary.
     * @return the realObject
     */
    @SuppressWarnings("unchecked")
    public O getRealObject() {

        if ( !this.isRealObjectSet()) {
            ProxyLoader loader = getProxyLoader();
            if ( loader != null ) {
                this.setRealObject((O) loader.get(this));
            }
        }
        return this.realObject == null? null: this.realObject.get();
    }

    public void setRealObject(O realObject) {
        this.realObject = realObject == null?null: new WeakReference<O>(realObject);
        this.realObjectSet = true;
    }

    public boolean isRealObjectSet() {
        // null may be the real value so can not just check realObject being null
        return this.realObjectSet;
    }
    public Object getKeyExpression() {
        return this.getCachedValue(getKeyProperty());
    }

    public O applyToRealObject() {
        O base = getRealObject();
        for(Map.Entry<String, Object> entry : this.getNewValues().entrySet()) {
            this.setValue(base, entry.getKey(), entry.getValue());
        }
        return base;
    }
    /**
     * @param externalFacingProxy the externalFacingProxy to set
     */
    public void setExternalFacingProxy(I externalFacingProxy) {
        this.externalFacingProxy = externalFacingProxy;
    }
    /**
     * @return the externalFacingProxy
     */
    public I getExternalFacingProxy() {
        return externalFacingProxy;
    }
    @SuppressWarnings("unchecked")
    protected I createExternalFacingProxy() {
        Class<?>[] interfaces;
        if ( getRealClass().isInterface()) {
            interfaces = new Class<?>[] { getRealClass() };
        } else {
            interfaces = getRealClass().getInterfaces();
        }
        if (interfaces.length == 0) {
            throw new IllegalArgumentException(this.getRealClass()+" is not an interface or does not have any interfaces.");
        }
        return (I) Proxy.newProxyInstance(getRealClass().getClassLoader(), interfaces, this);
    }
    /**
     * @param proxyLoader the proxyLoader to set
     */
    public void setProxyLoader(ProxyLoader proxyLoader) {
        this.proxyLoader = proxyLoader;
    }
    /**
     * @return the proxyLoader
     */
    public ProxyLoader getProxyLoader() {
        return proxyLoader;
    }

    @Override
    public String toString() {
        return this.getClass()+ " for " + this.getRealClass()+" new values="+this.getNewValues()+ " original="+this.getOriginalValues();
    }

    /**
     * returns existing or creates a new ProxyMapper and returns it for the property.
     * @param propertyName
     * @param propertyAdaptor TODO
     * @param base TODO
     * @return null if base's value for the property is null otherwise returns a ProxyMapper
     */
    protected abstract <CI, CO extends CI> ProxyMapper<CI, CO> getChildProxyMapper(String propertyName, PropertyAdaptor propertyAdaptor, Object base);

    /**
     * @param realClass the realClass to set
     */
    public void setRealClass(Class<? extends Object> realClass) {
        this.realClass = realClass;
    }

    /**
     * @return the realClass
     */
    public Class<? extends Object> getRealClass() {
        return realClass;
    }

    /**
     * gets the real object when the leaf node is being proxied
     * @param <I>
     * @param <O>
     * @param proxy
     * @return proxy or the real object if proxy is a ProxyMapper
     */
    public static <I, O extends I> I getRealObject(I proxy) {
        ProxyMapper<I, O> proxyMapper = getProxyMapper(proxy);
        if ( proxyMapper != null) {
            return proxyMapper.getRealObject();
        }
        return proxy;
    }
    @SuppressWarnings("unchecked")
    public static <I, O extends I> ProxyMapper<I, O> getProxyMapper(Object proxy) {
        if ( proxy != null && Proxy.isProxyClass(proxy.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            if ( handler instanceof ProxyMapper) {
                ProxyMapper<I, O> proxyMapper = (ProxyMapper<I, O>)handler;
                return proxyMapper;
            }
        }
        return null;
    }
}
