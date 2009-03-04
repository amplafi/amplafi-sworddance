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
