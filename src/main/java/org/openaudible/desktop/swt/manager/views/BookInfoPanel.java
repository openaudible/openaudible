package org.openaudible.desktop.swt.manager.views;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.openaudible.Audible;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.books.BookListener;
import org.openaudible.books.BookNotifier;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.i8n.Translate;
import org.openaudible.desktop.swt.util.shop.FontShop;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class BookInfoPanel extends GridComposite implements BookListener {

    private static final Log LOG = LogFactory.getLog(BookInfoPanel.class);

//    , BookElement.codec,  BookElement.genre, BookElement.asin, BookElement.infoLink, , BookElement.summary, BookElement.description,  BookElement.format, BookElement.rating_average, BookElement.rating_count, BookElement.genre, BookElement.shortTitle, BookElement.copyright, BookElement.user_id, BookElement.cust_id };
    final Image cover = PaintShop.getImage("images/cover.png");
    BookElement elems[] = {
            BookElement.fullTitle,
            BookElement.author,
            BookElement.narratedBy,
            BookElement.duration,
            BookElement.release_date,
            BookElement.purchase_date,
            BookElement.publisher,
            BookElement.copyright,
            BookElement.asin,
            BookElement.product_id
    };
    //static final BookElement elems[] = { BookElement.fullTitle, BookElement.author, BookElement.release_date, BookElement.publisher, BookElement.asin, BookElement.product_id };
    Label stats[] = new Label[BookElement.values().length];
    int imageSize = 200;
    Book curBook = null;
    Label imageLabel;

    BookInfoPanel(Composite c) {
        super(c, SWT.NONE);
        initLayout(2, false, GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        // this.debugLayout();
        this.noMargins();

        GridComposite statsView = createStatsView(this);
        createImageLabel(this);
        BookNotifier.getInstance().addListener(this);


    }

    public static String getName(BookElement n) {
        return n.displayName();
    }

    public static String getValue(BookElement n, Book b) {
        String out = b.get(n);

        out = out.replace("#169;", "\u00a9");
        out = out.replace("(C)", "\u00a9");
        out = out.replace("(P)", "\u2117");
        out = out.replace("&amp;", "&&");      // two of them for text items
        out = out.replace("&quot;", "\"");
        out = out.replace("&apos;", "\u0027");
        out = out.replace("&lt;", "<");
        out = out.replace("&gt;", ">");
        return out;
    }

    private void createImageLabel(Composite parent) {
        imageLabel = new Label(parent, SWT.BORDER_SOLID);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
        gd.widthHint = imageSize;
        gd.heightHint = imageSize;
        imageLabel.setLayoutData(gd);
        clearCoverArt();
    }

    private GridComposite createStatsView(GridComposite parent) {
        GridComposite c = null;
        if (true) {
            c = new GridComposite(parent, SWT.BORDER_DOT);

            c.initLayout(2, false, GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
            c.noMargins();


            c.getGridData().horizontalIndent = 0;
            c.getGridData().verticalIndent = 0;
            // c.debugLayout(SWT.COLOR_BLUE);

        } else {
            c = parent;
        }

        for (BookElement s : elems) {


            String labelName = getName(s);
            Label l = c.newLabel();
            l.setText(Translate.getInstance().labelName(labelName) + ": ");
            l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            l.setFont(FontShop.tableFontBold());
            l.setBackground(bgColor);

            Label d = c.newLabel();
            d.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            d.setFont(FontShop.tableFont());
            d.setBackground(bgColor);
            d.setData(s);
            stats[s.ordinal()] = d;
        }
        return c;
    }

    private void update(Book b) {
        curBook = b;


        setThumbnailImage(b);

        for (Label s : stats) {
            if (s == null) continue;
            BookElement e = (BookElement) s.getData();
            String value = "";
            if (b != null) {
                value = getValue(e, b);
            }
            s.setText(value);
        }

        if (b != null) {
            if (LOG.isTraceEnabled())
                LOG.trace(b.inspect("\n"));
        }
    }

    private void clearCoverArt() {
        Image i = imageLabel.getImage();
        if (i != null && i != cover) {
            imageLabel.setImage(null);
            i.dispose();
        }

        imageLabel.setImage(cover);

    }

    private void setThumbnailImage(Book b) {
        clearCoverArt();
        if (b == null) return;

        File imgFile = Audible.instance.getImageFileDest(b);
        if (imgFile.exists()) {
            try {
                try (FileInputStream fis = new FileInputStream(imgFile)) {
                    Image i = new Image(Display.getCurrent(), fis);

                    int width = i.getBounds().width;
                    int height = i.getBounds().height;

                    Image out = new Image(Display.getCurrent(), imageSize, imageSize);
                    GC gc = new GC(out);

                    int x = 0;
                    int y = 0;
                    int w = imageSize;
                    int h = imageSize;
                    if (width != height) {
                        if (width > height) {
                            // width will be imageSize, height imageSize*ratio
                            float ratio = height / (float) width;
                            assert (ratio >= 0 && ratio <= 1);
                            h = Math.round(h * ratio);
                            y = (imageSize - h) / 2;

                        } else {
                            float ratio = width / (float) height;
                            assert (ratio >= 0 && ratio <= 1);

                            w = Math.round(h * ratio);
                            x = (imageSize - w) / 2;
                        }
                        assert (x >= 0 && x <= imageSize);
                        assert (y >= 0 && y <= imageSize);
                        assert (w <= imageSize);
                        assert (h <= imageSize);

                    }

                    gc.drawImage(i, 0, 0, i.getBounds().width, i.getBounds().height, x, y, w, h);

                    Image thumb = PaintShop.resizeImage(i, imageSize, imageSize);
                    i.dispose();

                    imageLabel.setImage(thumb);
                }
            } catch (Throwable th) {
                assert (false);
            }
        }

    }

    private void refresh() {
        refresh(curBook);
    }

    private void refresh(final Book b) {
        SWTAsync.run(new SWTAsync("refresh") {
            @Override
            public void task() {
                update(b);
            }
        });
    }

    @Override
    public void booksSelected(final List<Book> list) {
        SWTAsync.run(new SWTAsync("update") {
            @Override
            public void task() {
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

    @Override
    public void bookAdded(Book book) {
    }

    @Override
    public void bookUpdated(Book book) {
        if (book.equals(curBook)) {
            refresh();
        }
    }

    @Override
    public void booksUpdated() {
        refresh();
    }

}
