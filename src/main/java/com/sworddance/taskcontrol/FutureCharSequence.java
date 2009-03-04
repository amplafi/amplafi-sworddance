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
package com.sworddance.taskcontrol;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Denis Rogov
 */
public abstract class FutureCharSequence implements CharSequence, Future<CharSequence>, Serializable {

    /**
     * the result was set or has been determined already.
     */
    protected CharSequence loadedCharSequence;

    protected FutureCharSequence() {
    }

    protected FutureCharSequence(CharSequence loadedCharSequence) {
        this.loadedCharSequence = loadedCharSequence;
    }

    public char charAt(int index) {
        CharSequence seq = getSeq();
        return seq.charAt(index);
    }

    public int length() {
        CharSequence seq = getSeq();
        return seq.length();
    }

    public CharSequence subSequence(int start, int end) {
        CharSequence seq = getSeq();
        return seq.subSequence(start, end);
    }

    protected CharSequence getSeq() {
        try {
            return get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        if (isDone()) {
            try {
                return get(0, SECONDS).toString();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return super.toString();
        }
    }
}
