package services;

import database.ConnectionPool;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import models.User;
import models.Workout;
import models.WorkoutLog;
import repositories.ExerciseRepository;
import repositories.WorkoutLogRepository;
import repositories.WorkoutRepository;

public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final SessionManager sessionManager;
    private static List<models.Exercise> cachedExercises;

    public WorkoutService(
        WorkoutRepository workoutRepository,
        ExerciseRepository exerciseRepository,
        WorkoutLogRepository workoutLogRepository,
        SessionManager sessionManager
    ) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.sessionManager = sessionManager;
    }

    public WorkoutService() {
        ConnectionPool pool = ConnectionPool.getInstance();
        this.workoutRepository = new WorkoutRepository(pool);
        this.exerciseRepository = new ExerciseRepository(pool);
        this.workoutLogRepository = new WorkoutLogRepository(pool);
        this.sessionManager = SessionManager.getInstance();
    }

    public Workout createWorkout(String name) {
        UUID userId = requireLoggedInUserId();
        String normalizedName = validateName(name);
        Workout workout = workoutRepository.createWorkout(userId, normalizedName);
        System.out.println("[SERVICE] Workout created -> id=" + workout.getId() + ", userId=" + userId + ", name=" + normalizedName);
        return workout;
    }

    public WorkoutLog addLog(UUID workoutId, UUID exerciseId, int setNumber, int reps, double weight) {
        UUID userId = requireLoggedInUserId();
        if (workoutId == null) {
            throw new IllegalArgumentException("workoutId cannot be null");
        }
        if (exerciseId == null) {
            throw new IllegalArgumentException("exerciseId cannot be null");
        }
        if (setNumber < 1) {
            throw new IllegalArgumentException("setNumber must be >= 1");
        }
        if (reps <= 0) {
            throw new IllegalArgumentException("reps must be > 0");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("weight must be >= 0");
        }

        ensureWorkoutBelongsToUser(workoutId, userId);
        WorkoutLog log = workoutLogRepository.saveLog(workoutId, exerciseId, setNumber, reps, weight);
        System.out.println("[SERVICE] Log saved -> logId=" + log.getId() + ", workoutId=" + workoutId + ", exerciseId=" + exerciseId);
        return log;
    }

    public List<Workout> getWorkoutHistory() {
        UUID userId = requireLoggedInUserId();
        return workoutRepository.getWorkoutsByUser(userId);
    }

    public WorkoutDetails getWorkoutDetails(UUID workoutId) {
        UUID userId = requireLoggedInUserId();
        if (workoutId == null) {
            throw new IllegalArgumentException("workoutId cannot be null");
        }

        Workout workout = null;
        List<Workout> workouts = workoutRepository.getWorkoutsByUser(userId);
        for (Workout item : workouts) {
            if (item.getId().equals(workoutId)) {
                workout = item;
                break;
            }
        }

        if (workout == null) {
            throw new IllegalArgumentException("Workout not found for current user.");
        }

        List<WorkoutLog> logs = workoutLogRepository.getLogsByWorkout(workoutId);
        System.out.println("[SERVICE] Fetched details -> workoutId=" + workoutId + ", logs=" + logs.size());
        return new WorkoutDetails(workout, logs);
    }

    private UUID requireLoggedInUserId() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in.");
        }

        String userIdText = currentUser.getId();
        if (userIdText == null) {
            throw new IllegalStateException("User session is invalid.");
        }

        try {
            return UUID.fromString(userIdText);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("User session is invalid.");
        }
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        return name.trim();
    }

    private void ensureWorkoutBelongsToUser(UUID workoutId, UUID userId) {
        List<Workout> workouts = workoutRepository.getWorkoutsByUser(userId);
        for (Workout workout : workouts) {
            if (workout.getId().equals(workoutId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Workout not found for current user.");
    }

    public static final class WorkoutDetails {
        private final Workout workout;
        private final List<WorkoutLog> logs;

        public WorkoutDetails(Workout workout, List<WorkoutLog> logs) {
            if (workout == null) {
                throw new IllegalArgumentException("workout cannot be null");
            }
            this.workout = workout;
            this.logs = logs == null ? new ArrayList<>() : new ArrayList<>(logs);
        }

        public Workout getWorkout() {
            return workout;
        }

        public List<WorkoutLog> getLogs() {
            return new ArrayList<>(logs);
        }
    }

    public synchronized List<models.Exercise> getAllExercises() {
        if (cachedExercises == null) {
            cachedExercises = exerciseRepository.getAllExercises();
            System.out.println("[DEBUG] Loaded exercises: " + cachedExercises.size());
        }
        return new ArrayList<>(cachedExercises);
    }
}
