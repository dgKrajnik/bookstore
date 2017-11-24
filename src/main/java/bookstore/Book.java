package org.dgkrajnik.bookstore;

import java.util.ArrayList;

import java.time.LocalDate;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Blob;

public class Book {
    private Integer id; // Nullable, if not in the DB.
    private String name;
    private LocalDate publishDate;
    private BigDecimal price;
    private String author;

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

    public byte[] getData() {
        if (id == null) {
            return null;
        }
        Connection c = ConnectionManager.getConnection();
        return null;
    }
    public void setData(byte[] data) {
        if (id == null) {
            throw new UnsupportedOperationException("Cannot set data for a book not currently in the database.");
        }
        Connection c = ConnectionManager.getConnection();
    }

    public ArrayList<String> getTags() {
        if (id == null) {
            return null;
        }
        Connection c = ConnectionManager.getConnection();
        return null;
    }
    public ArrayList<Integer> getTagIDs() {
        if (id == null) {
            return null;
        }
        Connection c = ConnectionManager.getConnection();
        return null;
    }
    public void setTags(ArrayList<Integer> tagIDs) {
        if (id == null) {
            throw new UnsupportedOperationException("Cannot set tags for a book not currently in the database.");
        }
        Connection c = ConnectionManager.getConnection();
    }
}
