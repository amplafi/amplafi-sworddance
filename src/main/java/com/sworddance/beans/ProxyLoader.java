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

/**
 * Used by ProxyMapper to retrieve realObect when it doesn't have the real backing object
 * already.
 *
 * Use case: a ProxyMapper was created in an earlier transaction. the previous transaction completed and the original real object was discarded.
 * in a new transaction, the proxymapper discovers it needs the real object again.
 *
 * @author patmoore
 *
 */
public interface ProxyLoader {
    public <O> Class<? extends O> getRealClass(O object);
    /**
     * The class used to determine the class used to get method objects.
     *
     * Usecase: hibernate definitions can specify a proxyclass. The class returned by {@link #getRealClass(Object)} is a hibernate generated class and so can not be used to
     * access the user objects.
     * @param <I>
     * @param object
     * @return class of object
     */
    public <I> Class<? extends I> getProxyClass(I object);
    public <I> Class<? extends I> getProxyClassFromClass(Class<? extends I> clazz);
    /**
     * Using the information that is available in the proxyMapper, the ProxyLoader will retrieve and return the real object.
     * This is called by {@link ProxyMapper#getRealObject(boolean, Object...)} when the ProxyMapper does NOT have a value ( so ProxyLoaders cannot call {@link ProxyMapper#getRealObject(boolean)} )
     *
     * @param <I>
     * @param <O>
     * @param proxyMapper
     * @return ?? I not O ?? because hibernate may use interfaces to wrap the real dbo ?? -- don't remember
     * @throws ChildObjectNotLoadableException
     */
    public <I, O extends I> O getRealObject(ProxyMapper<I,O> proxyMapper) throws ChildObjectNotLoadableException;


    /**
     * Used to signal that the object represented by the child proxy can not be loaded. An ancestor ProxyMapper must load a tree of objects that include this proxyMapper.
     *
     */
    public static class ChildObjectNotLoadableException extends RuntimeException {
    	public ChildObjectNotLoadableException() {

    	}
    	public ChildObjectNotLoadableException(String message) {
    		super(message);
    	}
    }
}
