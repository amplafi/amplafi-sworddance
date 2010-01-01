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

import java.util.ArrayList;
import java.util.List;

import static com.sworddance.util.CUtilities.*;

import static org.apache.commons.lang.StringUtils.*;

/**
 * @author patmoore
 *
 */
public class ApplicationGeneralException extends RuntimeException {

    private List<Throwable> additionalThrowables;
    /**
     *
     */
    public ApplicationGeneralException() {
    }

    /**
     * @param message
     */
    public ApplicationGeneralException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ApplicationGeneralException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ApplicationGeneralException(String message, Throwable cause) {
        super(message, cause);
    }

    public void add(Throwable throwable) {
        if ( additionalThrowables == null) {
            additionalThrowables = new ArrayList<Throwable>();
        }
        additionalThrowables.add(throwable);
    }
    @Override
    public String toString() {
        if ( isEmpty(additionalThrowables)) {
            return super.toString();
        } else {
            return super.toString() + join(additionalThrowables, "\nAdditional:\t");
        }
    }
}
