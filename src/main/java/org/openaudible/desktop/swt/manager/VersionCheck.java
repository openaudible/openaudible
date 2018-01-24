package org.openaudible.desktop.swt.manager;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;
import org.openaudible.util.HTTPGet;
import org.openaudible.util.Platform;

import java.io.IOException;

public enum VersionCheck {
    instance;
    private static final Log LOG = LogFactory.getLog(VersionCheck.class);

    // if verbose return state regardless.
    // if !verbose, only alert when new version is available.
    public void checkForUpdate(Shell shell, boolean verbose) {
        JsonObject obj = versionCheck();
        String msg = obj.get("msg").getAsString();
        String title = obj.get("title").getAsString();

        int diff = obj.get("diff").getAsInt();
        if (diff < 0) {
            MessageBoxFactory.showGeneral(shell, SWT.ICON_INFORMATION, title, msg);
        } else
        {
            if (verbose)
            {
                MessageBoxFactory.showGeneral(shell, SWT.ICON_INFORMATION, title, msg);
            }
        }


        // return msg;
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

    /**
     * Compares two version strings.
     *
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     *         The result is a positive integer if str1 is _numerically_ greater than str2.
     *         The result is zero if the strings are _numerically_ equal.
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

    // return "" if current version.
    public JsonObject versionCheck() {
        JsonObject obj =null;
        try {
            obj = getVersion();
            LOG.info(obj.toString());
            if (!obj.has("version"))
                throw new IOException("missing version field\n" + obj);
            String releaseVersion = obj.get("version").getAsString();

            int diff = versionCompare(Version.appVersion, releaseVersion);
            obj.addProperty("diff", ""+diff);
            String msg, title;

            if (diff<0) {
                title = "Update Available";
                msg = "An update is available!\nYour version: " + Version.appVersion + "\nRelease Version:" + releaseVersion;
            }else if (diff>0) {
                title = "Using Pre-release";
                msg = "You appear to be using a pre-release version\nYour version: " + Version.appVersion + "\nLatest Version:" + releaseVersion;
            }
            else
            {
                title = "No update at this time";
                msg = "Using the latest release version";
            }

            obj.addProperty("msg", msg);
            obj.addProperty("title", title);

        } catch (IOException e) {
            if (obj==null)
                obj  = new JsonObject();
            obj.addProperty("msg", "Error checking for latest version.\nError message: " + e.getMessage());
            obj.addProperty("title", "Version check failed");
        }
        return obj;
    }

}
