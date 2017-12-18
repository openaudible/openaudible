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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;

/**
 * Factory class for some LayoutData concerns in RSSOwl
 *
 * @author <a href="mailto:bpasero@rssowl.org">Benjamin Pasero </a>
 * @version 1.0.2
 */
public class LayoutDataShop {

    /**
     * This utility class constructor is hidden
     */
    private LayoutDataShop() {
        //Protect default constructor
    }

    /**
     * Create a new FormData with the given Parameters
     *
     * @param marginLeft   Margin in pixel to the left
     * @param marginRight  Margin in pixel to the right
     * @param marginTop    Margin in pixel to the top
     * @param marginBottom Margin in pixel to the bottom
     * @return FormData with the given parameters
     */
    public static FormData createFormData(int marginLeft, int marginRight, int marginTop, int marginBottom) {
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, marginTop);
        formData.left = new FormAttachment(0, marginLeft);
        formData.right = new FormAttachment(100, marginRight);
        formData.bottom = new FormAttachment(100, marginBottom);
        return formData;
    }

    /**
     * Create a new GridData with the given parameters
     *
     * @param style          GridData style
     * @param horizontalSpan Horizontal span
     * @return GridData with the given parameters
     */
    public static GridData createGridData(int style, int horizontalSpan) {
        return createGridData(style, horizontalSpan, SWT.DEFAULT);
    }

    /**
     * Create a new GridData with the given parameters
     *
     * @param style          GridData style
     * @param horizontalSpan Horizontal span
     * @param widthHint      Width hint in pixel
     * @return GridData with the given parameters
     */
    public static GridData createGridData(int style, int horizontalSpan, int widthHint) {
        return createGridData(style, horizontalSpan, widthHint, SWT.DEFAULT);
    }

    /**
     * Create a new GridData with the given parameters
     *
     * @param style          GridData style
     * @param horizontalSpan Horizontal span
     * @param widthHint      Width hint in pixel
     * @param heightHint     Height hint in pixel
     * @return GridData with the given parameters
     */
    public static GridData createGridData(int style, int horizontalSpan, int widthHint, int heightHint) {
        GridData g = new GridData(style);
        g.horizontalSpan = horizontalSpan;
        g.widthHint = widthHint;
        g.heightHint = heightHint;
        return g;
    }
}