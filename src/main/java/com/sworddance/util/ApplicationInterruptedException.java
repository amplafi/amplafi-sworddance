/**
 * Copyright 2006-8 by Amplafi, Inc.
 */
package com.sworddance.util;

/**
 *
 *
 */
public class ApplicationInterruptedException extends RuntimeException {

    public ApplicationInterruptedException(String msg) {
        super(msg);
    }
    public ApplicationInterruptedException(InterruptedException e) {
        ApplicationInterruptedException exception =
            new ApplicationInterruptedException(e.getMessage());
        exception.setStackTrace(e.getStackTrace());
    }
    public ApplicationInterruptedException(String msg, InterruptedException e) {
        ApplicationInterruptedException exception =
            new ApplicationInterruptedException(msg+ " " +e.getMessage());
        exception.setStackTrace(e.getStackTrace());
    }
}
