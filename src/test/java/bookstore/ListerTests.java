package org.dgkrajnik.bookstore;

import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

class ListerTests {
    private static ConnectionManager cm;
    @BeforeAll
    static void makeCM() {
        cm = new ConnectionManager("jdbc:hsqldb:mem:testdb");
    }
    @AfterAll
    static void closeCM() {
        cm.closeConnection();
    }

    @BeforeEach
    void clearDB() {
        try {
            Connection c = cm.getConnection();
            c.createStatement().execute("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }

    @Test
    void basicListTest() {
        try {
            Connection c = cm.getConnection();
            Statement prefillStatement = c.createStatement();
            prefillStatement.executeUpdate("INSERT INTO authors (name) VALUES ('S. Herrington')");
            prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                         + "VALUES ('Mornington', '2017-08-08', 124.80, 0)");
            prefillStatement.close();

            DefaultBookLister lister = new DefaultBookLister(cm);
            List<Book> books = lister.getBooks();
            assertEquals(books.get(0).getName(), "Mornington");
            assertEquals(books.get(0).getDate(), LocalDate.of(2017, 8, 8));
            assertEquals(books.get(0).getPrice(), new BigDecimal("124.8000"));
            assertEquals(books.get(0).getAuthor(), "S. Herrington");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
}
