/*   **********************************************************************  **
 **	 	Copyright notice																											 **
 **                                                                          **
 **   (c) 2003-2005 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **																																					 **
 **		This program and the accompanying materials are made available under 	 **
 **	 	the terms of the Common Public License v1.0 which accompanies this		 **
 **	 	distribution, and is available at:																		 **
 **		http://www.rssowl.org/legal/cpl-v10.html															 **
 **																																					 **
 **   A copy is found in the file cpl-v10.html and important notices to the  **
 **   license from the team is found in the textfile LICENSE.txt distributed **
 **   in this package.                                                       **
 **	 																																				 **
 **		This copyright notice MUST APPEAR in all copies of the file!					 **
 **																																					 **
 **	 	Contributors:																													 **
 **	  	RSSOwl - initial API and implementation (bpasero@rssowl.org)				 **
 **																																					 **
 **	 **********************************************************************	 */

package org.openaudible.desktop.swt.util.shop;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.GUI;


/**
 * Factory class for some LayoutData concerns in RSSOwl.
 *
 * @author <a href="mailto:bpasero@rssowl.org">Benjamin Pasero </a>
 * @version 1.0.2
 */
public class LayoutShop {

    /**
     * This utility class constructor is hidden
     */
    private LayoutShop() {
        // Protect default constructor
    }

    /**
     * Center a shell on the monitor
     *
     * @param display The display
     * @param shell   The shell to center
     */
    public static void centerShell(Display display, Shell shell) {
        Rectangle displayBounds = display.getPrimaryMonitor().getBounds();
        Rectangle shellBounds = shell.getBounds();
        int x = displayBounds.x + (displayBounds.width - shellBounds.width) >> 1;
        int y = displayBounds.y + (displayBounds.height - shellBounds.height) >> 1;
        shell.setLocation(x, y);
    }

    public static void centerShell(Shell shell) {
        Rectangle displayBounds = shell.getDisplay().getPrimaryMonitor().getBounds();
        Rectangle shellBounds = shell.getBounds();
        int x = displayBounds.x + (displayBounds.width - shellBounds.width) >> 1;
        int y = displayBounds.y + (displayBounds.height - shellBounds.height) >> 1;
        shell.setLocation(x, y);
    }

    /**
     * Create a new FillLayout with the given parameters
     *
     * @param marginWidth  Margin width in pixel
     * @param marginHeight Margin height in pixel
     * @return FillLayout New FillLayout with the given parameters
     */
    public static FillLayout createFillLayout(int marginWidth, int marginHeight) {
        FillLayout f = new FillLayout();
        f.marginHeight = marginHeight;
        f.marginWidth = marginWidth;
        return f;
    }

    /**
     * Create a new GridLayout with the given parameters
     *
     * @param rows The number of rows
     * @return GridLayout New GridLayout with the given parameters
     */
    public static GridLayout createGridLayout(int rows) {
        return createGridLayout(rows, 5, 5, 5, 5, false);
    }

    /**
     * Create a new GridLayout with the given parameters
     *
     * @param rows        The number of rows
     * @param marginWidth Margin width in pixel
     * @return GridLayout New GridLayout with the given parameters
     */
    public static GridLayout createGridLayout(int rows, int marginWidth) {
        return createGridLayout(rows, marginWidth, 5, 5, 5, false);
    }

    /**
     * Create a new GridLayout with the given parameters
     *
     * @param rows         The number of rows
     * @param marginWidth  Margin width in pixel
     * @param marginHeight Margin height in pixel
     * @return GridLayout New GridLayout with the given parameters
     */
    public static GridLayout createGridLayout(int rows, int marginWidth, int marginHeight) {
        return createGridLayout(rows, marginWidth, marginHeight, 5, 5, false);
    }

    /**
     * Create a new GridLayout with the given parameters
     *
     * @param rows                  The number of rows
     * @param marginWidth           Margin width in pixel
     * @param marginHeight          Margin height in pixel
     * @param makeColumnsEqualWidth TRUE if columns should be equals in size
     * @return GridLayout New GridLayout with the given parameters
     */
    public static GridLayout createGridLayout(int rows, int marginWidth, int marginHeight, boolean makeColumnsEqualWidth) {
        return createGridLayout(rows, marginWidth, marginHeight, 5, 5, makeColumnsEqualWidth);
    }

    /**
     * Create a new GridLayout with the given parameters
     *
     * @param rows            The number of rows
     * @param marginWidth     Margin width in pixel
     * @param marginHeight    Margin height in pixel
     * @param verticalSpacing Vertical spacing in pixel
     * @return GridLayout New GridLayout with the given parameters
     */
    public static GridLayout createGridLayout(int rows, int marginWidth, int marginHeight, int verticalSpacing) {
        return createGridLayout(rows, marginWidth, marginHeight, verticalSpacing, 5, false);
    }

    /**
     * Create a new GridLayout with the given parameters
     *
     * @param rows                  The number of rows
     * @param marginWidth           Margin width in pixel
     * @param marginHeight          Margin height in pixel
     * @param verticalSpacing       Vertical spacing in pixel
     * @param horizontalSpacing     Horizontal spacing in pixel
     * @param makeColumnsEqualWidth TRUE if columns should be equals in size
     * @return GridLayout New GridLayout with the given parameters
     */
    public static GridLayout createGridLayout(int rows, int marginWidth, int marginHeight, int verticalSpacing, int horizontalSpacing, boolean makeColumnsEqualWidth) {
        GridLayout g = new GridLayout(rows, false);
        g.marginHeight = marginHeight;
        g.marginWidth = marginWidth;
        g.verticalSpacing = verticalSpacing;
        g.horizontalSpacing = horizontalSpacing;
        g.makeColumnsEqualWidth = makeColumnsEqualWidth;
        return g;
    }

    /**
     * Pack all controls and sub composites
     *
     * @param control Control to start
     */
    public static void packAll(Control control) {
        if (control instanceof Composite) {
            Control[] childs = ((Composite) control).getChildren();
            for (Control child : childs) {
                packAll(child);
            }
            ((Composite) control).layout();
        }
    }

    /**
     * Sets the initial location to use for the shell. The default implementation
     * centers the shell horizontally (1/2 of the difference to the left and 1/2
     * to the right) and vertically (1/3 above and 2/3 below) relative to the
     * parent shell
     *
     * @param shell The shell to set the location
     */
    public static void positionShell(Shell shell) {
        positionShell(shell, true);
    }

    /**
     * Sets the initial location to use for the shell. The default implementation
     * centers the shell horizontally (1/2 of the difference to the left and 1/2
     * to the right) and vertically (1/3 above and 2/3 below) relative to the
     * parent shell
     *
     * @param shell       The shell to set the location
     * @param computeSize If TRUE, initialSize is computed from the Shell
     */
    public static void positionShell(Shell shell, boolean computeSize) {
        positionShell(shell, computeSize, 0);
    }

    /**
     * Sets the initial location to use for the shell. The default implementation
     * centers the shell horizontally (1/2 of the difference to the left and 1/2
     * to the right) and vertically (1/3 above and 2/3 below) relative to the
     * parent shell
     *
     * @param shell           The shell to set the location
     * @param computeSize     If TRUE, initialSize is computed from the Shell
     * @param sameDialogCount In the case the same dialog is opened more than
     *                        once, do not position the Shells on the same position. The sameDialogCount
     *                        integer tells how many dialogs of the same kind are already open. This
     *                        number is used to move the new dialog by some pixels.
     */
    public static void positionShell(Shell shell, boolean computeSize, int sameDialogCount) {
        Rectangle containerBounds = GUI.shell.getBounds();
        Point initialSize = (computeSize == true) ? shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true) : shell.getSize();
        int x = Math.max(0, containerBounds.x + (containerBounds.width - initialSize.x) >> 1);
        int y = Math.max(0, containerBounds.y + (containerBounds.height - initialSize.y) / 3);
        shell.setLocation(x + sameDialogCount * 20, y + sameDialogCount * 20);
    }

    /**
     * Set invisible labels as spacer to a composite. The labels will grab
     * vertical space.
     *
     * @param composite The control to add the spacer into
     * @param cols      Is used as horizontal span in the GridData
     * @param rows      Number of labels that are created
     */
    public static void setDialogSpacer(Composite composite, int cols, int rows) {
        for (int a = 0; a < rows; a++) {
            Label spacer = new Label(composite, SWT.NONE);
            spacer.setLayoutData(LayoutDataShop.createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, cols));
            spacer.setFont(FontShop.dialogFont());
        }
    }

    /**
     * Recursivly update layout for given control and childs
     *
     * @param control The control to set the layout
     */
    public static void setLayoutForAll(Control control) {
        if (control instanceof Composite) {
            Control[] childs = ((Composite) control).getChildren();
            for (Control child : childs) {
                setLayoutForAll(child);
            }
            ((Composite) control).layout();
        }
    }
}