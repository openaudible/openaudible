package org.openaudible.audible;

import org.openaudible.AudibleAccountPrefs;

/**
 * Created  6/27/2017.
 */
public interface ConnectionListener {
    public void connectionChanged(boolean connected);
    public AudibleAccountPrefs getAccountPrefs(AudibleAccountPrefs in);

}
