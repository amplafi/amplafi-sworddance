package org.easymock.matchers;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.easymock.IArgumentMatcher;

/**
 * A {@link IArgumentMatcher} that matches collections having the same length
 * and the same items at the same place.
 */
@SuppressWarnings("unchecked")
public class SameCollection implements IArgumentMatcher {
    private final Collection collection;
    private boolean exactMatch;
    
    public SameCollection(Collection collection) {
        this(collection, true);
    }
    public SameCollection(Collection collection, boolean exactMatch) {
        this.collection = collection;
        this.exactMatch = exactMatch;
    }    
    public void appendTo(StringBuffer buffer) {
        buffer.append("same as " + collection);
    }

    public boolean matches(Object argument) {
        if ( argument == this.collection) {
            // handles null case.
            return true;
        }
        boolean goodArgument = argument instanceof Collection || argument.getClass().isArray();
        if ( !goodArgument) {
            return false;
        } else if ( !exactMatch) {
            Collection that = argument.getClass().isArray()? Arrays.asList(argument) : (Collection) argument;
            return CollectionUtils.disjunction(this.collection, that).isEmpty();
        } else {
            Object[] that;
            if ( argument.getClass().isArray() ) {
                that = (Object[])argument;
            } else {
                that = ((Collection) argument).toArray();
            }
            if (collection.size() == that.length) {
                Object[] me = collection.toArray();
                for (int i = 0; i<me.length; i++) {
                    if (me[i]!=that[i]) {
                        return false;
                    }
                }
                return true;
            }            
        }
        return false;
    }

}
