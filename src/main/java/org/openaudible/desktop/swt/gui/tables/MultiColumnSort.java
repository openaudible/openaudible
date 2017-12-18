package org.openaudible.desktop.swt.gui.tables;

public class MultiColumnSort implements java.util.Comparator {
    final int sortOrder[];
    final boolean reverseOrder;
    final SuperTable st;

    public MultiColumnSort(SuperTable st, boolean reverseOrder) {
        this(st, null, reverseOrder);
    }

    public MultiColumnSort(SuperTable st, int sort[], boolean reverseOrder) {
        if (sort != null) {
            this.sortOrder = sort;
        } else {
            sortOrder = new int[st.getColumnCount()];
            int sortCol = st.sortCol;
            sortOrder[0] = sortCol;

            for (int x = 1; x < sortOrder.length; x++) {
                if (x == sortCol)
                    sortOrder[x] = sortCol - 1;
                else
                    sortOrder[x] = x;
            }
        }
        this.reverseOrder = reverseOrder;
        this.st = st;
        // ss.desktop.Platform.isMac() ? false :

    }

    @Override
    public int compare(Object o1, Object o2) {
        MultiColumnData t1 = (MultiColumnData) o1;
        MultiColumnData t2 = (MultiColumnData) o2;
        for (int x = 0; x < sortOrder.length; x++) {
            int v = t1.compareTo(st, t2, sortOrder[x]);
            if (v != 0)
                return reverseOrder ? -v : v;
        }
        return 0;
    }
}
