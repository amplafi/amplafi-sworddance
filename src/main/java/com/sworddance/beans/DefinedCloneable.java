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
 * A Cloneable that does not throw a java.lang.CloneNotSupportedException!
 *
 * Ideally <C> would be part of the interface definition.
 *
 * @author patmoore
 *
 */
public interface DefinedCloneable extends Cloneable {
    /**
     *
     * Implementation note: java.lang.Object#clone() should be called first by the implementor.
     * The fields that need to be cloned themselves should be cloned and replaced on the clone returned from java.lang.Object#clone()
     * @return the cloned object ( sadly can not use generic casting because of protected {@link Object#clone()} signature.
     */
    Object clone();
}
