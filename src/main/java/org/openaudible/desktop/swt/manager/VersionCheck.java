package org.openaudible.desktop.swt.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONObject;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;
import org.openaudible.desktop.swt.manager.menu.CommandCenter;
import org.openaudible.util.HTTPGet;
import org.openaudible.util.Platform;

import java.io.IOException;

public enum VersionCheck {
    instance;
    private static final Log LOG = LogFactory.getLog(VersionCheck.class);


    // if verbose return state regardless.
    // if !verbose, only alert when new version is available.
    public void checkForUpdate(Shell shell, boolean verbose) {
        JSONObject obj = versionCheck();
        String msg = obj.optString("msg");
        String title = obj.optString("title","Version Check");

        int diff = obj.optInt("diff", 0);
        if (diff < 0) {
            MessageBoxFactory.showGeneral(shell, SWT.ICON_INFORMATION, title, msg);
            if (obj.has("site")) {
                String url = obj.getString("site");
                AudibleGUI.instance.browse(url);
            }

            // TODO: Add buttons: go to web site (openaudible.org) or download update (go to mac,win, or linux download url)
        } else {
            if (verbose) {
                MessageBoxFactory.showGeneral(shell, SWT.ICON_INFORMATION, title, msg);
            }
        }


        // return msg;
    }

    public JSONObject getVersion() throws IOException {
        String url = Version.versionLink;
        url += "?";
        url += "platform=" + Platform.getPlatform().toString();
        url += "&version=" + Version.appVersion;
        // url += "&count=" + audible.getBookCount();
        LOG.info("versionCheck: " + url);
           return HTTPGet.instance.getJSON(url);
    }

    /**
     * Compares two version strings.
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     * The result is a positive integer if str1 is _numerically_ greater than str2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    public static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }

    public JSONObject versionCheck() {
        JSONObject obj = null;
        try {
            obj = getVersion();

            if (!obj.has("version"))
                throw new IOException("missing version field\n" + obj);
            String releaseVersion = obj.getString("version");

            int diff = versionCompare(Version.appVersion, releaseVersion);
            obj.put("diff", "" + diff);
            String msg, title;

            if (diff < 0) {
                title = "Update Available";
                msg = "An update is available!\nYour version: " + Version.appVersion + "\nRelease Version:" + releaseVersion;
                if (obj.optBoolean("required", false))
                {
                    msg += "\nThis upgrade is required. Old versions no longer supported.";
                    CommandCenter.instance.expiredApp = true;
                }
                msg += "\n"+obj.optString("old_news", "");

            } else if (diff > 0) {
                title = "Using Pre-release";
                msg = "You appear to be using a pre-release version\nYour version: " + Version.appVersion + "\nLatest Version:" + releaseVersion;
                msg += "\n"+obj.optString("pre_release_news", "");
            } else {
                title = "No update at this time";
                msg = "Using the latest release version.";
                msg += "\n"+obj.optString("current_news", "");
                // allow a news field
            }

            if (obj.has("kill"))
            {
                msg +="\n"+obj.getString("kill");
                CommandCenter.instance.expiredApp = true;
            }

            obj.put("msg", msg);
            obj.put("title", title);

        } catch (IOException e) {
            if (obj == null)
                obj = new JSONObject();
            obj.put("msg", "Error checking for latest version.\nError message: " + e.getMessage());
            obj.put("title", "Version check failed");
        }
        return obj;
    }

}
