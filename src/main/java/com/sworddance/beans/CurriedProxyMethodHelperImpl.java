package com.sworddance.beans;

import java.lang.reflect.Method;

public class CurriedProxyMethodHelperImpl<I, O extends I> implements CurriedProxyMethodHelper {

    private ProxyMapper<I, O> proxyMapper;
    private ProxyMethodHelper proxyMethodHelper;

    public CurriedProxyMethodHelperImpl(ProxyMapper<I, O> proxyMapper, ProxyMethodHelper proxyMethodHelper) {
        this.proxyMapper = proxyMapper;
        this.proxyMethodHelper = proxyMethodHelper;
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return this.proxyMethodHelper.invoke(proxyMapper, proxy, method, args);
    }

    public boolean isHandling(Object proxy, Method method, Object... args) {
        return this.proxyMethodHelper.isHandling(proxyMapper, proxy, method, args);
    }

}
