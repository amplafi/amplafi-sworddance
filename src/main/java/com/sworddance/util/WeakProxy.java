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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
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
            Reference <T>objectRef = getWeakReference((T)referent);
            T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, new ProxyInvocationHandler<T>(objectRef));
            return t;
        }
    }

    /**
     *
     * @param proxy a {@link Reference} or proxy created by {@link #newProxyInstance(Object, Class...)}
     * @return true if there is an actual object to access.
     */
    public static boolean isWired(Object proxy) {
        return getActual(proxy) != null;
    }
    /**
     *
     * @param <T>
     * @param proxy
     * @return the actual object that is wrapped by {@link Reference} and {@link #newProxyInstance(Object, Class...)} created
     * objects.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getActual(Object proxy) {
        if ( proxy == null) {
            return null;
        } else if ( Proxy.isProxyClass(proxy.getClass())){
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
            if ( invocationHandler instanceof ProxyInvocationHandler<?>) {
                return (T) ((ProxyInvocationHandler<?>)invocationHandler).getActual();
            }
        } else if ( proxy instanceof Reference<?>) {
            return (T) getActual(((Reference<T>)proxy).get());
        }
        return (T) proxy;
    }

    public static <T> WeakReference<T> getWeakReference(T referent) {
        if (referent == null) {
            return null;
        } else {
            return new WeakReference<T>(referent);
        }
    }
    public static <T> SoftReference<T> getSoftReference(T referent) {
        if (referent == null) {
            return null;
        } else {
            return new SoftReference<T>(referent);
        }
    }
    protected static class ProxyInvocationHandler<T> implements InvocationHandler {
        private final Reference<T> objectRef;
        protected ProxyInvocationHandler(Reference<T> objectRef) {
            this.objectRef = objectRef;
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
            if (objectRef == null) {
                return null;
            } else {
                return objectRef.get();
            }
        }
        public boolean isWired() {
            return getActual() != null;
        }
    }
}