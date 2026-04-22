package models;

import java.util.UUID;

/**
 * Represents an exercise that can be logged in a workout.
 */
public class Exercise {
    private final UUID id;
    private final String name;
    private final String muscleGroup;
    private final String equipment;
    private final String difficulty;

    public Exercise(UUID id, String name, String muscleGroup, String equipment, String difficulty) {
        this.id = requireUuid(id, "id");
        this.name = requireText(name, "name");
        this.muscleGroup = muscleGroup;
        this.equipment = equipment;
        this.difficulty = difficulty;
    }

    public Exercise(String id, String name, String muscleGroup, String equipment, String difficulty) {
        this(parseUuid(id, "id"), name, muscleGroup, equipment, difficulty);
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

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public String getEquipment() {
        return equipment;
    }

    public String getDifficulty() {
        return difficulty;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", muscleGroup='" + muscleGroup + '\'' +
                ", equipment='" + equipment + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
