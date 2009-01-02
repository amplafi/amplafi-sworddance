/*
 * Created on Dec 31, 2006
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.taskcontrol;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * add some convenience to the {@link FutureTask} class.
 * @param <T> type of value returned by this {@link Future}.
 * @author Patrick Moore
 */
public class FutureResult<T> extends FutureTask<T> implements FutureListenerNotifier {

    private final FutureListenerProcessor<T, ?> processor;
    public FutureResult() {
        this(new FutureListenerProcessor<T,T>());
    }
    public FutureResult(FutureListenerProcessor<T, ?> processor) {
        super(new Callable<T>() {
            @SuppressWarnings("unused")
            public T call() throws Exception {
                return null;
            }});
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
        try {
            // just to trigger the exception to be thrown.
            @SuppressWarnings("unused")
            Object o = super.get(1, TimeUnit.NANOSECONDS);
            return null;
        } catch (ExecutionException e) {
            return e.getCause();
        } catch (InterruptedException e) {
            return e;
        } catch(TimeoutException e) {
            return null;
        }
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
    /**
     *
     * @return null if ! {@link #isDone()} otherwise
     * ! {@link #isCancelled()} &&
     *  {@link FutureResult#getException()}==null
     */
    public Boolean getSuccessState() {
        if ( isDone()) {
            return !isCancelled() && getException()== null;
        } else {
            return null;
        }
    }
}
