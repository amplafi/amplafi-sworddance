/*
 * Created on Sep 22, 2007
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.util;

import java.util.Iterator;

/**
 * a iterator that returns itself when asked as an iterable.
 *
 * @author Patrick Moore
 * @param <T>
 */
public interface IterableIterator<T> extends Iterator<T>, Iterable<T> {

}
