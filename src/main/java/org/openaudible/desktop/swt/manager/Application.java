package org.openaudible.desktop.swt.manager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
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
        assert (instance == null);
        instance = this;
    }


    public void log(Object t) {
        logger.info(t);
    }

    public String getAppName() {
        return Version.appName;
    }

    public String getAppNameAndVersion() {
        return getAppName() + " " + Version.appVersion;
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
        /* Create the event manager */
        commandCenter = new CommandCenter(display, shell, this);
    }


    public void exportBookList() {
        try {
            String ext = "*.txt";
            String name = "Text File";
            FileDialog dialog = new FileDialog(shell, SWT.SAVE);
            dialog.setFilterNames(new String[]{name});
            dialog.setFilterExtensions(new String[]{ext});
            dialog.setFileName("books.txt");
            String path = dialog.open();
            if (path != null)
                audibleGUI.audible.export(new File(path));
        } catch (Exception e) {


            MessageBoxFactory.showError(shell, e.getMessage());


        }

    }


}
