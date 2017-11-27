package org.dgkrajnik.bookstore;

import java.sql.SQLException;

public interface BookDeleter {
    /**
     * Takes a book ID and deletes it from the database.
     * This function will *not* delete the Author of a book from the database, or
     * any tags attached to this book ID.
     *
     * @param bookID The ID field of a book.
     */
    public void deleteBook(int bookID) throws SQLException;
}
