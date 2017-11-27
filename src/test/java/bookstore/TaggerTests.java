package org.dgkrajnik.bookstore;

import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

class TaggerTests {
    @BeforeEach
    void clearDB() {
        try {
            Connection c = ConnectionManager.getConnection();
            c.createStatement().execute("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK");
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }

    @Test
    void basicTagCreateTest() {
        try {
            Connection c = ConnectionManager.getConnection();
            DefaultBookTagger tagger = new DefaultBookTagger();
            Tag ttag = tagger.createTag("testTag");
            Statement checkStatement = c.createStatement();
            ResultSet tags = checkStatement.executeQuery("SELECT * FROM tags");
            tags.next();
            assertEquals(tags.getString(2), "testTag");
            ttag = tagger.createTag("testTag");
            assertEquals(tags.getInt(1), ttag.getID());
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }

    @Test
    void basicTagRenameTest() {
        try {
            Connection c = ConnectionManager.getConnection();
            Statement prefillStatement = c.createStatement();
            prefillStatement.executeUpdate("INSERT INTO tags (tag) VALUES ('testTag')");
            Tag newTag = new Tag(0, "testTag");
            prefillStatement.close();

            DefaultBookTagger tagger = new DefaultBookTagger();
            Tag renamedTag = tagger.renameTag(newTag, "heyItWorked");
            Statement checkStatement = c.createStatement();
            ResultSet tags = checkStatement.executeQuery("SELECT * FROM tags");
            tags.next();
            assertEquals(tags.getString(2), "heyItWorked");
            assertEquals(tags.getString(2), renamedTag.getName());
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }

    @Test
    void basicTagAddTest() {
        try {
            Connection c = ConnectionManager.getConnection();
            Statement prefillStatement = c.createStatement();
            prefillStatement.executeUpdate("INSERT INTO authors (name) VALUES ('S. Herrington')");
            prefillStatement.executeUpdate("INSERT INTO books (name, publish_date, price, author_id) "
                                         + "VALUES ('Mornington', '2017-08-08', 124.80, 0)");
            prefillStatement.executeUpdate("INSERT INTO tags (tag) VALUES ('testTag')");
            prefillStatement.close();

            DefaultBookTagger tagger = new DefaultBookTagger();
            Tag newTag = new Tag(0, "testTag");
            tagger.addTag(0, newTag);

            Statement checkStatement = c.createStatement();
            ResultSet checkResults = checkStatement.executeQuery(
                "SELECT tags.id, tags.tag FROM book_tags "
              + " INNER JOIN books ON books.id = book_tags.book_id "
              + " INNER JOIN tags ON tags.id = book_tags.tag_id "
              + "WHERE books.id = 0"
            );
            checkResults.next();
            assertEquals(checkResults.getString(2), "testTag");
            assertTrue(!checkResults.next());
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }
    }
}
