package org.openaudible.audible;


import org.openaudible.util.EventNotifier;


public class ConnectionNotifier extends EventNotifier<ConnectionListener> implements ConnectionListener {
    private static ConnectionNotifier ourInstance = new ConnectionNotifier();
    State state = State.Not_Connected;

    private ConnectionNotifier() {
    }

    public static ConnectionNotifier getInstance() {
        return ourInstance;
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

    public State getState() {
        return state;
    }

    public String getStateString() {
        return state.name().replace('_', ' ');
    }

    enum State {Not_Connected, Connected, Disconnected}
}
