/*
 * Created on Aug 13, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.util;

/**
 * Used only to find the Class object of T.
 * @author Patrick Moore
 */
public class ClassCapture<T> {
    private final Class<T> capturedClass = null;

    @SuppressWarnings("unchecked")
    public ClassCapture() {
//        Type[] types = getClass().getTypeParameters();
//        TypeVariable<?>[] gtypes = ((TypeVariable)types[0]).getGenericDeclaration().getTypeParameters();
//        Type[] actualTypes = ((ParameterizedType)gtypes[0]).getActualTypeArguments();
//        this.capturedClass = (Class<T>)actualTypes[0].getClass();
    }

    public Class<T> getCapturedClass() {
        return capturedClass;
    }
}
