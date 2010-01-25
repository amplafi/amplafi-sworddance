/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.sworddance.beans;

import java.util.Map;

import com.sworddance.beans.ProxyLoader.ChildObjectNotLoadableException;

/**
 * @author patmoore
 *
 * @param <I>
 * @param <O>
 */
public interface ProxyMapper<I, O extends I> {

    Object getCachedValue(String propertyName);

    boolean containsKey(String propertyName);

    Map<String, Object> getNewValues();

    Map<String, Object> getOriginalValues();

    ProxyBehavior getProxyBehavior();

    /**
     * discard the realObject (which is transient in any event)
     */
    void clear();

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
     * @return the realObject
     * @throws ChildObjectNotLoadableException
     */
    O getRealObject() throws ChildObjectNotLoadableException;

    void setRealObject(O realObject);

    boolean isRealObjectSet();

    Object getKeyExpression();

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