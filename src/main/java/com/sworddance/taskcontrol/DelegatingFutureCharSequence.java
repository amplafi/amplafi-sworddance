/*
 * Created on Jan 24, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.taskcontrol;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * this represents a {@link CharSequence} that may not be presently available but is being
 * retreived.
 *
 * @author Patrick Moore
 */
public class DelegatingFutureCharSequence extends FutureCharSequence {

    private static final long serialVersionUID = -3012235398479001750L;
    
    private Future<CharSequence> pendingCharSequence;

    /**
     * @param loadedCharSequence the data has already been loaded.
     */
    public DelegatingFutureCharSequence(CharSequence loadedCharSequence) {
        super(loadedCharSequence);
    }

    /**
     * The data is in the process of loading.
     *
     * @param pendingCharSequence the future to monitor that will produce the data.
     */
    public DelegatingFutureCharSequence(Future<CharSequence> pendingCharSequence) {
        this.pendingCharSequence = pendingCharSequence;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.loadedCharSequence == null && this.pendingCharSequence.cancel(mayInterruptIfRunning);
    }

    public CharSequence get() throws InterruptedException, ExecutionException {
        if (this.loadedCharSequence == null) {
            this.loadedCharSequence = this.pendingCharSequence.get();
        }
        return this.loadedCharSequence;
    }

    public CharSequence get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (this.loadedCharSequence == null) {
            this.loadedCharSequence = this.pendingCharSequence.get(timeout, unit);
        }
        return this.loadedCharSequence;
    }

    public boolean isCancelled() {
        return this.loadedCharSequence == null && this.pendingCharSequence.isCancelled();
    }

    public boolean isDone() {
        return this.loadedCharSequence != null || this.pendingCharSequence.isDone();
    }
}
