import java.util.Set;
import java.util.UUID;

/**
 * Represents an exercise definition in the Iron Ledger fitness tracking application.
 * 
 * This immutable domain model defines a specific exercise with metadata about its
 * category, description, and the muscle groups it targets. Exercises are reusable
 * templates that can be included in multiple workouts.
 * 
 * @author Iron Ledger
 * @version 1.0
 */
public class Exercise {
    
    /**
     * The unique identifier for this exercise.
     */
    private final UUID id;
    
    /**
     * The name of the exercise (e.g., "Bench Press", "Squats").
     */
    private final String name;
    
    /**
     * The category this exercise belongs to (e.g., "Chest", "Legs", "Cardio").
     */
    private final String category;
    
    /**
     * A detailed description of how to perform this exercise correctly.
     */
    private final String description;
    
    /**
     * The set of muscle groups targeted by this exercise.
     * Examples: "Chest", "Triceps", "Shoulders"
     */
    private final Set<String> muscleGroups;
    
    /**
     * Constructs a new Exercise with the specified parameters.
     * 
     * @param id the unique identifier for this exercise
     * @param name the name of the exercise
     * @param category the category this exercise belongs to
     * @param description instructions for performing the exercise
     * @param muscleGroups the set of muscle groups targeted by this exercise
     */
    public Exercise(UUID id, String name, String category, String description, Set<String> muscleGroups) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.muscleGroups = muscleGroups;
    }
    
    /**
     * Gets the unique identifier for this exercise.
     * 
     * @return the exercise ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Gets the name of this exercise.
     * 
     * @return the exercise name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the category this exercise belongs to.
     * 
     * @return the exercise category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Gets the description of how to perform this exercise.
     * 
     * @return the exercise description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the set of muscle groups targeted by this exercise.
     * 
     * @return the set of muscle groups
     */
    public Set<String> getMuscleGroups() {
        return muscleGroups;
    }
    
    /**
     * Returns a string representation of this Exercise.
     * 
     * @return a string describing this exercise
     */
    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", muscleGroups=" + muscleGroups +
                '}';
    }
}
