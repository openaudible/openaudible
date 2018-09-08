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

package org.openaudible.desktop.swt.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.desktop.swt.util.shop.LayoutShop;
import org.openaudible.desktop.swt.util.shop.WidgetShop;


/**
 * A fake tooltip simulates the native ToolTip that shows on some controls when
 * hovering over with the mouse. The Tooltip is static since at the same time
 * only one Tooltip should be visible.
 *
 * @author <a href="mailto:bpasero@rssowl.org">Benjamin Pasero </a>
 * @version 1.0.2
 */
public class FakeToolTip {
	static Shell fakeToolTip;
	static Label fakeToolTipLabel;
	
	/**
	 * Hide the Tooltip
	 */
	public void hide() {
		if (WidgetShop.isset(fakeToolTip) && fakeToolTip.isVisible())
			fakeToolTip.setVisible(false);
	}
	
	/**
	 * Create the Shell and Label for the fake tooltip showing URLs
	 */
	private void createFakeToolTip() {
		fakeToolTip = new Shell(GUI.shell, SWT.ON_TOP);
		fakeToolTip.setLayout(LayoutShop.createFillLayout(2, 2));
		fakeToolTip.setForeground(GUI.display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fakeToolTip.setBackground(GUI.display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		fakeToolTipLabel = new Label(fakeToolTip, SWT.NONE);
		fakeToolTipLabel.setForeground(GUI.display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fakeToolTipLabel.setBackground(GUI.display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}
	
	/**
	 * Either show or hide the fake tooltip. The tooltip will become visible in
	 * case the given tooltip is a valid URL (non strict match).
	 *
	 * @param tooltip The new value of the Tooltip
	 * @param control The control the tooltip is appearing on
	 */
	public void updateFakeTooltip(String tooltip, Control control) {
		Rectangle rect = control.getMonitor().getBounds();
		updateFakeTooltip(tooltip, rect, false);
	}
	
	public void updateFakeTooltip(String tooltip, Composite c) {
		Rectangle rect = c.getMonitor().getBounds();
		updateFakeTooltip(tooltip, rect, false);
	}
	
	public void updateFakeTooltipOver(String tooltip, Composite c) {
		
		Point pos = c.toDisplay(0, 0);
		
		
		Rectangle rect = c.getBounds();
		rect.x = pos.x;
		rect.y = pos.y;
		
		updateFakeTooltip(tooltip, rect, true);
	}
	
	public void updateFakeTooltip(String tooltip, Rectangle rect, boolean over) {
		
		
		if (tooltip.equals("") && WidgetShop.isset(fakeToolTip)) {
			fakeToolTip.setVisible(false);
		} else {
			
			/* If tooltip was not yet created */
			if (!WidgetShop.isset(fakeToolTip))
				createFakeToolTip();
			
			/* Apply Text */
			fakeToolTipLabel.setText(tooltip);
			
			/* Calculate and set preferred Size */
			Point preferredSize = fakeToolTip.computeSize(SWT.DEFAULT,
					SWT.DEFAULT);
			fakeToolTip.setSize(preferredSize.x, preferredSize.y);
			Point pt;
			
			
			/* Assume that cursor height is 21 */
			int cursorHeight = 21;

            /*
              Position the tooltip and ensure that it is not located off the
              screen
             */
			
			
			// if (!over)
			{
				Point cursorLocation = GUI.display.getCursorLocation();
				// Rectangle rect = control.getMonitor().getBounds();
				pt = new Point(cursorLocation.x, cursorLocation.y
						+ cursorHeight + 2);
				pt.x = Math.max(pt.x, rect.x);
				if (pt.x + preferredSize.x > rect.x + rect.width)
					pt.x = rect.x + rect.width - preferredSize.x;
				if (pt.y + preferredSize.y > rect.y + rect.height)
					pt.y = cursorLocation.y - 2 - preferredSize.y;
			}
			
			
			if (over) {
				pt = new Point(rect.x, rect.y);
				pt.y -= preferredSize.y;
				pt.y -= 2;
			}
			
			
			/* Apply new location and set fake tooltip visible */
			fakeToolTip.setLocation(pt);
			fakeToolTip.setVisible(true);
		}
	}
}