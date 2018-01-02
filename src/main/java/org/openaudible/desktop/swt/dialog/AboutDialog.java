package org.openaudible.desktop.swt.dialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.manager.Version;
import org.openaudible.desktop.swt.manager.views.GridComposite;
import org.openaudible.desktop.swt.util.shop.FontShop;
import org.openaudible.desktop.swt.util.shop.PaintShop;
import org.openaudible.util.ManifestReader;


/**
 * Class displays a splash screen with info
 */
public class AboutDialog extends Window implements Version, Listener {
    final static String splashname = "images/cover.png";
    Color bgColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    Image splashImage = null;

    private AboutDialog(Shell parentShell) {
        super(parentShell);
    }

    public static void doAbout(Shell parent) {
        final Display display = Display.getCurrent();
        AboutDialog ab = new AboutDialog(parent);
        ab.open();
    }


    protected int getShellStyle() {
        return SWT.NONE;
    }

    protected void initializeBounds() {
        super.initializeBounds();
        getShell().setBackground(bgColor);
        getShell().addListener(SWT.MouseDown, this);
        Rectangle r = GUI.shell.getBounds();
        getShell().setLocation(r.x + 160, r.y + 160);
        getShell().addListener(SWT.Deactivate, this);
    }

    protected Control createContents(Composite parent) {
        GridComposite c = new GridComposite(parent, SWT.NONE);
        c.initLayout();
        splashImage = PaintShop.getImage(splashname);
        c.newImage(splashImage);
        c.addListener(SWT.MouseDown, this);
        String compileDate = ManifestReader.instance.getBuildVersion(); // from jar's manifest, if available
        String build = "Build:  " + Version.appName+" " + Version.appVersion;

        // c.newLabel(Version.appName).setFont(FontShop.dialogFontBold());
        c.newLabel(build.trim()).setFont(FontShop.dialogFont());
        if (!compileDate.isEmpty())
            c.newLabel("Released: "+ compileDate.trim()).setFont(FontShop.dialogFont());

        if (false) {
            Link link = new Link(c, SWT.NONE);
            link.setText(Version.appLink);
            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    String u = event.text;
                    System.out.println(u);
                }
            });
        }
        c.newLabel("");
        c.newLabel("An open source project").setFont(FontShop.dialogFont());
        c.newLabel("github.com/openaudible").setFont(FontShop.dialogFont());

        c.newLabel("");
        c.newLabel("Not affiliated with audible.com").setFont(FontShop.dialogFont());
        return null;
    }

    public void handleEvent(Event event) {
        switch (event.type) {
            case SWT.MouseDown:
            case SWT.Deactivate:
                this.close();
                Display.getCurrent().wake();
                break;
            case SWT.Dispose:
                PaintShop.disposeImage(splashname);
                break;
            default:
                break;
        }
    }
}