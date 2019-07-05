package org.sipfoundry.commons.diddb;

import java.util.Iterator;

public abstract class AbstractDecoratedIterator implements Iterator<Did> {
    private Iterator<Did> source;
    
    public AbstractDecoratedIterator(Iterator<Did> source) {
       this.source = source;
    }

    public boolean hasNext() {
       return source.hasNext();
    }

    public abstract Did next();

    public void remove() {
       throw new UnsupportedOperationException();
    }
    
    protected Iterator<Did> getSource() {
        return source;
    }
 }
