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
 * Handles proxy issues where the "real" class is not the class as reported by {@link Object#getClass()}.
 *
 * This happens most notably with hibernate.
 * @author patmoore
 *
 */
public interface ClassResolver {
    public <I> Class<? extends I> getRealClass(I possibleProxy);
}
