package org.openaudible.desktop.swt.util.shop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.i8n.Translate;
import org.openaudible.desktop.swt.manager.Application;

public class FontShop implements Cloneable, Comparable {
    public final static boolean staticFonts = true;
    public final static Log logger = LogFactory.getLog(FontShop.class);
    public static final int DIALOG_FONT = 1;
    public static final int HEADER_FONT = 4;
    public static final int TABLE_FONT = 3;
    public static final int TEXT_FONT = 0;
    public static final int TREE_FONT = 2;
    final public static int kNumFonts = 5; // last + 1, for zero index
    final public static String kFontNames[] = {"treefont", "tablefont", "dialogfont", "headerfont", "textfont"};
    private static FontShop curFonts = null;
    Font regFonts[];
    Font boldFonts[];
    Font italicFonts[];
    // private static FontRegistry registry;

    public FontShop(Display display) {
        if (display == null) {
            Application.report("font display null");
        }

        regFonts = new Font[kNumFonts];
        boldFonts = new Font[kNumFonts];
        italicFonts = new Font[kNumFonts];
        for (int x = 0; x < kNumFonts; x++) {
            regFonts[x] = newDefaultFont(x);
            newStyledFont(x);
        }

        if (curFonts == null)
            curFonts = this;

    }

    /**
     * This utility class constructor is hidden
     */
    private FontShop() {
        // Protect default constructor
        regFonts = new Font[kNumFonts];
        boldFonts = new Font[kNumFonts];

    }

    /**
     * Check the given Font for being NULL or disposed. Return false in that case.
     *
     * @param font The font to check
     * @return boolean TRUE if the font is available
     */
    public static boolean isset(Font font) {
        return (font != null && !font.isDisposed());
    }

    /**
     * Update fonts for all controls in RSSOwl
     */
    public static void setFontForAll(Application sync) {
        System.err.println("setFontForAll not implemented.");

    }

    /**
     * Update styled fonts and the dialog font used by JFace dialogs
     */
    public static void updateFonts() {
        // curFonts.initStyledFonts();
        //		JFaceResources.getFontRegistry().put(JFaceResources.DIALOG_FONT, FontShop.dialogFont().getFontData());
    }

    public static FontShop getCurFonts() {
        curFonts.checkFont();
        return curFonts;
    }

    public static Font dialogFont() {
        curFonts.checkFont();
        return curFonts.regFonts[DIALOG_FONT];
    }

    public static Font textFont() {
        curFonts.checkFont();
        return curFonts.regFonts[TEXT_FONT];
    }

    public static Font headerFont() {
        curFonts.checkFont();
        return curFonts.regFonts[HEADER_FONT];
    }

    public static Font tableFont() {
        curFonts.checkFont();
        return curFonts.regFonts[TABLE_FONT];
    }

    public static Font treeFont() {
        curFonts.checkFont();
        return curFonts.regFonts[TREE_FONT];
    }

    public static Font dialogFontBold() {
        curFonts.checkFont();
        return curFonts.boldFonts[DIALOG_FONT];
    }

    public static Font textFontBold() {
        curFonts.checkFont();
        return curFonts.boldFonts[TEXT_FONT];
    }

    public static Font headerFontBold() {
        curFonts.checkFont();
        return curFonts.boldFonts[HEADER_FONT];
    }

    public static Font tableFontBold() {
        curFonts.checkFont();
        return curFonts.boldFonts[TABLE_FONT];
    }

    public static Font treeFontBold() {
        curFonts.checkFont();
        return curFonts.boldFonts[TREE_FONT];
    }

    public static void setCurrentFonts(FontShop s) {
        if (staticFonts)
            return;

        s.checkFont();
        curFonts.checkFont();

        // curFonts.disposeFonts();
        curFonts = s;

        curFonts.checkFont();
    }

    public void finalize() {
        if (!staticFonts && isset(regFonts[0])) {
            logger.error("finalize found undisposed font.");
        }
    }

    public Object clone() {
        if (staticFonts)
            return curFonts;

        FontShop fs = new FontShop();

        for (int x = 0; x < kNumFonts; x++) {
            FontData[] fontData = this.regFonts[x].getFontData();
            fs.regFonts[x] = new Font(Display.getDefault(), fontData[0].getName(), fontData[0].getHeight(), 0);
            newStyledFont(x);
        }

        return fs;
    }

    public int compareTo(Object fs) {
        FontShop that = (FontShop) fs;
        for (int x = 0; x < kNumFonts; x++) {
            if (!this.regFonts[x].equals(that.regFonts[x]))
                return -1;
            if (!this.boldFonts[x].equals(that.boldFonts[x]))
                return -1;
        }
        return 0;
    }

    private Font newDefaultFont(int id) {
        Display display = Display.getCurrent();
        int bump = 0;
        Font fontCopy = null;

        switch (id) {
            case DIALOG_FONT:
                fontCopy = JFaceResources.getDialogFont();
                break;
            case TABLE_FONT:
                fontCopy = JFaceResources.getDefaultFont();
                if (GUI.isMac())
                    bump = -1;
                break;

            case TEXT_FONT:

                fontCopy = JFaceResources.getTextFont();
                break;

            case HEADER_FONT:
                fontCopy = JFaceResources.getDefaultFont();
                break;
            case TREE_FONT:
                fontCopy = JFaceResources.getTextFont();
                break;

            //	fontCopy = JFaceResources.getHeaderFont();
            //	break;

            default:
                break;
        }

        FontData fd;

        if (fontCopy != null)
            fd = fontCopy.getFontData()[0];
        else
            fd = display.getSystemFont().getFontData()[0];
        int size = Math.round(fd.getHeight()) + bump;

        Font f = new Font(display, fd.getName(), size, 0);
        FontData ff = f.getFontData()[0];

        return f;
    }

    private Font swapFont(Font old, Font newFont) {
        if (old == newFont)
            logger.error("font swap error.");

        if (isset(old))
            old.dispose();
        return newFont;
    }

    private void newStyledFont(int index) {
        boldFonts[index] = newStyledFont(index, SWT.BOLD);
        italicFonts[index] = newStyledFont(index, SWT.BOLD);
    }

    private Font newStyledFont(int index, int style) {

        Font f = regFonts[index];
        if (isset(f)) {
            FontData[] fontData = f.getFontData();
            fontData[0].setStyle(style);
            fontData[0].setLocale(Translate.getLocale().toString());
            return new Font(Display.getDefault(), fontData);
        } else {
            Application.report("newBold failed. f=" + f);
        }
        return null;
    }

    public String toString() {
        return "Fonts";
    }

    public void disposeFonts() {
        if (staticFonts)
            return;

        for (int x = 0; x < kNumFonts; x++) {
            if (isset(regFonts[x]))
                regFonts[x].dispose();
            if (isset(boldFonts[x]))
                boldFonts[x].dispose();
        }
    }

    public void checkFont() {
        for (int i = 0; i < regFonts.length; i++) {
            if (!isset(regFonts[i])) {
                logger.error("font not found:" + i + ", " + regFonts[i]);
                //logger.report(regFonts[i].disposedFrom, "Found not found.");
                break;
            }
            if (!isset(boldFonts[i])) {
                logger.error("font not found: " + i + ", " + boldFonts[i]);
                //logger.report(boldFonts[i].disposedFrom, "Found not found.");
                break;
            }

        }
    }

    public Font getFont(int i) {
        return regFonts[i];
    }

    public Font getBoldFont(int i) {
        return boldFonts[i];
    }

    public void setFont(int i, Font f) {
        if (staticFonts)
            return;

        if (isset(regFonts[i])) {
            if (staticFonts)
                return;
            regFonts[i].dispose();
        }

        if (regFonts[i] != null && !regFonts[i].isDisposed()) {
            if (regFonts[i] == f) {
                System.err.println("fonts equal...");
                return;
            }
            regFonts[i].dispose();
        }

        regFonts[i] = f;
        newStyledFont(i);

    }

}
