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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sworddance.util.ApplicationGeneralException;
import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationInterruptedException;
import com.sworddance.util.ApplicationNullPointerException;
import com.sworddance.util.ApplicationTimeoutException;
import com.sworddance.util.StaticCallable;

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
public class FutureResultImpl<T> extends FutureTask<T> implements FutureResultImplementor<T>, FutureListenerProcessorHolder {
    private Serializable mapKey;

    /**
     * The FutureListenerProcessor is not serialized and thus serialization would break the notification mechanism.
     * However, the futureListenerProcessor is still useful for cases where serialization is not performed.
     */
    private transient FutureListenerProcessor futureListenerProcessor;
    public FutureResultImpl() {
        this(new StaticCallable<T>(null));
    }
    public FutureResultImpl(Callable<T> callable) {
        super(callable);
    }

    public Serializable getMapKey() {
        return this.mapKey;
    }
    public void setMapKey(Serializable mapKey) {
        this.mapKey= ApplicationIllegalArgumentException.testSetOnceAndReturn(this.mapKey, mapKey, "mapKey");
    }
    /**
     * Intentionally not threadsafe. Expectation is that external code will coordinate how this is set.
     */
    public void setFutureListenerProcessor(FutureListenerProcessor futureListenerProcessor) {
        this.futureListenerProcessor = futureListenerProcessor;
    }
    public FutureListenerProcessor getFutureListenerProcessor() {
        return futureListenerProcessor;
    }
    /**
     * @throws ApplicationNullPointerException if futureListener is null
     * @throws UnsupportedOperationException if {@link #getFutureListenerProcessor()} == null
     */
    public void addFutureListener(FutureListener futureListener) throws ApplicationNullPointerException {
        ApplicationNullPointerException.notNull(futureListener, "futureListener must not be null");
        if ( this.getFutureListenerProcessor() != null ) {
            this.getFutureListenerProcessor().addFutureListener(futureListener);
        } else if ( this.isSuccessful()) {
            futureListener.futureSet(this, this.getUnchecked(1L, TimeUnit.NANOSECONDS, false));
        } else if ( this.isDone()) {
            futureListener.futureSetException(this, this.getException());
        } else {
            throw new UnsupportedOperationException("Use FutureListenerProcessorMap - when the Future has not yet been set ( this enables serialization of Futures )");
        }
    }
    @Override
	public void set(T value) {
        super.set(value);
        if ( this.getFutureListenerProcessor() != null) {
            getFutureListenerProcessor().futureSet(this, value);
        }
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
	public void setException(Throwable throwable) {
        super.setException(throwable);
        if ( getFutureListenerProcessor() != null ) {
            getFutureListenerProcessor().futureSetException(this, throwable);
        }
    }
    @Override
	public T get(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException {
        try {
            return super.get(timeout, unit);
        } catch (TimeoutException exception) {
            throw new TimeoutException("waited "+timeout+unit);
        }
    }

    public T poll() {
        T value;
        if ( isSuccessful()) {
            value = getUnchecked(1, TimeUnit.NANOSECONDS, false);
        } else {
            value = null;
        }
        return value;
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
            throw new ApplicationGeneralException(e);
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
}
