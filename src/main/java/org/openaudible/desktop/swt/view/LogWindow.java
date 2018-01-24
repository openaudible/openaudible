package org.openaudible.desktop.swt.view;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.desktop.swt.util.shop.PaintShop;
import org.openaudible.util.Platform;

import java.util.logging.Level;


public class LogWindow {
    public static LogWindow instance = null;
    private static final String lineSeparator = System.getProperty("line.separator");
    Shell shell;
    ConsoleView textPanel;

    public static boolean isOpen() {
        return getOpenShell() != null;
    }

    public static void show() {
        if (instance != null) {
            if (instance.shell.isDisposed()) {
                instance = null;
            }
        }
        if (instance == null) {
            instance = new LogWindow("Log Window");
        } else {
            instance.shell.setActive();
        }

        instance.textPanel.scrollToEnd();

    }

    public static Shell getOpenShell() {
        Shell s = (instance != null) ? instance.shell : null;
        if (s != null) {
            return s;
        }
        return null;
    }


    private LogWindow() {
    }


    public static void log(int componentId, Level level, int color, String text) {
        LogWindow w = instance;
        if (w != null)
            w.textPanel.log(componentId, level, color, text);
    }



    public LogWindow(String title) {
        shell = new Shell(SWT.BORDER | SWT.TITLE | SWT.CLOSE | SWT.RESIZE);
        if (!Platform.isMac()) {
            shell.setImages(PaintShop.appIcon);
        }
        shell.setText(title);
        int numCols = 1;
        GridLayout layout = new GridLayout();
        layout.numColumns = numCols;
        shell.setLayout(layout);
        GridData data;
        textPanel = new ConsoleView();
        Composite textView = textPanel.initialize(shell);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = numCols;
        textView.setLayoutData(data);


        Listener closeListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                close();
            }
        };

        shell.addListener(SWT.Close, closeListener);

        shell.layout();
        shell.open();
    }

    public void addListener(int id, Listener l) {
        shell.addListener(id, l);
    }

    private void close() {
        if (instance != null) {
            instance = null;
            if (textPanel != null)
                textPanel.delete();
            shell.dispose();
        }
    }

    public static void quit() {
        if (instance != null) {
            instance.close();
        }

    }
}
