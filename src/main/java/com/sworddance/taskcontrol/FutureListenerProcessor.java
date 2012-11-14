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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sworddance.core.Emptyable;
import com.sworddance.util.ApplicationIllegalStateException;
import com.sworddance.util.CUtilities;
import com.sworddance.util.NotNullIterator;

/**
 * manages list of {@link FutureListener}.
 *
 * Can handle monitor another {@link Future} as a {@link FutureListener}<T> with a different expected type, and when notified this
 * object will notify its own {@link FutureListener}<V>s. See use of {@link #monitoredFuture}.
 *
 * @author patmoore
 * @param <MV> The type that the monitored Future returns.
 * @param <RV> The type that {@link FutureListener}s registered with this instance are expecting to receive.
 *
 */
public class FutureListenerProcessor<MV,RV> implements FutureListeningNotifier<MV, RV>, Emptyable {
    private Log log = LogFactory.getLog(FutureListenerProcessor.class);
	private final ReentrantReadWriteLock rwListenersLock = new ReentrantReadWriteLock();
	private final Lock readListenersLock = rwListenersLock.readLock();
	private final Lock writeListenersLock = rwListenersLock.writeLock();
    private CountDownLatch done = new CountDownLatch(1);
    /**
     * Weak reference because Future may be serialized and the "forgotten"
     */
    private WeakReference<? extends Future<RV>> returnedFuture;
    /**
     * Used in chained situation where the {@link #returnedFuture} is dependent on the {@link #monitoredFuture}
     * For example, 'F1' is a future that depends on future 'F2', F1 has {@link FutureListener}s that are expecting F1 to be passed
     * in the F1 futureSet/futureSetException() methods.
     * If
     */
    private WeakReference<? extends Future<MV>> monitoredFuture;
    private RV returnedValue;
    private Throwable throwable;
    private Exception doneStack;
    // Seems like there might be and advantage to processing in order. But maybe used LinkedHashSet? and do concurrency some other way?
    private transient List<WeakReference<FutureListener<RV>>> listeners = new CopyOnWriteArrayList<WeakReference<FutureListener<RV>>>();

    public FutureListenerProcessor() {

    }

    /**
     * Used when this {@link FutureListenerProcessor} should return the value contained in returnedFuture
     * when {@link #futureSet(Future, Object)} or
     * {@link #futureSetException(Future, Throwable)} is called.
     * quickie refactoring.
     * @param <MV>
     * @param <RV>
     * @param <RF>
     * @param <MF>
     * @param returnedFuture
     * @param monitoredFuture
     * @return the chained FutureListenerProcessor
     */
    public static <MV, RV, RF extends Future<RV>, MF extends FutureListenerProcessorHolder> FutureListenerProcessor<MV, RV> createChainedFutureListenerProcessor(RF returnedFuture, MF monitoredFuture) {
        FutureListenerProcessor<MV,RV> futureListenerProcessor = new FutureListenerProcessor<MV, RV>();
        futureListenerProcessor.setReturnedFuture(returnedFuture);
        return chainFutureListenerProcessor(futureListenerProcessor, monitoredFuture);
    }
    public static <MV, RV, MF extends FutureListenerProcessorHolder> FutureListenerProcessor<MV, RV> chainFutureListenerProcessor(FutureListenerProcessor<MV, RV> futureListenerProcessor, MF monitoredFuture) {
        if ( monitoredFuture.getFutureListenerProcessor() == null) {
            // quickie HACK - assuming that monitoredFuture itself is not chained.
            monitoredFuture.setFutureListenerProcessor(new FutureListenerProcessor());
        }
        monitoredFuture.getFutureListenerProcessor().addFutureListener(futureListenerProcessor);
        return futureListenerProcessor;
    }

    /**
     * @see com.sworddance.taskcontrol.FutureListener#futureSet(java.util.concurrent.Future, Object)
     */
    @SuppressWarnings({ "hiding", "unchecked" })
    public <P extends Future<MV>> void futureSet(P future, MV returnedValue) {
    	checkDoneStateAndSaveStack();
    	this.readListenersLock.lock();
    	try {
	        this.setMonitoredFuture(future);
	        if ( this.returnedFuture == null ) {
	            // if monitored future already set then the returnedFuture != future.
	            this.setReturnedFuture( (Future<RV>)this.monitoredFuture.get());
	        }
	        if ( this.returnedFuture == null || this.returnedFuture.get() == null ) {
	            // TODO maybe should catch ClassCastExceptions?
	            this.setReturnedValue((RV)returnedValue);
	        }
	        // all following threads should have the notify happen immediately
	        this.done.countDown();
	        for(FutureListener<RV> futureListener: NotNullIterator.<FutureListener<RV>>newNotNullIterator(listeners)) {
	            notifyListener(futureListener);
	        }
    	} finally {
    		this.readListenersLock.unlock();
    	}
    	clear();
    }

    /**
     *
     * @see com.sworddance.taskcontrol.FutureListener#futureSetException(java.util.concurrent.Future, Throwable)
     */
    @SuppressWarnings("hiding")
    public <P extends Future<MV>> void futureSetException(P future, Throwable throwable) {
    	checkDoneStateAndSaveStack();
    	this.readListenersLock.lock();
    	try {
	        this.setMonitoredFuture(future);
	        this.throwable = throwable;
	        // all following threads should have the notify happen immediately
	        this.done.countDown();
	        for(FutureListener<RV> futureListener: NotNullIterator.<FutureListener<RV>>newNotNullIterator( listeners)) {
	            notifyListenerException(futureListener);
	        }
    	} finally {
    		this.readListenersLock.unlock();
    	}
    	clear();
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
     *
     * We hold on only to the returnedFuture ( maybe only the returned result ? )
     */
    public void clear() {
    	this.writeListenersLock.lock();
    	try {
    		if ( this.listeners != null) {
		        this.listeners.clear();
		        this.listeners = null;
    		}
    		this.monitoredFuture = null;
	    } finally {
	    	this.writeListenersLock.unlock();
	    }
    }

    public boolean isEmpty() {
        return CUtilities.isEmpty(this.listeners);
    }
    public void addFutureListener(FutureListener<RV> futureListener) {
    	if ( isDone()) {
    		if ( this.throwable == null) {
    			notifyListener(futureListener);
    		} else {
    			notifyListenerException(futureListener);
    		}
    	} else {
    		this.writeListenersLock.lock();
    		try {
    		    WeakReference<FutureListener<RV>> listener = new WeakReference<FutureListener<RV>>(futureListener);
    		    if( !this.listeners.contains(listener)) {
    		        this.listeners.add(listener);
    		    }
    		} finally {
    			this.writeListenersLock.unlock();
    		}
    	}
    }
    /**
     * @return this listener has completed.
     */
    public boolean isDone() {
        return this.done.getCount() == 0;
    }

    /**
     * @param futureListener
     */
    private void notifyListener(FutureListener<RV> futureListener) {
        RV value = null;
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
            // HACK need to handle exceptions. but don't want to interfere with other listeners
            getLog().warn("while doing futureSet", e);
        }
    }

    /**
     * @param futureListener
     */
    private void notifyListenerException(FutureListener<RV> futureListener) {
        try {
            futureListener.futureSetException(getReturnedFuture(), throwable);
        } catch (Exception e) {
            // HACK need to handle exceptions. but don't want to interfere with other listeners
            getLog().warn("while doing futureSetException", e);
        }
    }
    /**
     * If {@link #returnedFuture} is set then return the {@link #returnedFuture}.get() ( which maybe a null future if the future was gc'ed)
     * @return the future that is passed to the {@link FutureListener#futureSet(Future, Object)} and {@link FutureListener#futureSetException(Future, Throwable)}
     */
    @SuppressWarnings("unchecked")
    private Future<RV> getReturnedFuture() {
        if ( this.returnedFuture == null ) {
            return (Future<RV>) this.monitoredFuture.get();
        } else {
            return this.returnedFuture.get();
        }
    }
    /**
     * Sets the future being monitor for being set to #future
     * @param future
     */
    private <P extends Future<MV>> void setMonitoredFuture(P future) {
        if ( this.monitoredFuture == null ) {
            this.monitoredFuture = new WeakReference<P>(future);
        }
    }
    /**
     * @param returnedFuture
     */
    public <P extends Future<RV>> void setReturnedFuture(P returnedFuture) {
        if ( this.returnedFuture == null ) {
            this.returnedFuture = new WeakReference<P>(returnedFuture);
        }
    }

    private void setReturnedValue(RV returnedValue) {
        if ( this.returnedValue == null ) {
            this.returnedValue =returnedValue;
        } else {
            // throw exception? except that for some cases the returned value has been set in the constructor.
        }
    }
    public Log getLog() {
        return log;
    }
}
