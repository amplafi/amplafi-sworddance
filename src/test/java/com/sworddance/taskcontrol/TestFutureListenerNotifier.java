package com.sworddance.taskcontrol;

import static org.easymock.EasyMock.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import com.sworddance.util.ApplicationNullPointerException;

/**
 * Test the support code around {@link FutureListenerNotifier}
 * @author patmoore
 *
 */
public class TestFutureListenerNotifier {

    /**
     * ensure user is notified that there is no {@link FutureListenerProcessor} to hold the FutureListener when the {@link FutureResultImpl} is not
     * done.
     */
    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void testFutureResultNotDoneNoFutureListenerProcessor() {
        FutureListener futureListener = createMock(FutureListener.class);
        FutureResultImpl<Object>futureResultImpl = new FutureResultImpl<Object>();
        futureResultImpl.addFutureListener(futureListener );
    }
    /**
     * ensure that futureListener is not null. ( usually sign of external bug - a lost futureListener)
     */
    @Test(expectedExceptions=ApplicationNullPointerException.class)
    public void testFutureResultDoneNoFutureListener() {
        FutureListener futureListener = null;
        FutureResultImpl<Object>futureResultImpl = new FutureResultImpl<Object>();
        futureResultImpl.set(null);
        futureResultImpl.addFutureListener(futureListener );
    }
    /**
     * ensure thate {@link FutureListenerProcessor} is not need for case when Future has result.
     */
    @Test
    public void testFutureResultDoneNoFutureListenerProcessor() {
        FutureResultImpl<Object>futureResultImpl = new FutureResultImpl<Object>();
        futureResultImpl.set(null);
        FutureListener futureListener = createMock(FutureListener.class);
        futureListener.futureSet(futureResultImpl, null);
        expectLastCall();
        replay(futureListener);
        futureResultImpl.addFutureListener(futureListener );
        verify(futureListener);
    }
    /**
     * ensure that futureListener is not null. ( usually sign of external bug - a lost futureListener)
     */
    @Test
    public void testFutureResultNotDoneFutureListenerProcessor() {
        String resultStr = "Yeah!";
        FutureResultImpl<Object>futureResultImpl = new FutureResultImpl<Object>();
        futureResultImpl.setFutureListenerProcessor(new FutureListenerProcessor());
        FutureListener futureListener = createMock(FutureListener.class);
        futureListener.futureSet(futureResultImpl, resultStr);
        expectLastCall();
        replay(futureListener);
        futureResultImpl.addFutureListener(futureListener );
        futureResultImpl.set(resultStr);
        verify(futureListener);
    }
    /**
     * Test that a serialize /deserial operation using the FutureListenerProcessorMap restores the FutureListenerProcessor to the Future.
     * @throws Exception
     */
    @Test
    public void testSerializeDeserialize() throws Exception {
        String resultStr = "Yeah!";
        SerializableFutureResultImpl<String>futureResultImpl = new SerializableFutureResultImpl<String>();
        final FutureListenerProcessor futureListenerProcessor = new FutureListenerProcessor();
        futureResultImpl.setFutureListenerProcessor(futureListenerProcessor);
        FutureListener futureListener = createMock(FutureListener.class);
        futureResultImpl.addFutureListener(futureListener );
        SerializableFutureResultImpl futureResultImpl2 = serializeDeserialize(futureResultImpl);
        futureListener.futureSet(futureResultImpl2, resultStr);
        expectLastCall();
        replay(futureListener);

        FutureListenerProcessorMap futureListenerProcessorMap = new FutureListenerProcessorMap();
        futureListenerProcessorMap.saveFutureListenerProcessor(futureResultImpl, "Foo");
        assertEquals(futureResultImpl2.getFutureListenerProcessor(), null);
        futureListenerProcessorMap.restoreFutureListenerProcessor(futureResultImpl2, "Foo");
        assertEquals(futureResultImpl2.getFutureListenerProcessor(), futureListenerProcessor);
        futureResultImpl2.set(resultStr);
        verify(futureListener);
    }

    @Test
    public void testDuplicateSaveRestore() throws Exception {
        String resultStr = "Yeah!";
        SerializableFutureResultImpl<String>futureResultImpl = new SerializableFutureResultImpl<String>();
        final FutureListenerProcessor futureListenerProcessor = new FutureListenerProcessor();
        futureResultImpl.setFutureListenerProcessor(futureListenerProcessor);
        FutureListener futureListener = createMock(FutureListener.class);
        futureResultImpl.addFutureListener(futureListener );
        SerializableFutureResultImpl futureResultImpl2 = serializeDeserialize(futureResultImpl);
        futureListener.futureSet(futureResultImpl2, resultStr);
        expectLastCall();
        replay(futureListener);

        FutureListenerProcessorMap futureListenerProcessorMap = new FutureListenerProcessorMap();
        futureListenerProcessorMap.saveFutureListenerProcessor(futureResultImpl, "Foo");
        // intentionally duplicated
        futureListenerProcessorMap.saveFutureListenerProcessor(futureResultImpl, "Foo");

        assertEquals(futureResultImpl2.getFutureListenerProcessor(), null);
        futureListenerProcessorMap.restoreFutureListenerProcessor(futureResultImpl2, "Foo");
        // intentionally duplicated
        futureListenerProcessorMap.restoreFutureListenerProcessor(futureResultImpl2, "Foo");
        assertEquals(futureResultImpl2.getFutureListenerProcessor(), futureListenerProcessor);
        futureResultImpl2.set(resultStr);
        verify(futureListener);
    }
    private <F> F serializeDeserialize(F futureResult) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        objectOutputStream.writeObject(futureResult);
        objectOutputStream.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(in);
        F f = (F) objectInputStream.readObject();
        assertNotNull(f);
        return f;
    }

    private static class SerializableFutureResultImpl<T> extends FutureResultImpl<T> implements Serializable {

    }
}
