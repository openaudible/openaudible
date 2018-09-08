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

package org.openaudible.desktop.swt.i8n;

/**
 * Classes that implement the ITranslatable interface must provide a method to
 * update all String literals with the selected language.
 *
 * @author <a href="mailto:bpasero@rssowl.org">Benjamin Pasero </a>
 * @version 1.0.2
 */
public interface ITranslatable {
	
	/**
	 * Update translation of all String literals
	 */
	void updateI18N();
}