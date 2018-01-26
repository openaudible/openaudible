package org.openaudible.desktop.swt.manager.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.openaudible.books.Book;
import org.openaudible.books.BookListener;
import org.openaudible.books.BookNotifier;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.manager.menu.Command;
import org.openaudible.desktop.swt.manager.menu.CommandCenter;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.util.List;

public class BookButtonBar implements BookListener, SelectionListener {
    public final GridData gd;
    final ToolBar toolbar;
    Command cmds[] = {Command.Quick_Refresh, Command.Rescan_Library, Command.Download, Command.Convert, Command.ViewInAudible, Command.Show_MP3, Command.Play, Command.Preferences};

    BookButtonBar(Composite parent, int gridStyle) {
        toolbar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
        gd = new GridData(gridStyle);
        toolbar.setLayoutData(gd);

        for (Command cmd : cmds) {
            addIconButton(toolbar, cmd);
        }
        BookNotifier.getInstance().addListener(this);
        refresh();
    }

    public ToolItem addIconButton(ToolBar toolBar, Command cmd) {
        final ToolItem item = new ToolItem(toolBar, SWT.PUSH);

        String icon = "icons/" + cmd.name().toLowerCase() + ".png";

        item.setImage(PaintShop.getImage(icon));
        item.setText("");
        item.setToolTipText(cmd.getToolTip());
        item.setData(cmd);
        item.addSelectionListener(this);
        return item;
    }

    @Override
    public void booksSelected(List<Book> list) {
        refresh();
    }

    private void refresh() {
        SWTAsync.run(new SWTAsync("refresh") {
            @Override
            public void task() {
                for (ToolItem i : toolbar.getItems()) {
                    Command c = (Command) i.getData();
                    boolean enabled = CommandCenter.instance.getEnabled(c);
                    i.setEnabled(enabled);
                }
            }
        });
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        Command c = (Command) e.widget.getData();
        CommandCenter.instance.execute(c);
    }

    @Override
    public void bookAdded(Book book) {

    }

    @Override
    public void bookUpdated(Book book) {

    }

    @Override
    public void booksUpdated() {
        refresh();
    }
}
