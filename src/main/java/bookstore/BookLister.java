package org.dgkrajnik.bookstore;

import java.util.List;
import java.sql.SQLException;

public interface BookLister {
    /**
     * Get a list of every book in the database, and its ID.
     * 
     * @return A list-like object of identified books in the database.
     */
    public List<Book> getBooks() throws SQLException;

    /**
     * Get a list of every book in the database matching a filter.
     *
     * @return A filtered list-like object of identified books in the database.
     */
    public List<Book> getBooks(ListerFilter filter) throws SQLException;
}
