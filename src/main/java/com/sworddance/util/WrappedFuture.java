package com.sworddance.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sworddance.taskcontrol.FutureResult;

/**
 * Handle issue of multiple Futures that really are pointing to the same result.
 * Useful when it is discovered after the WrappedFuture is created that it really refers to the same object as another future.
 * Example, URIs when following http redirects.
 *
 */
public class WrappedFuture extends FutureResult<Object> {
    private CountDownLatch executeLatch;
    private FutureResult<Object> actualResult;

    public WrappedFuture(CountDownLatch executeLatch) {
        this.executeLatch = executeLatch;
    }

    public void setActualResult(FutureResult<Object> actualResult) {
        this.actualResult = actualResult;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if ( actualResult != null ) {
            return actualResult.cancel(mayInterruptIfRunning);
        } else {
            return super.cancel(mayInterruptIfRunning);
        }
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        executeLatch.await();
        if ( actualResult != null ) {
            return actualResult.get();
        } else {
            return super.get();
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (!executeLatch.await(timeout, unit)) {
            throw new TimeoutException();
        } else if ( actualResult != null ) {
            return actualResult.get(timeout, unit);
        } else {
            return super.get(timeout, unit);
        }
    }

    @Override
    public boolean isCancelled() {
        try {
            if (!executeLatch.await(0, TimeUnit.NANOSECONDS)) {
                return false;
            } else if ( actualResult != null ){
                return actualResult.isCancelled();
            } else {
                return super.isCancelled();
            }
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean isDone() {
        try {
            if (!executeLatch.await(0, TimeUnit.NANOSECONDS)) {
                return false;
            } else if (actualResult != null ){
                return actualResult.isDone();
            } else {
                return super.isDone();
            }
        } catch (InterruptedException e) {
            return false;
        }
    }
    @Override
    public void set(Object value) {
        if ( actualResult != null ) {
            actualResult.set(value);
        } else {
            super.set(value);
        }
    }
    @Override
    public Throwable getException() {
        if ( actualResult != null ) {
            return actualResult.getException();
        } else {
            return super.getException();
        }
    }
    /**
     * make the super class method visible.
     * @see java.util.concurrent.FutureTask#setException(java.lang.Throwable)
     */
    @Override
    public void setException(Throwable e) {
        if ( actualResult != null ) {
            actualResult.setException(e);
        } else {
            super.setException(e);
        }
    }
}