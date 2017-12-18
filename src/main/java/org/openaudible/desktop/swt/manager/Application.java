package org.openaudible.desktop.swt.manager;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.openaudible.Audible;
import org.openaudible.books.BookNotifier;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.gui.progress.ProgressDialog;
import org.openaudible.desktop.swt.gui.progress.ProgressTask;
import org.openaudible.desktop.swt.manager.menu.AppMenu;
import org.openaudible.desktop.swt.manager.menu.CommandCenter;
import org.openaudible.desktop.swt.manager.views.MainWindow;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.io.File;


public class Application extends GUI {
    public static Application instance;
    public boolean quitting = false;
    AudibleGUI audibleGUI = new AudibleGUI();

    public Application(Display d) {
        super(d);
        instance = this;

        try {

            /**
             * Use Sleak if (false) { DeviceData data = new DeviceData(); data.tracking = true; display = new Display(data); Sleak sleak = new Sleak(); sleak.open(); }
             */
            // shell = new Shell(display);
            // if (shell == null) throw new Exception("no shell");
        } catch (Throwable e) {
            report(e);
        }
    }

    // classes we want to check the integrity of

    /**
     * Get the correct formatted public version identifier
     *
     * @return String The public version
     */
    public final static String getPublicVersion() {
        return Version.MAJOR_VERSION; // + " " + MINOR_VERSION;
    }

    public static String buildInfo() {
        return gui.getAppName();
    }

    public void log(Object t) {
        logger.info(t);
    }

    public String getAppName() {
        return Version.shortAppName;
    }

    public String getAppNameAndVersion() {
        return getAppName() + " " + Version.MAJOR_VERSION;
    }

    public Composite createMainBody(Composite parent) {
        return new MainWindow(parent);
    }

    void createLayout() {
        GridLayout mainGridLayout = new GridLayout(1, false);
        mainGridLayout.marginHeight = 0;
        mainGridLayout.marginWidth = 0;

        shell.setLayout(mainGridLayout);
        createMainBody(shell);
    }

    protected AppMenu createAppMenu() {
        appMenu = new AppMenu(this, shell, commandCenter);
        return appMenu;
    }

    public void initComponents() {
        super.initComponents();
        instance = this;
        try {

            File b = new File("M:\\Books\\Audible");
            File b2 = new File("D:\\Audible");
            audibleGUI.init();


        } catch (Throwable th) {
            logger.error("error", th);
        }
        if (!GUI.isMac())
            shell.setImages(PaintShop.appIcon);
        shell.setText(getAppName());
        createLayout();
        shell.pack();
        shell.setBounds(20, 25, 790, 790);


    }

    public void applicationStarted() {
        super.applicationStarted();

        final Audible audible = Audible.instance;

        ProgressTask task = new ProgressTask("Loading") {
            int books = 0;

            public void setTask(String t, String s) {
                super.setTask(t, s);
                if (false && audible.getBookCount() != books) {
                    books = audible.getBookCount();
                    if (books % 3 == 0)
                        BookNotifier.getInstance().booksUpdated();

                }
            }


            public void run() {
                try {
                    audible.setProgress(this);
                    this.setTask("Loading");
                    audible.load();

                    this.setTask("Finding Audible Files");
                    audible.findOrphanedFiles(this);
                    this.setTask("Updating");
                    BookNotifier.getInstance().booksUpdated();

                    audible.updateFileCache();
                    // audibleGUI.updateFileCache();

                    BookNotifier.getInstance().booksUpdated();


                } catch (Exception e) {
                    MessageBoxFactory.showError(null, e);// , "loading library");
                } finally {
                    audible.setProgress(null);
                }

            }
        };
        ProgressDialog.doProgressTask(task);
    }

    protected void shutDown() {
        quit();
        super.shutDown();
    }

    public void quit() {
        try {
            synchronized (this) {
                if (quitting)
                    return;
                quitting = true;
            }

            SWTAsync.quit = true;
            audibleGUI.audible.quit();

            GUI.isClosing = true;
            println("application has been stopped.");
            if (!shell.isDisposed()) {
                if (isMac())
                    shell.setVisible(false);
                shell.dispose();
            }

        } catch (Throwable e) {
            report(e);
        }
    }

    public void onClose(Event event, boolean forceExit) {
        if (!forceExit) {
            boolean reallyQuit = commandCenter.reallyQuit();
            if (!reallyQuit) {
                if (event != null)
                    event.doit = false;
                return;
            }
        }
        super.onClose(event, forceExit);
        quit();
    }

    public void createEventManager() {
        /** Create the event manager */
        commandCenter = new CommandCenter(display, shell, this);
    }

}
