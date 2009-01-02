/**
 * Copyright 2006-8 by Amplafi, Inc.
 */
package com.sworddance.util;
/**
 * wraps a {@link java.util.concurrent.TimeoutException} as a runtime exception
 *
 */
public class ApplicationTimeoutException extends ApplicationIllegalStateException {

    /**
     *
     */
    public ApplicationTimeoutException() {
    }

    /**
     * @param s
     */
    public ApplicationTimeoutException(String s) {
        super(s);
    }

    /**
     * @param cause
     */
    public ApplicationTimeoutException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ApplicationTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
