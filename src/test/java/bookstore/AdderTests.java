package org.dgkrajnik.bookstore;

import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

class AdderTests {
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
    void basicAddTest() {
        try {
            Connection c = cm.getConnection();
            Book b = new Book("Mornington", LocalDate.of(2017, 8, 8), new BigDecimal("124.80"), "S. Herrington");
            DefaultBookAdder adder = new DefaultBookAdder(cm);
            Book outBook = adder.addBook(b);
            Statement checkStatement = c.createStatement();
            ResultSet authors = checkStatement.executeQuery("SELECT * FROM authors");
            ResultSet books = checkStatement.executeQuery("SELECT * FROM books");
            authors.next();
            books.next();
            assertEquals(authors.getString(2), "S. Herrington");
            assertEquals(books.getString(2), "Mornington");
            assertEquals(books.getDate(3), Date.valueOf(LocalDate.of(2017, 8, 8)));
            assertEquals(books.getBigDecimal(4), new BigDecimal("124.8000"));
            assertTrue(outBook.getID() != null);
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
}
