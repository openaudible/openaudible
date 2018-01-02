package org.openaudible.desktop.swt.manager.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.i8n.Translate;
import org.openaudible.desktop.swt.util.shop.*;

// Wraps Composite with GridLayout to support building simple grids and layouts.
public class GridComposite extends Composite {
    public GridLayout gridLayout;
    protected Color bgColor;
    int cols = 1;
    int gdstyle = 0;
    private GridData gd;
    private SelectionListener selectionListener;
    private KeyListener keyListener;

    public GridComposite(Composite parent, int style) {
        super(parent, style);
        if (parent instanceof GridComposite) {
            GridComposite gcp = (GridComposite) parent;
            if (gcp.selectionListener != null)
                setSelectionListener(gcp.selectionListener);

        } else {
            if (this instanceof SelectionListener) {
                // System.err.println("Setting self selectionListener");
                setSelectionListener((SelectionListener) this);
            }
        }

    }

    public GridComposite(Composite parent, int style, int cols, boolean equalWidthCols, int gridStyle) {
        this(parent, style);
        initLayout(cols, equalWidthCols, gridStyle);
    }

    public GridComposite(Composite parent, int style, int gridStyle) {
        this(parent, style);
        initLayout(1, false, gridStyle | gdstyle);
    }

    // style = SWT.RADIO or SWT.CHECK
    protected static Button newButton(Composite parent, int style, String title) {
        Button button = new Button(parent, style);
        button.setText(Translate.getInstance().buttonName(title));
        button.setFont(FontShop.dialogFont());
        return button;
    }

    public static void hspan(Control parent, int span) {
        GridData gd = (GridData) parent.getLayoutData();
        if (gd == null)
            gd = new GridData();
        gd.horizontalSpan = span;
        parent.setLayoutData(gd);
    }

    public static void vspan(Control parent, int span) {
        GridData gd = (GridData) parent.getLayoutData();
        if (gd == null)
            gd = new GridData();
        gd.verticalSpan = span;
        parent.setLayoutData(gd);
    }

    public static Button newButton(Composite parent, String title, SelectionListener action) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Translate.getInstance().buttonName(title));
        button.setFont(FontShop.dialogFont());

        /* Apply layoutdata to button */
        setButtonLayoutData(button);
        button.addSelectionListener(action);

        return button;
    }

    public static Button[] newRadioGroup(Composite parent, String label, Object titles[]) {

        GridComposite c = null;

        if (label != null) {
            newTitle(parent, label);
        } else
            label = "";
        Button out[] = new Button[titles.length];

        c = new GridComposite(parent, SWT.NONE);
        c.initLayout(titles.length, false, GridData.FILL_HORIZONTAL);
        for (int x = 0; x < titles.length; x++) {
            Object title = titles[x];
            out[x] = newButton(c, SWT.RADIO, title.toString());
            out[x].setData(title);
        }

        return out;
    }

    protected static Button[] newRadios(Composite parent, Object title[]) {
        Button b[] = new Button[title.length];
        int count = 0;

        for (Object s : title) {
            b[count++] = newButton(parent, SWT.RADIO, s.toString());
        }
        return b;
    }

    public static Group newGroup(Composite p, String title) {
        return newGroup(p, title, 2);
    }

    public static Group newGroup(Composite p, String title, int cols) {
        Group group = new Group(p, SWT.NONE);
        group.setText(Translate.getInstance().groupName(title));
        group.setLayout(new GridLayout(cols, false));
        if (cols == 2)
            group.setLayoutData(LayoutDataShop.createGridData(GridData.FILL_HORIZONTAL, cols));
        else
            group.setLayoutData(LayoutDataShop.createGridData(GridData.FILL_HORIZONTAL, cols));
        group.setFont(FontShop.dialogFont());
        setBG(group);
        return group;
    }


    public static boolean inGroup(Control composite) {
        for (; ; ) {
            Composite parent = composite.getParent();
            if (parent == null)
                break;
            if (parent instanceof Group)
                return true;
            composite = parent;
        }
        return false;
    }

    public static void setBG(Control composite, Color c) {

        if (GUI.isMac() && inGroup(composite))
            return;

        composite.setBackground(c);

    }

    public static void setBG(Control composite) {
        Composite parent = composite.getParent();
        if (parent != null) {
            Color c = parent.getBackground();
            if (c != null) {
                composite.setBackground(c);
            }
        }

    }

    public static Text newShortTextInput(Composite parent, String title) {
        return newShortTextInput(parent, title, 5, 200);
    }

    public static Text newShortTextInput(Composite parent, String title, int textLimit, int width) {
        Text t = newInput(parent, title, SWT.BORDER);

        t.setTextLimit(textLimit);
        Rectangle r = t.getBounds();
        r.width = width;
        t.setBounds(r);
        t.setSize(width, r.height);
        t.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        return t;
    }

    public static Text newTextInput(Composite parent, String title, int style) {
        return newInput(parent, title, style);
    }

    public static Text newTextInput(Composite parent, String title) {
        return newTextInput(parent, title, SWT.BORDER);
    }

    public static Label newTitle(Composite parent, String title) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Translate.getInstance().labelName(title) + ": ");
        label.setFont(FontShop.dialogFontBold());
        label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_BEGINNING));
        return label;
    }

    public static Text newInput(Composite parent, String title, int text_attributes) {
        if (title != null) {
            newTitle(parent, title);
        }
        return newText(parent, text_attributes);

    }

    public static Text newText(Composite parent, int text_attributes) {
        Text text = new Text(parent, text_attributes);
        text.setFont(FontShop.dialogFont());
        // text.setText(value);
        text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
        WidgetShop.tweakTextWidget(text);

        setBG(text);
        return text;
    }

    public static Combo newCombo(Composite parent, String title) {
        return newCombo(parent, title, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
    }

    protected static Combo newCombo(Composite parent, String title, int combostyle) {
        if (title != null) {
            Label label = new Label(parent, SWT.NONE);
            label.setText(Translate.getInstance().labelName(title) + ": ");
            label.setFont(FontShop.dialogFontBold());
            label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_BEGINNING));
        }

        Combo combo = new Combo(parent, combostyle);
        combo.setFont(FontShop.dialogFont());
        combo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
        combo.setVisibleItemCount(20);
        setBG(combo);

        return combo;
    }

    protected static void addSpace(Composite c) {
        /* Fill with some spacer */
        LayoutShop.setDialogSpacer(c, 2, 1);
    }

    protected static void setButtonLayoutData(Button button) {
        setButtonLayoutData(button, new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
    }

    /**
     * Set the layout data of the button to a GridData with appropriate heights and widths.
     *
     * @param button The button to set the layoutdata
     * @param data   The GridData to use
     */
    protected static void setButtonLayoutData(Button button, GridData data) {
        /* GC to retrieve fontmetrics object */
        GC gc = new GC(button);
        FontMetrics fontMetrics = gc.getFontMetrics();
        /* Apply appropiate gridata */
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        button.setLayoutData(data);
        /* Dispose GC */
        gc.dispose();
    }

    public static Label newLabel(Composite composite) {
        return newLabel(composite, SWT.NONE, "");
    }

    public static Label newLabel(Composite composite, String labelName) {
        return newLabel(composite, SWT.NONE, labelName);
    }

    public static Label newLabel(Composite composite, int style, String labelName) {
        Label l = new Label(composite, style);
        l.setText(labelName);
        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        l.setFont(FontShop.tableFontBold());
        setBG(l);

        Color c = composite.getBackground();
        if (c != null) {
            if (GUI.isMac() && c != null && (composite instanceof Group)) {
                c = PaintShop.newColor(c.getRed() - 9, c.getGreen() - 9, c.getBlue() - 9);

            }
            l.setBackground(c);
        }

        return l;
    }

    public static Label newLabelPair(Composite composite, String labelName) {
        newLabel(composite, labelName);
        Label d = new Label(composite, SWT.NONE);
        d.setText("");
        d.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        d.setFont(FontShop.tableFont());
        return d;
    }

    public static Text newTextPair(Composite composite, String labelName, int textStyle) {
        newLabel(composite, labelName);
        Text d = new Text(composite, textStyle);
        d.setText("");
        d.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        d.setFont(FontShop.tableFont());
        GridData gd = new GridData();
        gd.heightHint = 16;
        gd.widthHint = 160;

        // gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
        d.setLayoutData(gd);

        WidgetShop.tweakTextWidget(d);

        return d;
    }

    public static Text newTextPair(Composite composite, String labelName) {
        return newTextPair(composite, labelName, SWT.NONE);
    }

    public static Text newPasswordPair(Composite composite, String labelName) {
        return newTextPair(composite, labelName, SWT.PASSWORD);
    }

    public static Button newCheck(Composite c, String title) {
        Button b = newButton(c, SWT.CHECK, title);
        return b;
    }

    // Use tabs to traverse to next field..
    // Use for Text objects..
    public static void disallowTabs(Control c) {

        c.addTraverseListener(e -> {
            switch (e.detail) {
                case SWT.TRAVERSE_TAB_NEXT:
                case SWT.TRAVERSE_TAB_PREVIOUS:
                    e.doit = true;
                    break;
            }
        });
    }

    public static Label createErrorLabel(Composite p, int colSpan) {
        // new Label(g, SWT.NONE);
        Label l = new Label(p, SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = colSpan;
        gd.heightHint = 42;
        l.setLayoutData(gd);
        l.setForeground(GUI.display.getSystemColor(SWT.COLOR_DARK_RED));
        return l;
    }

    public static Object getRadioData(Button radioButtons[]) {
        for (Button b : radioButtons) {
            if (b.getSelection())
                return b.getData();
        }
        return null;
    }

    public static Label newImage(Composite c, Image image) {
        Label l = newLabel(c);
        l.setImage(image);
        return l;
    }

    public static void setEnabledRecursive(Control c, boolean on) {
        setEnabledRecursive(c, on, null);
    }

    public static void setEnabledRecursive(Control c, boolean on, Control exclude) {
        if (c instanceof Composite) {
            if (exclude != null && exclude.equals(c))
                return;

            Control children[] = ((Composite) c).getChildren();
            for (Control aChildren : children) {
                setEnabledRecursive(aChildren, on, exclude);
            }
        }
    }

    public static void setVisibleRecursive(Control c, boolean on) {
        if (c instanceof Composite) {
            Control children[] = ((Composite) c).getChildren();
            for (Control aChildren : children) {
                setEnabledRecursive(aChildren, on);
            }
        }
    }

    public static GridData gdFillHorizontal() {
        return new GridData(GridData.FILL_HORIZONTAL);
    }

    public static GridData gdHeightWidth(int w, int h) {
        GridData gd = new GridData();

        if (h != 0)
            gd.heightHint = h;
        if (w != 0)
            gd.widthHint = w;
        return gd;
    }

    public static GridData gdFillBoth() {
        return new GridData(GridData.FILL_HORIZONTAL);
    }

    public static GridComposite newFillBoth(Composite parent) {
        return new GridComposite(parent, SWT.NONE, 1, false, GridData.FILL_BOTH);
    }

    public static GridComposite newFillHorizontal(Composite parent) {
        return new GridComposite(parent, SWT.NONE, 1, false, GridData.FILL_HORIZONTAL);
    }

    public static GridComposite newFillVertical(Composite parent) {
        return new GridComposite(parent, SWT.NONE, 1, false, GridData.FILL_VERTICAL);
    }

    public static void debugLayout(final Composite c, final int color) {
        c.addPaintListener(e -> {
            Color save = e.gc.getForeground();
            e.gc.setForeground(Display.getCurrent().getSystemColor(color));
            e.gc.drawRectangle(1, 1, c.getBounds().width - 1, c.getBounds().width - 1);
            // e.gc.drawLine(1, 1, c.getBounds().width - 1, c.getBounds().height - 1);
            e.gc.setForeground(save);
        });
    }

    public GridData gd() {
        return gd;
    }

    public void appendGridStyle(int s) {
        gdstyle |= s;
    }

    public void expandHorizontal() {
        appendGridStyle(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    }

    public void expandVertical() {
        gdstyle |= GridData.VERTICAL_ALIGN_FILL;
        gdstyle |= GridData.GRAB_VERTICAL;
    }

    public GridData initLayout(int cols, boolean equalWidth, int gridStyle) {
        assert (gridLayout == null);

        gridLayout = new GridLayout(cols, equalWidth);
        this.setLayout(gridLayout);
        gd = new GridData(gridStyle);
        this.setLayoutData(gd);
        return gd;
    }

    public GridData initLayout() {
        return initLayout(1, true, gdstyle);
    }

    public GridData initLayout(int gsty) {
        return initLayout(1, true, gsty);
    }

    public void verticalIndent(int i) {
        getGridData().verticalIndent = i;
    }

    public void horizontalIndent(int i) {
        getGridData().horizontalIndent = i;
    }

    public GridData getGridData() {
        GridData gd = (GridData) getLayoutData();
        if (gd == null)
            gd = new GridData();
        return gd;
    }

    // style = SWT.RADIO or SWT.CHECK
    protected Button newButton(int style, String title) {
        Button button = newButton(this, style, title);
        if (selectionListener != null)
            button.addSelectionListener(selectionListener);
        return button;
    }

    public Button newButton(Composite parent, String title) {
        return newButton(parent, title, SWT.PUSH);
    }

    public Button newButton(Composite parent, String title, int style) {
        Button button = new Button(parent, style);
        button.setText(Translate.getInstance().buttonName(title));
        button.setFont(FontShop.dialogFont());

        /* Apply layoutdata to button */
        setButtonLayoutData(button);
        if (selectionListener != null)
            button.addSelectionListener(selectionListener);

        return button;
    }

    public Button[] newRadioGroup(String label, Object titles[], boolean vertical) {

        Composite g = newGroup(this, label, vertical ? 1 : titles.length);
        Button out[] = new Button[titles.length];

        for (int x = 0; x < titles.length; x++) {
            Object title = titles[x];
            out[x] = newButton(g, SWT.RADIO, title.toString());
            out[x].setData(title);
            if (selectionListener != null)
                out[x].addSelectionListener(selectionListener);
        }

        return out;
    }

    protected Button newRadio(String title) {
        Button b = newButton(this, SWT.RADIO, title);
        return b;
    }

    public Group newGroup(String title) {
        return newGroup(this, title, 2);
    }

    public Group newGroup(String title, int cols) {
        return newGroup(this, title, cols);
    }

    public Text newShortTextInput(int textLimit, int width) {
        Text t = newInput(this, null, SWT.BORDER);
        t.setTextLimit(textLimit);
        Rectangle r = t.getBounds();
        r.width = width;
        t.setBounds(r);
        t.setSize(width, r.height);
        t.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        return t;
    }

    public Label newTitle(String title) {
        return newTitle(this, title);
    }

    public Combo newCombo(String title) {
        return newCombo(this, title, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
    }

    public void addSpace() {
        /* Fill with some spacer */
        LayoutShop.setDialogSpacer(this, 2, 1);
    }

    public Label newLabel(String labelName) {
        return newLabel(this, SWT.NONE, labelName);
    }

    public Label newLabel() {
        return newLabel(this, SWT.NONE, "");
    }

    public void newLabels(int c) {
        for (int x = 0; x < c; x++)
            newLabel(this, SWT.NONE, "");
    }

    public Label newLabel(int style, String labelName) {
        return newLabel(this, style, labelName);
    }

    public Text newTextPair(String labelName) {
        Text t = newTextPair(this, labelName);
        if (keyListener != null)
            t.addKeyListener(keyListener);
        return t;
    }

    public StyledText newTextArea(Composite composite, boolean editable) {
        int style = SWT.MULTI | SWT.V_SCROLL;
        if (!editable)
            style |= SWT.READ_ONLY;
        else
            style |= SWT.WRAP;
        return newTextArea(composite, editable, style);
    }

    public StyledText newTextArea(Composite composite, boolean editable, int sty) {
        int style = SWT.MULTI | SWT.V_SCROLL;
        if (!editable)
            style |= SWT.READ_ONLY;
        else
            style |= SWT.WRAP;

        StyledText d = new StyledText(composite, style);
        d.setText("To be entered\ntest\n\test\ntest");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 80;
        gd.widthHint = 460;
        gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
        d.setEditable(editable);
        d.setLayoutData(gd);
        d.setFont(FontShop.textFont());
        if (keyListener != null)
            d.addKeyListener(keyListener);
        d.setWordWrap(editable);
        WidgetShop.tweakTextWidget(d);
        return d;
    }

    public Label newLabelPair(String labelName, int l2Style) {
        Label l = new Label(this, SWT.NONE);
        l.setText(Translate.getInstance().labelName(labelName) + ": ");
        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        l.setFont(FontShop.tableFontBold());
        l.setBackground(bgColor);
        Label d = new Label(this, l2Style);
        // d.setText("To be entered");
        d.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        d.setFont(FontShop.tableFont());
        d.setBackground(bgColor);
        return d;
    }

    public Label newLabelPair(String labelName) {
        return newLabelPair(labelName, SWT.DEFAULT);
    }

    public Label newVSpace(Composite composite, int hspan, int vheight) {
        GridData gd = new GridData();
        Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd.horizontalSpan = hspan;
        line.setLayoutData(gd);
        gd.heightHint = vheight;
        return line;
    }

    public Text newLabeledText(Composite composite, String labelName, int l2Style) {
        Label l = new Label(composite, SWT.NONE);
        l.setText(Translate.getInstance().labelName(labelName) + ": ");
        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        l.setFont(FontShop.tableFontBold());
        l.setBackground(bgColor);
        Text d = new Text(composite, l2Style);
        // d.setText("To be entered");
        d.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        d.setFont(FontShop.tableFont());
        d.setBackground(bgColor);
        if (keyListener != null)
            d.addKeyListener(keyListener);
        WidgetShop.tweakTextWidget(d);
        return d;
    }

    public Combo newCombo(Composite parent) {
        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        combo.setVisibleItemCount(20);
        return combo;
    }

    public Combo newCombo() {
        return newCombo(this);
    }

    protected Text newLabeledText(Composite p, String label) {
        Composite c = p; // new Composite(p, SWT.NONE);
        // c.setLayout(new GridLayout(2, false));
        Label m = new Label(c, SWT.NONE);

        m.setText(label);
        m.setBackground(bgColor);

        GridData gdm = LayoutDataShop.createGridData(0, 1);
        m.setLayoutData(gdm);
        Text t = new Text(c, SWT.BORDER);
        t.setText("");
        t.setEditable(true);
        t.setBackground(bgColor);
        if (keyListener != null)
            t.addKeyListener(keyListener);

        GridData gdt = LayoutDataShop.createGridData(GridData.HORIZONTAL_ALIGN_FILL, 1);
        gdt.widthHint = 200;
        t.setLayoutData(gdt);
        WidgetShop.tweakTextWidget(t);

        return t;
    }

    Control newLabeledControl(Composite composite, String labelName, Control other) {
        Label l = new Label(composite, SWT.NONE);
        l.setText(Translate.getInstance().labelName(labelName) + ": ");
        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        l.setFont(FontShop.tableFontBold());
        l.setBackground(bgColor);

        other.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        other.setBackground(bgColor);
        return other;
    }

    protected Combo newLabeledCombo(Composite composite, String labelName, int style) {
        Label l = new Label(composite, SWT.NONE);
        l.setText(Translate.getInstance().labelName(labelName) + ": ");
        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        l.setFont(FontShop.tableFontBold());
        l.setBackground(bgColor);
        Combo combo = new Combo(composite, style);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        combo.setBackground(bgColor);
        combo.setVisibleItemCount(20);
        if (selectionListener != null)
            combo.addSelectionListener(selectionListener);
        if (keyListener != null)
            combo.addKeyListener(keyListener);
        return combo;
    }

    public Label newBlankLabel() {
        return newLabel("");
    }

    //	public void width(int i)
    //	{
    //		gd.widthHint = i;
    //	}
    //
    //	public void height(int i)
    //	{
    //		gd.heightHint = i;
    //	}

    protected Combo newLabeledCombo(Composite composite, String labelName) {
        return newLabeledCombo(composite, labelName, SWT.DROP_DOWN | SWT.READ_ONLY);
    }

    public String getComboValue(Combo c) {
        String v[] = c.getItems();
        String test = c.getText();

        int i = c.getSelectionIndex();
        if (i != -1) {
            if (test.equalsIgnoreCase(v[i]))
                return v[i];
            c.setText(v[i]);
            selectAll(c);

            return v[i];
        }
        int bestGuess = -1;

        for (int x = 0; x < v.length; x++) {
            if (v[x].toLowerCase().startsWith(test.toLowerCase())) {
                if (bestGuess != -1)
                    return "";
                bestGuess = x;
            }
        }
        if (bestGuess == -1) {
            for (int x = 0; x < v.length; x++) {
                if (test.toLowerCase().startsWith(v[x].toLowerCase())) {
                    if (bestGuess != -1)
                        return "";
                    bestGuess = x;
                }
            }
        }

        if (bestGuess != -1) {
            c.setText(v[bestGuess]);
            selectAll(c);
            return v[bestGuess];
        }

        return "";
    }

    void selectAll(Combo c) {
        String test = c.getText();

        Point p = new Point(0, test.length());
        c.setSelection(p);

    }

    protected boolean setCombo(Combo fieldCombo, String items[], String fieldName) {
        fieldCombo.removeAll();
        int index = -1;
        for (int x = 0; x < items.length; x++) {
            fieldCombo.add(items[x]);
            if (fieldName != null && fieldName.equalsIgnoreCase(items[x]))
                index = x;
        }
        if (index == -1) {
            fieldCombo.select(0);
            // System.err.println("combo case not found: "+fieldName);
            return false;
        }
        fieldCombo.select(index);
        return true;
    }

    public void setSelectionListener(SelectionListener selectionListener) {
        if (this.selectionListener != null) {
            if (this.selectionListener != selectionListener) {
                System.err.println("Change setSelectionListener!");
                assert (this.selectionListener == selectionListener);
            } else {
                System.err.println("Dup Call To setSelectionListener");
            }
        }
        this.selectionListener = selectionListener;
    }

    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    public Button newCheck(String title) {
        return newButton(SWT.CHECK, title);
    }

    public Button newButton(String title) {
        return newButton(SWT.PUSH, title);
    }

    public Text newTextInput() {
        return GridComposite.newTextInput(this, null);
    }

    public Label newImage(Image image) {
        Label l = newLabel();
        l.setImage(image);
        return l;
    }

    //
    //	public static void addSelectAllHandler(final Text text)
    //	{
    //		/** Select All on Ctrl+A (Cmd+A on Mac) */
    //		text.addKeyListener(new KeyAdapter()
    //		{
    //			@Override
    //			public void keyReleased(KeyEvent e)
    //			{
    //				if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'a' || e.keyCode == 'A'))
    //					text.setSelection(new Point(0, text.getText().length()));
    //			}
    //		});
    //	}
    //
    //	public static void addSelectAllHandler(final StyledText text)
    //	{
    //		/** Select All on Ctrl+A (Cmd+A on Mac) */
    //		text.addKeyListener(new KeyAdapter()
    //		{
    //			@Override
    //			public void keyReleased(KeyEvent e)
    //			{
    //				if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'a' || e.keyCode == 'A'))
    //					text.setSelection(new Point(0, text.getText().length()));
    //			}
    //		});
    //	}

	/*	public void setTextAsync(final Label c, final String text)
        {
			SWTAsync.run(new SWTAsync("")
			{
				@Override
				public void task()
				{
					c.setText(text);
				}
			});
		}
	*/

    public void setWidthHint(Control c, int i) {
        GridData gd = (GridData) c.getLayoutData();
        if (gd == null)
            gd = new GridData();
        gd.widthHint = i;
        c.setLayoutData(gd);
    }

    public void setMargins(int i, int j) {
        gd().horizontalIndent = i;
        gd().verticalIndent = j;

    }

    public void setMargins(int i) {
        //
        gridLayout.marginBottom = i;
        gridLayout.marginTop = i;
        gridLayout.marginRight = i;
        gridLayout.marginLeft = i;
        // gridLayout.marginWidth = i;
        setMargins(i, i);

    }

    public void initLayoutFillMax() {
        assert (gridLayout == null);

        gridLayout = new GridLayout();
        gd = new GridData(GridData.FILL_BOTH);
        this.setLayoutData(gd);
        this.setLayout(gridLayout);
        noMargins();
    }

    public void debugLayout() {
        debugLayout(this, SWT.COLOR_DARK_RED);
    }

    public void debugLayout(int color) {
        debugLayout(this, color);
    }

    public void noMargins() {
        gd = getGridData();
        gd.verticalIndent = 0;
        gd.horizontalIndent = 0;
        ///gridLayout.horizontalSpacing = 0;
        // gridLayout.verticalSpacing = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginTop = 0;

    }
}
