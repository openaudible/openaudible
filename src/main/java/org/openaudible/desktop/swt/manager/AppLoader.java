/*
 */
package org.openaudible.desktop.swt.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.Directories;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.util.shop.FontShop;

import java.io.File;
import java.io.IOException;

public class AppLoader implements Version {
    public final static Log logger = LogFactory.getLog(GUI.class);
    /**
     * Set to TRUE to use sleak
     */
    private static final boolean useSleak = false;
    public static boolean death = true;
    /**
     * Flag to set TRUE when the application was built
     */
    static boolean guiBuilt = false;
    static boolean useSplash = false;
    static boolean showTerms = false;
    private static boolean didStartup;
    Display display;
    Shell invisibleShell;
    boolean expired = false;
    int daysRemain = 0;
    private Shell shell;

    public AppLoader() {
        this(new String[0]);
    }

    /**
     * Load GUI and display a Splashscreen while loading
     */
    public AppLoader(String args[]) {

        if (didStartup == false) {
            // System.out.println("LM: Startup...");
            startupProcess(args);
        }

        // System.out.println("LM: syncloader.. "+Globals.WORKING_DIR);
        /** Apply application name to Display */
        Display.setAppName(getAppName());

        display = new Display();

        /** Shell should not be visible in the taskbar */
        invisibleShell = new Shell(display, SWT.NONE);
        new FontShop(display);

        GUI g = createApp(display);

        g.startUp();
        guiBuilt = true;
        g.run();
        // Call exit to close any audio threads...
        display.dispose();
        Runtime.getRuntime().exit(0);
        System.exit(0);
    }

    public static void main(String[] args) {
        startupProcess(args);
        new AppLoader();
    }

    /**
     * Create the Home Directory
     */
    private static void createWorkingDir(String path) throws IOException {
        File dir = null;
        String homePath = null;
        if (path != null && !path.equals("")) {
            dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                homePath = dir.getAbsolutePath();
                return;
            }
            System.err.println("Invalid argument:" + path);
            System.exit(0);
        }
        homePath = (GUI.isMac() ? System.getProperty("user.home") : System.getProperty("user.home"));
        File hp = new File(homePath);
        if (!hp.exists()) {
            System.err.println("Bad path for home dir: " + hp.getAbsolutePath());
        }
        if (GUI.isMac()) {
            /** On Mac, append "Library/Preferences" to the user.home directory */
            hp = new File(hp, "Library");
            if (!hp.isDirectory())
                hp = new File(homePath);
        }

        File prefs = new File(hp, appName);
        if (!prefs.isDirectory()) {
            boolean ok = prefs.mkdirs();
            if (!ok) {
                throw new IOException("Unable to create preference directory:" + prefs.getAbsolutePath());
            }
        }


        Directories.init(prefs, prefs);


    }

    /**
     * Check if the given argument is a valid URI or URL
     *
     * @param arg The argument that has been passed to RSSOwl
     * @return TRUE if the argument is valid
     */
    private static boolean isValidArgument(String arg) {
        File f = new File(arg);
        return f.exists() && f.isDirectory();
    }

    /**
     * Write OS specific DWOrds into System properties
     */
    private static void setUpProperties() {
        /** Mac: Disable the blue focus ring on most Widgets */
        if (GUI.isMac())
            System.setProperty("org.eclipse.swt.internal.carbon.noFocusRing", "true");
        /** Mac: Use small fonts */
        if (GUI.isMac())
            System.setProperty("org.eclipse.swt.internal.carbon.smallFonts", "true");
    }

    /**
     * Things to do before launching
     *
     * @param args Arguments
     */
    public static void startupProcess(String[] args) {
        try {
            didStartup = true;


            String java = System.getProperty("java.version");
            logger.info("Starting " + getAppName() + " build " +Version.appVersion + " for " + SWT.getPlatform() + " swt " + SWT.getVersion() + " jvm " + java);
            // checkNIC();
            GUI.userArgs = null;
            /** Inform MainController about argument if it is valid */
            if (args.length > 0) {
                if (isValidArgument(args[0]))
                    GUI.userArgs = args[0];
            }

            /** Create the Working Directory if it does not yet exist */
            createWorkingDir(GUI.userArgs);
            // System.out.println("LM: WD="+Globals.WORKING_DIR);

            /** Setup OS specific properties (DWords) */
            setUpProperties();
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error(e);
            System.exit(1);

        }
    }

    public static final String getAppName() {
        return Version.appName;
    }

    public GUI createApp(Display d) {
        return new Application(d);
    }
}
