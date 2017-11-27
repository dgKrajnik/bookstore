package org.dgkrajnik.bookstore;

public interface BookAdder {
    /**
     * Takes an ID-less Book object and adds it to the database, then returns
     * the equivalent Book object with the new ID.
     *
     * @param book A null-ID book object with the desired information.
     * @return An identified book object with the same information as the input.
     *         If some error occurred, the new book will be equal to the old one.
     */
    public Book addBook(Book book);
}
