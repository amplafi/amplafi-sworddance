/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential. 
 */
package com.sworddance.util;

/**
 * @author patmoore
 *
 */
public class ApplicationGeneralException extends RuntimeException {

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

}
