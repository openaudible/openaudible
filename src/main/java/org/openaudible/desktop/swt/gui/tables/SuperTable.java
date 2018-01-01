package org.openaudible.desktop.swt.gui.tables;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.util.shop.FontShop;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SuperTable<E extends SuperTableData<? extends Comparable>> implements SelectionListener {
    // TableDescription desc;
    // SuperTableData items
    public final static Log log = LogFactory.getLog(EnumTable.class);
    // that are "visible" in list (not filtered out)
    public int sortCol = 0; // either -1 (defaultSort) or column to sort by.
    protected Table table;
    // Implement a "fake" tooltip
    final Listener labelListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            Label label = (Label) event.widget;
            Shell shell = label.getShell();
            switch (event.type) {
                case SWT.MouseDown:
                    Event e = new Event();
                    e.item = (TableItem) label.getData("_TABLEITEM");
                    // Assuming table is single select, set the selection as if
                    // the mouse down event went through to the table
                    table.setSelection(new TableItem[]{(TableItem) e.item});
                    table.notifyListeners(SWT.Selection, e);
                    shell.dispose();
                    break;
                case SWT.MouseExit:
                    shell.dispose();
                    break;
            }
        }
    };
    protected final ArrayList<E> list = new ArrayList<>(); // List of all SuperTableData
    protected final HashMap<Object, TableItem> map = new HashMap<>();
    protected Color tableColors[];
    protected boolean allowReverseSort = true;
    protected int numColumns = 1;
    protected Composite parent;
    protected boolean oddEvenColors = false;
    protected boolean needSort = false;
    protected boolean reverseSort = false;
    protected boolean verbose = false;
    protected boolean isVirtual;
    protected int defaultAttributes = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL;
    String tableName = "Super Table";
    boolean tableError = false;
    Menu sortMenu = null;
    // Optional mac optimization code
    ICallBackSetter macTable = null;
    SuperTableFilter filter = null;
    Listener tableListener = new Listener() {
        Shell tip = null;
        Label label = null;

        @Override
        public void handleEvent(Event event) {
            switch (event.type) {
                case SWT.Dispose:
                case SWT.KeyDown:
                case SWT.MouseMove: {
                    if (tip == null)
                        break;
                    tip.dispose();
                    tip = null;
                    label = null;
                    break;
                }
                case SWT.MouseHover: {
                    TableItem item = table.getItem(new Point(event.x, event.y));
                    if (item != null) {
                        if (tip != null && !tip.isDisposed())
                            tip.dispose();
                        Shell shell = table.getShell();
                        Display display = table.getDisplay();
                        tip = new Shell(shell, SWT.ON_TOP | SWT.TOOL);
                        tip.setLayout(new FillLayout());
                        label = new Label(tip, SWT.NONE);
                        label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                        label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                        label.setData("_TABLEITEM", item);
                        label.setText("tooltip " + item.getText());
                        label.addListener(SWT.MouseExit, labelListener);
                        label.addListener(SWT.MouseDown, labelListener);
                        Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        Rectangle rect = item.getBounds(0);
                        Point pt = table.toDisplay(rect.x, rect.y);
                        tip.setBounds(pt.x, pt.y, size.x, size.y);
                        tip.setVisible(true);
                    }
                }
                break;
            }
        }
    };
    // items, sorted, with filter capability.
    private ArrayList<E> virtualList = new ArrayList<>(); // List of all items
    private boolean keepSorted = true;
    private ResizeEvent resizeEvent = null;

    public SuperTable(Composite p) {
        numColumns = 1;
        parent = p;
    }

    public SuperTable(Composite p, int columns) {
        numColumns = columns;
        parent = p;
    }

	/*
    public void addAll(List<E> l)
	{
		synchronized (list)
		{
			list.addAll(l);
		}
	}
	*/

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

    public String getTableName() {
        return tableName;
    }

    // sorted,
    public void setTableName(String n) {
        tableName = n;
    }

    public void setFilter(SuperTableFilter f) {
        filter = f;
    }

    public boolean getKeepSorted() {
        return keepSorted;
    }

    public void setKeepSorted(boolean v) {
        keepSorted = v;

    }

    public void add(E td) {
        if (td == null)
            report("Error with add");
        synchronized (list) {
            list.add(td);
        }
    }

    public void init() {
        init(defaultAttributes);
    }

    public void rowsSelected(SelectionEvent e) {
    }

    // SelectionListener interface:
    @Override
    public void widgetSelected(SelectionEvent e) {

        if (e.detail == SWT.CHECK) {
            TableItem item = (TableItem) e.item;
            boolean state = false;
            if (item != null) {
                state = item.getChecked();
            }
            // Checkbox changed...
            TableItem ti[] = getSelection();
            if (ti.length > 1) {
                for (TableItem aTi : ti) {
                    if (!aTi.equals(e.data)) {
                        aTi.setChecked(state);
                    }
                }
            }
        }

        // println("table: " + e.toString() + " calling rowsSelected");
        rowsSelected(e);
    }

    // SelectionListener interface:
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public boolean setRowColors(RGB c[]) {
        SWTAsync.assertGUI();
        boolean needRefresh = false;
        if (tableColors != null && tableColors.length != c.length) {
            for (Color tableColor : tableColors) {
                tableColor.dispose();
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

    public void println(Object s) {
        log.info("Table[" + tableName + "] " + s);
    }

    public boolean needSort() {
        return needSort;
    }

    public void setNeedSort(boolean b) {
        needSort = b;
    }

    public void sortList(ArrayList<E> l) {
        synchronized (l) {
            if (numColumns > 1) {
                Collections.sort(l, new MultiColumnSort(this, reverseSort));
            } else {
                if (reverseSort) {
                    Collections.sort(l, Collections.reverseOrder());
                } else {
                    Collections.sort(l);
                }
            }
            needSort = false;
        }

    }

    // this may be overridden for advanced sorting (sort by A, then b, then c,
    // for instance.)
    public void sortList() {
        if (keepSorted) {
            sortList(list);
        }
    }

    private void _setTableItems(E d, TableItem ti) {
        ti.setData(d);

        Object r = d.getObject();
        if (r != null) {
            synchronized (list) {
                map.put(r, ti);
                // assert(map.size()==list.size());
            }
        }
        setTableItems(d, ti);
    }

    // may be overridden
    protected void setTableItems(E d, TableItem ti) {
        for (int i = 0; i < numColumns; i++) {
            ti.setText(i, d.getTextValue(this, i));
        }
    }

    public void resetVirtual() {
        if (isVirtual) {
            // assertTable();
            synchronized (list) {
                virtualList.clear();
                map.clear();

                int listIndex = 0;
                int displayPos = 0;
                for (E d : list) {
                    // E d = list.get(y);
                    if (d == null) {
                        report("d was null in table");
                        return;
                    }

                    d.setListIndex(listIndex++);
                    if (d.isFiltered())
                        continue;
                    d.setSortIndex(displayPos++);
                    virtualList.add(d);
                }

                int displaySize = virtualList.size();
                table.removeAll();
                table.setItemCount(displaySize); // this should be ok...
            }
        }
    }

    // can throw IndexOutOfBoundsException...
    public E get(int x) {
        return list.get(x);
    }

    public void resetListToTable(int items) {
        if (isVirtual) {
            resetVirtual();
            return;
        }
        table.setItemCount(items);
        TableItem tis[] = table.getItems();
        println("resetListToTable: Table Items: " + tis.length);
        synchronized (list) {
            int rows = list.size();
            int rowCount = 0;
            for (E std : list) {
                // E d = list.get(y);
                if (std.isFiltered())
                    continue;
                // TableItem ti = virtualTable.getItem(rowCount);
                TableItem ti = tis[rowCount];
                rowCount++;
                if (oddEvenColors)
                    ti.setBackground(tableColors[rowCount % 2]);
                ti.setData(std); // allows us to pull out our custom data from
                // selection of TableItems.
                if (!isVirtual)
                    _setTableItems(std, ti);
            }
        }
        // table.setRedraw(true);
        table.redraw();
    }

    public E getTableData(TableItem ti) {
        if (ti == null || ti.isDisposed())
            return null;
        E td = (E) ti.getData();

        if (td == null) {
            int index = table.indexOf(ti);
            if (index != -1 && index < virtualList.size()) {
                td = virtualList.get(index);
                // _setTableItems(td, ti);
            } else {
                println("getTableData null for TableItem index=" + index + " vs=" + virtualList.size() + " ls=" + list.size() + " sort=" + needSort + " tableSize=" + getItemCount());
                assert (false);
            }
        }

        return td;
    }

    // This must be called from the GUI thread.
    // TableItem is the Table's cell entry that represents the native table
    // cell.
    public TableItem getTableItem(E t) {
        assert (t != null);

        boolean f = t.isFiltered();
        if (f)
            return null;

        Object o = t.getObject();
        if (o != null) {
            TableItem ti = map.get(o);
            if (ti != null)
                return ti;

        }

        int index = -1;
        try {
            index = t.getSortIndex();
            if (index == -1) {
                index = virtualList.indexOf(t);
            }
            if (index != -1) {
                TableItem ti = table.getItem(index);
                assert (ti != null);
                if (ti != null) {
                    if (o != null)
                        map.put(o, ti);
                    ti.setData(t);
                    // if (f) log.println("getTableItem returned data on filtered
                    // item.");
                    return ti;
                }

            }
        } catch (IllegalArgumentException ignore) {
        } catch (Throwable ex) {
            log.error("getTableItem failed: index=" + index + " for table " + this, ex);
        }
        // if (!f) log.println("getTableItem null on unfiltered item.");
        // This can happen when a table item is no longer
        // visible.
        // println("getTableItem failed: "+t);
        return null;
    }

    protected Listener getVirtualDataListener() {
        return event -> {
            try {
                if (virtualList.size() > 0) {
                    TableItem item = (TableItem) event.item;
                    int index = table.indexOf(item);
                    if (index != -1) {
                        E d = virtualList.get(index);
                        _setTableItems(d, item);
                        if (oddEvenColors) {
                            item.setBackground(tableColors[index % 2]);
                        }
                    }

                }
                // println("virtual Data " + index + " d=" + d.toString());
            } catch (Throwable e) {
                tableError = true;
                println("virtual draw error " + e.getMessage());

                log.error(e);

            }
            /*
             * item.setText("Item " + index);
             * System.out.println(item.getText());
             */
        };
    }

    public void initCallbacks() {
        if (isVirtual) {
            table.addListener(SWT.SetData, getVirtualDataListener());
        }

    }

    public void disableCallbacks() {
        if (macTable != null)
            macTable.disableCallbacks();
    }

    public void enableCallbacks() {
        if (macTable != null)
            macTable.enableCallbacks();
    }

    public void _populateDataGUI() {
        updateSortByIndicator();
        sortList();
        resetVirtual();
        assertTable();
    }

    public void populateData() {
        SWTAsync.run(new SWTAsync("populateData") {
            @Override
            public void task() {
                _populateDataGUI();
            }
        });

    }

    public void report(String s) {
        log.error(s);
        new Throwable(s).printStackTrace();
    }

    public boolean setTableItemImage(TableItem ti, Image img, int col) {
        Image orig = ti.getImage();
        if (img == orig)
            return false;
        disableCallbacks();
        ti.setImage(col, img);
        enableCallbacks();
        return true;
    }

    public boolean setTableItemImages(TableItem ti, Image img[]) {
        disableCallbacks();
        for (int x = 0; x < img.length; x++)
            ti.setImage(x, img[x]);
        enableCallbacks();
        return true;
    }

    public void updateContents(int displayCount) {
        // ProgressDialog.setTask(null, "set table item count to " +
        // displayCount);
        sortList();
        if (isVirtual) {
            resetVirtual();
            return;
        }
        // THIS CODE IS NOT TYPICALLY USED, SINCE WE USE VIRTUAL TABLES
        // EVERYWHERE...
        table.setItemCount(displayCount);
        TableItem tis[] = table.getItems();
        // println("Table Items: " + tis.length);
        synchronized (list) {
            int rowCount = 0;
            int rows = list.size();
            for (E d : list) {
                if (d.isFiltered())
                    continue;
                // TableItem ti = virtualTable.getItem(rowCount);
                TableItem ti = tis[rowCount];
                rowCount++;
                if (oddEvenColors)
                    ti.setBackground(tableColors[rowCount % 2]);
                _setTableItems(d, ti);
            }
        }
    }

    public void clearSelection() {
        // println("clearSelection:"+table.getSelectionCount());
        // getTable().setSelection(0,0);
        getTable().deselectAll();
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
        for (int x = 0; x < c; x++) {
            if (sortCol == x) {
                tc = table.getColumn(x);
                break;
            }
        }
        final boolean rSort = this.reverseSort;
        final TableColumn ftc = tc;
        setSortColumn(ftc, null, rSort);

    }

    public void columnClicked(int col, int count) {
        if (keepSorted) {
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
                // EventTimer evt = new EventTimer();
                // println("sorting... " + col + " reverse=" + reverseSort);
                populateData();
                // if (getListCount() > 100)
                // println(evt.reportString("sorted list"));
            }
        }
    }

    protected void createSortMenu(String menuNames[]) {
        if (table == null)
            return;
        sortMenu = new Menu(table.getShell(), SWT.POP_UP);
        for (int x = 0; x < menuNames.length; x++) {
            MenuItem item = new MenuItem(sortMenu, SWT.PUSH);
            Integer ID = x;
            item.setData(ID);
            item.setText(menuNames[x]);
            item.addSelectionListener(this);
        }
        table.setMenu(sortMenu);
    }

    // This should be overridden if using multiple columns
    public String getColumnName(int zIndex) {
        if (numColumns == 1)
            return tableName;
        return "col " + (zIndex + 1);
    }

    public ColumnListener getColumnListener() {
        return new ColumnListener();
    }

    public void setColumWidths() {
        int c = getColumnCount();
        for (int x = 0; x < c; x++) {
            TableColumn tc = table.getColumn(x);
            tc.setWidth(64);
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

    public void setColumWidths(int t[]) {
        for (int x = 0; x < t.length; x++) {
            if (x < numColumns) {
                TableColumn tc = table.getColumn(x);
                tc.setWidth(t[x]);
            }
        }

    }

    public Image getColumnImage(int index) {
        return null;
    }

    public void clear() {
        synchronized (list) {
            list.clear();
            virtualList.clear();
            map.clear();
        }
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
        int index = sd.getSortIndex();
        table.setSelection(index);
    }

    public void addKeyBoardListener() {
        /* KeyPressed Event */
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
                System.err.println(e);

                if (e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) {

                    if (e.character == 'a' || e.character == 'A') {
                        table.selectAll();
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
        attributes |= SWT.VIRTUAL;
        isVirtual = (attributes & SWT.VIRTUAL) != 0;
        table = new Table(parent, attributes);
        table.setHeaderVisible(true);
        if (oddEvenColors)
            enableBasicOddEvenColors();

        changeTableLayout(numColumns);

        table.setData(this);
        if (sortMenu != null)
            table.setMenu(sortMenu);
        table.setFont(FontShop.tableFont());

        initCallbacks();
        addKeyBoardListener();
        addSelectionListener(this);
    }

    public int getColumnCount() {
        return numColumns;
    }

    public int[] getSelectionIndices() {
        return table.getSelectionIndices();
    }

    public TableItem[] getSelection() {
        return table.getSelection();
    }

    public List<E> getSelectedItems() {
        TableItem ti[] = table.getSelection();

        // int ids[] = table.getSelectionIndices();
        // int len = ids.length;
        ArrayList<E> out = new ArrayList<>(ti.length);
        for (TableItem i : ti) {
            E std = getTableData(i);
            assert (std != null);
            if (std != null)
                out.add(std);
        }
        return out;
    }

    // Get the number of items displayed in the list.
    // This is list.size() less the number of items filtered
    public int getItemCount() {
        return table.getItemCount();
    }

    // get the total number of items, may be greater than displayed list size.
    public int getListCount() {
        return list.size();
    }

    public Table getTable() {
        return table;
    }

    public void remove(E t) {
        synchronized (list) {
            list.remove(t);
        }
    }

    // return list of E
    public ArrayList<E> getList() {
        return list;
    }

    // Reset the default sort
    public boolean setDefaultSort() {
        if (sortCol != 0) {
            sortCol = 0;
            needSort = true;
            return true;
        }
        return false;
    }

	/*
	 * NOT WORKING.. 
	public void setSelectedList(Collection<?> list)
	{
		if (list.size() == 0)
		{
			clearSelection();
			return;
		}
		ArrayList<Integer> ints = new ArrayList<Integer>();
	
		for (Object o : list)
		{
			TableItem ti = null;
	
			if (o instanceof SuperTableData)
			{
				E s = (E) o;
				TableItem ti = getTableItem(s);
			} else
				ti = getTableItem(o);
	
			if (ti != null)
			{
				ints.add(s.getListIndex());
			}
	
		}
		int i[] = new int[ints.size()];
		for (int x = 0; x < ints.size(); x++)
			i[x] = ints.get(x);
		table.select(i);
	
	}
	*/

    public int getSortColumn() {
        return sortCol;
    }

    public boolean setSelected(Comparable<?> s) {
        boolean found = false;
        if (s != null) {
            int c = getListCount();
            for (int x = 0; x < c; x++) {
                E dt = get(x);
                if (dt.equals(s)) {
                    getTable().select(x);
                    found = true;
                }
            }
        }
        return found;
    }

    public void addSelectionListener(SelectionListener listener) {
        table.addSelectionListener(listener);
    }

    public void inspectTable(int max) {
        println("Inspect " + toString() + " vc=" + virtualCount() + " map=" + map.size() + " lc=" + list.size() + " tableError=" + tableError + " needSort=" + needSort);
        if (Display.getCurrent().getThread().equals(Thread.currentThread())) {
            if (true)
                for (int x = 0; x < table.getItemCount() && x < max; x++) {
                    TableItem ti = table.getItem(x);
                    println("ti:" + x + "=" + ti.getText() + " " + ti.getData());
                }
            if (true)
                for (int x = 0; x < virtualList.size() && x < max; x++) {
                    E std = virtualList.get(x);

                    println("vl:" + x + "=" + std.getListIndex() + " col0=" + std.getTextValue(this, 0));
                }
        }

    }

    public int virtualCount() {
        return virtualList.size();
    }

    public boolean assertTable() {
        if (table.getItemCount() != virtualCount()) {
            if (!tableError) {
                tableError = true;
            }

            return false;
        }
        tableError = false;
        return true; // if (!assertTable())
    }

    protected void installFakeTooptips() {
        table.addListener(SWT.Dispose, tableListener);
        table.addListener(SWT.KeyDown, tableListener);
        table.addListener(SWT.MouseMove, tableListener);
        table.addListener(SWT.MouseHover, tableListener);
        // Disable native tooltip
        table.setToolTipText("");
    }

    public int getColumnWidth(int c) {
        return 64;
    }

    public void changeTableLayout(int i) {
        table.setVisible(false);
        table.removeAll();
        TableColumn tc[] = table.getColumns();
        for (TableColumn aTc : tc) {
            aTc.dispose();
        }
        numColumns = i;

        for (int x = 0; x < numColumns; x++) {
            TableColumn column = new TableColumn(table, SWT.LEFT);
            String n = getColumnName(x);
            Image r = getColumnImage(x);
            column.setImage(r);
            column.setText(n);
            column.setWidth(getColumnWidth(x));
        }
        setColumWidths();
        // populateData(); // TODO: See about removing this.
        ColumnListener columnListener = getColumnListener();
        for (int c = 0; c < table.getColumnCount(); c++) {
            table.getColumn(c).addSelectionListener(columnListener);
        }
        table.setVisible(true);
    }

    public void addSelectAllListener() {
        table.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean ctlDown = ((e.stateMask & SWT.CTRL) != 0);
                if (e.keyCode == 97 && ctlDown) {
                    table.selectAll();
                }
                // System.err.println(e.toString()+" char="+e.character);
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
            // got one report of an error somewhere in this function.
            // search bug reports for mccary@hotmail.com
			/*
			 *
			 * error_stacktrace=java.lang.ArrayIndexOutOfBoundsException: 4 at
			 * org.eclipse.swt.widgets.Table._getItem(int) at
			 * org.eclipse.swt.widgets.Table.wmNotifyChild(NMHDR, int, int) at
			 * org.eclipse.swt.widgets.Control.wmNotify(NMHDR, int, int) at
			 * org.eclipse.swt.widgets.Composite.wmNotify(NMHDR, int, int) at
			 * org.eclipse.swt.widgets.Control.WM_NOTIFY(int, int) at
			 * org.eclipse.swt.widgets.Control.windowProc(int, int, int, int) at
			 * org.eclipse.swt.widgets.Display.windowProc(int, int, int, int)
			 * <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.CallWindowProcW(int, int, int,
			 * int, int) at
			 * org.eclipse.swt.internal.win32.OS.CallWindowProc(int, int, int,
			 * int, int) at org.eclipse.swt.widgets.Table.callWindowProc(int,
			 * int, int, int, boolean) at
			 * org.eclipse.swt.widgets.Table.callWindowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Control.windowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Table.windowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Display.windowProc(int, int, int, int)
			 * <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.CallWindowProcW(int, int, int,
			 * int, int) at
			 * org.eclipse.swt.internal.win32.OS.CallWindowProc(int, int, int,
			 * int, int) at org.eclipse.swt.widgets.Table.callWindowProc(int,
			 * int, int, int, boolean) at
			 * org.eclipse.swt.widgets.Table.callWindowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Control.windowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Table.windowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Display.windowProc(int, int, int, int)
			 * <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.CallWindowProcW(int, int, int,
			 * int, int) at
			 * org.eclipse.swt.internal.win32.OS.CallWindowProc(int, int, int,
			 * int, int) at org.eclipse.swt.widgets.Table.callWindowProc(int,
			 * int, int, int, boolean) at
			 * org.eclipse.swt.widgets.Table.callWindowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Table.windowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Display.windowProc(int, int, int, int)
			 * <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.CallWindowProcW(int, int, int,
			 * int, int) at
			 * org.eclipse.swt.internal.win32.OS.CallWindowProc(int, int, int,
			 * int, int) at org.eclipse.swt.widgets.Table.callWindowProc(int,
			 * int, int, int, boolean) at
			 * org.eclipse.swt.widgets.Table.callWindowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Control.windowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Table.windowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Display.windowProc(int, int, int, int)
			 * <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.SendMessageW(int, int, int,
			 * int) at org.eclipse.swt.internal.win32.OS.SendMessage(int, int,
			 * int, int) at org.eclipse.swt.widgets.TableColumn.setWidth(int) at
			 * ss.tables.SuperTable.noHorizontalScroll() at
			 * ss.tables.SuperTable$ResizeEvent.handleEvent(Event) at
			 * org.eclipse.swt.widgets.EventTable.sendEvent(Event) at
			 * org.eclipse.swt.widgets.Widget.sendEvent(Event) at
			 * org.eclipse.swt.widgets.Widget.sendEvent(int, Event, boolean) at
			 * org.eclipse.swt.widgets.Widget.sendEvent(int) at
			 * org.eclipse.swt.widgets.Table.setDeferResize(boolean) at
			 * org.eclipse.swt.widgets.Table.setBounds(int, int, int, int, int,
			 * boolean) at org.eclipse.swt.widgets.Control.setBounds(int, int,
			 * int, int, int) at org.eclipse.swt.widgets.Control.setBounds(int,
			 * int, int, int) at
			 * org.eclipse.swt.custom.SashFormLayout.layout(Composite, boolean)
			 * at org.eclipse.swt.widgets.Composite.updateLayout(boolean,
			 * boolean) at org.eclipse.swt.widgets.Composite.WM_SIZE(int, int)
			 * at org.eclipse.swt.widgets.Control.windowProc(int, int, int, int)
			 * at org.eclipse.swt.widgets.Display.windowProc(int, int, int, int)
			 * <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.DefWindowProcW(int, int, int,
			 * int) at org.eclipse.swt.internal.win32.OS.DefWindowProc(int, int,
			 * int, int) at
			 * org.eclipse.swt.widgets.Scrollable.callWindowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Control.WM_WINDOWPOSCHANGED(int,
			 * int) at org.eclipse.swt.widgets.Control.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Display.windowProc(int, int, int,
			 * int) <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.EndDeferWindowPos(int) at
			 * org.eclipse.swt.widgets.Composite.resizeChildren(boolean,
			 * WINDOWPOS[]) at
			 * org.eclipse.swt.widgets.Composite.resizeChildren() at
			 * org.eclipse.swt.widgets.Composite.setResizeChildren(boolean) at
			 * org.eclipse.swt.widgets.Composite.WM_SIZE(int, int) at
			 * org.eclipse.swt.widgets.Control.windowProc(int, int, int, int) at
			 * org.eclipse.swt.widgets.Display.windowProc(int, int, int, int)
			 * <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.DefWindowProcW(int, int, int,
			 * int) at org.eclipse.swt.internal.win32.OS.DefWindowProc(int, int,
			 * int, int) at
			 * org.eclipse.swt.widgets.Scrollable.callWindowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Control.WM_WINDOWPOSCHANGED(int,
			 * int) at org.eclipse.swt.widgets.Control.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Display.windowProc(int, int, int,
			 * int) <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.EndDeferWindowPos(int) at
			 * org.eclipse.swt.widgets.Composite.resizeChildren(boolean,
			 * WINDOWPOS[]) at
			 * org.eclipse.swt.widgets.Composite.resizeChildren() at
			 * org.eclipse.swt.widgets.Composite.setResizeChildren(boolean) at
			 * org.eclipse.swt.widgets.Composite.WM_SIZE(int, int) at
			 * org.eclipse.swt.widgets.Control.windowProc(int, int, int, int) at
			 * org.eclipse.swt.widgets.Display.windowProc(int, int, int, int)
			 * <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.DefWindowProcW(int, int, int,
			 * int) at org.eclipse.swt.internal.win32.OS.DefWindowProc(int, int,
			 * int, int) at
			 * org.eclipse.swt.widgets.Scrollable.callWindowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Control.WM_WINDOWPOSCHANGED(int,
			 * int) at org.eclipse.swt.widgets.Control.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Display.windowProc(int, int, int,
			 * int) <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.EndDeferWindowPos(int) at
			 * org.eclipse.swt.widgets.Composite.resizeChildren(boolean,
			 * WINDOWPOS[]) at
			 * org.eclipse.swt.widgets.Composite.resizeChildren() at
			 * org.eclipse.swt.widgets.Composite.setResizeChildren(boolean) at
			 * org.eclipse.swt.widgets.Composite.WM_SIZE(int, int) at
			 * org.eclipse.swt.widgets.Canvas.WM_SIZE(int, int) at
			 * org.eclipse.swt.widgets.Decorations.WM_SIZE(int, int) at
			 * org.eclipse.swt.widgets.Control.windowProc(int, int, int, int) at
			 * org.eclipse.swt.widgets.Canvas.windowProc(int, int, int, int) at
			 * org.eclipse.swt.widgets.Decorations.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Shell.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Display.windowProc(int, int, int,
			 * int) <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.DefWindowProcW(int, int, int,
			 * int) at org.eclipse.swt.internal.win32.OS.DefWindowProc(int, int,
			 * int, int) at org.eclipse.swt.widgets.Shell.callWindowProc(int,
			 * int, int, int) at
			 * org.eclipse.swt.widgets.Control.WM_WINDOWPOSCHANGED(int, int) at
			 * org.eclipse.swt.widgets.Canvas.WM_WINDOWPOSCHANGED(int, int) at
			 * org.eclipse.swt.widgets.Control.windowProc(int, int, int, int) at
			 * org.eclipse.swt.widgets.Canvas.windowProc(int, int, int, int) at
			 * org.eclipse.swt.widgets.Decorations.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Shell.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Display.windowProc(int, int, int,
			 * int) <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.DefWindowProcW(int, int, int,
			 * int) at org.eclipse.swt.internal.win32.OS.DefWindowProc(int, int,
			 * int, int) at org.eclipse.swt.widgets.Shell.callWindowProc(int,
			 * int, int, int) at org.eclipse.swt.widgets.Control.windowProc(int,
			 * int, int, int) at org.eclipse.swt.widgets.Canvas.windowProc(int,
			 * int, int, int) at
			 * org.eclipse.swt.widgets.Decorations.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Shell.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Display.windowProc(int, int, int,
			 * int) <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.DefWindowProcW(int, int, int,
			 * int) at org.eclipse.swt.internal.win32.OS.DefWindowProc(int, int,
			 * int, int) at org.eclipse.swt.widgets.Shell.callWindowProc(int,
			 * int, int, int) at org.eclipse.swt.widgets.Control.windowProc(int,
			 * int, int, int) at org.eclipse.swt.widgets.Canvas.windowProc(int,
			 * int, int, int) at
			 * org.eclipse.swt.widgets.Decorations.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Shell.windowProc(int, int, int,
			 * int) at org.eclipse.swt.widgets.Display.windowProc(int, int, int,
			 * int) <Break in method call trace. Could be due to JIT compiler
			 * inlining of method.> at
			 * org.eclipse.swt.internal.win32.OS.DispatchMessageW(MSG) at
			 * org.eclipse.swt.internal.win32.OS.DispatchMessage(MSG) at
			 * org.eclipse.swt.widgets.Display.readAndDispatch() at
			 * ss.controller.GUI.runEventLoop()
			 */
            log.error("error in noHorizontalScroll", t);

        }
    }

    // This keeps the size of the column the same size as the table,
    // which you want to do with a single column table.

    public List<?> getSelectedData() {
        ArrayList l = new ArrayList();
        for (E e : getSelectedItems()) {
            l.add(e.getObject());

        }
        return l;
    }

    public List<?> getDataList() {
        ArrayList l = new ArrayList();
        for (E e : getList()) {
            l.add(e.getObject());
        }
        return l;
    }

    public TableItem getTableItemByData(Comparable c) {
        assert (!(c instanceof SuperTableData)); // uses the object of
        return map.get(c);
    }

    public E getTableData(Comparable c) {
        TableItem t = getTableItemByData(c);
        if (t != null) {
            return (E) t.getData();
        }
        return null;
    }

    public void selectAll() {
        table.selectAll();
        rowsSelected(null); // signal that selection changed.
    }

    public void selectNone() {
        table.setSelection(new int[0]);
        rowsSelected(null); // signal that selection changed.
    }

    // Return false if should exclude from visible (Displayed) list.
    public interface SuperTableFilter {
        boolean filterTest(Object t);
    }

    class ColumnListener implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            TableColumn column = (TableColumn) e.widget;
            Table table = column.getParent();
            int columnIndex = table.indexOf(column);
            columnClicked(columnIndex, 1);
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
