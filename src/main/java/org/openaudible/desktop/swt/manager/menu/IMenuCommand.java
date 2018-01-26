package org.openaudible.desktop.swt.manager.menu;

import org.eclipse.swt.widgets.MenuItem;

public interface IMenuCommand {
    void execute();

    void update(MenuItem m);

    int getStyle();

    String getMenuName();

    char getMenuEquivalent();

    String getImage();

    boolean isChecked(MenuItem m);

    boolean isCheckable();

    boolean isEnabled(MenuItem m);


}

