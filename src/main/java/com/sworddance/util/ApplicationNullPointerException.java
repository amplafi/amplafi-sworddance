/*
 * Created on Jan 25, 2007
 * Copyright 2006 by Patrick Moore
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
