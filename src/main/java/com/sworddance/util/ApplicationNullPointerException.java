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

import static org.apache.commons.lang.StringUtils.join;

/**
 * The application was looking for a non-null value and discovered that the
 * value being checked was null. This is to distinguish from a
 * JDK-generated {@link NullPointerException}.
 * @author Patrick Moore
 */
public class ApplicationNullPointerException extends RuntimeException {

    private static final long serialVersionUID = -1095918218298365662L;

    public ApplicationNullPointerException() {
    }

    public ApplicationNullPointerException(String s) {
        super(s);
    }

    public ApplicationNullPointerException(Throwable cause) {
        super(cause);
    }

    public ApplicationNullPointerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationNullPointerException(Object... message) {
        super(join(message));
    }
}
