package org.dgkrajnik.bookstore;

import java.util.List;
import java.sql.SQLException;

public interface BookTagger {
    /**
     * Adds a tag to a given book.
     * Both the book and the tag must already be in the database.
     * 
     * @param bookID ID of the book to add the tag to. Tags cannot be added to books not in the database.
     * @param tag Tag to add to the given book.
     */
    public void addTag(int bookID, Tag tag) throws SQLException;

    /**
     * Removes a tag from the given book
     * 
     * @param bookID ID of the book to remove the tag from. Tags cannot be removed from books not in the database.
     * @param tag Tag to remove from the given book.
     */
    public void removeTag(int bookID, Tag tag) throws SQLException;

    /**
     * Creates a new tag in the database (or returns an existing tag if it already exists).
     * 
     * @param name Name of the tag to add.
     * @return An identified Tag object with the name given. If the tag already
     *         (case-insensitively) exists, this will return the existing tag instead.
     */
    public Tag createTag(String name) throws SQLException;

    /**
     * Renames an existing tag.
     *
     * @param tag The tag to rename.
     * @param newName The new name of the tag.
     * @return The new (renamed) tag, or the old tag if renaming was unsucessful.
     */
    public Tag renameTag(Tag tag, String newName) throws SQLException;
}
