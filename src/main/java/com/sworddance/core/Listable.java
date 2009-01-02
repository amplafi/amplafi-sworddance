/*
 * Created on Oct 12, 2006
 * Copyright 2006 by Patrick Moore
 */
package com.sworddance.core;

import java.util.Comparator;

/**
 * Implementers can be displayed as a list.
 * @author Patrick Moore
 */
public interface Listable {
    /**
     *
     */
    public static final String LIST_DISPLAY_VALUE = "listDisplayValue";
    public static ListableComparator COMPARATOR = new ListableComparator();
    /**
     * @return the string that is suitable when implementer is displayed in a list of similar items.
     */
    public String getListDisplayValue();
    /**
     * Use in tapestry-For's keyExpression attribute.
     * @return a key for tapestry for-expressions.
     */
    public Object getKeyExpression();

    public boolean hasKey(Object key);

    public static class ListableComparator implements Comparator<Listable> {
        @Override
        public int compare(Listable o1, Listable o2) {
            return o1.getListDisplayValue().compareTo(o2.getListDisplayValue());
        }

    }
}
