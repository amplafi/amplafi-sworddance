/*
 * Created on Jan 25, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.util;

import static org.apache.commons.lang.StringUtils.join;

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
        super(join(failMessage));
    }

}
