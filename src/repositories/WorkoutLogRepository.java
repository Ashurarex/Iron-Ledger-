package repositories;

import database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import models.WorkoutLog;

public class WorkoutLogRepository {
    private static final String CREATE_WORKOUT_LOGS_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS workout_logs (
            id UUID PRIMARY KEY,
            workout_id UUID NOT NULL REFERENCES workouts(id),
            exercise_id UUID NOT NULL REFERENCES exercises(id),
            set_number INTEGER NOT NULL CHECK (set_number >= 1),
            reps INTEGER NOT NULL CHECK (reps > 0),
            weight DOUBLE PRECISION NOT NULL CHECK (weight >= 0)
        )
        """;

    private static final String INSERT_LOG_SQL =
        "INSERT INTO workout_logs (id, workout_id, exercise_id, set_number, reps, weight) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_WORKOUT_SQL =
        "SELECT id, workout_id, exercise_id, set_number, reps, weight FROM workout_logs WHERE workout_id = ? ORDER BY set_number ASC, id ASC";

    private final ConnectionPool connectionPool;

    public WorkoutLogRepository(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public WorkoutLog saveLog(UUID workoutId, UUID exerciseId, int setNumber, int reps, double weight) {
        if (workoutId == null) {
            throw new IllegalArgumentException("workoutId cannot be null");
        }
        if (exerciseId == null) {
            throw new IllegalArgumentException("exerciseId cannot be null");
        }

        ensureTable();

        UUID id = UUID.randomUUID();
        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(INSERT_LOG_SQL)) {
                statement.setObject(1, id, Types.OTHER);
                statement.setObject(2, workoutId, Types.OTHER);
                statement.setObject(3, exerciseId, Types.OTHER);
                statement.setInt(4, setNumber);
                statement.setInt(5, reps);
                statement.setDouble(6, weight);
                statement.executeUpdate();
            }
            return new WorkoutLog(id, workoutId, exerciseId, setNumber, reps, weight);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save workout log.", ex);
        } finally {
            requirePool().releaseConnection(connection);
        }
    }

    public List<WorkoutLog> getLogsByWorkout(UUID workoutId) {
        if (workoutId == null) {
            throw new IllegalArgumentException("workoutId cannot be null");
        }

        ensureTable();

        List<WorkoutLog> logs = new ArrayList<>();
        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_WORKOUT_SQL)) {
                statement.setObject(1, workoutId, Types.OTHER);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        logs.add(new WorkoutLog(
                            (UUID) resultSet.getObject("id"),
                            (UUID) resultSet.getObject("workout_id"),
                            (UUID) resultSet.getObject("exercise_id"),
                            resultSet.getInt("set_number"),
                            resultSet.getInt("reps"),
                            resultSet.getDouble("weight")
                        ));
                    }
                }
            }
            return logs;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load workout logs.", ex);
        } finally {
            requirePool().releaseConnection(connection);
        }
    }

    private void ensureTable() {
        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(CREATE_WORKOUT_LOGS_TABLE_SQL)) {
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize workout logs table.", ex);
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