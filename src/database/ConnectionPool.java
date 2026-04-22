package database;

import config.DBConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionPool {
    private final BlockingQueue<Connection> pool;

    private ConnectionPool() {
        int poolSize = DBConfig.getPoolSize();
        this.pool = new ArrayBlockingQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            try {
                pool.offer(DBConnection.getInstance().getConnection());
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to initialize JDBC connection pool.", ex);
            }
        }
    }

    public static ConnectionPool getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final ConnectionPool INSTANCE = new ConnectionPool();

        private Holder() {
        }
    }

    public Connection acquireConnection() throws SQLException {
        Connection connection = pool.poll();
        if (connection == null) {
            return DBConnection.getInstance().getConnection();
        }

        try {
            if (connection.isClosed() || !connection.isValid(2)) {
                connection.close();
                return DBConnection.getInstance().getConnection();
            }
        } catch (SQLException ex) {
            try {
                connection.close();
            } catch (SQLException closeEx) {
                System.err.println("[ERROR] Failed to close invalid JDBC connection: " + closeEx.getMessage());
            }
            return DBConnection.getInstance().getConnection();
        }

        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (connection.isClosed()) {
                return;
            }

            if (!pool.offer(connection)) {
                connection.close();
            }
        } catch (SQLException ex) {
            System.err.println("[ERROR] Failed while releasing JDBC connection: " + ex.getMessage());
        }
    }

    public void shutdown() {
        Connection connection;
        while ((connection = pool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                System.err.println("[ERROR] Error closing pooled connection: " + ex.getMessage());
            }
        }
    }
}
