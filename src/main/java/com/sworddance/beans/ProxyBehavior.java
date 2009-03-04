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
 * @author patmoore
 *
 */
public enum ProxyBehavior {
    /**
     * throw exception if the property is not cached
     */
    strict,
    /**
     * attempts to get intermediate objects will be successful (otherwise strict)
     * Example, if proxy definitions include:
     * "foo.email"
     * then the proxy can return a "foo" that can in turn return a "email".
     */
    leafStrict,
    /**
     * return null if the property is not cached and print warning
     */
    nullValue,
    /**
     * load the real object and read the value.
     */
    readThrough;
}
