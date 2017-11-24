import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

class HyperUnitTests {
    private static Connection sqldb;
    @BeforeAll
    static void init() {
        try {
            HyperUnitTests.sqldb = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
        } catch (SQLException e) {
            fail(String.format("Uh oh: %s", e.getMessage()));
        }
    }

    @Test
    void basicTest() {
        assertEquals(1 + 2, 3);
    }
}
