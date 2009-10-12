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

package com.sworddance.core;

/**
 * Implementers check to see if {@link Expireable}s have expired.
 * @author patmoore
 *
 */
public interface ExpirationChecker {
    /**
     * sets expireable's {@link Expireable#setExpired(boolean)} to true if the current time is after the {@link Expireable#getExpiration()}
     * if {@link Expireable#isExpired()} is already true then the expiration check is not made
     * @param expireable
     * @return {@link Expireable#isExpired()} after the check has been performed.
     */
    boolean isExpired(Expireable expireable);
}
