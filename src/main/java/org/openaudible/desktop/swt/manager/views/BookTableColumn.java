package org.openaudible.desktop.swt.manager.views;

public enum BookTableColumn {
    File, Title, Author, Narrated_By, Time, Purchased;
    static int widths[] = {22, 250, 150, 150, 60, 90};

    // HasAAX, HasMP3,
    public static int[] getWidths() {
        return widths;
    }
}

