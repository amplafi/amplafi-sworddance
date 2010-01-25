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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static com.sworddance.util.CUtilities.*;

/**
 * @author patmoore
 *
 */
public class BaseProxyLoaderImpl implements ProxyLoader {


    private Class<? extends Annotation> hibernateProxyAnnotationClazz;
    private Method hibernateProxyAnnotationMethod;

    public static final BaseProxyLoaderImpl INSTANCE = new BaseProxyLoaderImpl();
    @SuppressWarnings("unchecked")
    protected BaseProxyLoaderImpl() {
        try {
            this.hibernateProxyAnnotationClazz = (Class<? extends Annotation>) Class.forName("org.hibernate.annotations.Proxy");
            this.hibernateProxyAnnotationMethod = this.hibernateProxyAnnotationClazz.getMethod("proxyClass");
        } catch (ClassNotFoundException e) {
            // oh well no hibernate class proxy.
        } catch (SecurityException e) {
            // oh well no hibernate class proxy.
        } catch (NoSuchMethodException e) {
            // oh well no hibernate class proxy.
        }
    }
    /**
     * @see com.sworddance.beans.ProxyLoader#getRealClass(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public <O> Class<? extends O> getRealClass(O object) {
        return (Class<O>) object.getClass();
    }

    @SuppressWarnings("unchecked")
    public <I> Class<? extends I> getProxyClassFromClass(I object) {
        return (Class<? extends I>) getProxyClassFromClass(object.getClass());
    }
    @SuppressWarnings("unchecked")
    public <I> Class<? extends I> getProxyClassFromClass(Class<? extends I> startingClazz) {
        Class<? extends I> proxyClass;
        if ( !startingClazz.isInterface()) {
            proxyClass = getProxyClassFromJustClass(startingClazz);
            if ( proxyClass == null ) {
                Class<?>[] interfaces = startingClazz.getInterfaces();
                if (size(interfaces)==1 ) {
                    // single interface? seems like a good choice.
                    // maybe check to make sure that the interface is not a generic java interface?
                    proxyClass= (Class<? extends I>) interfaces[0];
                } else {
                    for(Class<?> clazz = startingClazz.getSuperclass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
                        proxyClass = getProxyClassFromJustClass((Class<? extends I>) clazz);
                        if ( proxyClass != null ) {
                            break;
                        }
                    }
                }
            }
        } else {
            proxyClass = startingClazz;
        }
        return proxyClass;
    }
    /**
     * @param clazz
     */
    @SuppressWarnings("unchecked")
    private <I> Class<? extends I> getProxyClassFromJustClass(Class<? extends I> clazz) {
        Proxy proxyInfo = clazz.getAnnotation(Proxy.class);
        if ( proxyInfo != null && proxyInfo.proxyClass() != void.class) {
            return (Class<? extends I>) proxyInfo.proxyClass();
        }
        if ( hibernateProxyAnnotationClazz != null) {
            Annotation hibernateProxyInfo = clazz.getAnnotation(hibernateProxyAnnotationClazz);
            if ( hibernateProxyInfo != null) {
                try {
                    Class<?> c = (Class<?>) hibernateProxyAnnotationMethod.invoke(hibernateProxyInfo);
                    if ( c != null && c != void.class ) {
                        return (Class<? extends I>) c;
                    }
                } catch (Exception e) {
                }
            }
        }
        return null;
    }
    /**
     * @see com.sworddance.beans.ProxyLoader#getRealObject(com.sworddance.beans.ProxyMapper)
     */
    @Override
    @SuppressWarnings("unused")
    public <I, O extends I> O getRealObject(ProxyMapper<I, O> proxyMapper) throws ChildObjectNotLoadableException {
        // does not know how to find the real object if the proxyMapper does not already have one.
        throw new UnsupportedOperationException();
    }
    /**
     * @see com.sworddance.beans.ProxyLoader#getProxyClass(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <I> Class<? extends I> getProxyClass(I object) {
        return (Class<? extends I>) this.getProxyClassFromClass(object.getClass());
    }


}
