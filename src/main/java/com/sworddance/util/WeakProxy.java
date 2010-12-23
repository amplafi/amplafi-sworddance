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
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import static org.apache.commons.lang.StringUtils.*;

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
    public static <T> T newProxyInstance(final Object referent, Class<?>...interfaces) {
        return (T) newProxyInstance(referent, null, interfaces);
    }
    public static <T> T newProxyInstance(Callable<T> restoreCallable, Class<?>...interfaces) {
        return newProxyInstance(null, restoreCallable, interfaces);
    }
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(final Object referent, Callable<T> restoreCallable, Class<?>...interfaces) {
        if ( referent == null && restoreCallable == null ) {
            return null;
        } else {
            Class<?> clazz = getFirst(interfaces);
            T actualReferent = (T) getActual(referent);
            if ( clazz == null ) {
                if ( actualReferent == null ) {
                    actualReferent = invokeCallable(restoreCallable);
                }
                ApplicationIllegalArgumentException.notNull(actualReferent, "referent must be not null if there are no listed classes");
                clazz = actualReferent.getClass();
                interfaces = clazz.getInterfaces();
            }
            Reference <T>objectRef = getWeakReference(actualReferent);
            ProxyInvocationHandler<T> invocationHandler = new ProxyInvocationHandler<T>(objectRef, restoreCallable, interfaces);
            T t = invocationHandler.newProxyInstance(clazz.getClassLoader());
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
        Object actual = proxy;
        while(actual != null) {
            if ( Proxy.isProxyClass(actual.getClass())){
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(actual);
                if ( invocationHandler instanceof ProxyInvocationHandler<?>) {
                    actual = ((ProxyInvocationHandler<?>)invocationHandler).getActual();
                } else {
                    break;
                }
            } else if ( actual instanceof Reference<?>) {
                actual = getActual(((Reference<T>)proxy).get());
            } else {
                break;
            }
        }
        return (T) actual;
    }
    /**
     * @param actual
     * @return
     */
    private static <T> T invokeCallable(Callable<T> restoreCallable) {
        if ( restoreCallable == null) {
            return null;
        }
        try {
            return restoreCallable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationGeneralException(e);
        }
    }

    public static <T> WeakReference<T> getWeakReference(T referent) {
        if (referent == null) {
            return null;
        } else {
            return new WeakReference<T>(referent);
        }
    }
    protected static class ProxyInvocationHandler<T> implements InvocationHandler {
        private final Callable<T> restoreCallable;
        private Reference<T> objectRef;
        private final String stringDescription;
        private final Class<?>[] interfaces;
        protected ProxyInvocationHandler(Reference<T> objectRef, Callable<T> restoreCallable, Class<?>[] interfaces) {
            this.restoreCallable = restoreCallable;
            this.objectRef = objectRef;
            this.stringDescription = "implements = {"+join(interfaces, ", ")+"}";
            this.interfaces = interfaces;
        }
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ( isWired() ) {
                T actual = getActual();
                return method.invoke(actual, args);
            } else {
                throw new ApplicationNullPointerException("weakreference was dropped");
            }
        }
        /**
         * @param objectRef the objectRef to set
         * @return this
         */
        @SuppressWarnings("hiding")
        public ProxyInvocationHandler<T> initObjectRef(Reference<T> objectRef) {
            this.setObjectRef(objectRef);
            return this;
        }
        /**
         * @param objectRef the objectRef to set
         */
        public void setObjectRef(Reference<T> objectRef) {
            this.objectRef = objectRef;
        }
        /**
         * @return the objectRef
         */
        public Reference<T> getObjectRef() {
            return objectRef;
        }
        /**
         * @return the objectRef
         */
        public T getActual() {
            T actual = null;
            if (getObjectRef() != null) {
                actual = getObjectRef().get();
            }
            if ( actual == null ) {
                actual = invokeCallable(restoreCallable);
                setActual(actual);
            }
            return actual;
        }
        public void setActual(T actual) {
            if ( actual != null ) {
                setObjectRef(new WeakReference<T>(actual));
            } else {
                objectRef = null;
            }
        }
        /**
         * @return the interfaces
         */
        public Class<?>[] getInterfaces() {
            return interfaces;
        }
        public boolean isWired() {
            return getActual() != null;
        }
        @Override
        public String toString() {
            return this.stringDescription+ " object="+this.getActual();
        }
        @SuppressWarnings("unchecked")
        public T newProxyInstance(ClassLoader classLoader) {
            return (T) Proxy.newProxyInstance(classLoader, interfaces, this);
        }
    }
}