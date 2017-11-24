package org.dgkrajnik.bookstore;

import java.util.List;

public interface BookLister {
    public List<Book> getBooks();
    public List<Book> getBooks(ListerFilter filter);
}
