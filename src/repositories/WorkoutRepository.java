package repositories;

import database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import models.Workout;

public class WorkoutRepository {
    private static final String CREATE_WORKOUTS_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS workouts (
            id UUID PRIMARY KEY,
            user_id UUID NOT NULL,
            name VARCHAR(200) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW()
        )
        """;

    private static final String INSERT_WORKOUT_SQL =
        "INSERT INTO workouts (id, user_id, name, created_at) VALUES (?, ?, ?, ?)";

    private static final String SELECT_BY_USER_SQL =
        "SELECT id, user_id, name, created_at FROM workouts WHERE user_id = ? ORDER BY created_at DESC";

    private final ConnectionPool connectionPool;

    public WorkoutRepository(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public Workout createWorkout(UUID userId, String name) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        ensureTable();

        UUID workoutId = UUID.randomUUID();
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(INSERT_WORKOUT_SQL)) {
                statement.setObject(1, workoutId, Types.OTHER);
                statement.setObject(2, userId, Types.OTHER);
                statement.setString(3, name.trim());
                statement.setTimestamp(4, createdAt);
                statement.executeUpdate();
            }
            return new Workout(workoutId, userId, name.trim(), createdAt);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create workout.", ex);
        } finally {
            requirePool().releaseConnection(connection);
        }
    }

    public List<Workout> getWorkoutsByUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        ensureTable();

        List<Workout> workouts = new ArrayList<>();
        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_SQL)) {
                statement.setObject(1, userId, Types.OTHER);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        workouts.add(new Workout(
                            (UUID) resultSet.getObject("id"),
                            (UUID) resultSet.getObject("user_id"),
                            resultSet.getString("name"),
                            resultSet.getTimestamp("created_at")
                        ));
                    }
                }
            }
            return workouts;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load workouts.", ex);
        } finally {
            requirePool().releaseConnection(connection);
        }
    }

    private void ensureTable() {
        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(CREATE_WORKOUTS_TABLE_SQL)) {
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize workouts table.", ex);
        } finally {
            requirePool().releaseConnection(connection);
        }
    }

    private ConnectionPool requirePool() {
        if (connectionPool == null) {
            throw new IllegalStateException("Database connection unavailable.");
        }
        return connectionPool;
    }
}