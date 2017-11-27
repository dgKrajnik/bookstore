package org.dgkrajnik.bookstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDriver;

public class DefaultBookDeleter implements BookDeleter {
    public DefaultBookDeleter() {};

    public void deleteBook(int bookID) throws SQLException {
        Connection c = ConnectionManager.getConnection();
        PreparedStatement deletionStatement = c.prepareStatement("DELETE FROM books WHERE books.id = ?");
        PreparedStatement tagDeletionStatement = c.prepareStatement("DELETE FROM book_tags WHERE book_id = ?");
        try {
            deletionStatement.setInt(1, bookID);
            tagDeletionStatement.setInt(1, bookID);
            deletionStatement.executeUpdate();
            tagDeletionStatement.executeUpdate();
        } finally {
            deletionStatement.close();
            tagDeletionStatement.close();
        }
    }
}
