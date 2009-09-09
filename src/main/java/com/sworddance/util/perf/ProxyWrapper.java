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

package com.sworddance.util.perf;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

public class ProxyWrapper implements InvocationHandler {
    private Object real;
    private LapTimer lapTimer;
    private String className;

    private ProxyWrapper(String className) {
        this.className = className;
    }
    @SuppressWarnings("unchecked")
    public static Object getProxyWrapper(Class clazz) {
        if (clazz == null) {
            return null;
        }
        String className = clazz.getName()+'.';
        ClassLoader cl = clazz.getClassLoader();
        ArrayList<Class> l = new ArrayList<Class>();
        while(clazz != null ) {
            Class[]ifaces = clazz.getInterfaces();
            for (Class element : ifaces) {
                l.add(element);
            }
            clazz = clazz.getSuperclass();
        }
        Class[] interfaces = l.toArray(new Class[l.size()]);
        return Proxy.newProxyInstance(cl, interfaces, new ProxyWrapper(className));
    }

    public static Object getProxyWrapper(Object obj) {
        if (obj == null) {
            return null;
        }
        Object o = getProxyWrapper(obj.getClass());
        ProxyWrapper p = (ProxyWrapper) Proxy.getInvocationHandler(o);
        p.setRealObj(obj);
        return o;
    }

    public static Object getProxyWrapper(Object obj, LapTimer lapTimer) {
        ProxyWrapper o = (ProxyWrapper) getProxyWrapper(obj.getClass());
        ProxyWrapper p = (ProxyWrapper) Proxy.getInvocationHandler(o);
        p.setRealObj(obj);
        p.setLapTimer(lapTimer);
        return o;
    }

    /**
     * @param obj
     */
    private void setRealObj(Object obj) {
        this.real = obj;
    }

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @SuppressWarnings("unused")
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (this.lapTimer != null) {
            this.lapTimer.beginNewLap(this.className+method.getName());
        } else {
            LapTimer.sBegin(this.className+method.getName());
        }

        try {
            return method.invoke(real, args);
        } catch (InvocationTargetException e ) {
            // unpeel the wrapping and throw the real exception
            throw e.getTargetException();
        } finally {
            if (this.lapTimer != null) {
                this.lapTimer.endLap();
            } else {
                LapTimer.sEnd();
            }
        }
    }

    /**
     * @return Returns the lapTimer.
     */
    public LapTimer getLapTimer() {
        return lapTimer;
    }

    /**
     * @param lapTimer The lapTimer to set.
     */
    public void setLapTimer(LapTimer lapTimer) {
        this.lapTimer = lapTimer;
    }
}