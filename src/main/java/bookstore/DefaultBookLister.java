package org.dgkrajnik.bookstore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import java.time.LocalDate;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import org.hsqldb.jdbc.JDBCDriver;

public class DefaultBookLister implements BookLister {
    private ConnectionManager cm;

    public DefaultBookLister(ConnectionManager cm) {
        this.cm = cm;
    };

    public List<Book> getBooks() throws SQLException {
        ArrayList<Book> bookList = null;
        Connection c = cm.getConnection();
        Statement listerStatement = c.createStatement();
        try {
            ResultSet books = listerStatement.executeQuery(
                "SELECT * FROM books "
              + " LEFT OUTER JOIN authors "
              + "  ON books.author_id = authors.id"
            );
            bookList = new ArrayList<Book>();
            while (books.next()) {
                Book book = new Book(books.getInt(1), books.getString(2), books.getDate(3).toLocalDate(),
                                     books.getBigDecimal(4), books.getString(8));
                bookList.add(book);
            }
        } finally {
            listerStatement.close();
        }
        return bookList;
    }

    public List<Book> getBooks(ListerFilter filter) throws SQLException {
        // It is possible to do this entirely in the SQL layer but there
        // are some issues with making it work in a performant fashion.
        // This function would be a good candidate for reimplementation after
        // performance testing, should building and sending the SQL be slower
        // than HSQLDB figuring out what to do with NULL parameters.
        StringBuilder sb = new StringBuilder("SELECT * FROM books ");
        sb.append(" LEFT OUTER JOIN authors ");
        sb.append("  ON books.author_id = authors.id ");
        // Could replace this in Java 8 with a StringJoiner.
        boolean needsAnd = false;
        ArrayList<Object> parameters = new ArrayList<Object>();
        ArrayList<Integer> parameterTypes = new ArrayList<Integer>();
        if (filter.nameLike != null) {
            if (!needsAnd) {sb.append("WHERE ");}
            if (needsAnd) {sb.append("AND ");}
            sb.append("books.name LIKE ? ");
            parameters.add("%" + filter.nameLike + "%");
            parameterTypes.add(java.sql.Types.VARCHAR);
            needsAnd = true;
        }
        if (filter.dateMin != null) {
            if (!needsAnd) {sb.append("WHERE ");}
            if (needsAnd) {sb.append("AND ");}
            sb.append("books.publish_date >= ? ");
            parameters.add(Date.valueOf(filter.dateMin));
            parameterTypes.add(java.sql.Types.DATE);
            needsAnd = true;
        }
        if (filter.dateMax != null) {
            if (!needsAnd) {sb.append("WHERE ");}
            if (needsAnd) {sb.append("AND ");}
            sb.append("books.publish_date <= ? ");
            parameters.add(Date.valueOf(filter.dateMax));
            parameterTypes.add(java.sql.Types.DATE);
            needsAnd = true;
        }
        if (filter.priceMin != null) {
            if (!needsAnd) {sb.append("WHERE ");}
            if (needsAnd) {sb.append("AND ");}
            sb.append("books.price >= ? ");
            parameters.add(filter.priceMin);
            parameterTypes.add(java.sql.Types.DECIMAL);
            needsAnd = true;
        }
        if (filter.priceMax != null) {
            if (!needsAnd) {sb.append("WHERE ");}
            if (needsAnd) {sb.append("AND ");}
            sb.append("books.price <= ? ");
            parameters.add(filter.priceMax);
            parameterTypes.add(java.sql.Types.DECIMAL);
            needsAnd = true;
        }
        if (filter.authorLike != null) {
            if (!needsAnd) {sb.append("WHERE ");}
            if (needsAnd) {sb.append("AND ");}
            sb.append("authors.name LIKE ? ");
            parameters.add("%" + filter.authorLike + "%");
            parameterTypes.add(java.sql.Types.VARCHAR);
            needsAnd = true;
        }

        final ArrayList<Book> bookList = new ArrayList<Book>();
        Connection c = cm.getConnection();
        PreparedStatement filteredListerStatement = c.prepareStatement(sb.toString());
        try {
            for (int i = 0; i < parameters.size(); i++) {
                filteredListerStatement.setObject(i+1, parameters.get(i), parameterTypes.get(i));
            }
            ResultSet books = filteredListerStatement.executeQuery();
            while (books.next()) {
                Book book = new Book(books.getInt(1), books.getString(2), books.getDate(3).toLocalDate(),
                                     books.getBigDecimal(4), books.getString(8));
                bookList.add(book);
            }
        } finally {
            filteredListerStatement.close();
        }

        // tagState serves as an ad-hoc enum list;
        // -1 for "matches no tag".
        //  0 for "matches any tags", 
        //  1 for "matches all tags",
        ArrayList<Integer> tagState = new ArrayList<Integer>();
        if ((filter.anyTag != null && filter.anyTag.size() > 0) ||
            (filter.allTag != null && filter.allTag.size() > 0)) {
            List<String> filterTags;
            if (filter.anyTag != null) {
                filterTags = filter.anyTag;
            } else {
                filterTags = filter.allTag;
            }
            // Build a grouped, counting SQL statement that only matches
            // the books we've filtered out and the tags we want to find,
            // to count how many matching tags are associated to books.
            StringBuilder tagCountStatement = new StringBuilder(
                "SELECT books.id, COUNT(tags.id) FROM book_tags "
              + " INNER JOIN books ON (books.id = book_tags.book_id) "
              + " INNER JOIN tags ON (tags.id = book_tags.tag_id) "
              + "WHERE ("
            );
            boolean needsTagOr = false;
            for (Book b : bookList) {
                if (needsTagOr) {tagCountStatement.append("OR ");}
                tagCountStatement.append("books.id = ? ");
                needsTagOr = true;
            }
            tagCountStatement.append(") AND (");
            needsTagOr = false;
            for (String filterTag : filterTags) {
                if (needsTagOr) {tagCountStatement.append("OR ");}
                tagCountStatement.append("tags.tag = ? ");
                needsTagOr = true;
            }
            tagCountStatement.append(") ");
            tagCountStatement.append("GROUP BY books.id;");

            PreparedStatement tagCheckStatement = c.prepareStatement(tagCountStatement.toString());
            try {
                int bookPos;
                for (bookPos = 0; bookPos < bookList.size(); bookPos++) {
                    tagCheckStatement.setInt(bookPos+1, bookList.get(bookPos).getID());
                    tagState.add(-1);
                }
                for (int tagPos = 0; tagPos < filterTags.size(); tagPos++) {
                    tagCheckStatement.setString(bookPos+tagPos+1, filterTags.get(tagPos));
                }

                // Check the counts against the number of tags we've asked for,
                // and set the appropriate flag in the tagState list. Each tagState
                // index maps to the equivalent bookList index.
                ResultSet tagCheckResults = tagCheckStatement.executeQuery();
                while (tagCheckResults.next()) {
                    int bookCountID = tagCheckResults.getInt(1);
                    int bookCount = tagCheckResults.getInt(2);
                    int bookListPos = IntStream.range(0, bookList.size())
                                        .filter(i -> bookList.get(i).getID().equals(bookCountID))
                                        .findFirst()
                                        .orElse(-1);
                    if (bookCount == filterTags.size()) {
                        tagState.set(bookListPos, 1);
                    } else if (bookCount > 0) {
                        tagState.set(bookListPos, 0);
                    }
                }
            } finally {
                tagCheckStatement.close();
            }
        }

        ArrayList<Book> filteredBooks = null;
        if (filter.anyTag != null && filter.anyTag.size() > 0) {
            filteredBooks = new ArrayList<Book>();
            for (int i = 0; i < bookList.size(); i++) {
                if (tagState.get(i) >= 0) {
                    filteredBooks.add(bookList.get(i));
                }
            }
        } else if (filter.allTag != null && filter.allTag.size() > 0) {
            filteredBooks = new ArrayList<Book>();
            for (int i = 0; i < bookList.size(); i++) {
                if (tagState.get(i) == 1) {
                    filteredBooks.add(bookList.get(i));
                }
            }
        } else {
            filteredBooks = bookList;
        }

        return filteredBooks;
    }
}
