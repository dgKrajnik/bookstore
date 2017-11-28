package org.dgkrajnik.bookstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDriver;

class ConnectionManager {
    private Connection sqldb = null;
    private String sqlURL;

    public ConnectionManager(String sqlURL) {
        this.sqlURL = sqlURL;
    }

    protected Connection getConnection() {
        try {
            if (sqldb == null || sqldb.isClosed()) {
                initDB();
            }
            return sqldb;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void closeConnection() {
        try {
            if (sqldb != null && !sqldb.isClosed()) {
                    sqldb.close();
                    sqldb = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDB() throws SQLException {
        sqldb = DriverManager.getConnection(this.sqlURL, "SA", "");
        Statement initStatement = sqldb.createStatement();
        initStatement.execute(DBConsts.SCHEMA);
        initStatement.close();
    }
}
