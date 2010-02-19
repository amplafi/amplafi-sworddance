// Copyright 2006 Howard M. Lewis Ship
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 *
 */
package org.easymock.matchers;

import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import static org.easymock.EasyMock.*;

/**
 * An EasyMock 2.0 argument matcher that captures a method argument value. This allows an object
 * created inside a test method to be interrogated after the method completes, even when the object
 * is not a return value, but is merely passed as a parameter to a mock object.
 *
 * @author Howard M. Lewis Ship
 * @param <T>
 *            the type of object to capture
 */
public class Capturer<T> implements IArgumentMatcher, IAnswer<T>
{
    private final Class<T> _matchType;

    private T _captured;

    /**
     * Creates a new Capturer for the given type. Because of Generics syntax, it is easier to use
     * the static method.
     * @param matchType
     */
    public Capturer(Class<T> matchType)
    {
        _matchType = matchType;
    }

    public void appendTo(StringBuffer buffer)
    {
        buffer.append(String.format("capture(%s)", _matchType.getName()));
    }

    public boolean matches(Object parameter)
    {
        boolean result = _matchType.isInstance(parameter);

        if (result) {
            _captured = _matchType.cast(parameter);
        }

        return result;
    }

    /** @return the method argument value previously captured. */
    public T getCaptured()
    {
        return _captured;
    }

    /**
     * Usage (with static imports):
     * <p>
     * Capturer&lt;Type&gt; c = newCapturer(Type.class);
     * <p>
     * mock.someMethod(capture(c));
     * <p> . . .
     * <p>
     * c.getCaptured().getXXX()
     * <p>
     * The interrogation of the captured argument should occur after the test subject has invoked
     * the method on the mock.
     * <p>
     * Remember that when you use an argument matcher for one argument of a method invocation, you
     * must use argument matchers for <em>all</em> arguments of the method invocation.
     * @param capturer
     * @param <T>
     * @return null
     */
    public static <T> T capture(Capturer<T> capturer)
    {
        reportMatcher(capturer);

        return null;
    }

    /**
     * offer up the captured object in response to later mock object call.
     *
     * This is useful for handling getter/setter pairs.
     * @see org.easymock.IAnswer#answer()
     */
    public T answer() {
        return getCaptured();
    }

}
