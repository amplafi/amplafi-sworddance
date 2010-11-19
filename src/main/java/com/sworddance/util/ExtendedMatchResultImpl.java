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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

/**
 * @author patmoore
 *
 */
public class ExtendedMatchResultImpl implements ExtendedMatchResult {

    private MatchResult matchResult;
    private List<String> groups;
    private ArrayList<String> parts;
    public ExtendedMatchResultImpl(MatchResult matchResult) {
        this.matchResult = matchResult;
        this.groups = new ArrayList<String>();
        int groupCount = this.matchResult.groupCount();
        this.groups.add(this.matchResult.group(0));
        this.parts = new ArrayList<String>();
        for(int group = 1; group < groupCount; group++) {
            this.groups.add(this.matchResult.group(group));
            this.parts.add(this.matchResult.group(group));
        }
    }
    public static List<ExtendedMatchResult> toExtendedMatchResult(Matcher matcher) {
        List<ExtendedMatchResult> result = new ArrayList<ExtendedMatchResult>();
        while(matcher.find()) {
            result.add(new ExtendedMatchResultImpl(matcher.toMatchResult()));
        }
        return result;
    }


    public int end() {
        return this.matchResult.end();
    }
    public int end(int group) {
        return this.matchResult.end(group);
    }
    public String group() {
        return this.matchResult.group();
    }
    public String group(int group) {
        return this.matchResult.group(group);
    }
    public int groupCount() {
        return this.matchResult.groupCount();
    }
    public int start() {
        return this.matchResult.start();
    }
    public int start(int group) {
        return this.matchResult.start(group);
    }

    /**
     * @see com.sworddance.util.ExtendedMatchResult#groups()
     */
    public List<String> groups() {
        return this.groups;
    }

    /**
     * @see com.sworddance.util.ExtendedMatchResult#parts()
     */
    public List<String> parts() {
        return this.parts;
    }
    @Override
    public String toString() {
        return this.matchResult.toString();
    }
}
