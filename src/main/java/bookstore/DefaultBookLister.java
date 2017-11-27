package org.dgkrajnik.bookstore;

import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDriver;

public class DefaultBookLister implements BookLister {
    public DefaultBookLister() {};

    public List<Book> getBooks() throws SQLException {
        ArrayList<Book> bookList = null;
        Connection c = ConnectionManager.getConnection();
        Statement listerStatement = c.createStatement();
        try {
            ResultSet books = listerStatement.executeQuery(
                "SELECT * FROM books "
              + " LEFT OUTER JOIN authors "
              + "  ON books.author_id = authors.id"
            );
            bookList = new ArrayList<Book>();
            while (books.next()) {
                Book book = new Book(books.getInt(1), books.getString(2), books.getDate(3).toLocalDate(),
                                     books.getBigDecimal(4), books.getString(8));
                bookList.add(book);
            }
        } finally {
            listerStatement.close();
        }
        return bookList;
    }
    public List<Book> getBooks(ListerFilter filter) {
        throw new UnsupportedOperationException("Filtered book getting is not yet implemented.");
    }
}
