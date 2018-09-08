package org.openaudible.desktop.swt.util.shop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.openaudible.desktop.swt.gui.GUI;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

public class PaintShop {
	private final static Log logger = LogFactory.getLog(PaintShop.class);
	
	public static Image appIcon[];
	static Display display;
	private static Hashtable imageCache = new Hashtable();
	
	private static Hashtable<String, Resource> resourceCache = new Hashtable<>();
	
	
	public static boolean contains(Image i) {
		boolean b = imageCache.contains(i);
		return b;
	}
	
	public static Image[] getAppIconList() {
		return appIcon;
	}
	
	public static Image getSystemImage(int i) {
		return Display.getCurrent().getSystemImage(i);
	}
	
	public static Image resizeImage(Image in, int width, int height) {
		Image out = new Image(Display.getCurrent(), width, height);
		GC gc = new GC(out);
		gc.drawImage(in, 0, 0, in.getBounds().width, in.getBounds().height, 0, 0, width, height);
		return out;
	}
	
	public static void disposeImage(String s) {
		Image i = (Image) imageCache.get(s);
		if (i != null) {
			if (!i.isDisposed())
				i.dispose();
			imageCache.remove(i);
			
		}
	}
	
	public static Image getImage(String s) {
		return getImage(s, false);
	}
	
	// static Class mainClass;
	public static Image getImage(String s, boolean warn) {
		Image i = (Image) imageCache.get(s);
		if (i != null) {
			if (i.isDisposed()) {
				// System.err.println("Warning, image disposed!:"+s);
				imageCache.remove(i);
				i = null;
			}
		}
		if (i == null) {
			try {
				
				Thread t = Thread.currentThread();
				Class mainClass = t.getClass();
				String fn = "/" + s;
				
				InputStream r = mainClass.getResourceAsStream(fn);
				if (r == null) {
					r = mainClass.getResourceAsStream(s);
					if (r == null) {
						if (!warn)
							return null;
						
						logger.error("Missing image:" + fn);
						return null;
					}
				}
				i = new Image(GUI.display, r);
				
			} catch (Exception e) {
				GUI.report(e, s + " not found.");
			}
			if (i == null) {
				GUI.report("image not local:" + s);
				Thread t = Thread.currentThread();
				Class mainClass = t.getClass();
				if (s.startsWith("/"))
					i = new Image(GUI.display, mainClass.getResourceAsStream(s));
				else
					i = new Image(GUI.display, mainClass.getResourceAsStream("/resources/" + s));
			}
			imageCache.put(s, i);
		}
		return i;
	}
	
	/**
	 * Dispose icons
	 */
	public static void disposeIcons() {
		for (Enumeration e = imageCache.elements(); e.hasMoreElements(); ) {
			Object obj = e.nextElement();
			if (obj instanceof Image) {
				Image img = (Image) obj;
				img.dispose();
			} else if (obj instanceof Color) {
				Color c = (Color) obj;
				c.dispose();
			}
		}
	}
	
	
	/**
	 * Initialize the multiple used icons
	 *
	 * @param display The display
	 */
	public static void initIcons(Display display) {
		PaintShop.display = display;
		appIcon = new Image[4];
		int i = 0;
		appIcon[i++] = getImage("16x16.png");
		appIcon[i++] = getImage("32x32.png");
		appIcon[i++] = getImage("48x48.png");
		appIcon[i++] = getImage("128x128.png");
	}
	
	public static Image getShellImage() {
		return getImage("128x128.png");
	}
	
	
	/**
	 * Check the given Image for being NULL or disposed. Return false in that case.
	 *
	 * @param image The image to check
	 * @return boolean TRUE if the Image is available
	 */
	public static boolean isset(Image image) {
		return (image != null && !image.isDisposed());
	}
	
	public static Color newColor(RGB rgb) {
		return newColor(rgb.red, rgb.green, rgb.blue);
	}
	
	
	public static Color newColor(int r, int g, int b) {
		String key = "c[" + r + "," + g + "," + b + "]";
		Color c = (Color) resourceCache.get(key);
		if (c != null && c.isDisposed()) {
			resourceCache.remove(key);
			c = null;
		}
		if (c == null) {
			c = new Color(Display.getCurrent(), r, g, b);
			resourceCache.put(key, c);
		}
		return c;
	}
	
}