package com.sworddance.beans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public interface CurriedProxyMethodHelper extends InvocationHandler {
    boolean isHandling(Object proxy, Method method, Object... args);
}
