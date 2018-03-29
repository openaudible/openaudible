package org.openaudible.desktop.swt.manager.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.openaudible.books.Book;
import org.openaudible.books.BookListener;
import org.openaudible.books.BookNotifier;
import org.openaudible.desktop.swt.gui.SWTAsync;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SummaryPanel implements BookListener {
    StyledText summary;

    SummaryPanel(Composite parent) {
        summary = new StyledText(parent, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        summary.setCaret(null);
        summary.setEditable(false);

        GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        gd.horizontalSpan = 2;
        // gd.heightHint = 100;
        summary.setLayoutData(gd);
        BookNotifier.getInstance().addListener(this);
    }
    AtomicInteger cache = new AtomicInteger();
    @Override
    public void booksSelected(List<Book> list) {
        if (cache.getAndIncrement()>0) return;
        SWTAsync.run(new SWTAsync("update") {
            @Override
            public void task() {
                cache.set(0);
                switch (list.size()) {
                    case 0:
                        update(null);
                        break;
                    case 1:
                        update(list.get(0));
                        break;
                    default:
                        update(null);
                        break;
                }

            }
        });

    }

    private void update(Book book) {
        summary.setText(book != null ? book.getSummary() : "");
    }

}
