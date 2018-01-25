package org.openaudible.desktop.swt.manager.views;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.openaudible.Audible;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.AudibleRegion;
import org.openaudible.Directories;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;

import java.io.IOException;

public class Preferences extends Dialog {

    private static final Log LOG = LogFactory.getLog(Preferences.class);
    private static Preferences instance;
    final Directories dirs[] = {Directories.BASE, Directories.WEB};
    final String paths[] = new String[Directories.values().length];
    final Text dirText[] = new Text[dirs.length];
    Combo region;

    private Text email, password;
    private boolean pathsChanged = false;

    public Preferences(Shell parent) {
        super(parent);
        int style = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL
                | getDefaultOrientation();
        style &= ~SWT.CLOSE;

        setShellStyle(style);

    }

    public static void show(Shell s) {

        if (instance != null && !instance.getShell().isDisposed()) {
            instance.getShell().setActive();
            return;
        }

        try {
            Preferences p = instance = new Preferences(s);
            int result = instance.open();
            if (result==0)
            {
                try {
                    Audible.instance.save();
                } catch (IOException e) {
                    MessageBoxFactory.showError(null, "Error saving preferences");
                    e.printStackTrace();
                }
            }
        } finally {
            instance = null;
        }
    }

    private void populate() {
        email.setText(Audible.instance.getAccount().audibleUser);
        password.setText(Audible.instance.getAccount().audiblePassword);
        region.select(Audible.instance.getAccount().audibleRegion.ordinal());

        for (Text t : dirText) {
            Directories d = (Directories) t.getData();
            t.setText(d.getPath());
        }

    }

    private void fetch() {


        String u = email.getText();
        String p = password.getText();
        AudibleAccountPrefs prefs = Audible.instance.getAccount();
        boolean changed = false;

        AudibleRegion r = AudibleRegion.fromText(region.getText());

        if (!prefs.audiblePassword.equals(p)) changed = true;
        if (!prefs.audibleUser.equals(u)) changed = true;
        if (!prefs.audibleRegion.equals(r)) changed = true;
        if (changed) {
            prefs.audibleUser = u;
            prefs.audiblePassword = p;
            prefs.audibleRegion = r;
        }

        if (pathsChanged) {
            for (Directories d : dirs) {
                if (paths[d.ordinal()] != null)
                    d.setPath(paths[d.ordinal()]);
            }

            try {
                Directories.save();
                MessageBoxFactory.showGeneral(getShell(), 0, "REQUIRES_RESTART", "REQUIRES_RESTART");
            } catch (Throwable th) {
                MessageBoxFactory.showError(getShell(), "Unable to save", th.toString());
            }
        }


    }

    @Override
    protected void okPressed() {
        LOG.info("okPressed");
        fetch();
        super.okPressed();
    }


    private Text newDir(final GridComposite c, final Group group, final Directories d) {
        final Text text = GridComposite.newTextPair(group, d.displayName());
        text.setData(d);
        text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

        Button b = c.newButton(group, "Set");
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                String newPath = dialog.open();
                if (newPath != null) {
                    if (!d.getPath().equals(newPath)) {
                        paths[d.ordinal()] = newPath;
                        d.setPath(newPath);
                        text.setText(newPath);
                        pathsChanged = true;
                    }
                }
            }
        });
        return text;
    }

    private void createDirectoryGroup(GridComposite c) {
        Group group = c.newGroup("Directories", 3);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        group.setLayoutData(gd);

        int index = 0;
        for (Directories d : dirs) {
            dirText[index++] = newDir(c, group, d);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        GridComposite c = new GridComposite(parent, SWT.NONE, 1, false, GridData.FILL_HORIZONTAL);
        c.setWidthHint(c, 500);
        createAccountGroup(c);
        createDirectoryGroup(c);

        this.getShell().setText("Preferences");
        populate();

        return null;
    }

    private void createAccountGroup(GridComposite c) {

        Group group = c.newGroup("Audible Account", 3);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        region = GridComposite.newCombo(group, "Region");
        for (AudibleRegion r:AudibleRegion.values())
        {
            region.add(r.displayName());
        }
        new Label(group, 0);

        email = GridComposite.newTextPair(group, "Audible Email");
        new Label(group, 0);
        GridData gd = new GridData();
        gd.widthHint = 250;
        email.setLayoutData(gd);
        password = GridComposite.newPasswordPair(group, "Password");
        gd = new GridData();
        gd.widthHint = 250;
        email.setLayoutData(gd);
        new Label(group, SWT.NONE).setText("(optional)");
        gd = new GridData();
        gd.widthHint = 50;


        gd = new GridData();
        gd.widthHint = 150;
        region.setLayoutData(gd);

        // key.setEditable(false);
    }


    @Override
    protected Control createContents(Composite parent) {
        Control c = super.createContents(parent);
        getShell().pack(); // pack layout so it is resized

        return c;
    }


}
