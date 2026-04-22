package repositories;

import database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Optional;
import models.User;

public class UserRepository {
    private static final String CREATE_USERS_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS users (
            id UUID PRIMARY KEY,
            full_name VARCHAR(120),
            email VARCHAR(320) UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            is_active BOOLEAN DEFAULT TRUE,
            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
        )
        """;

    private static final String ADD_IS_ACTIVE_SQL = "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE";
    private static final String ADD_FULL_NAME_SQL = "ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(120)";

    private final ConnectionPool connectionPool;

    public UserRepository(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void ensureUsersTable() throws SQLException {
        try {
            executeUpdate(CREATE_USERS_TABLE_SQL, statement -> {
            });
            executeUpdate(ADD_IS_ACTIVE_SQL, statement -> {
            });
            executeUpdate(ADD_FULL_NAME_SQL, statement -> {
            });
        } catch (SQLException ex) {
            throw new SQLException("Database schema initialization failed.", ex);
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        final String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";
        String normalizedEmail = normalizeEmail(email);

        Connection connection = null;
        try {
            connection = connectionPool.acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizedEmail);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Failed to check user existence.", ex);
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }

    public void save(User user) throws SQLException {
        final String sql = "INSERT INTO users (id, full_name, email, password_hash, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            executeUpdate(sql, statement -> {
                statement.setObject(1, user.getId(), Types.OTHER);
                statement.setString(2, user.getFullName());
                statement.setString(3, normalizeEmail(user.getEmail()));
                statement.setString(4, user.getPasswordHash());
                statement.setBoolean(5, user.isActive());
                statement.setTimestamp(6, Timestamp.from(user.getCreatedAt()));
            });
        } catch (SQLException ex) {
            throw new SQLException("Failed to save user.", ex);
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        final String sql = "SELECT id, full_name, email, password_hash, is_active, created_at FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";
        String normalizedEmail = normalizeEmail(email);

        Connection connection = null;
        try {
            connection = connectionPool.acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizedEmail);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    String id = resultSet.getString("id");
                    String fullName = resultSet.getString("full_name");
                    String storedEmail = resultSet.getString("email");
                    String passwordHash = resultSet.getString("password_hash");
                    boolean isActive = resultSet.getBoolean("is_active");
                    Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
                    Instant createdAt = createdAtTimestamp != null ? createdAtTimestamp.toInstant() : Instant.now();

                    return Optional.of(new User(id, fullName, storedEmail, passwordHash, isActive, createdAt));
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Failed to load user by email.", ex);
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }

    private void executeUpdate(String sql, StatementConfigurer configurer) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionPool.acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                configurer.configure(statement);
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new SQLException("Failed to execute database update.", ex);
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    @FunctionalInterface
    private interface StatementConfigurer {
        void configure(PreparedStatement statement) throws SQLException;
    }
}
