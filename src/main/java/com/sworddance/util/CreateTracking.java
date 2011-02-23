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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.StringUtils.*;


/**
 * Simple class to track where an object was created.
 * @author Patrick Moore
 */
public class CreateTracking implements Serializable {
    /**
     * set to a comma separated list of class names. Class names may be full class names, regex or just the simple class names ( class name without the package ).
     *
     * If set to true then all objects are tracked.
     */
    public static final String CREATE_TRACKING_SYSTEM_PROPERTY = "create-tracking";
    private static boolean CREATE_TRACK_ALL;
    /**
     * Set by "create-tracking" system property.
     */
    private static final List<String> classNames = new ArrayList<String>();
    private static Pattern classNameMatching;
    private static final Map<Class, Boolean> createTracking = new ConcurrentHashMap<Class,Boolean>();
    private String createStr;
    private Exception e;

    static {
        String property = System.getProperty(CREATE_TRACKING_SYSTEM_PROPERTY);
        initialize(property);
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
            create = CREATE_TRACK_ALL;
        }
        if ( !create && clazz != null && classNameMatching != null) {
            create = createTracking.get(clazz);
            if ( create == null ) {
                // do not yet know for this class
                Matcher matcher = classNameMatching.matcher(clazz.getName());
                create = Boolean.valueOf(matcher.find());
                createTracking.put(clazz, create);
            }
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
    public static void initialize(String propertySetting) {
        CREATE_TRACK_ALL = Boolean.parseBoolean(propertySetting);
        classNames.clear();
        createTracking.clear();
        classNameMatching = null;
        if ( !CREATE_TRACK_ALL && isNotBlank(propertySetting)) {
            StringBuilder patternBuilder = new StringBuilder();
            String[] classNamesArr = propertySetting.split(",");
            for(String className: NotNullIterator.<String>newNotNullIterator(classNamesArr)) {
                if ( StringUtils.isNotBlank(className)) {
                    String trimmedClassName = className.trim();
                    if ( trimmedClassName.indexOf('.') < 0 ) {
                        // no package name so probably simple name.
                        // therefore look for any package. ( leading . or $ for inner class )
                        patternBuilder.append("(?:.*(?:\\.|\\$))").append(trimmedClassName);
                        patternBuilder.append("|");
                    }
                    patternBuilder.append(trimmedClassName);
                    classNames.add(trimmedClassName);
                }
            }
            classNameMatching = Pattern.compile(patternBuilder.toString());
        }
    }
}
