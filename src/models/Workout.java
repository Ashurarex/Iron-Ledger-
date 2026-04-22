package models;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Represents a workout session.
 */
public class Workout {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final Timestamp createdAt;

    public Workout(UUID id, UUID userId, String name, Timestamp createdAt) {
        this.id = requireUuid(id, "id");
        this.userId = requireUuid(userId, "userId");
        this.name = requireText(name, "name");
        this.createdAt = requireTimestamp(createdAt, "createdAt");
    }

    public Workout(UUID id, UUID userId, String name) {
        this(id, userId, name, new Timestamp(System.currentTimeMillis()));
    }

    public Workout(String id, String userId, String name, Timestamp createdAt) {
        this(parseUuid(id, "id"), parseUuid(userId, "userId"), name, createdAt);
    }

    public Workout(String id, String userId, String name) {
        this(parseUuid(id, "id"), parseUuid(userId, "userId"), name);
    }

    private static UUID parseUuid(String uuidText, String fieldName) {
        if (uuidText == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        try {
            return UUID.fromString(uuidText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for " + fieldName + ": " + uuidText);
        }
    }

    private static UUID requireUuid(UUID value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        return value;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        return value;
    }

    private static Timestamp requireTimestamp(Timestamp value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
