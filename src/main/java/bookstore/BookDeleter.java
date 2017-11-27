import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDriver;

public interface BookDeleter {
    public void deleteBook(int bookID);
}
