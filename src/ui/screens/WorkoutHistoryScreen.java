package ui.screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import models.Workout;
import models.WorkoutLog;
import services.WorkoutService;

public class WorkoutHistoryScreen extends JFrame {
    private final WorkoutService workoutService;
    private JList<Workout> workoutList;
    private JTextArea logsArea;
    private Map<UUID, String> exerciseNameMap;

    public WorkoutHistoryScreen() {
        super("Workout History");
        this.workoutService = new WorkoutService();
        initialize();
        loadExercises();
        loadWorkouts();
    }

    private void loadExercises() {
        exerciseNameMap = new HashMap<>();
        try {
            List<models.Exercise> exercises = workoutService.getAllExercises();
            for (models.Exercise e : exercises) {
                exerciseNameMap.put(e.getId(), e.getName());
            }
        } catch (Exception ex) {
            System.err.println("Failed to load exercises for mapping: " + ex.getMessage());
        }
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        workoutList = new JList<>();
        workoutList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workoutList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Workout) {
                    Workout w = (Workout) value;
                    setText(w.getName());
                }
                return this;
            }
        });

        workoutList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleWorkoutSelected();
            }
        });

        JScrollPane listScroller = new JScrollPane(workoutList);
        listScroller.setPreferredSize(new Dimension(200, 0));
        add(listScroller, BorderLayout.WEST);

        logsArea = new JTextArea();
        logsArea.setEditable(false);
        JScrollPane logsScroller = new JScrollPane(logsArea);
        add(logsScroller, BorderLayout.CENTER);
    }

    private void loadWorkouts() {
        try {
            List<Workout> workouts = workoutService.getWorkoutHistory();
            Workout[] workoutArray = workouts.toArray(new Workout[0]);
            workoutList.setListData(workoutArray);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading history: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleWorkoutSelected() {
        Workout selected = workoutList.getSelectedValue();
        if (selected == null) {
            logsArea.setText("");
            return;
        }

        try {
            WorkoutService.WorkoutDetails details = workoutService.getWorkoutDetails(selected.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("Workout: ").append(selected.getName()).append("\n");
            sb.append("Date: ").append(selected.getCreatedAt()).append("\n\n");
            sb.append("Logs:\n");

            List<WorkoutLog> logs = details.getLogs();
            System.out.println("[DEBUG] Fetched logs count: " + logs.size());
            if (logs.isEmpty()) {
                sb.append("No logs for this workout.");
            } else {
                for (WorkoutLog log : logs) {
                    String exerciseName = exerciseNameMap.getOrDefault(log.getExerciseId(), "Unknown Exercise");
                    sb.append("Exercise: ").append(exerciseName).append("\n");
                    sb.append(" Set: ").append(log.getSetNumber());
                    sb.append(", Reps: ").append(log.getReps());
                    sb.append(", Weight: ").append(log.getWeight()).append("\n\n");
                }
            }

            logsArea.setText(sb.toString());
            logsArea.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading workout details: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
