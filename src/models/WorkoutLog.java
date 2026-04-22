package models;

import java.util.UUID;

/**
 * Represents a single set/rep entry in a workout.
 */
public class WorkoutLog {
    private final UUID id;
    private final UUID workoutId;
    private final UUID exerciseId;
    private final int setNumber;
    private final int reps;
    private final double weight;

    public WorkoutLog(UUID id, UUID workoutId, UUID exerciseId,
                      int setNumber, int reps, double weight) {
        this.id = requireUuid(id, "id");
        this.workoutId = requireUuid(workoutId, "workoutId");
        this.exerciseId = requireUuid(exerciseId, "exerciseId");
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
    }

    public WorkoutLog(String id, String workoutId, String exerciseId,
                      int setNumber, int reps, double weight) {
        this(parseUuid(id, "id"), parseUuid(workoutId, "workoutId"), parseUuid(exerciseId, "exerciseId"),
                setNumber, reps, weight);
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

    public UUID getId() {
        return id;
    }

    public UUID getWorkoutId() {
        return workoutId;
    }

    public UUID getExerciseId() {
        return exerciseId;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public int getReps() {
        return reps;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "WorkoutLog{" +
                "id=" + id +
                ", workoutId=" + workoutId +
                ", exerciseId=" + exerciseId +
                ", setNumber=" + setNumber +
                ", reps=" + reps +
                ", weight=" + weight +
                '}';
    }
}
