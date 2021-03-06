package org.dgkrajnik.bookstore;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Blob;
import java.sql.SQLException;

public class Book {
    protected Integer id; // Nullable, if not in the DB.
    protected String name;
    protected LocalDate publishDate;
    protected BigDecimal price;
    protected String author;

    protected Book(Integer id, String name, LocalDate publishDate,
                   BigDecimal price, String author) {
        this.id = id;
        this.name = name;
        this.publishDate = publishDate;
        this.price = price;
        this.author = author;
    }

    public Book(String name, LocalDate publishDate, BigDecimal price, String author) {
        this(null, name, publishDate, price, author);
    }

    public Integer getID() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public LocalDate getDate() {
        return this.publishDate;
    }
    public BigDecimal getPrice() {
        return this.price;
    }
    public String getAuthor() {
        return this.author;
    }

    public byte[] getData(ConnectionManager cm) throws SQLException {
        if (id == null) {
            return null;
        }
        byte[] dataBytes = null;
        Connection c = cm.getConnection();
        PreparedStatement dataStatement = c.prepareStatement(
                "SELECT book_data FROM books WHERE id = ?",
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        try {
            dataStatement.setInt(1, this.id);
            ResultSet dataResults = dataStatement.executeQuery();
            dataResults.first();
            Blob dataBlob = dataResults.getBlob(1);
            // This conversion is safe because we know that the size of the blob is
            // less than 2.14 gigabytes, so the length will fit in an int.
            dataBytes = dataBlob.getBytes(1, (int) dataBlob.length());
        } finally {
            dataStatement.close();
        }
        return dataBytes;
    }
    protected void setData(ConnectionManager cm, byte[] data) throws SQLException {
        if (id == null) {
            throw new UnsupportedOperationException("Cannot set data for a book not currently in the database.");
        }
        Connection c = cm.getConnection();
        Blob dataBlob = c.createBlob();
        dataBlob.setBytes(1, data);
        PreparedStatement dataStatement = c.prepareStatement("UPDATE books SET book_data = ? WHERE id = ?");
        try {
            dataStatement.setBlob(1, dataBlob);
            dataStatement.setInt(2, this.id);
            dataStatement.executeUpdate();
        } finally {
            dataStatement.close();
        }
    }

    public List<Tag> getTags(ConnectionManager cm) throws SQLException {
        if (id == null) {
            return null;
        }
        Connection c = cm.getConnection();
        PreparedStatement tagStatement = c.prepareStatement(
            "SELECT tags.id, tags.tag FROM book_tags "
          + " INNER JOIN books ON books.id = book_tags.book_id "
          + " INNER JOIN tags ON tags.id = book_tags.tag_id "
          + "WHERE books.id = ?"
        );
        List<Tag> tags = new ArrayList<Tag>();
        try {
            tagStatement.setInt(1, this.id);
            ResultSet tagResults = tagStatement.executeQuery();
            while (tagResults.next()) {
                Tag tag = new Tag(tagResults.getInt(1), tagResults.getString(2));
                tags.add(tag);
            }
        } finally {
            tagStatement.close();
        }
        return tags;
    }
}
