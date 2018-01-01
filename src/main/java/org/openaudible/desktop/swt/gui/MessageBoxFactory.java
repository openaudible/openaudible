package org.openaudible.desktop.swt.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.desktop.swt.util.shop.PaintShop;


/**
 * Class for displaying a MessageBox in an easy way in just one method call.
 *
 * @author <a href="mailto:bpasero@rssowl.org">Benjamin Pasero </a>
 * @version 1.0.2
 */
public class MessageBoxFactory {

    /**
     * This utility class constructor is hidden
     */
    private MessageBoxFactory() {
        // Protect default constructor
    }

    /**
     * Show an error dialog
     *
     * @param shell The shell
     * @param e     The exception
     */
    public static void showError(Shell shell, Throwable e) {
        String m = e.toString();
        showError(shell, m);
    }

    public static void showError(final Shell shell, final String e) {
        showError(shell, "MESSAGE_BOX_TITLE_ERROR", e);
    }

    public static void showError(final Shell shell, final String title, final String e) {

        SWTAsync.slow(new SWTAsync("showError") {
            public void task() {
                MessageDialog.openError(shell, GUI.i18n.getTranslation(title), e);

            }
        });
    }

    /**
     * Show a message box
     *
     * @param shell   The shell
     * @param style   Style of the message box
     * @param title   Title of the message box
     * @param message Message of the message box
     * @return int Key that the user has clicked
     */
    public static int showMessage(Shell shell, int style, String title, String message) {
        SWTAsync.assertGUI();
        /* Create new Shell in case Shell was disposed before */
        if (shell == null || shell.isDisposed())
            shell = new Shell(Display.getCurrent());

        MessageBox box = new MessageBox(shell, style);
        box.setText(title);
        box.setMessage(message);
        return box.open();
    }


    public static boolean showGeneralYesNo(Shell shell, String title, String message) {
        String buttons[] = {"BUTTON_YES", "BUTTON_NO"};
        int result = showGeneral(shell, title, message, buttons, 0, null);
        return (result == 0);
    }


    public static void showGeneral(final Shell shell, int flags, String title, String message) {
        SWTAsync.slow(new SWTAsync("showError") {
            public void task() {
                Shell s = shell;

                /* Create new Shell in case Shell was disposed before */
                if (s == null || s.isDisposed())
                    s = new Shell(Display.getCurrent());
                // ICON_QUESTION
                MessageBox box = new MessageBox(s, flags);
                box.setText(GUI.i18n.getTitle(title));
                box.setMessage(GUI.i18n.getMessage(message));
                int i = box.open();
            }
        });

    }


    // uses i18n translations automatically...
    public static int showGeneral(Shell shell, String title, String message, String rawButtons[], int defButton, Image dialogImage) {
        SWTAsync.assertGUI();        // must run in desktop thread. Also, returns value, so will block in desktop thread.
        if (message == null) message = "";
        if (title == null) title = "";

        /* Create new Shell in case Shell was disposed before */
        if (shell == null || shell.isDisposed())
            shell = new Shell(Display.getCurrent());
        Image shellImage = PaintShop.getShellImage();

        String buttons[] = new String[rawButtons.length];
        for (int x = 0; x < rawButtons.length; x++) {
            buttons[x] = GUI.i18n.getTranslation(rawButtons[x]);
        }
        message = GUI.i18n.getTranslation(message);

        MessageDialog d = new MessageDialog(shell, GUI.i18n.getTranslation(title), shellImage, message, MessageDialog.NONE, buttons, defButton);


        int v = d.open();
        return v;

    }


}

