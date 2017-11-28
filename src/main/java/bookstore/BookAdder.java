package org.dgkrajnik.bookstore;

import java.sql.SQLException;

public interface BookAdder {
    /**
     * Takes an ID-less Book object and adds it to the database, then returns
     * the equivalent Book object with the new ID.
     *
     * @param book A null-ID book object with the desired information.
     * @return An identified book object with the same information as the input.
     *         If some error occurred, the new book will be equal to the old one.
     */
    public Book addBook(Book book) throws SQLException;
    /**
     * Takes an ID-less Book object and adds it and some data to the database,
     * then returns the equivalent Book object with the new ID.
     *
     * @param book A null-ID book object with the desired information.
     * @param data A generic byte array to associate with this book.
     * @return An identified book object with the same information as the input.
     *         If some error occurred, the new book will be equal to the old one.
     */
    public Book addBook(Book book, byte[] data) throws SQLException;

    /**
     * Takes a book object and sets its data to the given byte array.
     *
     * @param book An identified book object to change the associated data for..
     * @param data A generic byte array to associate with this book.
     */
    public void changeData(Book book, byte[] data) throws SQLException;
}
