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

package com.sworddance.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.sworddance.util.CUtilities.*;

/**
 * Weakly held object. Allows surrounding code to be unaware of WeakReference usage.
 * @author patmoore
 *
 */
public class WeakProxy {

    /**
     * Create a WeakReference that is wrapped in a Proxy implementing the interfaces.
     * @param <T>
     * @param referent
     * @param interfaces optional - uses referent.getClass().getInterfaces() if not supplied.
     * @return null if referent == null otherwise returns a proxy implementing the interfaces
     */
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(final Object referent, Class<?>...interfaces) {
        if ( referent == null ) {
            return null;
        } else {
            Object actualReferent = getActual(referent);
            Class<?> clazz = getFirst(interfaces);
            if ( clazz == null) {
                clazz = actualReferent.getClass();
                interfaces = clazz.getInterfaces();
            }
            T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, new ProxyInvocationHandler<T>(actualReferent));
            return t;
        }
    }

    public static boolean isWired(Object proxy) {
        if ( proxy != null && Proxy.isProxyClass(proxy.getClass())){
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
            if ( invocationHandler instanceof ProxyInvocationHandler<?>) {
                return ((ProxyInvocationHandler<?>)invocationHandler).isWired();
            }
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    public static <T> T getActual(Object proxy) {
        if ( proxy != null && Proxy.isProxyClass(proxy.getClass())){
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
            if ( invocationHandler instanceof ProxyInvocationHandler<?>) {
                return (T) ((ProxyInvocationHandler<?>)invocationHandler).getActual();
            }
        }
        return (T) proxy;
    }
    protected static class ProxyInvocationHandler<T> implements InvocationHandler {
        private final WeakReference<T> objectRef;
        protected ProxyInvocationHandler(Object referent) {
            objectRef = new WeakReference<T>((T)referent);
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ( isWired() ) {
                T actual = getActual();
                return method.invoke(actual, args);
            } else {
                throw new ApplicationNullPointerException("weakreference was dropped");
            }
        }
        /**
         * @return the objectRef
         */
        public T getActual() {
            return objectRef.get();
        }
        public boolean isWired() {
            return objectRef != null && getActual() != null;
        }
    }
}