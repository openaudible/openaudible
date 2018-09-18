package org.openaudible.desktop.swt.util.shop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.util.Platform;

import java.util.HashMap;

// ugly singleton swt font manager
public class FontShop {
	public final Log logger = LogFactory.getLog(FontShop.class);
	private HashMap<String, Font> fontCache = new HashMap<>();
	
	private FontData system, text, banner, header, dialog;
	public static FontShop instance;
	final Display display;
	
	public enum FontType {DIALOG, HEADER, TABLE, SYSTEM, TEXT, TREE}
	
	public enum FontStyle {REGULAR, BOLD, ITALIC}
	
	public FontShop(Display display) {
		assert (display != null);
		assert (instance == null);
		instance = this;
		this.display = display;
		
		system = display.getSystemFont().getFontData()[0];
		text = JFaceResources.getDefaultFont().getFontData()[0];
		banner = JFaceResources.getBannerFont().getFontData()[0];
		header = JFaceResources.getHeaderFont().getFontData()[0];
		dialog = JFaceResources.getDialogFont().getFontData()[0];
	}
	
	
	private String getFontKey(String fontName, FontType fontType, FontStyle fontStyle, int sizeAdjust) {
		return fontName + "_" + fontType.name() + "_" + fontStyle.name() + "_" + sizeAdjust;
	}
	
	public Font getFont(String fontName, FontType fontType, FontStyle fontStyle, int sizeAdjust) {
		if (fontType == null)
			fontType = FontType.TEXT;
		if (fontStyle == null)
			fontStyle = FontStyle.REGULAR;
		if (fontName == null)
			fontName = "";
		
		String key = getFontKey(fontName, fontType, fontStyle, sizeAdjust);
		
		Font f = fontCache.get(key);
		if (isset(f)) return f;
		
		// create Font
		f = createFont(fontName, fontType, fontStyle, sizeAdjust);
		assert (f != null);
		fontCache.put(key, f);
		
		
		return f;
	}
	
	public Font getFont(FontType fontType, FontStyle fontStyle) {
		
		return getFont(null, fontType, fontStyle, 0);    // return getFont(defaultFontName(fontType), fontType,)
	}
	
	public Font getFont(FontType fontType) {
		return getFont(null, fontType, null, 0);    // return getFont(defaultFontName(fontType), fontType,)
	}
	
	/**
	 * Check the given Font for being NULL or disposed. Return false in that case.
	 *
	 * @param font The font to check
	 * @return boolean TRUE if the font is available
	 */
	public boolean isset(Font font) {
		if (font == null) return false;
		assert (!font.isDisposed());
		return !font.isDisposed();
	}
	
	public Font dialogFont() {
		return getFont(FontType.DIALOG, FontStyle.REGULAR);
	}
	
	public Font textFont() {
		return getFont(FontType.TEXT);
	}
	
	public Font headerFont() {
		return getFont(FontType.HEADER);
		
	}
	
	public Font tableFont() {
		return getFont(FontType.TABLE);
	}
	
	public Font treeFont() {
		return getFont(FontType.TREE);
	}
	
	public Font dialogFontBold() {
		return getFont(FontType.DIALOG, FontStyle.BOLD);
	}
	
	public Font textFontBold() {
		return getFont(FontType.TEXT, FontStyle.BOLD);
	}
	
	public Font headerFontBold() {
		return getFont(FontType.HEADER, FontStyle.BOLD);
	}
	
	public Font tableFontBold() {
		return getFont(FontType.TABLE, FontStyle.BOLD);
	}
	
	public Font treeFontBold() {
		return getFont(FontType.TREE, FontStyle.BOLD);
	}
	
	private Font createFont(String fontName, FontType fontType, FontStyle fontStyle, int sizeAdjust) {
		
		FontData fontData;
		Display current = Display.getCurrent();
		assert (current != null);
		
		if (Platform.isLinux()) {
			sizeAdjust -= 2;
		}
		
		
		switch (fontType) {
			case DIALOG:
				fontData = dialog;
				break;
			case TABLE:
				fontData = text;
				if (GUI.isMac())
					sizeAdjust = -1;
				break;
			
			case TEXT:
				fontData = text;
				break;
			case SYSTEM:
				fontData = system;
				break;
			
			case HEADER:
				fontData = header;
				break;
			case TREE:
				fontData = text;
				break;
			
			default:
				assert (false);
				fontData = current.getSystemFont().getFontData()[0];
				
				break;
		}
		int swtStyle = 0;
		if (fontStyle == null) fontStyle = FontStyle.REGULAR;
		
		switch (fontStyle) {
			case REGULAR:
				break;
			case BOLD:
				swtStyle = SWT.BOLD;
				break;
			case ITALIC:
				swtStyle = SWT.ITALIC;
				break;
			default:
				assert (false);
		}
		
		FontData fd = new FontData(fontData.getName(), fontData.getHeight() + sizeAdjust, swtStyle);
		Font font = new Font(current, fd);
		isset(font);
		return font;
	}
	
	
	private void inspect(String name, FontData f) {
		assert (f != null);
		System.out.println("Inspect: " + name + " name=" + f.getName() + " size=" + f.getHeight() + " sty=" + f.getStyle());
	}
	
	public String toString() {
		return "Fonts";
	}
	
	
	public void inspect() {
		
		inspect("system", system);
		inspect("text", text);
		inspect("banner", banner);
		inspect("header", header);
		
		
		FontData[] list = display.getFontList("Courier", false);
		logger.info("list=" + list.length);
		for (FontData fd : list) {
			inspect("getFontList", header);
		}
		
	}
	
}
