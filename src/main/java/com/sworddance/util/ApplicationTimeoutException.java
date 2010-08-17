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

import java.util.concurrent.TimeUnit;

/**
 * wraps a {@link java.util.concurrent.TimeoutException} as a runtime exception
 *
 */
public class ApplicationTimeoutException extends ApplicationIllegalStateException {

    private final long time;
    private final TimeUnit timeUnit;
    /**
     *
     */
    public ApplicationTimeoutException() {
        time = -1;
        timeUnit = TimeUnit.MILLISECONDS;
    }

    /**
     * @param s
     */
    public ApplicationTimeoutException(String s) {
        super(s);
        time = -1;
        timeUnit = TimeUnit.MILLISECONDS;
    }

    /**
     * @param cause
     */
    public ApplicationTimeoutException(Throwable cause) {
        super(cause);
        time = -1;
        timeUnit = TimeUnit.MILLISECONDS;
    }

    /**
     * @param message
     * @param cause
     */
    public ApplicationTimeoutException(String message, Throwable cause) {
        super(message, cause);
        time = -1;
        timeUnit = TimeUnit.MILLISECONDS;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @return the timeUnit
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

}
