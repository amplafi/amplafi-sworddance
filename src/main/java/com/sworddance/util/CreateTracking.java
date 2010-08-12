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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.StringUtils.*;


/**
 * Simple class to track where an object was created.
 * @author Patrick Moore
 */
public class CreateTracking implements Serializable {
    private static final boolean ENABLE;
    private static final List<String> classNames;
    private String createStr;
    private Exception e;
    static {
        String property = System.getProperty("create-tracking");
        ENABLE = Boolean.parseBoolean(property);
        classNames = new ArrayList<String>();
        if ( !ENABLE && isNotBlank(property)) {
            String[] classNamesArr = property.split("\\s*,\\s*");
            Collections.addAll(classNames,classNamesArr);
        }
    }
    public CreateTracking() {
        e = new Exception(new Date().toString());
    }

    public static CreateTracking getInstance() {
        return getInstance(null, null);
    }
    public static CreateTracking getInstance(Class<?> clazz) {
        return getInstance(null, clazz);
    }
    public static CreateTracking getInstance(Boolean create, Class<?> clazz) {
        if ( create == null ) {
            create = ENABLE;
        }
        if ( !create && clazz != null) {
            create = classNames.contains( clazz.getName());
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
