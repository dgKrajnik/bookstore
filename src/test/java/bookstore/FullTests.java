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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

import org.dgkrajnik.bookstore.ConnectionManager;
import org.dgkrajnik.bookstore.Book;
import org.dgkrajnik.bookstore.Tag;
import org.dgkrajnik.bookstore.DefaultBookAdder;
import org.dgkrajnik.bookstore.BookAdder;
import org.dgkrajnik.bookstore.DefaultBookLister;
import org.dgkrajnik.bookstore.BookLister;
import org.dgkrajnik.bookstore.DefaultBookDeleter;
import org.dgkrajnik.bookstore.BookDeleter;
import org.dgkrajnik.bookstore.DefaultBookTagger;
import org.dgkrajnik.bookstore.BookTagger;

class FullTests {
    private static ConnectionManager cm;
    @BeforeAll
    static void makeCM() {
        // Shutting down an in-memory database deletes it.
        cm = new ConnectionManager("jdbc:hsqldb:mem:fullTestDatabase;shutdown=true");
    }
    @AfterAll
    static void closeCM() {
        cm.closeConnection();
    }

    @BeforeEach
    void clearDB() {
        cm.closeConnection();
        cm = new ConnectionManager("jdbc:hsqldb:mem:fullTestDatabase;shutdown=true");
    }

    @Test
    void addListDeleteTest() throws SQLException {
        Book mornington = new Book("Mornington", LocalDate.of(2017, 8, 8),
                                    new BigDecimal("124.80"), "Q. Harrington");
        Book evenington = new Book("Evenington", LocalDate.of(2017, 9, 8),
                                    new BigDecimal("90.00"), "Vertiav Lernon");
        Book sunriseQue = new Book("Morning Sunrise?", LocalDate.of(2017, 10, 8),
                                    new BigDecimal("300.80"), "Mrs. Author");
        Book sunriseExc = new Book("Morning Sunrise!", LocalDate.of(2017, 11, 9),
                                    new BigDecimal("80.00"), "Mrs. Author");
        Book alsoABook  = new Book("Also a Book", LocalDate.of(1992, 1, 2),
                                    new BigDecimal("90210.00"), "N.O. Missing");

        BookAdder bookAdder = new DefaultBookAdder(cm);
        BookLister bookLister = new DefaultBookLister(cm);
        BookDeleter bookDeleter = new DefaultBookDeleter(cm);

        bookAdder.addBook(mornington);
        bookAdder.addBook(evenington);
        bookAdder.addBook(sunriseQue);
        bookAdder.addBook(sunriseExc);
        bookAdder.addBook(alsoABook );

        List<Book> bookList = bookLister.getBooks();
        assertTrue(bookList.size() == 5);
        assertTrue(bookList.get(0).getID() >= 0);
        assertTrue(bookList.get(1).getID() >= 0);
        assertTrue(bookList.get(2).getID() >= 0);
        assertTrue(bookList.get(3).getID() >= 0);
        assertTrue(bookList.get(4).getID() >= 0);
        assertEquals("Mornington", bookList.get(0).getName());
        assertEquals("Evenington", bookList.get(1).getName());
        assertEquals("Morning Sunrise?", bookList.get(2).getName());
        assertEquals("Morning Sunrise!", bookList.get(3).getName());
        assertEquals("Also a Book", bookList.get(4).getName());
        assertEquals("Q. Harrington", bookList.get(0).getAuthor());

        bookDeleter.deleteBook(bookList.get(2).getID());

        bookList = bookLister.getBooks();
        assertTrue(bookList.size() == 4);
        assertTrue(bookList.get(0).getID() >= 0);
        assertTrue(bookList.get(1).getID() >= 0);
        assertTrue(bookList.get(2).getID() >= 0);
        assertTrue(bookList.get(3).getID() >= 0);
        assertEquals("Mornington", bookList.get(0).getName());
        assertEquals("Evenington", bookList.get(1).getName());
        assertEquals("Morning Sunrise!", bookList.get(2).getName());
        assertEquals("Also a Book", bookList.get(3).getName());
    }

    @Test
    void tagTest() throws SQLException {
        Book mornington = new Book("Mornington", LocalDate.of(2017, 8, 8),
                                    new BigDecimal("124.80"), "Q. Harrington");
        Book evenington = new Book("Evenington", LocalDate.of(2017, 9, 8),
                                    new BigDecimal("90.00"), "Vertiav Lernon");
        Book sunriseQue = new Book("Morning Sunrise?", LocalDate.of(2017, 10, 8),
                                    new BigDecimal("300.80"), "Mrs. Author");
        Book sunriseExc = new Book("Morning Sunrise!", LocalDate.of(2017, 11, 9),
                                    new BigDecimal("80.00"), "Mrs. Author");
        Book alsoABook  = new Book("Also a Book", LocalDate.of(1992, 1, 2),
                                    new BigDecimal("90210.00"), "N.O. Missing");

        BookAdder bookAdder = new DefaultBookAdder(cm);
        BookTagger bookTagger = new DefaultBookTagger(cm);

        mornington = bookAdder.addBook(mornington);
        evenington = bookAdder.addBook(evenington);
        sunriseQue = bookAdder.addBook(sunriseQue);
        sunriseExc = bookAdder.addBook(sunriseExc);
        alsoABook  = bookAdder.addBook(alsoABook );

        Tag tagFict = bookTagger.createTag("fiction");
        Tag tagNFic = bookTagger.createTag("mon-fiction");
        Tag tagScie = bookTagger.createTag("science");
        Tag tagHist = bookTagger.createTag("history");
        Tag tagNInQ = bookTagger.createTag("not in this query");

        bookTagger.addTag(mornington.getID(), tagNFic);
        bookTagger.addTag(mornington.getID(), tagHist);
        bookTagger.addTag(sunriseExc.getID(), tagFict);
        bookTagger.addTag(sunriseExc.getID(), tagHist);
        bookTagger.addTag(sunriseExc.getID(), tagNInQ);
        bookTagger.removeTag(sunriseExc.getID(), tagNInQ);

        bookTagger.renameTag(tagNFic, "non-fiction");

        List<Tag> sunriseTags = sunriseExc.getTags(cm);
        assertEquals(2, sunriseTags.size());
        assertEquals("fiction", sunriseTags.get(0).getName());
        assertEquals("history", sunriseTags.get(1).getName());

        List<Tag> morningtonTags = mornington.getTags(cm);
        assertEquals(2, morningtonTags.size());
        assertEquals("non-fiction", morningtonTags.get(0).getName());
        assertEquals("history", morningtonTags.get(1).getName());
    }

    @Test
    void dataTest() throws SQLException {
        Book mornington = new Book("Mornington", LocalDate.of(2017, 8, 8),
                                    new BigDecimal("124.80"), "Q. Harrington");

        BookAdder bookAdder = new DefaultBookAdder(cm);

        mornington = bookAdder.addBook(mornington, new byte[]{0, 5, 2, 4});
        byte[] dataOut = mornington.getData(cm);
        assertEquals(0, dataOut[0]);
        assertEquals(5, dataOut[1]);
        assertEquals(2, dataOut[2]);
        assertEquals(4, dataOut[3]);
    }
}
