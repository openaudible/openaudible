package org.openaudible.desktop.swt.gui.tables;

import org.eclipse.swt.widgets.Table;

public abstract class ICallBackSetter {
    public abstract void disableCallbacks();

    public abstract void enableCallbacks();

    public abstract void initCallbacks(Table t);

}
