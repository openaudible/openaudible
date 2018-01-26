package org.openaudible.desktop.swt.manager.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.audible.ConnectionListener;
import org.openaudible.audible.ConnectionNotifier;
import org.openaudible.books.Book;
import org.openaudible.books.BookListener;
import org.openaudible.books.BookNotifier;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.i8n.Translate;
import org.openaudible.desktop.swt.manager.AudibleGUI;
import org.openaudible.desktop.swt.util.shop.FontShop;

import java.util.List;

public class StatusPanel extends GridComposite implements BookListener, ConnectionListener {
    Label stats[];
    // Label connected;

    StatusPanel(Composite c) {
        super(c, SWT.NONE);
        initLayout(2, false, GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);

        BookNotifier.getInstance().addListener(this);
        ConnectionNotifier.getInstance().addListener(this);
        Status elems[] = Status.values();
        stats = new Label[elems.length];

        for (int x = 0; x < elems.length; x++) {
            if (!elems[x].display())
                continue;
            String labelName = elems[x].displayName();
            Label l = newLabel();
            l.setText(Translate.getInstance().labelName(labelName) + ": ");
            l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            l.setFont(FontShop.tableFontBold());
            l.setBackground(bgColor);
            // TODO: Add more stats when user hovers over stats
            // l.addListener(SWT.MouseHover);
            Label d = newLabel();
            GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
            // gd.widthHint=120;
            d.setLayoutData(gd);

            d.setFont(FontShop.tableFont());
            d.setBackground(bgColor);
            d.setData(elems[x]);
            stats[x] = d;
        }

        _update();
    }

    private void _update() {
        SWTAsync.run(new SWTAsync("update") {
            @Override
            public void task() {

                for (Label s : stats) {
                    if (s == null) continue;

                    Status e = (Status) s.getData();
                    String value = AudibleGUI.instance.getStatus(e);
                    s.setText(value);
                }

            }
        });

    }

    @Override
    public void booksSelected(final List<Book> list) {
        _update();
    }

    @Override
    public void bookAdded(Book book) {
        _update();

    }

    @Override
    public void bookUpdated(Book book) {
        _update();
    }

    @Override
    public void booksUpdated() {
        _update();
    }

    @Override
    public void connectionChanged(boolean connected) {
        _update();
    }

    @Override
    public AudibleAccountPrefs getAccountPrefs(AudibleAccountPrefs in) {
        return in;
    }

    public enum Status {
        Connected, Books, AAX_Files, MP3_Files, To_Download, To_Convert, Downloading, Converting;  //Connection,

        public String displayName() {
            return name().replace('_', ' ');
        }

        public boolean display() {
            switch (this) {
                case Books:
                case To_Convert:
                case Downloading:
                case Converting:
                case MP3_Files:
                case Connected:
                    return true;

                case AAX_Files:
                case To_Download:
                    return false;

                default:
                    assert (false);

            }


            return true;
        }
    }


}
