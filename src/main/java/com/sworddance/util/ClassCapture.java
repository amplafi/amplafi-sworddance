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

/**
 * Attempt to find the Class object of T.
 * @author Patrick Moore
 * @param <T>
 */
public class ClassCapture<T> {
    private final Class<T> capturedClass = null;

    public ClassCapture() {
//        Type[] types = getClass().getTypeParameters();
//        TypeVariable<?>[] gtypes = ((TypeVariable)types[0]).getGenericDeclaration().getTypeParameters();
//        Type[] actualTypes = ((ParameterizedType)gtypes[0]).getActualTypeArguments();
//        this.capturedClass = (Class<T>)actualTypes[0].getClass();
    }

    public Class<T> getCapturedClass() {
        return capturedClass;
    }
}
