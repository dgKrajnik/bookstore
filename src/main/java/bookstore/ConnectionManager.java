package org.dgkrajnik.bookstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDriver;

class ConnectionManager {
    private static Connection sqldb = null;

    protected static Connection getConnection() {
        try {
            if (sqldb == null || sqldb.isClosed()) {
                sqldb = DriverManager.getConnection("jdbc:hsqldb:mem:bookdb", "SA", "");
                Statement initStatement = sqldb.createStatement();
                initStatement.executeUpdate(DBConsts.SCHEMA);
                initStatement.close();
            }
            return sqldb;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void closeConnection() {
        try {
            if (sqldb != null && !sqldb.isClosed()) {
                    sqldb.close();
                    sqldb = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initDB() {
    }
}
