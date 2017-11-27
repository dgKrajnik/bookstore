package org.dgkrajnik.bookstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDriver;
import org.hsqldb.error.ErrorCode;

public class DefaultBookTagger implements BookTagger {
    public DefaultBookTagger() {};

    public void addTag(int bookID, Tag tag) throws SQLException {
        Connection c = ConnectionManager.getConnection();
        PreparedStatement tagAddStatement = c.prepareStatement(
            "INSERT INTO book_tags VALUES (?, ?)"
        );
        try {
            tagAddStatement.setInt(1, bookID);
            tagAddStatement.setInt(2, tag.getID());
            tagAddStatement.executeUpdate();
        } finally {
            tagAddStatement.close();
        }
    }

    public void removeTag(int bookID, Tag tag) throws SQLException {
        Connection c = ConnectionManager.getConnection();
        PreparedStatement removeStatement = c.prepareStatement(
            "DELETE FROM book_tags WHERE book_id = ? AND tag_id = ?;"
        );
        try {
            removeStatement.setInt(1, bookID);
            removeStatement.setInt(2, tag.getID());
            removeStatement.executeUpdate();
        } finally {
            removeStatement.close();
        }
    }

    public Tag createTag(String name) throws SQLException {
        Connection c = ConnectionManager.getConnection();
        PreparedStatement tagAddStatement = c.prepareStatement(
            "MERGE INTO tags AS target "
          + "USING (VALUES(?)) AS source(tag) "
          + " ON (target.tag = source.tag) "
          + "WHEN NOT MATCHED THEN "
          + " INSERT (tag) VALUES (source.tag);",
            Statement.RETURN_GENERATED_KEYS
        );
        PreparedStatement tagGetStatement = null;
        
        Tag newTag = null;
        try {
            tagAddStatement.setString(1, name);
            tagAddStatement.executeUpdate();
            ResultSet tagIns = tagAddStatement.getGeneratedKeys();
            if (!tagIns.next()) {
                tagGetStatement = c.prepareStatement(
                    "SELECT id FROM tags WHERE tag = ?"
                );
                tagGetStatement.setString(1, name);
                tagIns = tagGetStatement.executeQuery();
                tagIns.next();
            }
            int newTagID = tagIns.getInt(1);
            newTag = new Tag(newTagID, name);
        } finally {
            tagAddStatement.close();
            if (tagGetStatement != null) {
                tagGetStatement.close();
            }
        }

        return newTag;
    }

    public Tag renameTag(Tag tag, String newName) throws SQLException {
        Connection c = ConnectionManager.getConnection();
        PreparedStatement tagUpdateStatement = c.prepareStatement(
            "MERGE INTO tags AS target "
          + "USING (VALUES(?, ?)) AS source(id, tag) "
          + " ON (target.id = source.id) "
          + "WHEN MATCHED THEN "
          + " UPDATE SET target.tag = source.tag;"
        );
        Tag newTag = null;
        try {
            tagUpdateStatement.setInt(1, tag.getID());
            tagUpdateStatement.setString(2, newName);
            int tagUpdCount = tagUpdateStatement.executeUpdate();
            if (tagUpdCount < 1) {
                newTag = tag;
            } else if (tagUpdCount == 1) {
                newTag = new Tag(tag.getID(), newName);
            } else {
                throw new RuntimeException("Non-unique ID in tags table.");
            }
        } finally {
            tagUpdateStatement.close();
        }

        return newTag;
    }
}
