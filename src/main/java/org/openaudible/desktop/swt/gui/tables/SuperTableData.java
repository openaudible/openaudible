package org.openaudible.desktop.swt.gui.tables;


public interface SuperTableData<E> extends Comparable<SuperTableData<E>> {
    String getTextValue(SuperTable table, int col);

    int compareTo(SuperTable table, SuperTableData<E> that, int col);

    boolean isFiltered();

    void setFiltered(boolean i);

    int getSortIndex();

    void setSortIndex(int i);

    int getListIndex();

    void setListIndex(int i);

    @Override
    int compareTo(SuperTableData<E> t);

    E getObject();
}
