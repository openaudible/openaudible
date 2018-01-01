package org.openaudible.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class EventNotifier<T> {
    private final ArrayList<T> listeners = new ArrayList<>();

    public void addListener(T l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeListener(T l) {
        synchronized (listeners) {
            boolean o = listeners.remove(l);
            assert (o);
        }
    }

    public Collection<T> getListeners() {
        return Collections.unmodifiableCollection(listeners);
    }

    protected Object getLock() {
        return listeners;
    }
}
