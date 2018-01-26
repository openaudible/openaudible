package org.openaudible.books;

import java.util.List;

public interface BookListener {
    // public void bookSelected(final Book b);

    void booksSelected(final List<Book> list);

    void bookAdded(final Book book);

    void bookUpdated(final Book book);

    void booksUpdated();    // refresh all books

}
