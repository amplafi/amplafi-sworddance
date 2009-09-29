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

import java.security.SecureRandom;
import java.util.Random;

/**
 * This utility class can be used to generate short but random strings.
 *
 * Strings are case insensitive and contain only characters [a-z1-9]
 *
 *
 */
public class RandomKeyGenerator {

    private Random rnd = new SecureRandom();

    private static final char[] charTable;

    public int defaultLength;
    public RandomKeyGenerator(int defaultLength) {
        this.defaultLength = defaultLength;
    }

    /**
     *
     * @return next key with length of 7 symbols.
     */
    public String nextKey() {
        return nextKey(this.defaultLength);
    }

    /**
     * @param length
     * @return a key with specified length.
     */
    public String nextKey(int length) {
        StringBuilder result = new StringBuilder();
        for(int i=0;i<length;i++) {
            result.append(charTable[rnd.nextInt(charTable.length)]);
        }
        return result.toString();
    }

    /**
     * @return cached table with allowed characters.
     */
    static {
        charTable = new char[35];
        int cursor = 0;
        for (int i = 'a'; i <= 'z'; i++) {
            charTable[cursor++] = (char) i;
        }
        for(int i='1'; i <= '9'; i++) {
            charTable[cursor++] = (char) i;
        }
    }

}
