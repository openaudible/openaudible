package org.openaudible.desktop.swt.manager;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;
import org.openaudible.util.HTTPGet;
import org.openaudible.util.Platform;

import java.io.IOException;

public enum VersionCheck {
    instance;
    private static final Log LOG = LogFactory.getLog(VersionCheck.class);

    public String checkForUpdate(boolean verbose) {
        String msg = versionCheck();
        if (!msg.isEmpty()) {
            MessageBoxFactory.showGeneral(null, SWT.ICON_INFORMATION, "Update Check", msg);
        } else {
            if (verbose) {
                String noUpdate = "No updates available";
                MessageBoxFactory.showGeneral(null, SWT.ICON_INFORMATION, "Using latest version", noUpdate);
            }

        }
        return msg;
    }

    public JsonObject getVersion() throws IOException {
        String url = Version.versionLink;
        url += "?";
        url += "platform=" + Platform.getPlatform().toString();
        url += "&version=" + Version.appVersion;
        // url += "&count=" + audible.getBookCount();
        LOG.info("versionCheck: " + url);
        return HTTPGet.instance.getJSON(url);
    }

    // return "" if current version.
    public String versionCheck() {

        try {
            JsonObject obj = getVersion();
            LOG.info(obj.toString());
            if (!obj.has("version"))
                throw new IOException("missing version field\n" + obj);
            String curVers = obj.get("version").getAsString();
            if (curVers.equals(Version.appVersion)) {
                return "";
            }
            return "An update is available!\nYour version: " + Version.appVersion + "\nLatest Version:" + curVers;
        } catch (IOException e) {
            LOG.error("Error checking for latest version: " + e);
            return "Error checking for latest version.\nError message: " + e.getMessage();
        }
    }

}
