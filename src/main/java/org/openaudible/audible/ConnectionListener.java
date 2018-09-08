package org.openaudible.audible;

import org.openaudible.AudibleAccountPrefs;

/**
 * Created  6/27/2017.
 */
public interface ConnectionListener {
	void connectionChanged(boolean connected);
	
	AudibleAccountPrefs getAccountPrefs(AudibleAccountPrefs in);
	
	void loginFailed(String url, String html);
}
