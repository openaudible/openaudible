package org.openaudible.desktop.swt.gui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.desktop.swt.i8n.ITranslatable;
import org.openaudible.desktop.swt.i8n.Translate;
import org.openaudible.desktop.swt.manager.menu.AppMenu;
import org.openaudible.desktop.swt.manager.menu.CommandCenter;
import org.openaudible.desktop.swt.util.shop.LayoutShop;
import org.openaudible.desktop.swt.util.shop.PaintShop;
import org.openaudible.util.Platform;

import java.io.File;
import java.io.IOException;

public abstract class GUI implements ITranslatable {
    /**
     * Internationalization for App
     */
    public final static Translate i18n = Translate.getInstance();
    /**
     * Log all catched Exceptions
     */
    public final static Log logger = LogFactory.getLog(GUI.class);
    /**
     * A lot of other obj have to access display
     */
    public static Display display;
    /**
     * Flag is set to TRUE when App is exiting
     */
    public static boolean isClosing = false;
    /**
     * For classes who needs to access the MainController
     */
    public static GUI gui;
    /**
     * A lot of other obj have to access shell
     */
    public static Shell shell;
    /**
     * If the user has passed an argument to the application, it is stored here
     */
    public static String userArgs;
    public AppMenu appMenu;
    public CommandCenter commandCenter;
    private FakeToolTip fakeToolTip;

    /**
     * Instantiate a new GUI
     *
     * @param display The display
     */
    public GUI(Display display) {
        /* Init fields */
        GUI.display = display;
        GUI.gui = this;
        // displayThread = Thread.currentThread();
        /* Startup process */
    }

    /**
     * This method is called to check if App is still alive or was just closed by the user.
     *
     * @return boolean TRUE if App is still running
     */
    public static boolean isAlive() {
        return (display != null && shell != null && !display.isDisposed() && !shell.isDisposed() && !isClosing);
    }

    public static void report(Throwable e, String s) {
        logger.error(s, e);

    }

    public static void report(String s) {
        logger.error(s);
    }

    public static void report(Throwable e) {
        logger.error(e);
    }


    /**
     * Return TRUE if the platform is Linux
     *
     * @return boolean TRUE if platform is Linux
     */
    public static boolean isLinux() {
        return (SWT.getPlatform().equalsIgnoreCase("gtk"));
    }

    /**
     * Return TRUE if the platform is Mac
     *
     * @return boolean TRUE if platform is Mac
     */
    public static boolean isMac() {
        String platform = SWT.getPlatform();

        return (platform.equalsIgnoreCase("carbon") || platform.equalsIgnoreCase("cocoa"));
    }

    /**
     * Return TRUE if the platform is Solaris
     *
     * @return boolean TRUE if platform is Solaris
     */
    public static boolean isSolaris() {
        return (SWT.getPlatform().equalsIgnoreCase("motif"));
    }

    /**
     * Return TRUE if the platform is Windows
     *
     * @return boolean TRUE if platform is Windows
     */
    public static boolean isWindows() {
        return (SWT.getPlatform().equalsIgnoreCase("win32"));
    }

    /**
     * Get wether drag and drop is supported Currently: win32 / gtk / motif and Mac supported
     *
     * @return TRUE if it should be used
     */
    public static boolean useDragAndDrop() {
        return (isMac() || isWindows() || isLinux() || isSolaris());
    }

    /**
     * Get wether the Internal Browser should be used Currently: win32, gtk and mac supported
     *
     * @return TRUE if it should be used
     */
    public static boolean useInternalBrowser() {
        return (isWindows() || isLinux() || isMac());
    }

    /**
     * Get wether iText should be used. That library is responsible to generate PDF, RTF or HTML from a newsfeed. Mac is not supported, since on Mac its not possible to use SWT and AWT in one application yet.
     *
     * @return TRUE if it should be used
     */
    public static boolean useIText() {
        return (isWindows() || isLinux() || isSolaris());
    }

    /**
     * Get wether Printing should be used Currently: win32 / carbon / motif supported
     *
     * @return TRUE if it should be used
     */
    public static boolean usePrinting() {
        return (isWindows() || isSolaris() || isMac());
    }

    public static void explore(File m) {

        String mac = "open ";


        String cmd = null;
        switch(Platform.getPlatform())
        {

            case mac:
                cmd = "open ";
                if (!m.isDirectory()) cmd += "-R ";
                break;
            case win:
                cmd = "Explorer /select, ";
                break;
            case linux:
                cmd = "gnome-open PATH ";
                break;
        }

        if (cmd != null) {
            cmd += "\"" + m.getAbsolutePath() + "\"";
            System.err.println(cmd);
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public abstract String getAppName();

    public abstract String getAppNameAndVersion();

    /**
     * Startup process of org.openaudible.desktop.Application (called once at start)
     */
    public void startUp() {
        /* Language of App */

        /* Init Icons */
        PaintShop.initIcons(display);
        /* Init all components */
        initComponents();


    }

    /**
     * This method is called from the Shutdown Hook Thread in case App was shut down not the normal way (e.g. the OS is shutting down). <br />
     * It is not guaranteed, that this method will be executed in any case.
     */
    public void abnormalShutDown() {
    }

    public boolean trayIsTeasing() {
        return false;
    }

    /**
     * Get the event manager for App.
     *
     * @return Returns the commandCenter.
     */
    public CommandCenter getEventManager() {
        return commandCenter;
    }

    /**
     * Get the FakeToolTip Object
     *
     * @return FakeToolTip The fake tooltip object
     */
    public FakeToolTip getFakeToolTip() {
        return fakeToolTip;
    }

    /**
     * Method to let other obj access this object
     *
     * @return AppMenu
     */
    public AppMenu getAppMenu() {
        return appMenu;
    }

    /**
     * Check if App is currently busy loading a newsfeed or aggregation.
     *
     * @return boolean TRUE if App is busy loading
     */
    public boolean isBusyLoading() {
        return false; // return (AppStatusLine.isBusyLoading());
    }

    /**
     * Update all controlls text with i18n
     */
    public void updateI18N() {
        /* Update I18N in the menuStructure */
        appMenu.updateI18N();
        /* Pack all */
        LayoutShop.packAll(shell);
    }

    public void createEventManager() {
    }


    protected AppMenu createAppMenu() {
        return null;
    }

    /**
     * Init all components
     */
    protected void initComponents() {

        /* Build a new shell that holds the application */
        shell = new Shell(display);
        shell.setLayout(LayoutShop.createGridLayout(1, 3, 2, 3));
        shell.setText(i18n.getTranslation("APP_NAME"));
        /* On Mac do not set Shell Image since it will change the Dock Image */
        if (isWindows())
            shell.setImages(PaintShop.appIcon);
        /* Save favorites before quit */
        shell.addDisposeListener(e -> onDispose());
        /* Listen for close event to set isClosing flag */
        shell.addListener(SWT.Close, event -> onClose(event));
        /* Listen for iconify event */
        shell.addListener(SWT.Iconify, event -> onIconify());
        /* Listen for deactivate event */
        shell.addListener(SWT.Deactivate, event -> onDeactivate());

        if (GUI.isLinux()) {
            System.setProperty("SWT_GTK3", "0");
        }

        createEventManager();
        /* Fake ToolTip */
        fakeToolTip = new FakeToolTip();
        appMenu = createAppMenu();
        /* Sync controls with event manager */
    }

    /**
     * Runs the event loop for App
     */
    private void runEventLoop() {
        /*
          This is not very good style, but I will catch any exception, to log and display the message!
         */
        for (; ; ) {
            try {
                while (!shell.isDisposed()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                        if (isClosing)
                            break;
                    }
                }
            } catch (Throwable e) {
                logCritical("runEventLoop (Unforseen Exception)", e);
            }
            if (isClosing)
                break;
        }
        /* Dispose display */
        display.dispose();
    }

    public void logCritical(final String msg, final Throwable e) {
        /* Log and display Message */
        logger.warn(msg, e);
        logger.warn(e);

    }

    protected void scheduleStartupTasks() {
    }

    public void updateMenus() {
        if (isClosing)
            return;
        SWTAsync.run(new SWTAsync("updateMenus") {
            public void task() {
                appMenu.updateMenus();
            }
        });
    }

    /**
     * Called when the Shell is closed
     *
     * @param event The occuring Event
     */
    void onClose(Event event) {
        onClose(event, false);
    }

    /**
     * Called when the Shell is closed. If boolean parameter forceExit is set to TRUE, App will exit, even if "Minimize to Tray on Exit" is set to TRUE. This forced exit is called when the user has pressed the "Exit" menuitem from the "File" menu.
     *
     * @param event     The occuring Event
     * @param forceExit If TRUE, force App to exit
     */
    public void onClose(Event event, boolean forceExit) {
        /* Else: Exit application */
        isClosing = true;
        /* Save Shell bounds if not minimized to tray */
    }

    /**
     * Called when the Shell is Deactivated
     */
    void onDeactivate() {
        /* Hide the FakeToolTip that could be open in this moment */
        fakeToolTip.hide();
    }

    /**
     * Called when the Shell is disposed
     */
    protected void onDispose() {
        /* Shutdown procedure */
        shutDown();
    }

    /**
     * Called when the Shell is Iconified
     */
    void onIconify() {
    }

    /**
     * Save the user settings
     */
    public void saveUserSettings() {
    }

    public void applicationStarted() {
    }

    /**
     * Open shell
     */
    public void run() {
        shell.open();
        shell.setVisible(true);
        applicationStarted();
        /* Start the event loop to read and dispatch events */
        runEventLoop();
    }

    public String getTranslation(String tag) {
        return i18n.getTranslation(tag);
    }

    /**
     * Shut down
     */
    protected void shutDown() {
        PaintShop.disposeIcons();
    }

}