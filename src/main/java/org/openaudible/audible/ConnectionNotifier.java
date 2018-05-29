package org.openaudible.audible;


import org.openaudible.AudibleAccountPrefs;
import org.openaudible.util.EventNotifier;


public class ConnectionNotifier extends EventNotifier<ConnectionListener> implements ConnectionListener {
    public static final ConnectionNotifier instance = new ConnectionNotifier();
    State state = State.Not_Connected;

    private ConnectionNotifier() {
    }

    public static ConnectionNotifier getInstance() {
        return instance;
    }

    @Override
    public void connectionChanged(boolean connected) {
        state = connected ? State.Connected : State.Disconnected;

        for (ConnectionListener l : getListeners()) {
            l.connectionChanged(connected);
        }
    }

    public boolean isConnected() {
        return getState() == State.Connected;
    }

    // allow gui to pass back new credentials.
    public AudibleAccountPrefs getAccountPrefs(AudibleAccountPrefs in) {
        AudibleAccountPrefs out = in;

        for (ConnectionListener l : getListeners()) {
            out = l.getAccountPrefs(out);
            if (out == null) return null; // canceled
        }
        return out;
    }


    public State getState() {
        return state;
    }

    public String getStateString() {
        return state.name().replace('_', ' ');
    }

    public boolean isDisconnected() {
        return getState() == State.Disconnected;
    }

    public void setLastURL(String u) {


    }

    // not connected is unknown.
    // connected means in account
    // disconnected means a password is being asked for.
    enum State {
        Not_Connected, Connected, Disconnected
    }
}


