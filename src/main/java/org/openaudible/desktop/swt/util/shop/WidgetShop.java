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
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.GUI;

import java.util.Vector;

/**
 * Factory class to tweak Widgets in RSSOwl
 *
 * @author <a href="mailto:bpasero@rssowl.org">Benjamin Pasero </a>
 * @version 1.0.2
 */
public class WidgetShop {
	
	/**
	 * This utility class constructor is hidden
	 */
	private WidgetShop() {
		// Protect default constructor
	}
	
	/**
	 * From the given Vector of words calculate all StyleRanges using the Color and Font Style that is given.
	 *
	 * @param textField     Textfield where the text is in
	 * @param words         Words to be highlighted
	 * @param foreground    Color of the text-foreground
	 * @param background    Color of the text-background
	 * @param fontstyle     Fontstyle for the highlighted words
	 * @param caseSensitive FALSE if the case of the word to highlight should be ignored
	 * @return Vector A Vector containing all calculated StyleRanges
	 */
	public static Vector calculateStyleRanges(StyledText textField, Vector words, Color foreground, Color background, int fontstyle, boolean caseSensitive) {
		
		/* Use Vector for the StyleRanges */
		Vector styleRanges = new Vector();
		
		/* Text with words to style */
		String text = textField.getText();
		
		/* Regard case sensitivity */
		if (!caseSensitive)
			text = textField.getText().toLowerCase();
		
		/* Foreach word to style */
		for (Object word : words) {
			int start = 0;
			String curWord = (String) word;
			
			/* ToLowerCase if case is regarded */
			if (!caseSensitive)
				curWord = curWord.toLowerCase();
			
			/* Save current position */
			int pos;
			
			/* For each occurance of the word in the text */
			while ((pos = text.indexOf(curWord, start)) > -1) {
				
				/* New stylerange for the word */
				StyleRange styleRange = new StyleRange();
				styleRange.start = pos;
				styleRange.length = (curWord.length());
				styleRange.fontStyle = fontstyle;
				styleRange.foreground = foreground;
				styleRange.background = background;
				styleRanges.add(styleRange);
				
				/* Goto next words */
				start = styleRange.start + styleRange.length;
			}
		}
		
		return styleRanges;
	}
	
	/**
	 * Apply a wildcard popup menu to the text. Wildcards a displayed as "[TEXT]" and represent replaceable parameters.
	 *
	 * @param text      The control to append the menu
	 * @param wildcards The wildcards to add to the menu
	 */
	public static void createWildCardMenu(Text text, String[] wildcards) {
		createWildCardMenu(text, wildcards, wildcards);
	}
	
	/**
	 * Apply a wildcard popup menu to the text. Wildcards a displayed as "[TEXT]" and represent replaceable parameters.
	 *
	 * @param text      The control to append the menu
	 * @param wildcards The wildcards to add to the menu
	 * @param labels    The labels for the menuitems
	 */
	private static void createWildCardMenu(final Text text, String[] wildcards, String[] labels) {
		
		/* Both arrays need to have the same size */
		if (wildcards.length != labels.length)
			return;
		
		Menu wildCardMenu = new Menu(text);
		
		/* Foreach wildcards */
		for (int a = 0; a < wildcards.length; a++) {
			final String wildcard = wildcards[a];
			MenuItem menuItem = new MenuItem(wildCardMenu, SWT.POP_UP);
			menuItem.setText(labels[a]);
			if (!GUI.isMac())
				//	menuItem.setImage(PaintShop.iconBackward);
				menuItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						text.insert(wildcard);
					}
				});
		}
		text.setMenu(wildCardMenu);
	}
	
	/**
	 * Set the given StyleRanges from the Vector to the StyledText
	 *
	 * @param textField   Textfield where the text is in
	 * @param styleRanges Vector containing StyleRanges
	 */
	public static void highlightText(StyledText textField, Vector styleRanges) {
		StyleRange styleRangesArray[] = new StyleRange[styleRanges.size()];
		for (int a = 0; a < styleRanges.size(); a++)
			styleRangesArray[a] = (StyleRange) styleRanges.get(a);
		
		textField.setStyleRanges(styleRangesArray);
	}
	
	/**
	 * Change fontstyle / background / foreground of the given words adding a StyleRange to the given TextField.
	 *
	 * @param textField     Textfield where the text is in
	 * @param words         Words to be highlighted
	 * @param foreground    Color of the text-foreground
	 * @param background    Color of the text-background
	 * @param fontstyle     Fontstyle for the highlighted words
	 * @param caseSensitive FALSE if the case of the word to highlight should be ignored
	 */
	public static void highlightText(StyledText textField, Vector words, Color foreground, Color background, int fontstyle, boolean caseSensitive) {
		highlightText(textField, calculateStyleRanges(textField, words, foreground, background, fontstyle, caseSensitive));
	}
	
	/**
	 * Set Mnemonics to the given Array of Buttons.
	 *
	 * @param buttons The Buttons
	 */
	public static void initMnemonics(Button buttons[]) {
		
		/* Store chars that have been used as mnemonic */
		Vector chars = new Vector();
		
		/* For each Button */
		for (Button button : buttons) {
			String name = button.getText();
			
			/* Replace any & that are existing */
			name = name.replaceAll("&", "");
			
			/* For each char in the name */
			for (int b = 0; b < name.length(); b++) {
				
				/* Check if char is available and no whitespace */
				if (name.substring(b, b + 1) != null && !name.substring(b, b + 1).equals(" ")) {
					
					/* Check if char has been used as mnemonic before */
					if (!chars.contains(name.substring(b, b + 1).toLowerCase())) {
						
						/* Set mnemonic */
						button.setText(name.substring(0, b) + "&" + name.substring(b, name.length()));
						
						/* Add char as used mnemonic */
						chars.add(name.substring(b, b + 1).toLowerCase());
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Check the given widget for being NULL or disposed. Return false in that case.
	 *
	 * @param widget The widget to check
	 * @return boolean TRUE if the widget is alive
	 */
	public static boolean isset(Widget widget) {
		return (widget != null && !widget.isDisposed());
	}
	
	/**
	 * Tweak the Text widget with adding listeners to call the selectAll() Method. The Method is called on MouseDoubleClick and on CTRL+A / CMD+A pressed.
	 *
	 * @param text The Text widget to tweak
	 */
	public static void tweakTextWidget(final Text text) {
		
		/* Check Widget */
		if (!isset(text))
			return;
		
		/* MouseDoubleClick Event */
		text.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				text.selectAll();
			}
		});
		
		/* KeyPressed Event */
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'a' || e.keyCode == 'A'))
					text.selectAll();
			}
		});
	}
	
	public static void tweakTextWidget(final StyledText text) {
		/* Check Widget */
		if (!isset(text))
			return;
		/* MouseDoubleClick Event */
		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				text.selectAll();
			}
		});
		/* KeyPressed Event */
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'a' || e.keyCode == 'A'))
					text.selectAll();
			}
		});
	}
}