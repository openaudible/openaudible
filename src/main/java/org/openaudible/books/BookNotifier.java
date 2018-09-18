package org.openaudible.books;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.util.EventNotifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created  6/27/2017.
 * Used as singleton to notify all book listeners about a book event
 * Events are defined in interface BookListener
 */
public class BookNotifier extends EventNotifier<BookListener> implements BookListener {
	private static final Log LOG = LogFactory.getLog(BookNotifier.class);
	private static BookNotifier instance = new BookNotifier();      // singleton.
	
	private ArrayList<Book> selected = new ArrayList<>();
	volatile boolean enabled = true;
	
	private BookNotifier() {
	}
	
	public static BookNotifier getInstance() {
		return instance;
	}
	
	public synchronized List<Book> getSelected() {
		
		return Collections.unmodifiableList(selected);
	}
	
	public void clearSelectedBooks() {
		booksSelected(new ArrayList<>());
	}
	
	@Override
	public void booksSelected(List<Book> list) {
		if (!enabled) return;
		synchronized (getLock()) {
			selected.clear();
			selected.addAll(list);
		}
		
		for (BookListener l : getListeners())
			l.booksSelected(list);
	}
	
	@Override
	public void bookAdded(Book book) {
		if (!enabled) return;
		for (BookListener l : getListeners())
			l.bookAdded(book);
	}
	
	
	@Override
	public void bookUpdated(Book book) {
		if (!enabled) return;
		for (BookListener l : getListeners())
			l.bookUpdated(book);
	}
	
	public static Log getLOG() {
		return LOG;
	}
	
	@Override
	public void booksUpdated() {
		if (!enabled) return;
		
		for (BookListener l : getListeners())
			l.booksUpdated();
	}
	
	@Override
	public void bookProgress(final Book book, final String task) {
		if (enabled) {
			for (BookListener l : getListeners())
				l.bookProgress(book, task);
		}
		
	}
	
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
