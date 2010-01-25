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

import java.lang.reflect.Method;

import com.sworddance.util.ApplicationIllegalArgumentException;

import static com.sworddance.util.CUtilities.*;

/**
 * @author patmoore
 *
 */
public class BaseProxyMethodHelperImpl implements ProxyMethodHelper {

    public static final ProxyMethodHelper INSTANCE = new BaseProxyMethodHelperImpl();

    /**
     * @see com.sworddance.beans.ProxyMethodHelper#invoke(com.sworddance.beans.ProxyMapper, java.lang.Object, java.lang.reflect.Method, java.lang.Object...)
     */
    @Override
    @SuppressWarnings("unused")
    public <I, O extends I> Object invoke(ProxyMapper<I, O> proxyMapper, Object proxy, Method method, Object... args) throws Throwable {
        String methodName = method.getName();
        if ( methodName.equals("toString") && size(args) == 0) {
            // TODO doesn't seem right should go through ... ( but real object may not exist... )
            return proxyMapper.getClass()+" "+proxyMapper.getNewValues();
        } else if ( "equals".equals(methodName) && size(args) == 1) {
            Object key = proxyMapper.getValue(args[0], ((ProxyMapperImplementor<I,O>)proxyMapper).getKeyProperty());
            Boolean eq = proxyMapper.getKeyExpression().equals(key);
            return eq;
        } else if ("hashCode".equals(methodName) && size(args) == 0) {
            // TODO: pluggable behavior :: get the key of the object and use as the hashCode()
            Object key = proxyMapper.getKeyExpression();
            return key.hashCode();
        } else {
            throw new ApplicationIllegalArgumentException(" does not handle method "+ proxyMapper.getRealClass()+"."+ method);
        }
    }

    /**
     * @see com.sworddance.beans.ProxyMethodHelper#isHandling(com.sworddance.beans.ProxyMapper, java.lang.Object, java.lang.reflect.Method, java.lang.Object...)
     */
    @Override
    @SuppressWarnings("unused")
    public <I, O extends I> boolean isHandling(ProxyMapper<I, O> proxyMapper, Object proxy, Method method, Object... args) {
        String methodName = method.getName();
        if ( methodName.equals("toString") && size(args) == 0 || "equals".equals(methodName) && size(args) == 1 || "hashCode".equals(methodName) && size(args) == 0) {
            return true;
        } else {
            return false;
        }
    }
}
