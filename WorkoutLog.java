import java.util.UUID;

/**
 * Represents a single set/rep/weight entry in the Iron Ledger fitness tracking application.
 * 
 * This immutable domain model records the performance details of a specific exercise
 * during a specific workout. It creates the link between workouts and exercises,
 * allowing fine-grained tracking of performance metrics like sets, reps, and weight.
 * 
 * @author Iron Ledger
 * @version 1.0
 */
public class WorkoutLog {
    
    /**
     * The unique identifier for this workout log entry.
     */
    private final UUID id;
    
    /**
     * The ID of the workout this log entry belongs to.
     */
    private final UUID workoutId;
    
    /**
     * The ID of the exercise being logged.
     */
    private final UUID exerciseId;
    
    /**
     * The set number within this workout (1st set, 2nd set, etc.).
     */
    private final int setNumber;
    
    /**
     * The number of repetitions completed in this set.
     */
    private final int reps;
    
    /**
     * The weight used for this set (in pounds or kilograms, depending on user preference).
     */
    private final double weight;
    
    /**
     * Constructs a new WorkoutLog entry with the specified parameters.
     * 
     * @param id the unique identifier for this log entry
     * @param workoutId the ID of the parent workout
     * @param exerciseId the ID of the exercise being logged
     * @param setNumber the set number within the workout
     * @param reps the number of repetitions completed
     * @param weight the weight used for this set
     */
    public WorkoutLog(UUID id, UUID workoutId, UUID exerciseId, int setNumber, int reps, double weight) {
        this.id = id;
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
    }
    
    /**
     * Gets the unique identifier for this workout log entry.
     * 
     * @return the log entry ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Gets the ID of the workout this entry belongs to.
     * 
     * @return the workout ID
     */
    public UUID getWorkoutId() {
        return workoutId;
    }
    
    /**
     * Gets the ID of the exercise being logged.
     * 
     * @return the exercise ID
     */
    public UUID getExerciseId() {
        return exerciseId;
    }
    
    /**
     * Gets the set number for this log entry.
     * 
     * @return the set number
     */
    public int getSetNumber() {
        return setNumber;
    }
    
    /**
     * Gets the number of repetitions completed in this set.
     * 
     * @return the number of reps
     */
    public int getReps() {
        return reps;
    }
    
    /**
     * Gets the weight used for this set.
     * 
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * Returns a string representation of this WorkoutLog entry.
     * 
     * @return a string describing this log entry
     */
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
