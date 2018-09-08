package org.openaudible.books;

import java.util.List;

public interface BookListener {
	default void booksSelected(final List<Book> list) {
	}
	
	default void bookAdded(final Book book) {
	}
	
	default void bookUpdated(final Book book) {
	}
	
	default void booksUpdated() {
	}    // refresh all books
	
	default void bookProgress(final Book book, final String msg) {
	}
	
}
