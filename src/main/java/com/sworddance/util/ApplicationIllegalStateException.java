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
import static com.sworddance.util.CUtilities.*;

public class ApplicationIllegalStateException extends IllegalStateException {

    private static final long serialVersionUID = -7161961478084657672L;

    public ApplicationIllegalStateException() {
    }

    public ApplicationIllegalStateException(String s) {
        super(s);
    }

    public ApplicationIllegalStateException(Object... messages) {
        super(join(combineToSpecifiedClass(Object.class, messages)));
    }

    public ApplicationIllegalStateException(Throwable cause) {
        super(cause);
    }

    public ApplicationIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param failMessageParts any number of objects that are concatenated and converted to strings only if message is thrown.
     * @param validResult if true then return null. Otherwise throw {@link ApplicationIllegalArgumentException}.
     * @return null always
     * @throws ApplicationIllegalStateException if validResult is false.
     */
    public static ApplicationIllegalStateException checkState(boolean validResult, Object... failMessageParts) {
        if (!validResult) {
            throw new ApplicationIllegalStateException(failMessageParts);
        }
        return null;
    }
    public static ApplicationIllegalStateException notNull(Object mustBeNotNull,Object... failMessageParts) {
        return checkState(mustBeNotNull != null, failMessageParts);
    }
    /**
     * Used to make sure a value is not already set
     * @param mustBeNotNull
     * @param failMessageParts
     * @return
     */
    public static ApplicationIllegalStateException notSet(Object mustBeNull,Object... failMessageParts) {
        return checkState(mustBeNull == null, failMessageParts);
    }
}