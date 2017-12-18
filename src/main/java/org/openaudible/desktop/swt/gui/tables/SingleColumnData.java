package org.openaudible.desktop.swt.gui.tables;

public class SingleColumnData implements SuperTableData<Comparable> {
    protected Comparable data;        // Can be any comparable data type.
    protected boolean filtered = false;
    protected int sortIndex, listIndex;

    public SingleColumnData(Comparable<?> c) {
        data = c;
    }

    public Comparable<?> getData() {
        return data;
    }

    public void setData(Comparable<?> d) {
        data = d;
    }

    @Override
    public String getTextValue(SuperTable table, int col) {
        return data.toString();
    }

    @Override
    public int compareTo(SuperTable table, SuperTableData that, int col) {
        if (that instanceof SingleColumnData) {
            SingleColumnData s = (SingleColumnData) that;
            return data.compareTo(s.data);
        }
        return data.compareTo(that); // this can fail.
    }

    @Override
    public int compareTo(SuperTableData t) {
        if (data == null || t == null) {
            return -1;
        }
        return data.compareTo(((SingleColumnData) t).getData());
    }

    @Override
    public boolean isFiltered() {
        return filtered;
    }

    @Override
    public void setFiltered(boolean i) {
        filtered = i;
    }

    @Override
    public int getSortIndex() {
        return sortIndex;
    }

    @Override
    public void setSortIndex(int i) {
        sortIndex = i;
    }

    @Override
    public int getListIndex() {
        return listIndex;
    }

    @Override
    public void setListIndex(int i) {
        listIndex = i;
    }

    @Override
    public boolean equals(Object d) {
        if (d instanceof SingleColumnData) {
            SingleColumnData scd = (SingleColumnData) d;
            return data.equals(scd.data);
        }
        // this may fail.. but may never happen.
        return data.equals(d);
    }

    @Override
    public Comparable getObject() {
        return data;
    }

    public String getString() {
        Comparable c = getObject();
        if (c == null) return "s";
        return c.toString();
    }

}
