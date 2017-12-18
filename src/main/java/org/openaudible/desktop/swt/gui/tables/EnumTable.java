package org.openaudible.desktop.swt.gui.tables;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.util.shop.FontShop;
import org.openaudible.desktop.swt.util.shop.PaintShop;
import org.openaudible.util.EventTimer;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;

public class EnumTable<E extends Object, F extends Enum> implements SelectionListener {
    public final static Log logger = LogFactory.getLog(EnumTable.class);
    protected final Composite parent;
    public F sortCol = null; // null (defaultSort) or column to sort by.
    protected Table table;
    protected ArrayList<E> list = new ArrayList<E>(); // List of all SuperTableData
    protected Color tableColors[];
    protected boolean allowReverseSort = true;
    protected boolean oddEvenColors = true;
    protected boolean needSort = false;
    protected boolean reverseSort = false;
    protected int defaultAttributes = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL;
    protected ArrayList<TableColumn> tableColumns = new ArrayList<TableColumn>();
    ArrayList<RowListener> rowListeners = new ArrayList<RowListener>(1);
    F columns[]; // enum constants to display
    PackMode packMode = PackMode.packLast;

    String tableName = "Table";
    private int packCount = 0;
    private ResizeEvent resizeEvent = null;

    public EnumTable(Composite p) {
        this(p, null);
    }

    public EnumTable(Composite p, F cols[]) {
        columns = cols;
        parent = p;
    }

    // for table row odd/even color adjustment
    public static RGB adjustColor(RGB c, int amt) {
        int r = c.red;
        int g = c.green;
        int b = c.blue;
        r += amt;
        g += amt;
        b += amt;
        if (r > 255)
            r = 255;
        if (g > 255)
            g = 255;
        if (b > 255)
            b = 255;
        if (r < 0)
            r = 0;
        if (g < 0)
            g = 0;
        if (b < 0)
            b = 0;
        return new RGB(r, g, b);
    }

    public static void main(String[] args) {
        final Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        final Tree tree = new Tree(shell, SWT.BORDER);
        tree.setHeaderVisible(true);
        TreeColumn col1 = new TreeColumn(tree, 0);
        col1.setText("col1");
        TreeColumn col2 = new TreeColumn(tree, 0);
        col2.setText("col2");

        for (int i = 0; i < 4; i++) {
            TreeItem iItem = new TreeItem(tree, 0);
            iItem.setText(new String[]{"TreeItem (0) -" + i, "x"});
            for (int j = 0; j < 4; j++) {
                TreeItem jItem = new TreeItem(iItem, 0);
                jItem.setText(new String[]{"TreeItem (1) -" + i, "x"});
                for (int k = 0; k < 4; k++) {
                    TreeItem kItem = new TreeItem(jItem, 0);
                    kItem.setText(new String[]{"TreeItem (2) -" + i, "x"});
                    for (int l = 0; l < 4; l++) {
                        TreeItem lItem = new TreeItem(kItem, 0);
                        lItem.setText(new String[]{"TreeItem (3) -" + i, "x"});
                    }
                }
            }
        }

        col1.pack();
        col2.pack();

        Listener listener = new Listener() {

            @Override
            public void handleEvent(Event e) {
                final TreeItem treeItem = (TreeItem) e.item;
                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        for (TreeColumn tc : treeItem.getParent().getColumns())
                            tc.pack();
                    }
                });
            }
        };

        tree.addListener(SWT.Collapse, listener);
        tree.addListener(SWT.Expand, listener);

        shell.setSize(200, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    public void addRowListener(RowListener l) {
        rowListeners.add(l);
    }

    public void removeRowListener(RowListener l) {
        rowListeners.remove(l);
    }

    public void setPackMode(PackMode m) {
        packMode = m;
    }

    public void add(E td) {
        SWTAsync.assertGUI();
        assert (td != null);
        synchronized (list) {
            list.add(td);
        }
    }

    public F[] getColumnsToDisplay() {
        return columns;
    }

    public void setColumnsToDisplay(F[] in) {
        columns = in;
    }

    public void setTableName(String s) {
        tableName = s;
    }

    public void setItems(final Collection<E> l) {

        SWTAsync.assertGUI();

        synchronized (list) {
            list.clear();
            list.addAll(l);
        }
        populateData();
    }

    public void init() {
        init(defaultAttributes);
    }

    public boolean setRowColors(RGB c[]) {
        SWTAsync.assertGUI();
        boolean needRefresh = false;
        if (tableColors != null && tableColors.length != c.length) {
            for (int x = 0; x < tableColors.length; x++) {
                tableColors[x].dispose();
            }
            tableColors = null;
        }
        if (tableColors == null) {
            tableColors = new Color[c.length];
        }
        for (int x = 0; x < tableColors.length; x++) {
            if (tableColors[x] == null || !tableColors[x].getRGB().equals(c[x])) {
                tableColors[x] = PaintShop.newColor(c[x]);
                needRefresh = true;
            }
        }
        return needRefresh;
    }

    public void enableBasicOddEvenColors() {
        RGB even = new RGB(255, 255, 255);
        RGB odd = new RGB(245, 245, 245);
        RGB c[] = {even, odd};
        this.setRowColors(c);
        oddEvenColors = true;
    }

    public boolean needSort() {
        return needSort;
    }

    public void setNeedSort(boolean b) {
        needSort = b;
    }

    public void sortList(ArrayList<E> l) {

        if (sortCol != null) {
            synchronized (l) {
                Collections.sort(l, new _MultiColumnSort());
            }
        }
    }

    // this may be overridden for advanced sorting (sort by A, then b, then c,
    // for instance.)
    public ArrayList<E> sortList() {
        EventTimer evt = new EventTimer();
        ArrayList<E> copy = getList();
        sortList(copy);
        return copy;
    }

    // can throw IndexOutOfBoundsException...
    public E get(int x) {
        synchronized (list) {
            return list.get(x);
        }
    }

    protected void setTableItems(TableItem item, E e) {
        int index = 0;
        for (TableColumn i : tableColumns) {
            F o = (F) i.getData();
            String text = getColumnDisplayable(o, e);
            item.setText(index++, text);

        }
    }

    public void redrawItems(Set<E> items) {
        for (TableItem ti : table.getItems()) {
            E e = (E) ti.getData();
            assert (e != null);
            if (items.contains(e)) {
                setTableItems(ti, e);
            }
        }
    }

    public void redrawItem(E item) {
        for (TableItem ti : table.getItems()) {
            E e = (E) ti.getData();
            assert (e != null);
            if (e.equals(item)) {
                setTableItems(ti, e);
                return;
            }
        }
        // Not a huge deal.. but the item wasn't already in the table.
        assert (false);
    }

    public void _populateDataGUI() {
        updateSortByIndicator();
        ArrayList<E> copy = sortList();

        table.setItemCount(copy.size());
        TableItem ti[] = table.getItems();
        assert (ti.length == copy.size());
        int index = 0;
        for (E e : copy) // int x = 0; x < list.size(); x++)
        {
            ti[index].setData(e);
            setTableItems(ti[index], e);
            index++;
        }

        assertTable();
    }

    public void populateData() {
        SWTAsync.run(new SWTAsync("populateData") {
            @Override
            public void task() {
                table.setSelection(new int[0]);
                _populateDataGUI();
                packColumns();
            }
        });

    }

    public void report(String s) {
        logger.error(s);
        new Throwable(s).printStackTrace();
    }

    public void setSortColumn(final TableColumn ftc, String name, boolean reverse) {
        SWTAsync.assertGUI();

        if (ftc != null) {
            if (!GUI.isMac()) {
                table.setSortDirection(!reverse ? SWT.UP : SWT.DOWN);
                table.setSortColumn(ftc);
                if (name != null)
                    ftc.setText(name);

            } else {
                if (name != null)
                    // ftc.setText(name + (reverse?" (reverse)":""));
                    ftc.setText(name);

            }

        } else
            table.setSortDirection(SWT.NONE);
    }

    public void updateSortByIndicator() {
        SWTAsync.assertGUI();

        if (this.allowReverseSort == false)
            return;
        TableColumn tc = null;

        int c = getColumnCount();
        /*
        for (int x = 0; x < c; x++)
		{
			if (sortCol == x)
			{
				tc = table.getColumn(x);
				break;
			}
		}
		final boolean rSort = this.reverseSort;
		final TableColumn ftc = tc;
		setSortColumn(ftc, null, rSort);
		*/

    }

    public void columnClicked(F col, int count) {
        needSort = true;
        if (sortCol != col) {
            reverseSort = false;
            sortCol = col;
        } else {
            if (allowReverseSort) {
                reverseSort = !reverseSort;
            } else
                needSort = false;
        }
        if (needSort) {
            populateData();
        }

    }

    // This should be overridden if using multiple columns
    public String getColumnName(F col) {
        if (col == null)
            return tableName;
        return col.toString().replace('_', ' ');
    }

    public ColumnListener getColumnListener() {
        return new ColumnListener();
    }

    public void setColumWidths() {
        int c = getColumnCount();
        int widths[] = getColumWidths();
        if (c == widths.length) {
            for (int x = 0; x < c; x++) {
                TableColumn tc = table.getColumn(x);
                tc.setWidth(widths[x]);
            }

        } else {

            for (int x = 0; x < c; x++) {
                TableColumn tc = table.getColumn(x);
                tc.setWidth(64);
            }
        }
    }

    public int[] getColumWidths() {
        TableColumn tc[] = table.getColumns();
        int c = getColumnCount();
        int out[] = new int[c];
        for (int x = 0; x < c; x++) {
            out[x] = tc[x].getWidth();
        }
        return out;
    }

    // override to support multiple columns
    public Comparable getColumnComparable(F column, E e) {
        String method = "get" + column.name();
        try {
            Method m = e.getClass().getMethod(method);
            Object r = m.invoke(e);
            if (r != null) {
                return (Comparable) r;
            }
            return (Comparable) e;
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return "????";

    }

    // override to support multiple columns
    public String getColumnDisplayable(F column, E e) {
        assert (column != null);
        assert (e != null);
        Comparable f = getColumnComparable(column, e);
        assert (f != null);

        return "" + f;
    }

    public Image getColumnImage(F column) {
        return null;
    }

    public void clear() {
        synchronized (list) {
            list.clear();
        }
        populateData();

    }

    // User has typed something into the active table
    // Use this keyboard entry to find the table entry
    // the user is trying to find.
    // If the user types "Books"
    // this will be called with B, Bo, Boo, Book, & Books
    // If the user types backspace, a letter will be removed
    // if a user hits escape or waits 2 seconds, the text
    // will be cleared for a fresh entry
    // return true if text should be cleared..
    // return false normally.
    public E processKeyboardText(String keyboardText) {
        return null;
    }

    public void itemSelectedByKeyboard(E sd) {
        select(sd);
    }

    public void addKeyBoardListener() {
        /** KeyPressed Event */
        table.addKeyListener(new KeyAdapter() {
            String text = "";
            long lastKey = 0;
            long maxDelta = 2000;

            @Override
            public void keyPressed(KeyEvent e) {
                long now = System.currentTimeMillis();
                long delta = now - lastKey;
                if (delta > maxDelta)
                    text = "";
                lastKey = now;

                if (e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) {

                    if (e.character == 'a' || e.character == 'A') {
                        selectAll();
                        e.doit = false;
                    }

                } else {
                    char c = e.character;
                    switch (e.keyCode) {
                        case 8: // backspace
                            if (text.length() > 0)
                                text = text.substring(0, text.length() - 1);
                            break;
                        case 27: // escape
                            text = "";
                            break;

                        case SWT.DEL:
                            text = "";
                            break;

                        default:
                            if (Character.isLetterOrDigit(c)) {
                                text += e.character;
                                e.doit = false;
                            }
                    }

                }

                // println(text + " " + e + " char=" + (int) e.character);

                if (text.length() > 0) {
                    E sd = processKeyboardText(text);
                    if (sd == null) {
                        text = "";
                    } else {
                        itemSelectedByKeyboard(sd);

                        // table.setSelection(sd.getListIndex());
                    }

                }

            }
        });

    }

    public void init(int attributes) {
        if (table != null)
            throw new InvalidParameterException("init already called on table!");

        table = new Table(parent, attributes);
        table.setHeaderVisible(true);
        if (oddEvenColors)
            enableBasicOddEvenColors();

        GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        table.setLayoutData(gd);
        assert (tableColumns.size() == 0);

        layoutColumns();

        table.setVisible(true);

        table.setData(this);
        table.setFont(FontShop.tableFont());

        addKeyBoardListener();

        if (parent instanceof SelectionListener)
            addSelectionListener((SelectionListener) parent);

        addSelectionListener(this);

        enableBasicOddEvenColors();

    }

    private void layoutColumns() {
        columns = getColumnsToDisplay();
        tableColumns.clear();

        TableColumn cols[] = table.getColumns();
        int index = 0;
        for (F o : columns) {
            TableColumn column;
            // Check to see if column already set...
            if (cols.length > index) {
                column = cols[index];

            } else {
                column = new TableColumn(table, SWT.LEFT);
                column.addSelectionListener(getColumnListener());
            }
            String n = getColumnName(o);
            Image r = getColumnImage(o);
            column.setImage(r);
            column.setText(n);
            column.setWidth(getColumnWidth(o));
            column.setData(o);

            tableColumns.add(column);
            index++;
        }

        // Make sure we didn't shrink. May not be tested.
        while (table.getColumns().length > columns.length) {
            table.getColumn(columns.length).dispose(); // last one
        }

        assert (table.getColumns().length == columns.length);

        //

        setColumWidths();
    }

    public int getColumnCount() {
        return tableColumns.size();
    }

    public int[] getSelectionIndices() {
        return table.getSelectionIndices();
    }

    public TableItem[] getSelection() {
        return table.getSelection();
    }

    public List<E> getSelectedItems() {
        TableItem ti[] = table.getSelection();
        ArrayList<E> out = new ArrayList<E>(ti.length);
        for (TableItem i : ti) {
            if (!i.isDisposed())
                out.add((E) i.getData());
        }
        return out;
    }

    public E getFirstSelected() {
        TableItem ti[] = table.getSelection();
        if (ti.length > 0)
            return (E) ti[0].getData();
        return null;
    }

    // Get the number of items displayed in the list.
    // This is list.size() less the number of items filtered
    public int size() {
        synchronized (list) {
            assert (list.size() == table.getItemCount());
            return list.size();
        }
    }

    public Table getTable() {
        return table;
    }

    // return list of E
    public ArrayList<E> getList() {
        return new ArrayList<E>(list);
    }

    // Reset the default sort
    public boolean setDefaultSort() {
        if (sortCol != null) {
            sortCol = null;
            needSort = true;
            return true;
        }
        return false;
    }

    public F getSortColumn() {
        return sortCol;
    }

    public boolean select(E s) {
        if (s != null) {
            int count = 0;

            for (E e : getList()) {
                if (e.equals(s)) {
                    table.select(count);
                    return true;
                }
                count++;
            }
        }
        return false;
    }

    public void addSelectionListener(SelectionListener listener) {

        table.addSelectionListener(listener);
    }

    public void inspectTable(int max) {

    }

    public void assertTable() {
        if (table.getItemCount() != list.size()) {
            System.err.println("table:" + table.getItemCount() + " list:" + list.size() + " for " + this);
        }

        assert (table.getItemCount() == list.size());
    }

    public int getColumnWidth(F c) {
        return 64;
    }

    public void addSelectAllListener() {
        table.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean ctlDown = ((e.stateMask | SWT.CTRL) != 0);
                if (e.keyCode == 97 && ctlDown) {
                    selectAll();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    public void setNoScroll(boolean b) {
        if (b) {
            if (resizeEvent == null) {
                resizeEvent = new ResizeEvent();
            }
            noHorizontalScroll();
            table.addListener(SWT.Resize, resizeEvent);
        } else {
            if (resizeEvent != null)
                table.removeListener(SWT.Resize, resizeEvent);
        }
    }

    public void noHorizontalScroll() {
        try {
            Table t = getTable();
            int count = t.getColumnCount();
            if (count == 0)
                return;

            TableColumn tc = t.getColumn(count - 1);
            int w = tc.getWidth();
            w = t.getBounds().width;
            int amt = 23;
            // int val = 0;
            // String debug = "";
            ScrollBar vb = t.getVerticalBar();
            if (vb != null) {
                boolean v = vb.getVisible();
                // debug += "vis = "+v;
                v = vb.getEnabled();
                // debug += " ena="+v;
                int vx = vb.getSize().x;
                // debug += " size="+vb.getSize();
                // val = vx;
                // if (v) amt -= vx;
                // val = vb.getSelection();
                // debug += " max="+vb.getMaximum();
                // debug += " min="+vb.getMinimum();
                // debug += " sel="+vb.getSelection();
                if (vb.getMaximum() == 0) {
                    amt -= vx;
                }
            }
            ScrollBar hb = t.getHorizontalBar();
            if (hb != null) {
                hb.setVisible(false);
            }
            int nv = w - amt;
            if (nv != w && nv > 0)
                tc.setWidth(nv);
            // println(debug+" tc Width=" + w + " tableWidth=" +
            // t.getBounds().width
            // + "New Width=" + tc.getWidth()+ " val="+val);
        } catch (Throwable t) {
            logger.error("error in noHorizontalScroll", t);

        }
    }

    public void selectAll() {
        table.selectAll();
        selectionChanged();
    }

    // This keeps the size of the column the same size as the table,
    // which you want to do with a single column table.

    public void selectNone() {
        table.setSelection(new int[0]);
        selectionChanged();
    }

    public void packColumns() {

        boolean pack = false;
        switch (packMode) {
            case packAways:
                pack = true;
                break;
            case packNever:
                break;
            case packOnce:
                pack = (packCount == 0);
                break;

            case packLast:

                TableColumn tc[] = table.getColumns();
                tc[tc.length - 1].pack();
                pack = (packCount == 0);
                break;

            default:
                break;

        }
        if (pack && list.size() > 0) {
            for (TableColumn tc : table.getColumns())
                tc.pack();

            packCount++;
        }

    }

    // SelectionListener interface:
    @Override
    public void widgetSelected(final SelectionEvent e) {
        if (e.item instanceof TableItem) {
            TableItem i = (TableItem) e.item;
            E data = (E) i.getData();
            // rowsSelected(e, data);
        }
        selectionChanged();
    }

    protected void selectionChanged() {
    }

    // SelectionListener interface:
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public enum PackMode {
        packNever, packOnce, packLast, packAways
    }

    interface RowListener {
        void rowsSelected(final SelectionEvent evt, final Object row);
    }

    public class _MultiColumnSort implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            E e1 = (E) o1;
            E e2 = (E) o2;
            if (o1 == null && o2 == null)
                return 0;
            if (o1 == null && o2 != null)
                return -1;
            if (o2 == null && o1 != null)
                return 1;

            assert (sortCol != null);

            int i = getColumnComparable(sortCol, e1).compareTo(getColumnComparable(sortCol, e2));
            return reverseSort ? -i : i;
        }
    }

    class ColumnListener implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            TableColumn column = (TableColumn) e.widget;
            F f = (F) column.getData();
            columnClicked(f, 1);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // Do Nothing
        }
    }

    class ResizeEvent implements Listener {
        @Override
        public void handleEvent(Event evt) {
            noHorizontalScroll();
        }
    }

}
