package org.openaudible.desktop.swt.manager.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openaudible.desktop.swt.manager.AudibleGUI;
import org.openaudible.desktop.swt.util.shop.PaintShop;
// Small search text input field for filtering results based on string.
public class SearchField extends GridComposite {
    Text text;

    public SearchField(Composite c, int widthHint, int gdStyle) {
        super(c, SWT.NONE);
        initLayout(2, false, gdStyle);
        Label l = new Label(this, SWT.NONE);
        l.setImage(PaintShop.getImage("icons/search.png")); // use our search icon rather than the SWT.ICON_SEARCH
        text = new Text(this, SWT.SEARCH | SWT.ICON_CANCEL);
        text.setEditable(true);
        text.setCursor(null);
        GridData gd = new GridData();
        gd.widthHint = widthHint;
        text.setLayoutData(gd);

        text.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                AudibleGUI.instance.filterDisplayedBooks(text.getText());
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
    }
}