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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

<<<<<<< HEAD
import com.sworddance.util.ApplicationIllegalStateException;
=======
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
import com.sworddance.util.NotNullIterator;

/**
 * manages list of {@link FutureListener}.
 *
 * Can handle monitor another {@link Future} as a {@link FutureListener}<T> with a different expected type, and when notified this
 * object will notify its own {@link FutureListener}<V>s.
 *
 * @author patmoore
 * @param <L> The type that the monitored Future returns.
 * @param <N> The type that {@link FutureListener}s registered with this instance are expecting to receive.
 *
 */
public class FutureListenerProcessor<L,N> implements FutureListeningNotifier<L, N> {
    private CountDownLatch done = new CountDownLatch(1);
    private WeakReference<? extends Future<N>> returnedFuture;
    private WeakReference<? extends Future<L>> monitoredFuture;
    private N returnedValue;
    private Throwable throwable;
<<<<<<< HEAD
    private Exception doneStack;
=======
    private Exception e;
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    // Seems like there might be and advantage to processing in order. But maybe used LinkedHashSet? and do concurrency some other way?
    private List<WeakReference<FutureListener<N>>> listeners = new CopyOnWriteArrayList<WeakReference<FutureListener<N>>>();

    public FutureListenerProcessor() {

    }

    /**
     * Used when this {@link FutureListenerProcessor} should return the value contained in returnedFuture
     * when {@link #futureSet(Future, Object)} or
     * {@link #futureSetException(Future, Throwable)} is called.
     * @param <P>
     * @param returnedFuture
     */
    public <P extends Future<N>> FutureListenerProcessor(P returnedFuture) {
        this.setReturnedFuture(returnedFuture);
    }

    /**
     * Used when this {@link FutureListenerProcessor} should return returnedValue
     * when {@link #futureSet(Future, Object)} or
     * {@link #futureSetException(Future, Throwable)} is called.
     * @param returnedValue
     */
    public FutureListenerProcessor(N returnedValue) {
        this.setReturnedValue(returnedValue);
    }
    /**
     * @see com.sworddance.taskcontrol.FutureListener#futureSet(java.util.concurrent.Future, Object)
     */
<<<<<<< HEAD
    @SuppressWarnings({ "hiding", "unchecked" })
    public <P extends Future<L>> void futureSet(P future, L returnedValue) {
        checkDoneStateAndSaveStack();
=======
    @Override
    @SuppressWarnings({ "hiding", "unchecked" })
    public <P extends Future<L>> void futureSet(P future, L returnedValue) {
        if ( this.done.getCount() == 0 ) {
            throw new IllegalStateException(e);
        } else {
            e = new Exception();
        }
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        this.setMonitoredFuture(future);
        if ( this.returnedFuture == null ) {
            this.setReturnedFuture( (Future<N>)this.monitoredFuture.get());
        }
        if ( this.returnedFuture == null || this.returnedFuture.get() == null ) {
            // TODO maybe should catch ClassCastExceptions?
            this.setReturnedValue((N)returnedValue);
        }
        // all following threads should have the notify happen immediately
        this.done.countDown();
<<<<<<< HEAD
        for(FutureListener<N> futureListener: NotNullIterator.<FutureListener<N>>newNotNullIterator(listeners)) {
            notifyListener(futureListener);
        }
        clearReferences();
=======
        for(FutureListener<N> futureListener: new NotNullIterator<FutureListener<N>>(listeners)) {
            notifyListener(futureListener);
        }
        clear();
    }

    /**
     * once the call to {@link #futureSet(Future, Object)} or {@link #futureSetException(Future, Throwable)} has occurred,
     * there is no need to hang on to the listeners nor the monitoredFuture. They are released so they can be  gc'ed.
     */
    private void clear() {
        this.listeners.clear();
        this.listeners = null;
        this.monitoredFuture = null;
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
    }

    /**
     *
     * @see com.sworddance.taskcontrol.FutureListener#futureSetException(java.util.concurrent.Future, Throwable)
     */
<<<<<<< HEAD
    @SuppressWarnings("hiding")
    public <P extends Future<L>> void futureSetException(P future, Throwable throwable) {
        checkDoneStateAndSaveStack();
=======
    @Override
    @SuppressWarnings("hiding")
    public <P extends Future<L>> void futureSetException(P future, Throwable throwable) {
        if ( this.done.getCount() == 0 ) {
            throw new IllegalStateException(e);
        } else {
            e = new Exception();
        }
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
        this.setMonitoredFuture(future);
        this.throwable = throwable;
        // all following threads should have the notify happen immediately
        this.done.countDown();
<<<<<<< HEAD
        for(FutureListener<N> futureListener: NotNullIterator.<FutureListener<N>>newNotNullIterator( listeners)) {
            notifyListenerException(futureListener);
        }
        clearReferences();
    }


    /**
     *
     */
    private void checkDoneStateAndSaveStack() {
        ApplicationIllegalStateException.checkState(!this.isDone(), doneStack);
        doneStack = new Exception("Previously set future value from this location:");
    }

    /**
     * once the call to {@link #futureSet(Future, Object)} or {@link #futureSetException(Future, Throwable)} has occurred,
     * there is no need to hang on to the listeners nor the monitoredFuture. They are released so they can be  gc'ed.
     */
    private void clearReferences() {
        this.listeners.clear();
        this.listeners = null;
        this.monitoredFuture = null;
    }

    public void addFutureListener(FutureListener<N> futureListener) {
        if ( isDone()) {
=======
        for(FutureListener<N> futureListener: new NotNullIterator<FutureListener<N>>( listeners)) {
            notifyListenerException(futureListener);
        }
        clear();
    }


    public void addFutureListener(FutureListener<N> futureListener) {
        if ( this.done.getCount() == 0) {
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
            if ( this.throwable == null) {
                notifyListener(futureListener);
            } else {
                notifyListenerException(futureListener);
            }
        } else {
            this.listeners.add(new WeakReference<FutureListener<N>>(futureListener));
        }
    }

    /**
<<<<<<< HEAD
     * @return this listener has completed.
     */
    public boolean isDone() {
        return this.done.getCount() == 0;
    }

    /**
=======
>>>>>>> d9837c1bd14d3b3a2b0822f0efefa4e4cda50970
     * @param futureListener
     */
    private void notifyListener(FutureListener<N> futureListener) {
        N value = null;
        try {
            if ( this.returnedValue != null ) {
                value = this.returnedValue;
            }
            if ( value == null && getReturnedFuture() != null) {
                value = getReturnedFuture().get(1, TimeUnit.NANOSECONDS);
            }
        } catch (Exception e) {
            // HACK need to handle exceptions.
        }
        try {
            futureListener.futureSet(getReturnedFuture(), value);
        } catch (Exception e) {
            // HACK need to handle exceptions.
        }
    }

    /**
     * @param futureListener
     */
    private void notifyListenerException(FutureListener<N> futureListener) {
        try {
            futureListener.futureSetException(getReturnedFuture(), throwable);
        } catch (Exception e) {
            // HACK need to handle exceptions.
        }
    }
    @SuppressWarnings("unchecked")
    private Future<N> getReturnedFuture() {
        if ( this.returnedFuture == null ) {
            return (Future<N>) this.monitoredFuture.get();
        } else {
            return this.returnedFuture.get();
        }
    }
    /**
     * @param returnedFuture
     */
    private <P extends Future<L>> void setMonitoredFuture(P future) {
        if ( this.monitoredFuture == null ) {
            this.monitoredFuture = new WeakReference<P>(future);
        }
    }
    /**
     * @param returnedFuture
     */
    private <P extends Future<N>> void setReturnedFuture(P returnedFuture) {
        if ( this.returnedFuture == null ) {
            this.returnedFuture = new WeakReference<P>(returnedFuture);
        }
    }

    private void setReturnedValue(N returnedValue) {
        if ( this.returnedValue == null ) {
            this.returnedValue =returnedValue;
        } else {
            // throw exception? except that for some cases the returned value has been set in the constructor.
        }
    }
}
