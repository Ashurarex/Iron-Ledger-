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
import models.Exercise;

public class ExerciseRepository {
    private static final String CREATE_EXERCISES_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS exercises (
            id UUID PRIMARY KEY,
            name VARCHAR(200) NOT NULL,
            muscle_group VARCHAR(120),
            equipment VARCHAR(120),
            difficulty VARCHAR(80)
        )
        """;

    private static final String SELECT_ALL_SQL =
        "SELECT id, name, muscle_group, equipment, difficulty FROM exercises ORDER BY name ASC";

    private static final String INSERT_EXERCISE_SQL =
        "INSERT INTO exercises (id, name, muscle_group, equipment, difficulty) VALUES (?, ?, ?, ?, ?)";

    private final ConnectionPool connectionPool;

    public ExerciseRepository(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public List<Exercise> getAllExercises() {
        ensureTable();

        List<Exercise> exercises = fetchAll();
        if (!exercises.isEmpty()) {
            return exercises;
        }

        seedExercises();
        return fetchAll();
    }

    private List<Exercise> fetchAll() {
        List<Exercise> exercises = new ArrayList<>();
        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exercises.add(new Exercise(
                        (UUID) resultSet.getObject("id"),
                        resultSet.getString("name"),
                        resultSet.getString("muscle_group"),
                        resultSet.getString("equipment"),
                        resultSet.getString("difficulty")
                    ));
                }
            }
            return exercises;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load exercises.", ex);
        } finally {
            requirePool().releaseConnection(connection);
        }
    }

    private void seedExercises() {
        final String[][] seeds = {
            {"Bench Press", "Chest", "Barbell", "Intermediate"},
            {"Squat", "Legs", "Barbell", "Intermediate"},
            {"Deadlift", "Back", "Barbell", "Advanced"},
            {"Shoulder Press", "Shoulders", "Dumbbell", "Intermediate"},
            {"Bicep Curl", "Arms", "Dumbbell", "Beginner"}
        };

        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_SQL)) {
                for (String[] seed : seeds) {
                    statement.setObject(1, UUID.randomUUID(), Types.OTHER);
                    statement.setString(2, seed[0]);
                    statement.setString(3, seed[1]);
                    statement.setString(4, seed[2]);
                    statement.setString(5, seed[3]);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to seed exercises.", ex);
        } finally {
            requirePool().releaseConnection(connection);
        }
    }

    private void ensureTable() {
        Connection connection = null;
        try {
            connection = requirePool().acquireConnection();
            try (PreparedStatement statement = connection.prepareStatement(CREATE_EXERCISES_TABLE_SQL)) {
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize exercises table.", ex);
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