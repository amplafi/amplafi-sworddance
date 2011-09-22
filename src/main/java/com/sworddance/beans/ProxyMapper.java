/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.sworddance.beans;

import java.lang.reflect.InvocationHandler;
import java.util.Map;

import com.sworddance.beans.ProxyLoader.ChildObjectNotLoadableException;

/**
 *
 * ProxyMappers are useful when
 * <ul>
 * <li>there are objects that only a certain set of methods should be available. For example, an object that has getter/setter methods where
 * only the getters should be allowed to be called.</li>
 * <li>the objects may map to database objects and the changes to the database object need to be delayed until the conversation is completed (think several requests
 * back and forth to the user before they confirm the changes)</li>
 * </ul>
 *
 *
 * @author patmoore
 *
 * @param <I>
 * @param <O>
 */
public interface ProxyMapper<I, O extends I> extends InvocationHandler {

    Object getCachedValue(String propertyName);

    boolean containsKey(Object propertyName);

    /**
     *
     * @return unmodifiable map to the new values
     */
    Map<String, Object> getNewValues();

    /**
     *
     * @return unmodifiable map to the original values
     */
    Map<String, Object> getOriginalValues();

    ProxyBehavior getProxyBehavior();

    /**
     * discard the realObject (which is transient in any event)
     */
    void clearCached();

    /**
     * @return key Value used to determine equality and hashCode
     */
    @Deprecated
    // use ProxyMethodHelper String getKeyProperty();
    /**
     * @return the basePropertyPath
     */
    String getBasePropertyPath();

    /**
     * the real object may no longer be available. This method reloads the realObject if necessary.
     * @param mustBeNotNull TODO
     * @return the realObject
     * @throws ChildObjectNotLoadableException
     */
    O getRealObject(boolean mustBeNotNull, Object...messages) throws ChildObjectNotLoadableException;

    void setRealObject(O realObject);

    boolean isRealObjectSet();

    Object getKeyExpression();

    /**
     * Use the property mappings copy the values stored in {@link #getNewValues()} back to the
     * {@link #getRealObject(boolean, Object...)} object.
     * @return the real object.
     */
    O applyToRealObject();

    /**
     * @param externalFacingProxy the externalFacingProxy to set
     */
    void setExternalFacingProxy(I externalFacingProxy);

    /**
     * @return the externalFacingProxy
     */
    I getExternalFacingProxy();

    /**
     * @param proxyLoader the proxyLoader to set
     */
    void setProxyLoader(ProxyLoader proxyLoader);

    /**
     * @return the proxyLoader
     */
    ProxyLoader getProxyLoader();

    /**
     * @param realClass the realClass to set
     */
    void setRealClass(Class<? extends O> realClass);

    /**
     * @return the realClass
     */
    Class<? extends Object> getRealClass();

    <T> T getValue(Object base, String property);

    /**
     * @return the proxyMethodHelper
     */
    ProxyMethodHelper getProxyMethodHelper();

}