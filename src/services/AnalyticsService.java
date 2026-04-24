package services;

import database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AnalyticsService {
    private final ConnectionPool connectionPool;

    public AnalyticsService() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    /** Returns list of [dateMillis, weight] for a specific exercise across workouts */
    public List<double[]> getExerciseProgress(UUID userId, UUID exerciseId) {
        String sql = "SELECT w.created_at, MAX(wl.weight) as max_weight " +
            "FROM workout_logs wl JOIN workouts w ON wl.workout_id = w.id " +
            "WHERE w.user_id = ? AND wl.exercise_id = ? " +
            "GROUP BY w.created_at ORDER BY w.created_at ASC";

        List<double[]> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = connectionPool.acquireConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, userId, Types.OTHER);
                ps.setObject(2, exerciseId, Types.OTHER);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double dateMs = rs.getTimestamp("created_at").getTime();
                        double weight = rs.getDouble("max_weight");
                        result.add(new double[]{dateMs, weight});
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("[ERROR] getExerciseProgress: " + ex.getMessage());
        } finally {
            connectionPool.releaseConnection(conn);
        }
        return result;
    }

    /** Returns list of [dateMillis, totalVolume] per workout */
    public List<double[]> getVolumeTrend(UUID userId) {
        String sql = "SELECT w.created_at, SUM(wl.reps * wl.weight) as volume " +
            "FROM workout_logs wl JOIN workouts w ON wl.workout_id = w.id " +
            "WHERE w.user_id = ? " +
            "GROUP BY w.id, w.created_at ORDER BY w.created_at ASC";

        List<double[]> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = connectionPool.acquireConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, userId, Types.OTHER);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double dateMs = rs.getTimestamp("created_at").getTime();
                        double volume = rs.getDouble("volume");
                        result.add(new double[]{dateMs, volume});
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("[ERROR] getVolumeTrend: " + ex.getMessage());
        } finally {
            connectionPool.releaseConnection(conn);
        }
        return result;
    }

    /** Returns map of exerciseName → maxWeight */
    public Map<String, Double> getPRs(UUID userId) {
        String sql = "SELECT e.name, MAX(wl.weight) as pr " +
            "FROM workout_logs wl " +
            "JOIN exercises e ON wl.exercise_id = e.id " +
            "JOIN workouts w ON wl.workout_id = w.id " +
            "WHERE w.user_id = ? " +
            "GROUP BY e.name ORDER BY pr DESC";

        Map<String, Double> result = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = connectionPool.acquireConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, userId, Types.OTHER);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.put(rs.getString("name"), rs.getDouble("pr"));
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("[ERROR] getPRs: " + ex.getMessage());
        } finally {
            connectionPool.releaseConnection(conn);
        }
        return result;
    }
}
