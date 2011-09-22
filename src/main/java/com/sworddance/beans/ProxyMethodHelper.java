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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Implementors provide method functionality that is equivalent to the object's functionality.
 * Usual usecase is equals(), hashCode(), toString() which usually has the same functionality as the real object without the
 * real object being present.
 *
 * @author patmoore
 *
 */
public interface ProxyMethodHelper {
    <I,O extends I> boolean isHandling(ProxyMapper<I,O> proxyMapper, Object proxy, Method method, Object... args);
    <I,O extends I> Object invoke(ProxyMapper<I,O> proxyMapper, Object proxy, Method method, Object... args) throws Throwable;
    <I,O extends I> InvocationHandler newProxyInvocationHandler(ProxyMapper<I,O> proxyMapper);
}
