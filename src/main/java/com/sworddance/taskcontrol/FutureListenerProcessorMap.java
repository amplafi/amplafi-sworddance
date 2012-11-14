package com.sworddance.taskcontrol;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sworddance.util.ApplicationIllegalStateException;
import com.sworddance.util.map.ConcurrentInitializedMap;
import com.sworddance.util.map.MapKeyed;

/**
 * {@link FutureListenerProcessor}s are not serializable because they represent business logic that needs to be triggered when a
 * future is set.
 * Because they are not serializable, the {@link FutureListenerProcessor}s are held external to the {@link FutureResult}s.
 *
 * This allows FutureResults to be serialized and the deserialized with the listeners restored.
 *
 * This allows Futures to be serialized to disk and later restored without breaking the notification mechanism.
 *
 * This is a temporary solution because it does not handle case where there is a chain of {@link FutureListenerProcessor}s.
 * (Would really like there to be a library that handles serializing, listening on Futures.)
 *
 * @author patmoore
 *
 */
public class FutureListenerProcessorMap {
    Map<Object, FutureListenerProcessor<?, ?>> futureListenerProcessorMap = new ConcurrentInitializedMap<Object, FutureListenerProcessor<?,?>>(FutureListenerProcessor.class);

    /**
     * Used when it is o.k. to wrap the FutureResult
     * @param futureResultImplementor
     * @return the futureResultImplementor if it is done otherwise FutureResultWrapper wrapping it.
     */
    public FutureResultImplementor wrap(FutureResultImplementor futureResultImplementor) {
        if ( !futureResultImplementor.isDone() ) {
            return new FutureResultWrapper(futureResultImplementor);
        } else {
            if (this.futureListenerProcessorMap.containsKey(futureResultImplementor)) {
                // existing FutureListenerProcessor: need to notify existing FutureListeners
                FutureListenerProcessor futureListenerProcessor = this.futureListenerProcessorMap.get(futureResultImplementor);
                // TODO : HACK : investigate what is to happen here.
            }
            return futureResultImplementor;
        }
    }
    public FutureListenerProcessor getFutureListenerProcessor(Object key) {
        if ( key instanceof MapKeyed<?>) {
            key = ((MapKeyed<?>)key).getMapKey();
        }
        return this.futureListenerProcessorMap.get(key);
    }

    public boolean containsKey(Object key) {
        return this.futureListenerProcessorMap.containsKey(key);
    }

    public void put(Object key, FutureListenerProcessor value) {
        this.futureListenerProcessorMap.put(key, value);
    }
    /**
     * save/restore
     * @param value
     * @param keys
     */
    public <K,V> void saveFutureListenerProcessor(V value, K... keys) {
        if ( value instanceof FutureListenerProcessorHolder) {
            FutureListenerProcessorHolder futureListenerProcessorHolder = (FutureListenerProcessorHolder) value;
            // handle case where a new entry comes in with a FutureListenerNotifier set up.
            FutureListenerProcessor futureListenerProcessor = futureListenerProcessorHolder.getFutureListenerProcessor();
            for (K key : keys) {
                if ( futureListenerProcessor != null) {
                    if ( !this.futureListenerProcessorMap.containsKey(key) ) {
                        this.futureListenerProcessorMap.put(key, futureListenerProcessor);
                    } else {
                        FutureListenerProcessor knownFutureListenerProcessor = this.getFutureListenerProcessor(key);

                        if ( knownFutureListenerProcessor != futureListenerProcessor ) {
                            if ( !knownFutureListenerProcessor.isEmpty()) {
                                // merge issue
                            	//Kostya: what's the problem at this point? Why raise an exception?
                                // PAT : because we are going to lose listeners. FutureListeners in the old knownFutureListenerProcessor will be discarded and never notified.
                                // HACK : revert! and fix the lost FutureListener problem.
                                ApplicationIllegalStateException.checkState(false, knownFutureListenerProcessor, " ", futureListenerProcessor);
                            } else {
                                futureListenerProcessorMap.put(key, futureListenerProcessor);
                            }
                        }
                    }
                } else if ( this.futureListenerProcessorMap.containsKey(key)){
                    FutureListenerProcessor knownFutureListenerProcessor = this.getFutureListenerProcessor(key);
                    futureListenerProcessorHolder.setFutureListenerProcessor(knownFutureListenerProcessor);
                }
            }
        }
    }
    public <K,V> void restoreFutureListenerProcessor(V value, K key) {
        if ( value instanceof FutureListenerProcessorHolder) {
            FutureListenerProcessorHolder futureListenerProcessorHolder = (FutureListenerProcessorHolder) value;
            // handle case where a new entry comes in with a FutureListenerNotifier set up.
            FutureListenerProcessor futureListenerProcessor = futureListenerProcessorHolder.getFutureListenerProcessor();
            if ( futureListenerProcessor == null && this.futureListenerProcessorMap.containsKey(key)) {
                FutureListenerProcessor knownFutureListenerProcessor = this.getFutureListenerProcessor(key);
                futureListenerProcessorHolder.setFutureListenerProcessor(knownFutureListenerProcessor);
            }
        }
    }
    /**
     * Used when wrapping a Future
     */
    private class FutureResultWrapper implements FutureResultImplementor{
        private final FutureResultImplementor futureResultImplementor;
        FutureResultWrapper(FutureResultImplementor futureResultImplementor) {
            this.futureResultImplementor = futureResultImplementor;
        }

        private FutureListenerProcessor getFutureListenerProcessor() {
            return futureListenerProcessorMap.get(this.futureResultImplementor);
        }
        public void set(Object value) {
            futureResultImplementor.set(value);
            getFutureListenerProcessor().futureSet(futureResultImplementor, value);
        }
        public void setException(Throwable throwable) {
            futureResultImplementor.setException(throwable);
            getFutureListenerProcessor().futureSetException(futureResultImplementor, throwable);
        }

        public void addFutureListener(FutureListener futureListener) {
            getFutureListenerProcessor().addFutureListener(futureListener);

        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return futureResultImplementor.cancel(mayInterruptIfRunning);
        }

        public Object get() throws InterruptedException, ExecutionException {
            return futureResultImplementor.get();
        }

        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return futureResultImplementor.get(timeout, unit);
        }

        public Throwable getException() {
            return futureResultImplementor.getException();
        }

        public Object getMapKey() {
            return futureResultImplementor.getMapKey();
        }

        public boolean isCancelled() {
            return futureResultImplementor.isCancelled();
        }

        public boolean isDone() {
            return futureResultImplementor.isDone();
        }

        public boolean isFailed() {
            return futureResultImplementor.isFailed();
        }

        public boolean isSuccessful() {
            return futureResultImplementor.isSuccessful();
        }

        public Object poll() {
            return futureResultImplementor.poll();
        }

    }

}
