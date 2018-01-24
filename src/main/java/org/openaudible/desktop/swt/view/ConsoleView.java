package org.openaudible.desktop.swt.view;


import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.SWTAsync;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;


public class ConsoleView {
    private Display display;
    private StyledText consoleText;

    private static Color[] colors;
    private static ConsoleView instance = null;
    private static final int PREFERRED_LINES = 256;
    private static final int MAX_LINES = 4000 + PREFERRED_LINES;
    private static final List<LogInfo> logHistory;
    private static final SimpleDateFormat dateFormatter;
    private static final FieldPosition formatPos;

    private Clipboard clipboard;

    // private static final AEMonitor classMon = new AEMonitor("ConsoleView:S");
    static {
        logHistory = new LinkedList<LogInfo>();
        dateFormatter = new SimpleDateFormat("[h:mm:ss]  ");
        formatPos = new FieldPosition(0);
    }

    /**
     * Preinitializes the ConsoleView logging handler. This method must be
     * called before a ConsoleView instance is created.
     */
    public static void preInitialize() {
        // LGLogger.setListener(new LogEventHandler());
    }

    /**
     * <p>
     * Sets the singleton ConsoleView instance
     * </p>
     * <p>
     * If records exist in the session log history, it is logged to the text
     * view
     * </p>
     *
     * @param view ConsoleView instance
     * @throws IllegalStateException If this method is called when the singleton instance has
     *                               already been set
     */
    private static void setInstance(ConsoleView view) throws IllegalStateException {
        try {
            // classMon.enter();
            if (ConsoleView.instance != null) {
                throw new IllegalStateException("Only one ConsoleView is allowed");
            }
            ConsoleView.instance = view;
            // latent initialization
            if (colors == null) {
                /*
                 * Blue: 0=255,255,255 Blue: 1=226,240,255 Blue: 2=198,226,255
                 * Blue: 3=169,212,254 Blue: 4=141,198,254 Blue: 5=113,184,255
                 * Blue: 6=84,170,254 Blue: 7=56,156,255 Blue: 8=28,142,255
                 * Blue: 9=0,128,255
                 */

            }

            // prefill history text
            Iterator<LogInfo> iter = logHistory.iterator();
            for (int i = 0; i < logHistory.size(); i++) {
                ConsoleView.instance.doLog(iter.next(), true);
            }
            if (logHistory.size() > 0) {
                SWTAsync.run(new SWTAsync("console setTopIndex") {
                    @Override
                    public void task() {
                        ConsoleView ce = ConsoleView.instance;
                        if (ce != null && ce.consoleText != null && !ce.consoleText.isDisposed()) {
                            ce.consoleText.setTopIndex(ce.consoleText.getLineCount() - 1);
                        }
                    }
                });
            }
            // reset state
            ConsoleView.instance.consoleText.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent event) {
                    if (ConsoleView.instance != null) {
                        if (ConsoleView.instance.clipboard != null) {
                            ConsoleView.instance.clipboard.dispose();
                        }

                    }

                    ConsoleView.instance = null;
                }
            });
        } finally {
            // classMon.exit();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gudy.azureus2.ui.swt.IView#initialize(org.eclipse.swt.widgets.Composite
     * )
     */
    public StyledText initialize(Composite composite) {
        display = composite.getDisplay();
        consoleText = new StyledText(composite, SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);

        setInstance(this);
        Menu copyMenu = new Menu(consoleText);
        MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
        copyItem.addSelectionListener(new SelectionListener() {
            /*
             * @see SelectionListener.widgetSelected (SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                copyToClipboard();
            }

            /*
             * @see SelectionListener.widgetDefaultSelected(SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                copyToClipboard();
            }
        });
        copyItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
        consoleText.setMenu(copyMenu);
        consoleText.getShell().setMenu(copyMenu);

        consoleText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'a' || e.keyCode == 'A'))
                    consoleText.setSelection(new Point(0, consoleText.getText().length()));
                if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'c' || e.keyCode == 'C'))
                    copyToClipboard();
                if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'w' || e.keyCode == 'W'))
                    consoleText.getShell().close();
            }
        });

        return consoleText;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gudy.azureus2.ui.swt.IView#getComposite()
     */
    public StyledText getStyledText() {
        return consoleText;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gudy.azureus2.ui.swt.IView#delete()
     */
    public void delete() {
        consoleText.dispose();
        instance = null;
    }

    ArrayList<LogInfo> queue = new ArrayList<LogInfo>();

    private void _toConsole(final LogInfo logInfo, final boolean supressScrolling) {
        if (consoleText == null || consoleText.isDisposed())
            return;
        // System.out.println(sb.getSelection()+ "/" +
        // (sb.getMaximum()
        // - sb.getThumb()));

    }

    private void doLog(final LogInfo logInfo, final boolean supressScrolling) {
        if (display == null || display.isDisposed())
            return;

        boolean needUpdate = false;
        synchronized (queue) {
            queue.add(logInfo);
            if (queue.size() == 1)
                needUpdate = true;
        }
        if (!needUpdate)
            return;

        // if (logInfo.level.intValue() < show[logInfo.id].intValue())
        // return;
        SWTAsync.run(new SWTAsync("doLog") {
            @Override
            public void task() {
                try {
                    ArrayList<LogInfo> copy;
                    synchronized (queue) {
                        if (queue.size() == 0) {
                            return;

                        }
                        copy = new ArrayList<LogInfo>();
                        copy.addAll(queue);
                        queue.clear();
                    }

                    if (consoleText == null || consoleText.isDisposed())
                        return;

                    ScrollBar sb = consoleText.getVerticalBar();
                    boolean autoScroll = !supressScrolling && (sb.getSelection() == (sb.getMaximum() - sb.getThumb()));
                    StringBuffer buf = new StringBuffer();
                    int nbLines = consoleText.getLineCount();
                    if (nbLines + copy.size() > MAX_LINES)
                        consoleText.replaceTextRange(0, consoleText.getOffsetAtLine(PREFERRED_LINES), ""); //$NON-NLS-1$

                    for (LogInfo l : copy) {
                        dateFormatter.format(l.timestamp, buf, formatPos);
                        buf.append(l.text).append('\n');

                        if (buf.length() > 10240) {
                            consoleText.append(String.valueOf(buf));
                            buf.setLength(0);
                        }
                    }

                    consoleText.append(String.valueOf(buf));
                    // nbLines = consoleText.getLineCount();
                    // consoleText.setLineBackground(nbLines - 2, 1, colors[logInfo.color]);

                    if (autoScroll)
                        consoleText.setSelection(consoleText.getText().length());

                } catch (Throwable t) {
                    // Have seen outofmemory error here...
                    t.printStackTrace();
                    // consoleText.dispose();
                }

            }
        });
    }

    public void log(int componentId, Level level, int color, String text) {
        LogInfo info = new LogInfo(componentId, level, text);

        synchronized (logHistory) {
            logHistory.add(info);
        }
        if (ConsoleView.instance != null) {
            doLog(info, false);
        }

    }

    public int getHistorySize() {
        return logHistory.size();
    }

    public void clear() {
        SWTAsync.assertGUI();
        consoleText.setText("");
        consoleText.setSelection(0);
    }

    public void scrollToEnd() {
        SWTAsync.assertGUI();

        ScrollBar sb = consoleText.getVerticalBar();
        if (sb != null) {
            sb.setSelection(sb.getMaximum());
        }
    }

    private static class LogInfo {
        private final Level level;
        private final int id;
        private final int color;
        private final String text;
        private final Date timestamp;

        public LogInfo(int id, Level level, String text, Date timestamp) {
            this.id = id;
            this.color = id;
            this.level = level;
            this.text = text;
            this.timestamp = timestamp;
        }

        public LogInfo(int id, Level level, String text) {
            this.id = id;
            this.color = id;
            this.level = level;
            this.text = text;
            this.timestamp = new Date();
        }
    }

    /**
     * Copy the contents of the statuses to the clipboard.
     */
    public void copyToClipboard() {
        // consoleText.copy(); // not working?

        if (clipboard == null) {
            clipboard = new Clipboard(consoleText.getDisplay());// clipboard.dispose();
        }
        String text = consoleText.getSelectionText();
        if (text.length() == 0)
            text = consoleText.getText();
        text = text.replace((char) 0, ' '); // for some reason we have null
        // (char)0 chars that terminate the
        // clipboard...

        if (text.length() > 0) {
            clipboard.setContents(new Object[]{text}, new Transfer[]{TextTransfer.getInstance()});
        }
    }

}
