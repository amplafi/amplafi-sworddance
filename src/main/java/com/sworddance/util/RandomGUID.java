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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 *
 * {@link RandomGUID} will return a randomize UUID string.
 * Can be used while creating a file whitch name must be unique.
 * Useful for testing and temporary files.
 *
 */
public class RandomGUID {

    private String valueBeforeMD5 = "";

    private String valueAfterMD5 = "";

    private static Random MyRand;

    private static SecureRandom MySecureRand;

    private static String SId;

    static {
        MySecureRand = new SecureRandom();
        long secureInitializer = MySecureRand.nextLong();
        MyRand = new Random(secureInitializer);
        try {
            SId = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public RandomGUID() throws IOException {
        getRandomGUID(false);
    }

    public RandomGUID(boolean secure) throws IOException {
        getRandomGUID(secure);
    }

    private void getRandomGUID(boolean secure) throws IOException {
        MessageDigest md5 = null;
        StringBuffer sbValueBeforeMD5 = new StringBuffer();

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }

        try {
            long time = System.currentTimeMillis();
            long rand = 0;

            if (secure) {
                rand = MySecureRand.nextLong();
            } else {
                rand = MyRand.nextLong();
            }

            // This StringBuffer can be a long as you need; the MD5
            // hash will always return 128 bits. You can change
            // the seed to include anything you want here.
            // You could even stream a file through the MD5 making
            // the odds of guessing it at least as great as that
            // of guessing the contents of the file!
            sbValueBeforeMD5.append(SId);
            sbValueBeforeMD5.append(":");
            sbValueBeforeMD5.append(Long.toString(time));
            sbValueBeforeMD5.append(":");
            sbValueBeforeMD5.append(Long.toString(rand));

            valueBeforeMD5 = sbValueBeforeMD5.toString();
            md5.update(valueBeforeMD5.getBytes());

            byte[] array = md5.digest();
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < array.length; ++j) {
                int b = array[j] & 0xFF;
                if (b < 0x10){
                    sb.append('0');
                }
                sb.append(Integer.toHexString(b));
            }

            valueAfterMD5 = sb.toString();

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public String toString() {
        String raw = valueAfterMD5.toUpperCase();
        StringBuffer sb = new StringBuffer();
        sb.append(raw.substring(0, 8));
        sb.append(raw.substring(8, 12));
        sb.append(raw.substring(12, 16));
        sb.append(raw.substring(16, 20));
        sb.append(raw.substring(20));

        return sb.toString();
    }

    public String getValueBeforeMD5() {
        return valueBeforeMD5;
    }

    public void setValueBeforeMD5(String valueBeforeMD5) {
        this.valueBeforeMD5 = valueBeforeMD5;
    }

    public String getValueAfterMD5() {
        return valueAfterMD5;
    }

    public void setValueAfterMD5(String valueAfterMD5) {
        this.valueAfterMD5 = valueAfterMD5;
    }
}
