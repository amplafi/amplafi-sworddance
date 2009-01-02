/*
 * Created on May 2, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.util;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;

/**
 * Simple class to track where an object was created.
 * @author Patrick Moore
 */
public class CreateTracking implements Serializable {
    public static final boolean ENABLE = Boolean.getBoolean("create-tracking");
    private String createStr;
    private Exception e;
    public CreateTracking() {
        e = new Exception(new Date().toString());
    }

    public static CreateTracking getInstance() {
        return getInstance(null);
    }
    public static CreateTracking getInstance(Boolean create) {
        if ( create == null ) {
            create = ENABLE;
        }
        return create?new CreateTracking():null;
    }

    @Override
    public String toString() {
        if ( this.createStr == null ) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.createStr = sw.toString();
        }
        return this.createStr;
    }
}
