package org.openaudible.desktop.swt.manager.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class MainWindow extends GridComposite {
    final int gdFlags = GridData.VERTICAL_ALIGN_BEGINNING;
    BookTable bookTable;
    BookInfoPanel info;
    StatusPanel status;
    boolean useToolbar = false;

    public MainWindow(Composite c) {
        super(c, SWT.NONE);
        initLayoutFillMax();
        SashForm main = new SashForm(this, SWT.VERTICAL);
        main.setLayout(new GridLayout(1, true));
        main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createTop(main);
        createBottom(main);
        // top a little bigger than the bottom..
        int weights[] = {60, 40};
        main.setWeights(weights);

        FileDropTarget.attach(main);

    }

    void createTop(Composite parent) {
        GridComposite c = new GridComposite(parent, SWT.NONE);

        c.initLayout(1, false, GridData.FILL_BOTH);
        GridComposite row;
        // c.debugLayout(SWT.COLOR_DARK_MAGENTA);

        // Row 1, search bar and button bar.
        if (useToolbar) {
            row = new GridComposite(c, SWT.BORDER_DOT, useToolbar ? 2 : 1, false, GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
            SearchField sf = new SearchField(row, 180, GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
            BookButtonBar b = new BookButtonBar(row, GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
        } else {
            row = new GridComposite(c, SWT.BORDER_DOT, 1, false, GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
            row.noMargins();
            SearchField sf = new SearchField(row, 120, GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
        }

        // Row 2. Status and selected book
        {
            row = new GridComposite(c, 0);
            row.initLayout(2, false, gdFlags | GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
            libraryStatus(row);
            selectedBook(row);


        }


    }

    void libraryStatus(GridComposite row) {
        Composite statusGroup = row.newGroup("Library Status");
        GridData gd = new GridData(gdFlags);
        gd.widthHint = 170;
        statusGroup.setLayoutData(gd);
        status = new StatusPanel(statusGroup);


    }

    private void createBottom(Composite parent) {
        bookTable = new BookTable(parent);
        bookTable.getTable().setFocus();
    }

    private void selectedBook(GridComposite row) {
        Composite group = row.newGroup("Audio Book Info", 1);
        group.setLayoutData(new GridData(gdFlags | GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        info = new BookInfoPanel(group);
        info.setLayoutData(new GridData(gdFlags | GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        SummaryPanel p = new SummaryPanel(group);
        p.summary.setLayoutData(new GridData(gdFlags | GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    }
}
