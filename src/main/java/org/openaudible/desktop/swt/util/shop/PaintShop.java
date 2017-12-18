package org.openaudible.desktop.swt.util.shop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.desktop.swt.gui.GUI;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

public class PaintShop {
    private final static Log logger = LogFactory.getLog(PaintShop.class);
    public static Color grayViewFormColor = new Color(Display.getCurrent(), 241, 240, 234);
    public static Color localColor;
    public static Color remoteColor;
    public static Color synchronizedColor;

    public static Image appIcon[];
    static Display display;
    private static Image iconError;
    private static Image iconWarning;
    private static Image iconStop;
    // public static Image iconSearch;
    private static Image iconReloadBrowser;
    private static Hashtable imageCache = new Hashtable();
    private static boolean useFolder = false; // new
    private static Hashtable<String, Resource> resourceCache = new Hashtable<String, Resource>();

    /**
     * This utility class constructor is hidden
     */
    private PaintShop() {
        // Protect default constructor
    }

    public static Image iconWarning() {
        return getImage("warning.gif");
    }

    public static Image iconStop() {
        return getImage("stop.gif");
    }

    public static Image iconReloadBrowser() {
        return getImage("reload.gif");
    }

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
                if (useFolder) {
                    i = new Image(display, "resources/" + s);
                } else {
                    Thread t = Thread.currentThread();
                    Class mainClass = t.getClass();
                    String fn = "/" + s;    // "/resources/" + s;

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
                }
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
     * Fill an Image with a given color
     *
     * @param display The display as device for the color
     * @param color   The color to display
     * @param width   Width of the image
     * @param height  Height of the image
     * @return Image Filled image
     */
    public static Image getFilledImage(Display display, RGB color, int width, int height) {
        Image filledImage = new Image(display, width, height);
        if (color == null) {
            GUI.println("Returning default color");
            color = new RGB(0, 0, 150);
        }
        Color selectedColor = new Color(display, color);
        /** Paint the image */
        GC gc = new GC(filledImage);
        gc.setBackground(selectedColor);
        gc.fillRectangle(0, 0, width, height);
        gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.drawRectangle(0, 0, width - 1, height - 1);
        /** Cleanup */
        gc.dispose();
        selectedColor.dispose();
        return filledImage;
    }

    public static Color setColor(Color old, RGB c) {
        if (old != null && !old.isDisposed()) {
            old.dispose();
        }
        Color out = new Color(display, c);
        return out;
    }

    public static void setLocalColor(RGB c) {
        localColor = setColor(localColor, c);
    }

    public static void setRemoteColor(RGB c) {
        remoteColor = setColor(remoteColor, c);
    }
    // File("resources").exists();

    public static void setSynchronizedColor(RGB c) {
        synchronizedColor = setColor(synchronizedColor, c);
    }

    /**
     * Initialize the multiple used icons
     *
     * @param display The display
     */
    public static void initIcons(Display display) {
        PaintShop.display = display;
        // iconSearch = getImage("search.gif");

        /** Progress icons */
        /** org.openaudible.desktop.Application icons */
        appIcon = new Image[4];
        int i = 0;
        appIcon[i++] = getImage("16x16.png");
        appIcon[i++] = getImage("32x32.png");
        appIcon[i++] = getImage("48x48.png");
        appIcon[i++] = getImage("128x128.png");
    }

    public static Image getShellImage() {
        return getImage("128x128.png");
        // return getImage("32x32.png");
    }

    public static void setShellImage(Shell s) {
        s.setImages(appIcon);
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

    public static Color newColor(int sysColor) {
        String key = "sys_color_" + sysColor;
        Color c = (Color) resourceCache.get(key);
        if (c != null && c.isDisposed()) {
            resourceCache.remove(key);
            c = null;
        }
        if (c == null) {
            c = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
            resourceCache.put(key, c);
        }
        return c;
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