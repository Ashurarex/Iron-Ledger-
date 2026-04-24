package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import models.Exercise;
import models.Workout;
import models.WorkoutLog;
import services.WorkoutService;
import ui.components.CardPanel;
import ui.theme.NotificationManager;
import ui.theme.ThemeManager;

public class WorkoutHistoryPanel extends JPanel {
    private final WorkoutService workoutService;
    private JList<Workout> workoutList;
    private JTextArea logsArea;
    private JLabel statusLabel;
    private Map<UUID, String> exerciseNameMap;
    private boolean exercisesLoaded = false;

    public WorkoutHistoryPanel() {
        this.workoutService = new WorkoutService();
        this.exerciseNameMap = new HashMap<>();
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.getInstance();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel header = new JLabel("Workout History");
        header.setFont(tm.getHeaderFont());
        header.setForeground(tm.getTextPrimary());
        header.setAlignmentX(LEFT_ALIGNMENT);
        container.add(header);
        container.add(Box.createRigidArea(new Dimension(0, 8)));

        statusLabel = new JLabel("Loading history...");
        statusLabel.setFont(tm.getSmallFont());
        statusLabel.setForeground(tm.getTextMuted());
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        container.add(statusLabel);
        container.add(Box.createRigidArea(new Dimension(0, 16)));

        // Content: list + details
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);
        content.setAlignmentX(LEFT_ALIGNMENT);

        // Left: Workout list
        CardPanel listCard = new CardPanel(new BorderLayout());
        JLabel listTitle = new JLabel("Workouts");
        listTitle.setFont(tm.getLabelFont());
        listTitle.setForeground(tm.getTextSecondary());
        listTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        listCard.add(listTitle, BorderLayout.NORTH);

        workoutList = new JList<>();
        workoutList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workoutList.setFont(tm.getBodyFont());
        workoutList.setBackground(tm.getCardBg());
        workoutList.setForeground(tm.getTextPrimary());
        workoutList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ThemeManager t = ThemeManager.getInstance();
                if (value instanceof Workout) {
                    setText(((Workout) value).getName());
                }
                if (isSelected) {
                    setBackground(t.getPrimary());
                    setForeground(Color.WHITE);
                } else {
                    setBackground(t.getCardBg());
                    setForeground(t.getTextPrimary());
                }
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                return this;
            }
        });
        workoutList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) handleWorkoutSelected();
        });

        JScrollPane listScroll = new JScrollPane(workoutList);
        listScroll.setPreferredSize(new Dimension(220, 0));
        listScroll.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        listCard.add(listScroll, BorderLayout.CENTER);
        content.add(listCard, BorderLayout.WEST);

        // Right: Details
        CardPanel detailCard = new CardPanel(new BorderLayout());
        JLabel detailTitle = new JLabel("Details");
        detailTitle.setFont(tm.getLabelFont());
        detailTitle.setForeground(tm.getTextSecondary());
        detailTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        detailCard.add(detailTitle, BorderLayout.NORTH);

        logsArea = new JTextArea();
        logsArea.setEditable(false);
        logsArea.setFont(tm.getMonoFont());
        logsArea.setForeground(tm.getTextPrimary());
        logsArea.setBackground(tm.getPreviewBg());
        logsArea.setText("Select a workout to view details.");
        JScrollPane logsScroll = new JScrollPane(logsArea);
        logsScroll.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        detailCard.add(logsScroll, BorderLayout.CENTER);
        content.add(detailCard, BorderLayout.CENTER);

        container.add(content);
        add(container, BorderLayout.CENTER);
    }

    private void loadExercisesIfNeeded() {
        if (exercisesLoaded) return;
        try {
            List<Exercise> exercises = workoutService.getAllExercises();
            for (Exercise e : exercises) exerciseNameMap.put(e.getId(), e.getName());
            exercisesLoaded = true;
            System.out.println("[HISTORY] Exercise names cached: " + exerciseNameMap.size());
        } catch (Exception ex) {
            System.err.println("[HISTORY] Failed to load exercises: " + ex.getMessage());
        }
    }

    public void loadWorkouts() {
        statusLabel.setText("Loading...");
        logsArea.setText("Loading history...");

        new SwingWorker<List<Workout>, Void>() {
            @Override
            protected List<Workout> doInBackground() {
                loadExercisesIfNeeded();
                return workoutService.getWorkoutHistory();
            }

            @Override
            protected void done() {
                try {
                    List<Workout> workouts = get();
                    workoutList.setListData(workouts.toArray(new Workout[0]));

                    if (workouts.isEmpty()) {
                        statusLabel.setText("No workouts yet. Start one from the Workout tab!");
                        logsArea.setText("No workouts found.\n\nGo to 'Start Workout' to create your first session.");
                    } else {
                        statusLabel.setText(workouts.size() + " workout(s) found");
                        logsArea.setText("Select a workout to view details.");
                    }
                    System.out.println("[HISTORY] Loaded workouts: " + workouts.size());
                } catch (Exception ex) {
                    System.err.println("[HISTORY] Load failed: " + ex.getMessage());
                    statusLabel.setText("Failed to load history");
                    logsArea.setText("Error loading workout history.\n\n" + ex.getMessage());
                }
            }
        }.execute();
    }

    private void handleWorkoutSelected() {
        Workout selected = workoutList.getSelectedValue();
        if (selected == null) {
            logsArea.setText("Select a workout to view details.");
            return;
        }

        logsArea.setText("Loading details...");
        final UUID workoutId = selected.getId();
        System.out.println("[HISTORY] Fetching logs -> workoutId=" + workoutId);

        new SwingWorker<WorkoutService.WorkoutDetails, Void>() {
            @Override
            protected WorkoutService.WorkoutDetails doInBackground() {
                return workoutService.getWorkoutDetails(workoutId);
            }

            @Override
            protected void done() {
                try {
                    WorkoutService.WorkoutDetails details = get();
                    List<WorkoutLog> logs = details.getLogs();
                    System.out.println("[HISTORY] Fetched logs: " + logs.size() + " for workoutId=" + workoutId);

                    StringBuilder sb = new StringBuilder();
                    sb.append("Workout: ").append(selected.getName()).append("\n");
                    sb.append("Date: ").append(selected.getCreatedAt()).append("\n\n");

                    if (logs.isEmpty()) {
                        sb.append("No logs for this workout.");
                    } else {
                        // Group by exercise
                        java.util.LinkedHashMap<UUID, java.util.List<WorkoutLog>> grouped = new java.util.LinkedHashMap<>();
                        for (WorkoutLog log : logs) {
                            grouped.computeIfAbsent(log.getExerciseId(), k -> new java.util.ArrayList<>()).add(log);
                        }
                        for (Map.Entry<UUID, java.util.List<WorkoutLog>> entry : grouped.entrySet()) {
                            String exerciseName = exerciseNameMap.getOrDefault(entry.getKey(), "Unknown Exercise");
                            sb.append(exerciseName).append("\n");
                            for (WorkoutLog log : entry.getValue()) {
                                String w = (log.getWeight() % 1 == 0) ? String.format("%.0f", log.getWeight()) : String.valueOf(log.getWeight());
                                sb.append(String.format("  Set %d: %d reps x %skg\n", log.getSetNumber(), log.getReps(), w));
                            }
                            sb.append("\n");
                        }
                    }

                    logsArea.setText(sb.toString());
                    logsArea.setCaretPosition(0);
                } catch (Exception ex) {
                    System.err.println("[HISTORY] Detail load failed: " + ex.getMessage());
                    logsArea.setText("Error loading workout details.\n\n" + ex.getMessage());
                }
            }
        }.execute();
    }
}
