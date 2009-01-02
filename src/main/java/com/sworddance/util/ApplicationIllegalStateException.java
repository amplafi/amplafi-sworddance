/*
 * Created on Sep 10, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.util;

import static org.apache.commons.lang.StringUtils.join;

public class ApplicationIllegalStateException extends IllegalStateException {

    private static final long serialVersionUID = -7161961478084657672L;

    public ApplicationIllegalStateException() {
    }

    public ApplicationIllegalStateException(String s) {
        super(s);
    }

    public ApplicationIllegalStateException(Object... messages) {
        super(join(messages));
    }

    public ApplicationIllegalStateException(Throwable cause) {
        super(cause);
    }

    public ApplicationIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

}
