package org.openaudible.desktop.swt.manager.views;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openaudible.desktop.swt.util.shop.PaintShop;
import org.openaudible.util.Console;
import org.openaudible.util.Platform;


public class LogWindow {
    public static LogWindow instance = null;
    private final Text commandLine;
    boolean cmdLine = true;
    Shell shell;
    ConsoleView textPanel;
    private static final Log LOG = LogFactory.getLog(LogWindow.class);

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

        commandLine = new Text(shell, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.VERTICAL_ALIGN_END);
        data.horizontalSpan = numCols;
        commandLine.setLayoutData(data);


        shell.addListener(SWT.Close, event -> close());

        shell.layout();
        shell.open();

        // hook in with main console.
        Console.instance.setListener(textPanel);
    }


    public void addListener(int id, Listener l) {
        shell.addListener(id, l);
    }

    private void close() {
        if (instance != null) {
            instance = null;
            Console.instance.setListener(null);
            shell.dispose();
        }
    }
}
