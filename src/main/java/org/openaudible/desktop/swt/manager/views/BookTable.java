package org.openaudible.desktop.swt.manager.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.openaudible.Audible;
import org.openaudible.books.Book;
import org.openaudible.books.BookListener;
import org.openaudible.books.BookNotifier;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.gui.tables.EnumTable;
import org.openaudible.desktop.swt.manager.AudibleGUI;
import org.openaudible.desktop.swt.manager.menu.AppMenu;
import org.openaudible.desktop.swt.util.shop.PaintShop;
import org.openaudible.util.TimeToSeconds;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BookTable extends EnumTable<Book, BookTableColumn> implements BookListener {
	
	final Image hasMP3;
	final Image hasAAX;
	final Image hasNothing;
	
	
	public BookTable(Composite c) {
		super(c, BookTableColumn.values());
		init();
		setPackMode(PackMode.packNever);
		addSelectAllListener();
		populate();
		BookNotifier.getInstance().addListener(this);
		
		table.setMenu(AppMenu.instance.getBookTableMenu());
		
		hasMP3 = PaintShop.getImage("images/green.gif");
		hasAAX = PaintShop.getImage("images/audible.png");
		hasNothing = PaintShop.getImage("images/white.png");
		final Rectangle rect = new Rectangle(0, 0, 12, 12);
		table.getColumn(0).setImage(hasAAX);
		table.getColumn(0).setText("");
		
		final Listener paintListener = event -> {
			final Image image = hasMP3;
			
			switch (event.type) {
				case SWT.MeasureItem: {
					event.width += rect.width;
					event.height = Math.max(event.height, rect.height + 2);
					break;
				}
				case SWT.PaintItem: {
					int x = event.x + event.width;
					Rectangle rect1 = image.getBounds();
					int offset = Math.max(0, (event.height - rect1.height) / 2);
					event.gc.drawImage(image, x, event.y + offset);
					break;
				}
			}
		};
		if (false) {
			table.addListener(SWT.MeasureItem, paintListener);
			table.addListener(SWT.PaintItem, paintListener);
		}
		
	}
	
	@Override
	protected void setTableItems(TableItem item, Book b) {
		super.setTableItems(item, b);
		
		int index = 0;
		for (TableColumn i : tableColumns) {
			BookTableColumn o = (BookTableColumn) i.getData();
			if (o == BookTableColumn.File) {
				item.setImage(getFileImage(b));
				item.setText("");
			}
		}
	}
	
	private Image getFileImage(Book b) {
		if (Audible.instance.hasMP3(b))
			return hasMP3;
		if (Audible.instance.hasAAX(b))
			return hasAAX;
		return hasNothing;
	}
	
	AtomicInteger cache = new AtomicInteger();
	
	public void populate() {
		if (cache.getAndIncrement() == 0) {
			SWTAsync.run(new SWTAsync("populate_table") {
				@Override
				public void task() {
					cache.set(0);
					setItems(AudibleGUI.instance.getDisplayedBooks());
				}
			});
		}
	}
	
	public String getColumnName(BookTableColumn col) {
		switch (col) {
/*
            case HasAAX:
                return "X";
            case HasMP3:
                return "M";
*/
			case File:
				return "F";
			default:
				return super.getColumnName(col);
		}
	}
	
	
	public String getColumnDisplayable(BookTableColumn column, Book b) {
		String s;
		if (column.equals(BookTableColumn.Time)) {
			//long seconds = TimeToSeconds.parseTimeStringToSeconds(b.getDuration());
			// TimeToSeconds.secondsToTime()
			return b.getDurationHHMM();
			
		}
		s = super.getColumnDisplayable(column, b);
		return s;
	}
	
	
	public Comparable<?> getColumnComparable(BookTableColumn column, Book b) {
		switch (column) {
			case Author:
				return b.getAuthor();
			case File:
				String x = AudibleGUI.instance.hasAAX(b) ? "Y" : "N";
				String y = AudibleGUI.instance.hasMP3(b) ? "Y" : "N";
				return x + y;

/*
            case HasAAX:
                return AudibleGUI.instance.hasAAX(b) ? "Y" : "N";
            case HasMP3:
                return AudibleGUI.instance.hasMP3(b) ? "Y" : "N";
*/
			case Narrated_By:
				return b.getNarratedBy();
			
			case Time:
				// compare duration as seconds, not as a string..
				return TimeToSeconds.parseTimeStringToSeconds(b.getDuration());
			case Title:
				return b.getFullTitle();
			
			case Released:
				return b.getReleaseDateSortable();
			case Purchased:
				return b.getPurchaseDateSortable();
			
			default:
				assert (false);
				break;
			
		}
		
		return "";
		
	}
	
	public int[] getColumWidths() {
		return BookTableColumn.getWidths();
	}
	
	@Override
	protected void selectionChanged() {
		BookNotifier.getInstance().booksSelected(this.getSelectedItems());
	}
	
	@Override
	public void booksSelected(List<Book> list) {
	}
	
	@Override
	public void bookAdded(Book book) {
		populate();
	}
	
	@Override
	public void bookUpdated(final Book book) {
		SWTAsync.run(new SWTAsync("populateData") {
			@Override
			public void task() {
				redrawItem(book);
			}
		});
		
	}
	
	@Override
	public void booksUpdated() {
		populate();
	}
	
}
