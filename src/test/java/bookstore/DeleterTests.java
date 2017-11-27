package org.dgkrajnik.bookstore;

import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

class DeleterTests {
    @BeforeAll
    static void clearDB() {
        try {
            Connection c = ConnectionManager.getConnection();
            c.createStatement().execute("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }

    @Test
    void basicDeleteTest() {
        try {
            Connection c = ConnectionManager.getConnection();
            Statement prefillStatement = c.createStatement();
            prefillStatement.executeUpdate("INSERT INTO authors (name) VALUES ('S. Herrington')");
            prefillStatement.executeUpdate(
                "INSERT INTO books (name, publish_date, price, author_id) "
              + "VALUES ('Mornington', '2017-08-08', 124.80, 0)",
                Statement.RETURN_GENERATED_KEYS
            );
            ResultSet insertres = prefillStatement.getGeneratedKeys();
            insertres.next();
            int insertID = insertres.getInt(1);
            prefillStatement.close();

            DefaultBookDeleter deleter = new DefaultBookDeleter();
            deleter.deleteBook(insertID);

            Statement getterStatement = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                                                          ResultSet.CONCUR_READ_ONLY);
            ResultSet books = getterStatement.executeQuery("SELECT * FROM books");
            assertTrue(!books.first());
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
}
