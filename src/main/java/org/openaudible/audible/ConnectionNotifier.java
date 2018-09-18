package org.openaudible.audible;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.util.EventNotifier;


public class ConnectionNotifier extends EventNotifier<ConnectionListener> implements ConnectionListener {
	public static final ConnectionNotifier instance = new ConnectionNotifier();
	State state = State.Not_Connected;
	private static final Log LOG = LogFactory.getLog(ConnectionNotifier.class);
	
	
	private ConnectionNotifier() {
	}
	
	public static ConnectionNotifier getInstance() {
		return instance;
	}
	
	@Override
	public void connectionChanged(boolean connected) {
		
		State newState = connected ? State.Connected : State.Disconnected;
		if (state != newState) {
			state = newState;
			for (ConnectionListener l : getListeners()) {
				l.connectionChanged(connected);
			}
		}
		
	}
	
	public String lastErrorURL = "";
	public String lastErrorTitle = "";
	
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
	
	public void signout() {
		state = State.SignedOut;
		ConnectionNotifier.getInstance().connectionChanged(false);
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
	
	
	// not connected is unknown.
	// connected means in account
	// disconnected means a password is being asked for.
	enum State {
		Not_Connected, Connected, Disconnected, SignedOut
	}
	
	
	@Override
	public void loginFailed(String url, String title, String xml) {
		
		lastErrorTitle = title;
		lastErrorURL = url;
		
		if (state != State.SignedOut) {
			for (ConnectionListener l : getListeners()) {
				l.loginFailed(url, title, xml);
			}
		}
		
	}
}


