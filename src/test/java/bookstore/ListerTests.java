package org.dgkrajnik.bookstore;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Test
    void filterNameTest() {
        try {
            prefillDB(cm);
            DefaultBookLister lister = new DefaultBookLister(cm);

            ListerFilter morningFilter = new ListerFilter();
            morningFilter.filterName("Morning");
            List<Book> results = lister.getBooks(morningFilter);
            assertEquals(results.size(), 3);
            assertEquals(results.get(0).getName(), "Mornington");
            assertEquals(results.get(1).getName(), "Morning Sunrise?");
            assertEquals(results.get(2).getName(), "Morning Sunrise!");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
    @Test
    void filterAuthorTest() {
        try {
            prefillDB(cm);
            DefaultBookLister lister = new DefaultBookLister(cm);

            ListerFilter authorFilter = new ListerFilter();
            authorFilter.filterAuthor("Veritas");
            List<Book> results = lister.getBooks(authorFilter);
            assertEquals(results.size(), 1);
            assertEquals(results.get(0).getName(), "Evenington");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
    @Test
    void filterDateTest() {
        try {
            prefillDB(cm);
            DefaultBookLister lister = new DefaultBookLister(cm);

            ListerFilter dateFilter = new ListerFilter();
            dateFilter.filterDateAfter(LocalDate.of(2017, 8, 8));
            dateFilter.filterDateBefore(LocalDate.of(2017, 9, 7));
            List<Book> results = lister.getBooks(dateFilter);
            assertEquals(results.size(), 1);
            assertEquals(results.get(0).getName(), "Mornington");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
    @Test
    void filterPriceTest() {
        try {
            prefillDB(cm);
            DefaultBookLister lister = new DefaultBookLister(cm);

            ListerFilter priceFilter = new ListerFilter();
            priceFilter.filterPriceAbove(new BigDecimal("90.0"));
            priceFilter.filterPriceBelow(new BigDecimal("124.70"));
            List<Book> results = lister.getBooks(priceFilter);
            assertEquals(results.size(), 1);
            assertEquals(results.get(0).getName(), "Evenington");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
    @Test
    void filterAnyTagTest() {
        try {
            prefillDB(cm);
            DefaultBookLister lister = new DefaultBookLister(cm);

            ListerFilter anyTagFilter = new ListerFilter();
            List<String> anyTags = Arrays.asList("history", "science");
            anyTagFilter.filterMatchAnyTag(anyTags);
            List<Book> results = lister.getBooks(anyTagFilter);
            assertEquals(results.size(), 2);
            assertEquals(results.get(0).getName(), "Mornington");
            assertEquals(results.get(1).getName(), "Morning Sunrise!");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
    @Test
    void filterAllTagTest() {
        try {
            prefillDB(cm);
            DefaultBookLister lister = new DefaultBookLister(cm);

            ListerFilter allTagFilter = new ListerFilter();
            List<String> allTags = Arrays.asList("history", "non-fiction");
            allTagFilter.filterMatchAllTags(allTags);
            List<Book> results = lister.getBooks(allTagFilter);
            assertEquals(results.size(), 1);
            assertEquals(results.get(0).getName(), "Mornington");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
    @Test
    void filterMultiTest() {
        try {
            prefillDB(cm);
            DefaultBookLister lister = new DefaultBookLister(cm);

            ListerFilter bigFilter = new ListerFilter();
            List<String> bigAnyTags = Arrays.asList("history", "science", "in this query");
            bigFilter.filterMatchAnyTag(bigAnyTags);
            bigFilter.filterPriceAbove(new BigDecimal("40.50"));
            bigFilter.filterPriceBelow(new BigDecimal("9999.0"));
            bigFilter.filterDateAfter(LocalDate.of(2017, 8, 8));
            bigFilter.filterDateBefore(LocalDate.of(2017, 12, 1));
            bigFilter.filterName("ing");
            List<Book> results = lister.getBooks(bigFilter);
            assertEquals(results.size(), 4);
            assertEquals(results.get(0).getName(), "Mornington");
            assertEquals(results.get(1).getName(), "Evenington");
            assertEquals(results.get(2).getName(), "Morning Sunrise?");
            assertEquals(results.get(3).getName(), "Morning Sunrise!");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }

    private void prefillDB(ConnectionManager cm) throws SQLException {
        Connection c = cm.getConnection();
        Statement prefillStatement = c.createStatement();
        prefillStatement.executeUpdate("INSERT INTO authors (name) VALUES ('S. Herrington')");
        prefillStatement.executeUpdate("INSERT INTO authors (name) VALUES ('Veritas Vernon')");
        prefillStatement.executeUpdate("INSERT INTO authors (name) VALUES ('Mr. Author')");
        prefillStatement.executeUpdate("INSERT INTO authors (name) VALUES ('S. Missing')");

        prefillStatement.executeUpdate("INSERT INTO tags (tag) VALUES ('fiction')");
        prefillStatement.executeUpdate("INSERT INTO tags (tag) VALUES ('non-fiction')");
        prefillStatement.executeUpdate("INSERT INTO tags (tag) VALUES ('science')");
        prefillStatement.executeUpdate("INSERT INTO tags (tag) VALUES ('history')");
        prefillStatement.executeUpdate("INSERT INTO tags (tag) VALUES ('in this query')");
        prefillStatement.executeUpdate("INSERT INTO tags (tag) VALUES ('not in this query')");

        prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                     + "VALUES ('Mornington', '2017-08-08', 124.80, 0)");
        prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                     + "VALUES ('Evenington', '2017-09-08', 90.0, 1)");
        prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                     + "VALUES ('Morning Sunrise?', '2017-10-08', 300.80, 2)");
        prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                     + "VALUES ('Morning Sunrise!', '2017-11-08', 80.0, 2)");
        prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                     + "VALUES ('Not Appearing In This Query', '2017-07-08', 80.0, 0)");
        prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                     + "VALUES ('Too Not Appearing In This Query', '2017-09-08', 9999.0, 0)");
        prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                     + "VALUES ('Not Appearing In This Query?', '2017-09-08', 9999.0, 3)");
        prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                     + "VALUES ('Not Appearing In This Query!', '2017-09-08', 40.0, 3)");

        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (0, 1)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (0, 3)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (3, 0)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (3, 3)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (1, 4)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (2, 4)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (4, 5)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (5, 1)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (6, 1)");
        prefillStatement.executeUpdate("INSERT INTO book_tags (book_id, tag_id) VALUES (7, 1)");
        prefillStatement.close();
    }
}
