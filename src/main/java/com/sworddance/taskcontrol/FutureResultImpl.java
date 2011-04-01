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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sworddance.util.ApplicationGeneralException;
import com.sworddance.util.ApplicationInterruptedException;
import com.sworddance.util.ApplicationTimeoutException;

/**
 * add some convenience to the {@link FutureTask} class.
 *
 * TODO:
 * FutureResult should have 3 possible callables:
 *
 *     * call on success
 *     * call on failure
 *     * call when result (success or failure)
 *
 * In this way when the future has a result it is able to proactive trigger the next action.
 * @param <T> type of value returned by this {@link Future}.
 * @author Patrick Moore
 */
public class FutureResultImpl<T> extends FutureTask<T> implements FutureResultImplementor<T> {

    private final FutureListenerProcessor<T, ?> processor;

    public FutureResultImpl() {
        this(new NullCallable<T>(), new FutureListenerProcessor<T,T>());
    }
    public FutureResultImpl(Callable<T> callable) {
        this(callable, new FutureListenerProcessor<T,T>());
    }
    public FutureResultImpl(Callable<T> callable, FutureListenerProcessor<T, ?> processor) {
        super(callable);
        this.processor = processor;
    }
    public void addFutureListener(FutureListener futureListener) {
        processor.addFutureListener(futureListener);
    }
    @Override
	public void set(T value) {
        super.set(value);
        processor.futureSet(this, value);
    }
    public Throwable getException() {
        if ( super.isDone()) {
            try {
                // just to trigger the exception to be thrown.
                @SuppressWarnings("unused")
                Object o = super.get(1, TimeUnit.NANOSECONDS);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                return cause;
            } catch (InterruptedException e) {
                return e;
            } catch(TimeoutException e) {
                return null;
            }
        }
        return null;
    }
    /**
     * make the super class method visible.
     * @see java.util.concurrent.FutureTask#setException(java.lang.Throwable)
     */
    @Override
	public void setException(Throwable e) {
        super.setException(e);
        processor.futureSetException(this, e);
    }
    @Override
	public T get(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException {
        try {
            return super.get(timeout, unit);
        } catch (TimeoutException exception) {
            throw new TimeoutException("waited "+timeout+unit.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public T getUnchecked(long timeout, TimeUnit unit, boolean returnNullIfTimeout) {
        try {
            return get(timeout, unit);
        } catch (ExecutionException e) {
            return (T)doTimeoutBehavior(returnNullIfTimeout, e);
        } catch (TimeoutException e) {
            return (T)doTimeoutBehavior(returnNullIfTimeout, e);
        } catch (InterruptedException e) {
            throw new ApplicationInterruptedException(e);
        }
    }

    /**
     * @param returnNullIfTimeout
     * @param e
     * @return null if returnNullIfTimeout == true and e is {@link TimeoutException} or {@link ApplicationTimeoutException}, otherwise no timeout as exception is thrown.
     */
    protected Object doTimeoutBehavior(boolean returnNullIfTimeout, Throwable e) throws ApplicationTimeoutException, ApplicationGeneralException {
        Throwable t = e; // TODO: move Defense to sworddance
        if (t instanceof TimeoutException || t instanceof ApplicationTimeoutException) {
            if (returnNullIfTimeout) {
                return null;
            } else {
                throw new ApplicationTimeoutException(e);
            }
        } else {
            throw new ApplicationGeneralException(e); //            throw rethrow(e);
        }
    }
    /**
     * Note that ! isFailed() != {@link #isSuccessful()} because the request may not be done.
     * @return {@link #isDone()} && ! {@link #isCancelled()} &&
     *  {@link FutureResult#getException()}==null
     */
    public boolean isSuccessful() {
        return isDone() && !isCancelled() && getException() == null;
    }

    /**
     * Note that ! isFailed() != {@link #isSuccessful()} because the request may not be done.
     * @return {@link #isDone()} && (! {@link #isCancelled()} ||
     *  {@link FutureResult#getException()}!=null)
     */
    public boolean isFailed() {
        return isDone() && (isCancelled() || getException() != null);
    }

    public static class NullCallable<T> implements Callable<T> {
        @SuppressWarnings("unused")
        public T call() throws Exception {
            return null;
        }};
}
