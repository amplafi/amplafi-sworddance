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
