package services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import database.ConnectionPool;
import models.Exercise;
import repositories.ExerciseRepository;

public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private static final Map<String, ExerciseGuidance> guidanceMap = new HashMap<>();

    static {
        // In-memory guidance data for common exercises
        addGuidance("Bench Press",
            Arrays.asList("Lie flat on bench with feet on floor", "Grip bar slightly wider than shoulders", "Unrack and lower bar to mid-chest", "Press up to full extension"),
            Arrays.asList("Keep shoulder blades retracted", "Drive feet into floor for stability", "Control the descent — don't bounce"),
            Arrays.asList("Flaring elbows too wide", "Lifting hips off the bench", "Inconsistent bar path")
        );
        addGuidance("Squat",
            Arrays.asList("Position bar on upper traps", "Feet shoulder-width apart, toes slightly out", "Break at hips and knees simultaneously", "Descend until thighs are parallel", "Drive up through heels"),
            Arrays.asList("Keep chest up throughout", "Push knees out over toes", "Brace core before descending"),
            Arrays.asList("Knees caving inward", "Rounding lower back", "Shifting weight to toes")
        );
        addGuidance("Deadlift",
            Arrays.asList("Stand with feet hip-width, bar over mid-foot", "Hinge at hips, grip bar outside knees", "Flatten back, engage lats", "Drive through floor, extending hips and knees", "Lock out at top"),
            Arrays.asList("Keep bar close to body", "Think about pushing floor away", "Squeeze glutes at lockout"),
            Arrays.asList("Rounding the back", "Jerking the bar off the floor", "Hyperextending at the top")
        );
        addGuidance("Overhead Press",
            Arrays.asList("Start with bar at shoulder height", "Grip slightly outside shoulders", "Press bar overhead in slight arc", "Lock out with bar over mid-foot"),
            Arrays.asList("Brace core tightly", "Squeeze glutes for stability", "Move head through once bar passes"),
            Arrays.asList("Excessive back lean", "Using leg drive (strict press)", "Not locking out fully")
        );
        addGuidance("Barbell Row",
            Arrays.asList("Hinge forward ~45 degrees", "Grip bar shoulder-width", "Pull bar to lower chest/upper belly", "Squeeze shoulder blades at top"),
            Arrays.asList("Keep core braced", "Control the negative", "Don't swing the weight"),
            Arrays.asList("Using too much momentum", "Not pulling high enough", "Rounding the back")
        );
        addGuidance("Pull-up",
            Arrays.asList("Hang from bar with overhand grip", "Retract shoulder blades", "Pull chin above bar", "Lower under control"),
            Arrays.asList("Avoid kipping or swinging", "Full range of motion", "Engage lats, not just arms"),
            Arrays.asList("Half reps", "Excessive swinging", "Shrugging shoulders")
        );
        addGuidance("Leg Press",
            Arrays.asList("Sit in machine with back flat", "Place feet shoulder-width on platform", "Lower platform until knees at 90°", "Press up without locking knees"),
            Arrays.asList("Keep lower back pressed to pad", "Don't let knees cave in", "Control the descent"),
            Arrays.asList("Going too deep and rounding back", "Locking knees at top", "Feet too high or too low")
        );
    }

    private static void addGuidance(String name, List<String> steps, List<String> tips, List<String> mistakes) {
        guidanceMap.put(name.toLowerCase(), new ExerciseGuidance(steps, tips, mistakes));
    }

    public ExerciseService() {
        this.exerciseRepository = new ExerciseRepository(ConnectionPool.getInstance());
    }

    public List<Exercise> getAllExercises() {
        return exerciseRepository.getAllExercises();
    }

    public List<Exercise> filterExercises(String muscleGroup, String equipment, String difficulty) {
        return getAllExercises().stream()
            .filter(e -> muscleGroup == null || muscleGroup.isEmpty() || muscleGroup.equals(e.getMuscleGroup()))
            .filter(e -> equipment == null || equipment.isEmpty() || equipment.equals(e.getEquipment()))
            .filter(e -> difficulty == null || difficulty.isEmpty() || difficulty.equals(e.getDifficulty()))
            .collect(Collectors.toList());
    }

    public ExerciseGuidance getGuidance(String exerciseName) {
        if (exerciseName == null) return getDefaultGuidance();
        ExerciseGuidance g = guidanceMap.get(exerciseName.toLowerCase());
        return g != null ? g : getDefaultGuidance();
    }

    public List<Exercise> getSubstitutions(UUID exerciseId) {
        List<Exercise> all = getAllExercises();
        Exercise target = null;
        for (Exercise e : all) {
            if (e.getId().equals(exerciseId)) { target = e; break; }
        }
        if (target == null) return new ArrayList<>();

        final String muscleGroup = target.getMuscleGroup();
        final String equipment = target.getEquipment();
        final UUID excludeId = exerciseId;

        // Prefer same muscle group, sort same-equipment first
        return all.stream()
            .filter(e -> !e.getId().equals(excludeId))
            .filter(e -> e.getMuscleGroup() != null && e.getMuscleGroup().equals(muscleGroup))
            .sorted((a, b) -> {
                boolean aMatch = equipment != null && equipment.equals(a.getEquipment());
                boolean bMatch = equipment != null && equipment.equals(b.getEquipment());
                return Boolean.compare(bMatch, aMatch);
            })
            .collect(Collectors.toList());
    }

    private ExerciseGuidance getDefaultGuidance() {
        return new ExerciseGuidance(
            Arrays.asList("Follow standard form for this exercise"),
            Arrays.asList("Focus on controlled movement", "Use appropriate weight"),
            Arrays.asList("Using momentum instead of muscle", "Poor range of motion")
        );
    }

    public static class ExerciseGuidance {
        private final List<String> steps;
        private final List<String> tips;
        private final List<String> mistakes;

        public ExerciseGuidance(List<String> steps, List<String> tips, List<String> mistakes) {
            this.steps = steps;
            this.tips = tips;
            this.mistakes = mistakes;
        }

        public List<String> getSteps() { return steps; }
        public List<String> getTips() { return tips; }
        public List<String> getMistakes() { return mistakes; }
    }
}
