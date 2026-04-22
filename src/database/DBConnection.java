package database;

import config.DBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private DBConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("PostgreSQL JDBC driver not found. Add driver to classpath.", ex);
        }
    }

    public static DBConnection getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final DBConnection INSTANCE = new DBConnection();

        private Holder() {
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(
                DBConfig.getDbUrl(),
                DBConfig.getUsername(),
                DBConfig.getPassword()
            );
        } catch (SQLException ex) {
            throw new SQLException("Failed to create JDBC connection.", ex);
        }
    }
}
