package org.dgkrajnik.bookstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDriver;

public class DefaultBookAdder {
    public DefaultBookAdder() {};

    public Book addBook(Book book) throws SQLException {
        Connection c = ConnectionManager.getConnection();
        PreparedStatement authorAddStatement = c.prepareStatement("MERGE INTO authors AS target "
                                                                + "USING (VALUES(?)) AS source(name) "
                                                                + " ON (LOWER(target.name) = LOWER(source.name)) "
                                                                + "WHEN NOT MATCHED THEN "
                                                                + " INSERT (name) VALUES (source.name);");
        PreparedStatement authorGetStatement = c.prepareStatement("SELECT id FROM authors "
                                                                + "WHERE (LOWER(authors.name) = LOWER(?));",
                                                                ResultSet.TYPE_SCROLL_SENSITIVE,
                                                                ResultSet.CONCUR_READ_ONLY);
        PreparedStatement adderStatement = c.prepareStatement("INSERT INTO books (name, publish_date, "
                                                            + "                   price, author_id) "
                                                            + "VALUES (?, ?, ?, ?);",
                                                              Statement.RETURN_GENERATED_KEYS);
        Book outBook = book;
        try {
            authorAddStatement.setString(1, book.author);
            authorAddStatement.executeUpdate();

            authorGetStatement.setString(1, book.author);
            ResultSet authorIns = authorGetStatement.executeQuery();
            authorIns.first();
            int authorID = authorIns.getInt(1);

            adderStatement.setString(1, book.name);
            adderStatement.setDate(2, Date.valueOf(book.publishDate));
            adderStatement.setBigDecimal(3, book.price);
            adderStatement.setInt(4, authorID);
            adderStatement.executeUpdate();
            ResultSet bookIns = adderStatement.getGeneratedKeys();
            bookIns.next();
            int bookID = bookIns.getInt(1);
            outBook = new Book(bookID, book.name, book.publishDate, book.price, book.author);
        } finally {
            authorAddStatement.close();
            authorGetStatement.close();
            adderStatement.close();
        }
        return outBook;
    }
}
