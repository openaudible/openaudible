package org.openaudible.desktop.swt.dialog;


/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.manager.Version;
import org.openaudible.desktop.swt.util.shop.FontShop;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.util.ArrayList;


/**
 * Class displays a splash screen with info
 */
public class AboutDialog extends Window implements Version, Listener {
    final static String splashname = "splash.png";
    public boolean painted = false;
    ArrayList<String> bodyText = new ArrayList<String>(); // strings to draw
    Font bigFont;
    boolean moreInfo = false;
    boolean demo;
    Color bgColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    Image splashImage = null;

    private AboutDialog(Shell parentShell, String img, long ex, boolean more) {
        super(parentShell);
        moreInfo = more;
        painted = false;
        String fontName = FontShop.dialogFont().getFontData()[0].getName();
        bigFont = new Font(Display.getCurrent(), fontName, 14, 0);
        String appName = Version.longAppName + " " + Version.MAJOR_VERSION;

        String copyright = (char) 169 + " " + COPYRIGHT_YEAR + " All Rights Reserved";
        bodyText.add("");
        bodyText.add(appName);
        bodyText.add(copyright);
        bodyText.add("Build " + INT_VERSION);
    }

    public static void doAbout(Shell parent, long ex) {
        final Display display = Display.getCurrent();
        AboutDialog ab = new AboutDialog(parent, splashname, ex, true);
        ab.open();
    }

    public static AboutDialog create(Shell parent, long ex) {
        AboutDialog ab = new AboutDialog(parent, splashname, ex, false);
        ab.open();
        ab.painted = true;
        return ab;
    }

    protected int getShellStyle() {
        return SWT.NONE;
    }

    protected void initializeBounds() {
        // getShell().setSize(500, 500);
        super.initializeBounds();
        getShell().setBackground(bgColor);
        getShell().addListener(SWT.MouseDown, this);
        Rectangle r = GUI.shell.getBounds();
        /*		r.width = 260;
                r.height = 150;
				r.x += 60;
				r.y += 60;
		*/
        getShell().setLocation(r.x + 160, r.y + 160);
        getShell().addListener(SWT.Deactivate, this);


    }

    protected Control createContents(Composite parent) {
        GridLayout gl = new GridLayout();
        int m = 40;
        gl.marginBottom = m - 10;
        gl.marginTop = m - 10;
        gl.marginLeft = m;
        gl.marginRight = m;
        // gl.marginWidth = m;
        parent.setLayout(gl);
        GridData gd = new GridData(SWT.CENTER);
        Label splash = new Label(parent, SWT.NONE);
        // splashImage = PaintShop.getImage("splash.png");
        // splash.setImage(splashImage);
        splash.setBackground(bgColor);
        splash.setLayoutData(gd);
        // gd.widthHint = splashImage.getBounds().width+40;
        splash.addListener(SWT.MouseDown, this);
        for (String b : bodyText) {
            Label l = new Label(parent, SWT.NONE);
            l.setText(b);
            l.setBackground(bgColor);
            l.addListener(SWT.MouseDown, this);
            gd = new GridData(SWT.CENTER);
            l.setLayoutData(gd);
        }
        getShell().pack();
        return null;
    }

    public void handleEvent(Event event) {
        switch (event.type) {
            case SWT.MouseDown:
            case SWT.Deactivate:
                // stop will get us out of the loop
                // stop = System.currentTimeMillis() -1;
                // hide control will make it (seem) immediate
                this.close();
                // shell.setVisible(false);
                // this will get us out of the event loop a little sooner.
                Display.getCurrent().wake();
                break;
            case SWT.Dispose:
                bigFont.dispose();
                PaintShop.disposeImage(splashname);
                break;
            default:
                break;
        }
    }
}