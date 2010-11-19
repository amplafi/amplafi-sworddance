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

import java.util.List;
import java.util.regex.MatchResult;

/**
 * @author patmoore
 *
 */
public interface ExtendedMatchResult extends MatchResult {
    /**
     *
     * @return an array of all the results from {@link #group(int)}
     */
    List<String> groups();
    /**
     * an array of all the results from {@link #group(int)} except for group(0) which is the entire input string matched.
     * Note that some elements maybe null.
     * @return the {@link #group(int)} results
     */
    List<String> parts();
}
