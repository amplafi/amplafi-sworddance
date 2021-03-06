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

import static com.sworddance.util.CUtilities.combine;
import static org.apache.commons.lang.StringUtils.*;

/**
 * An application-specific {@link IllegalArgumentException} so that it is possible
 * to test for and distinguish an 'expected' exception rather than an exception generated
 * by the jdk or other package.
 *
 * @author Patrick Moore
 */
public class ApplicationIllegalArgumentException extends
        IllegalArgumentException {

    private static final long serialVersionUID = -5103328085321639906L;

    public ApplicationIllegalArgumentException() {
    }

    public ApplicationIllegalArgumentException(String s) {
        super(s);
    }

    public ApplicationIllegalArgumentException(Throwable cause) {
        super(cause);
    }

    public ApplicationIllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationIllegalArgumentException(Object... failMessage) {
        super(join(combine(failMessage)));
    }
    /**
     * @param failMessageParts any number of objects that are concatenated and converted to strings only if message is thrown.
     * @param validResult if true then return null. Otherwise throw {@link ApplicationIllegalArgumentException}.
     * @return null always
     * @throws ApplicationIllegalArgumentException if validResult is false.
     */
    public static ApplicationIllegalArgumentException valid(boolean validResult, Object... failMessageParts) {
        if (!validResult) {
            throw new ApplicationIllegalArgumentException(failMessageParts);
        }
        return null;
    }

	public static ApplicationIllegalArgumentException fail(Object... failMessageParts) {
		throw new ApplicationIllegalArgumentException(failMessageParts);
	}
    public static ApplicationIllegalArgumentException notNull(Object notNullArgument, Object... failMessageParts) {
        return valid(notNullArgument != null, failMessageParts);
    }
    /**
     *
     * @param originalValue
     * @param changedValue
     * @param field
     * @param failMessageParts
     * @throws ApplicationIllegalArgumentException - originalValue != null
     * @return true - if changedValue != null and not equal to originalValue, false if no change
     */
    public static boolean testSetOnce(Object originalValue, Object changedValue, String field, Object... failMessageParts) {
        if ( originalValue == changedValue || changedValue == null) {
            // both null or identical
            return false;
        } else if ( originalValue != null) {
            valid(originalValue.equals(changedValue), field,
                ": Only allowed to set this field once. Current value='", originalValue, "' != (new)=", changedValue, " ",
                failMessageParts);
            return false;
        } else {
            return true;
        }
    }

    /**
     * If {@link #testSetOnce(Object, Object, String, Object...)} false then originalValue is return, otherwise changedValue
     *
     * @param <T>
     * @param originalValue
     * @param changedValue
     * @param field
     * @param failMessageParts
     * @return If {@link #testSetOnce(Object, Object, String, Object...)} false then originalValue is return, otherwise changedValue.
     */
    public static <T> T testSetOnceAndReturn(Object originalValue, Object changedValue, String field, Object... failMessageParts) {
        return testSetOnce(originalValue, changedValue, field, failMessageParts)?
            (T)changedValue : (T)originalValue;
    }
}
