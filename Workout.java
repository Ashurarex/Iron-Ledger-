import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Represents a complete workout session in the Iron Ledger fitness tracking application.
 * 
 * This immutable domain model encapsulates all information about a single workout,
 * including the date it occurred, any notes the user added, and the list of exercises
 * performed during that workout.
 * 
 * @author Iron Ledger
 * @version 1.0
 */
public class Workout {
    
    /**
     * The unique identifier for this workout.
     */
    private final UUID id;
    
    /**
     * The date on which this workout was performed.
     */
    private final LocalDate date;
    
    /**
     * Optional notes about the workout (e.g., "felt strong today", "low energy").
     */
    private final String notes;
    
    /**
     * List of exercise UUIDs that were performed during this workout.
     * This maintains the relationship between workouts and the exercises completed.
     */
    private final List<UUID> exercises;
    
    /**
     * Constructs a new Workout with the specified parameters.
     * 
     * @param id the unique identifier for this workout
     * @param date the date the workout was performed
     * @param notes optional notes about the workout
     * @param exercises the list of exercise IDs performed during this workout
     */
    public Workout(UUID id, LocalDate date, String notes, List<UUID> exercises) {
        this.id = id;
        this.date = date;
        this.notes = notes;
        this.exercises = exercises;
    }
    
    /**
     * Gets the unique identifier for this workout.
     * 
     * @return the workout ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Gets the date this workout was performed.
     * 
     * @return the workout date
     */
    public LocalDate getDate() {
        return date;
    }
    
    /**
     * Gets the optional notes associated with this workout.
     * 
     * @return the workout notes, or null if none were provided
     */
    public String getNotes() {
        return notes;
    }
    
    /**
     * Gets the list of exercise IDs performed during this workout.
     * 
     * @return the list of exercise UUIDs
     */
    public List<UUID> getExercises() {
        return exercises;
    }
    
    /**
     * Returns a string representation of this Workout.
     * 
     * @return a string describing this workout
     */
    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", date=" + date +
                ", notes='" + notes + '\'' +
                ", exercises=" + exercises +
                '}';
    }
}
