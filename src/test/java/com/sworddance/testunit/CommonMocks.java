package com.sworddance.testunit;

import static com.sworddance.testunit.MockControlManager.assignThreadControlSource;
import static com.sworddance.testunit.MockControlManager.newControlSource;
import static com.sworddance.testunit.MockControlManager.newStrictControlSource;
import static com.sworddance.testunit.MockControlManager.unassignThreadControlSource;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.easymock.internal.MocksControl.MockType;
import org.easymock.matchers.Capturer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.sworddance.testunit.MockControlManager.ControlSource;

/**
 * Utility methods required for using Mocks.
 */
public class CommonMocks extends Assert {

    protected final ControlSource easyMockControlSource;

    protected final ThreadLocal<IMocksControl> lastIndependent = new ThreadLocal<IMocksControl>();

    private Log log;

    public CommonMocks() {
        this(MockType.DEFAULT);
    }

    /**
     * Allows controlling the strict-ness of the generated mocks. If
     * Testclass.hivemodule.xml exists then hivemindFilename is set to that
     * file.
     *
     * @param strict set to true to have strict mocks created by this class.
     */
    public CommonMocks(MockType type) {
    	switch (type) {
		case NICE:
			easyMockControlSource = MockControlManager.newNiceControlSource();
			break;
		case STRICT:
			easyMockControlSource = newStrictControlSource();
			break;
		case DEFAULT:
		default:
			easyMockControlSource = newControlSource();
			break;
		}
    }

    /**
     * Creates a new mock object of the indicated type. The created object is
     * retained for the duration of the test.
     *
     * @param <T> the type of the mock object
     * @param mockClass the class to mock
     * @param methodNames limit the methods mock to just methods with these names.
     * @return the mock object, ready for training
     * @throws NoSuchMethodError a method listed does not exist.
     */
    public final <T> T newMock(Class<T> mockClass, String... methodNames) throws NoSuchMethodError {
        if (methodNames.length == 0) {
            return getMocksControl().createMock(mockClass);
        } else {
            Method[] methods = new Method[methodNames.length];
            int i = 0;
            OUTER: for (String method : methodNames) {
                for (Method m : mockClass.getMethods()) {
                    if (m.getName().equals(method)) {
                        methods[i++] = m;
                        continue OUTER;
                    }
                }
                throw new NoSuchMethodError(mockClass.getName() + "." + method);
            }
            return newMock(mockClass, methods);
        }
    }

    public final <T> T newMock(Class<T> mockClass, Method... methods) {
        return getMocksControl().createMock(mockClass, methods);
    }
    /**
     * create a mock object that is not using the thread's mock Control -- useful when creating mocks in DataProviders.
     * @see #createIndependentControl()
     *
     * @param <T>
     * @param mockClass
     * @param methods
     * @return mock
     */
    public final <T> T newIndependentMock(Class<T> mockClass) {
        return createIndependentControl().createMock(mockClass);
    }

	protected IMocksControl createIndependentControl() {
		this.lastIndependent.set(EasyMock.createControl());
		return getLastIndependentControl();
	}

	protected IMocksControl getLastIndependentControl() {
		return this.lastIndependent.get();
	}

    /**
     * Creates a Mock for given class, all methods will be mocked
     *
     * @param <T>
     * @param mockClass
     * @return the new mock.
     */
    public final <T> T newMock(Class<T> mockClass) {
        return getMocksControl().createMock(mockClass);
    }
    public final <T> T createMock(String name, Class<T> mockClass) {
        return getMocksControl().createMock(name, mockClass);
    }

    /**
     * @return the control object used for all mocks created by this test case.
     */
    public final IMocksControl getMocksControl() {
        return easyMockControlSource.get();
    }

    /**
     * Replays the mocks control, preparing all mocks for testing.
     */
    public void replay() {
        getMocksControl().replay();
    }

    /**
     * Replays the mocks control, preparing all mocks for testing.
     */
    public void reset() {
        getMocksControl().reset();
    }

    /**
     * Verify mocks
     */
    protected void verify() {
        IMocksControl control = getMocksControl();
        try {
            control.verify();
        } catch (IllegalStateException e) {
            // maybe a test that did not ever use mocks.
        }
    }

    @BeforeMethod
    public void usingControlSource() {
        assignThreadControlSource(easyMockControlSource);
    }

    /**
     * Discards any mock objects created during the test. When using TestBase as
     * a utility class, not a base class, you must be careful to either invoke
     * this method, or discard the TestBase instance at the end of each test.
     */
    @AfterMethod()
    public void cleanupControlSource() {
        // TestNG reuses the same class instance across all tests within that
        // class, so if we don't
        // clear out the mocks, they will tend to accumulate. That can get
        // expensive, and can
        // cause unexpected cascade errors when an earlier test fails.

        // After each method runs, we clear this thread's mocks control.
        easyMockControlSource.remove();
        unassignThreadControlSource();
    }

    protected <T> T capture(Capturer<T> capturer) {
        return Capturer.capture(capturer);
    }

    /**
     *
     * @return log
     */
    public Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }
}
