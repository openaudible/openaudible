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
import org.openaudible.desktop.swt.manager.views.GridComposite;
import org.openaudible.desktop.swt.util.shop.FontShop;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.util.ArrayList;


/**
 * Class displays a splash screen with info
 */
public class AboutDialog extends Window implements Version, Listener {
    final static String splashname = "48x48.png";
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


    protected int getShellStyle()
    {
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

        String copyright = (char) 169 + " " + COPYRIGHT_YEAR + " All Rights Reserved";
        String build = "Build " + Version.MAJOR_VERSION+" " + INT_VERSION;


        c.newLabel(Version.longAppName).setFont(FontShop.dialogFontBold());
        c.newLabel(build).setFont(FontShop.dialogFont());
        c.newLabel(copyright).setFont(FontShop.dialogFont());

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
                PaintShop.disposeImage(splashname);
                break;
            default:
                break;
        }
    }
}