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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * similar to Hibernate's Proxy annotation - which can be used instead of this annotation.
 * @author patmoore
 *
 */
@Target({TYPE,FIELD,METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxy {
    /**
     *
     * @return the class used when accessing methods. Avoid problems of getClass() on objects.
     */
    Class<?> proxyClass() default void.class;
}
