package com.sworddance.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * This utility class can be used to generate short but random strings.
 *
 * Strings are case insensitive and contain only characters [A-Z1-9]
 *
 *
 */
public class RandomKeyGenerator {

    private Random rnd = new SecureRandom();

    private char[] charTable;

    /**
     *
     * @return next key with length of 7 symbols.
     */
    public String nextKey() {
        return nextKey(7);
    }

    /**
     * @param length
     * @return a key with specified length.
     */
    public String nextKey(int length) {
        char[] table = getCharacterTable();
        StringBuffer result = new StringBuffer();
        for(int i=0;i<length;i++) {
            result.append(table[rnd.nextInt(table.length)]);
        }
        return result.toString();
    }

    /**
     * @return cached table with allowed characters.
     */
    private char[] getCharacterTable() {
        if (charTable == null) {
            charTable = new char[35];
            int cursor = 0;
            for (int i = 'a'; i <= 'z'; i++) {
                charTable[cursor++] = (char) i;
            }
            for(int i='1'; i <= '9'; i++) {
                charTable[cursor++] = (char) i;
            }
        }
        return charTable;
    }

}
