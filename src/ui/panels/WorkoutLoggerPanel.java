package ui.panels;

import ui.animation.Animator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import models.Exercise;
import models.Workout;
import services.WorkoutService;
import ui.components.CardPanel;
import ui.components.FeedbackBar;
import ui.components.MaterialComboBox;
import ui.components.MaterialTextField;
import ui.components.PrimaryButton;
import ui.theme.ThemeManager;

public class WorkoutLoggerPanel extends JPanel {
    private final WorkoutService workoutService;
    private Workout currentWorkout;
    private List<Exercise> exercisesList;
    private Map<String, UUID> exerciseMap;
    private Map<UUID, List<SetEntry>> workoutData;
    private Map<UUID, Double> lastWeightMap;
    private boolean isSaving = false;
    private long lastSaveTime = 0;

    private FeedbackBar feedbackBar;
    private MaterialTextField nameField;
    private PrimaryButton createButton;
    private MaterialComboBox<String> muscleGroupComboBox;
    private MaterialComboBox<String> exerciseComboBox;
    private MaterialTextField setNumberField;
    private MaterialTextField repsField;
    private MaterialTextField weightField;
    private PrimaryButton addSetButton;
    private PrimaryButton saveButton;
    private JTextArea previewArea;
    private JLabel statusLabel;
    private Animator statusAnimator;
    private float statusPulse = 0f;
    private boolean statusPulseForward = true;

    public WorkoutLoggerPanel() {
        this.workoutService = new WorkoutService();
        this.exerciseMap = new HashMap<>();
        this.workoutData = new LinkedHashMap<>();
        this.lastWeightMap = new HashMap<>();
        this.exercisesList = new ArrayList<>();
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
        loadExercisesAsync();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.getInstance();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Inline feedback bar at top
        feedbackBar = new FeedbackBar();
        feedbackBar.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(feedbackBar);

        // Header
        JLabel header = new JLabel("Log Workout");
        header.setFont(tm.getHeaderFont());
        header.setForeground(tm.getTextPrimary());
        header.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(header);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        statusLabel = new JLabel("Loading exercises...");
        statusLabel.setFont(tm.getSmallFont());
        statusLabel.setForeground(tm.getTextMuted());
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section A: Workout Setup
        CardPanel setupCard = new CardPanel(new GridBagLayout());
        setupCard.setAlignmentX(LEFT_ALIGNMENT);
        setupCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.weightx = 1.0; g.gridwidth = 2;
        nameField = new MaterialTextField("Workout Name");
        setupCard.add(nameField, g);
        g.gridx = 2; g.weightx = 0; g.gridwidth = 1;
        createButton = new PrimaryButton("Create");
        createButton.addActionListener(e -> handleCreateWorkout());
        setupCard.add(createButton, g);

        mainPanel.add(setupCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section B: Exercise Builder
        CardPanel builderCard = new CardPanel(new GridBagLayout());
        builderCard.setAlignmentX(LEFT_ALIGNMENT);
        g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        builderCard.add(createLabel("Muscle Group"), g);
        g.gridx = 1; g.weightx = 1.0;
        muscleGroupComboBox = new MaterialComboBox<>();
        muscleGroupComboBox.addItem("Select Muscle Group");
        builderCard.add(muscleGroupComboBox, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        builderCard.add(createLabel("Exercise"), g);
        g.gridx = 1; g.weightx = 1.0;
        exerciseComboBox = new MaterialComboBox<>();
        exerciseComboBox.addItem("Select Exercise");
        builderCard.add(exerciseComboBox, g);

        g.gridx = 0; g.gridy = 2; g.weightx = 1.0; g.gridwidth = 2;
        setNumberField = new MaterialTextField("Set #");
        builderCard.add(setNumberField, g);

        g.gridx = 0; g.gridy = 3; g.weightx = 1.0; g.gridwidth = 2;
        repsField = new MaterialTextField("Reps");
        builderCard.add(repsField, g);

        g.gridx = 0; g.gridy = 4; g.weightx = 1.0; g.gridwidth = 2;
        weightField = new MaterialTextField("Weight (kg)");
        builderCard.add(weightField, g);

        g.gridx = 1; g.gridy = 5; g.weightx = 0; g.gridwidth = 1;
        addSetButton = new PrimaryButton("Add Set");
        addSetButton.addActionListener(e -> handleAddSet());
        builderCard.add(addSetButton, g);

        mainPanel.add(builderCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section C: Session Preview
        CardPanel previewCard = new CardPanel(new BorderLayout());
        previewCard.setAlignmentX(LEFT_ALIGNMENT);
        JLabel previewTitle = createLabel("Session Preview");
        previewTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        previewCard.add(previewTitle, BorderLayout.NORTH);

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(tm.getMonoFont());
        previewArea.setForeground(tm.getInputText());
        previewArea.setBackground(tm.getPreviewBg());
        previewArea.setText("No sets added yet.");
        JScrollPane scroll = new JScrollPane(previewArea);
        scroll.setPreferredSize(new Dimension(400, 150));
        scroll.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        previewCard.add(scroll, BorderLayout.CENTER);

        mainPanel.add(previewCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Section D: Save
        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(LEFT_ALIGNMENT);
        saveButton = new PrimaryButton("Save / Finish Workout");
        saveButton.addActionListener(e -> handleFinish());
        actionPanel.add(saveButton);
        mainPanel.add(actionPanel);

        JScrollPane mainScroll = new JScrollPane(mainPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        setLogControlsEnabled(false);
        exerciseComboBox.addActionListener(e -> handleExerciseSelected());
    }

    private void loadExercisesAsync() {
        setStatus("Loading exercises...", true);
        new SwingWorker<List<Exercise>, Void>() {
            @Override
            protected List<Exercise> doInBackground() {
                return workoutService.getAllExercises();
            }

            @Override
            protected void done() {
                try {
                    exercisesList = get();
                    System.out.println("[EXERCISE] Loaded: " + exercisesList.size() + " exercises");
                    populateMuscleGroups();
                    setStatus("Ready - " + exercisesList.size() + " exercises available", false);
                } catch (Exception ex) {
                    System.err.println("[EXERCISE] Failed to load: " + ex.getMessage());
                    exercisesList = new ArrayList<>();
                    setStatus("Failed to load exercises. Check connection.", false);
                    feedbackBar.showError("Failed to load exercises");
                }
            }
        }.execute();
    }

    private void populateMuscleGroups() {
        muscleGroupComboBox.removeAllItems();
        muscleGroupComboBox.addItem("Select Muscle Group");

        Set<String> groups = exercisesList.stream()
            .map(Exercise::getMuscleGroup)
            .filter(mg -> mg != null && !mg.isEmpty())
            .collect(Collectors.toSet());

        for (String mg : groups) muscleGroupComboBox.addItem(mg);
        System.out.println("[EXERCISE] Muscle groups: " + groups);

        for (java.awt.event.ActionListener al : muscleGroupComboBox.getActionListeners()) {
            muscleGroupComboBox.removeActionListener(al);
        }
        muscleGroupComboBox.addActionListener(e -> {
            String sel = (String) muscleGroupComboBox.getSelectedItem();
            updateExerciseDropdown(sel);
        });
    }

    private void updateExerciseDropdown(String group) {
        exerciseComboBox.removeAllItems();
        exerciseMap.clear();

        if (group == null || group.equals("Select Muscle Group")) {
            exerciseComboBox.addItem("Select Exercise");
            return;
        }

        List<Exercise> filtered = exercisesList.stream()
            .filter(ex -> ex.getMuscleGroup() != null && ex.getMuscleGroup().equals(group))
            .collect(Collectors.toList());

        System.out.println("[EXERCISE] Filtered for '" + group + "': " + filtered.size());

        if (filtered.isEmpty()) {
            exerciseComboBox.addItem("No exercises found");
            return;
        }

        exerciseComboBox.addItem("Select Exercise");
        for (Exercise ex : filtered) {
            exerciseComboBox.addItem(ex.getName());
            exerciseMap.put(ex.getName(), ex.getId());
        }
    }

    private void handleExerciseSelected() {
        String selName = (String) exerciseComboBox.getSelectedItem();
        if (selName == null || selName.equals("Select Exercise") || selName.equals("No exercises found")) return;

        UUID exerciseId = exerciseMap.get(selName);
        if (exerciseId == null) return;

        // Auto-increment set number
        List<SetEntry> existing = workoutData.get(exerciseId);
        int nextSet = (existing != null) ? existing.size() + 1 : 1;
        setNumberField.setText(String.valueOf(nextSet));

        // Pre-fill last weight
        Double lastWeight = lastWeightMap.get(exerciseId);
        if (lastWeight != null) {
            String w = (lastWeight % 1 == 0) ? String.format("%.0f", lastWeight) : String.valueOf(lastWeight);
            weightField.setText(w);
        }
    }

    private void handleCreateWorkout() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            feedbackBar.showError("Enter a workout name");
            return;
        }
        try {
            currentWorkout = workoutService.createWorkout(name);
            System.out.println("[WORKOUT] Created -> id=" + currentWorkout.getId() + ", name=" + name);
            nameField.setEnabled(false);
            createButton.setEnabled(false);
            setLogControlsEnabled(true);
            feedbackBar.showSuccess("Workout '" + name + "' created!");
        } catch (Exception ex) {
            System.err.println("[WORKOUT] Create failed: " + ex.getMessage());
            feedbackBar.showError("Error: " + ex.getMessage());
        }
    }

    private void handleAddSet() {
        String setStr = setNumberField.getText().trim();
        String repStr = repsField.getText().trim();
        String wStr = weightField.getText().trim();

        if (setStr.isEmpty() || repStr.isEmpty() || wStr.isEmpty()) {
            feedbackBar.showError("Fill in all set fields");
            return;
        }

        int setNumber; int reps; double weight;
        try {
            setNumber = Integer.parseInt(setStr);
            reps = Integer.parseInt(repStr);
            weight = Double.parseDouble(wStr);
        } catch (NumberFormatException ex) {
            feedbackBar.showError("Enter valid numbers");
            return;
        }

        if (setNumber < 1) { feedbackBar.showError("Set # must be >= 1"); return; }
        if (reps <= 0) { feedbackBar.showError("Reps must be > 0"); return; }
        if (weight < 0) { feedbackBar.showError("Weight must be >= 0"); return; }

        String selName = (String) exerciseComboBox.getSelectedItem();
        if (selName == null || selName.equals("Select Exercise") || selName.equals("No exercises found")) {
            feedbackBar.showError("Please select an exercise");
            return;
        }
        UUID exerciseId = exerciseMap.get(selName);
        if (exerciseId == null) {
            feedbackBar.showError("Invalid exercise selection");
            return;
        }

        workoutData.computeIfAbsent(exerciseId, k -> new ArrayList<>()).add(new SetEntry(setNumber, reps, weight));
        lastWeightMap.put(exerciseId, weight);

        System.out.println("[SET] Added -> exercise=" + selName + ", set=" + setNumber + ", reps=" + reps + ", weight=" + weight);

        // Auto-increment for next set
        setNumberField.setText(String.valueOf(setNumber + 1));
        repsField.setText("");
        repsField.requestFocusInWindow();

        updatePreview();
        feedbackBar.showSuccess("Set " + setNumber + " added");
    }

    private void updatePreview() {
        if (workoutData.isEmpty()) { previewArea.setText("No sets added yet."); return; }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, List<SetEntry>> entry : workoutData.entrySet()) {
            String name = getExerciseNameById(entry.getKey());
            sb.append(name).append("\n");
            for (SetEntry s : entry.getValue()) {
                String w = (s.weight % 1 == 0) ? String.format("%.0f", s.weight) : String.valueOf(s.weight);
                sb.append(String.format("  Set %d: %d reps x %skg\n", s.setNumber, s.reps, w));
            }
            sb.append("\n");
        }
        previewArea.setText(sb.toString());
    }

    private String getExerciseNameById(UUID id) {
        for (Exercise ex : exercisesList) if (ex.getId().equals(id)) return ex.getName();
        return "Unknown";
    }

    private void handleFinish() {
        if (workoutData.isEmpty()) { feedbackBar.showInfo("No sets to save"); return; }

        long now = System.currentTimeMillis();
        if (now - lastSaveTime < 2000) { feedbackBar.showInfo("Please wait..."); return; }
        if (isSaving) return;

        isSaving = true;
        lastSaveTime = now;
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        if (currentWorkout == null) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { feedbackBar.showError("Enter workout name first"); resetSaveState(); return; }
            try {
                currentWorkout = workoutService.createWorkout(name);
                System.out.println("[WORKOUT] Created (late) -> id=" + currentWorkout.getId());
            } catch (Exception ex) {
                feedbackBar.showError("Failed: " + ex.getMessage());
                resetSaveState();
                return;
            }
        }

        final UUID workoutId = currentWorkout.getId();

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    for (Map.Entry<UUID, List<SetEntry>> entry : workoutData.entrySet()) {
                        UUID exerciseId = entry.getKey();
                        if (exerciseId == null) continue;
                        for (SetEntry s : entry.getValue()) {
                            System.out.println("[LOG] Saving -> workoutId=" + workoutId + ", exerciseId=" + exerciseId + ", set=" + s.setNumber);
                            workoutService.addLog(workoutId, exerciseId, s.setNumber, s.reps, s.weight);
                        }
                    }
                    return true;
                } catch (Exception ex) {
                    System.err.println("[LOG] Save failed: " + ex.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        feedbackBar.showSuccess("Workout saved successfully!");
                        resetForm();
                    } else {
                        feedbackBar.showError("Error saving workout");
                    }
                } catch (Exception ex) {
                    feedbackBar.showError("Error: " + ex.getMessage());
                }
                resetSaveState();
            }
        }.execute();
    }

    private void resetSaveState() {
        isSaving = false;
        saveButton.setEnabled(true);
        saveButton.setText("Save / Finish Workout");
    }

    public void resetForm() {
        currentWorkout = null;
        workoutData.clear();
        nameField.setText(""); nameField.setEnabled(true);
        createButton.setEnabled(true);
        setNumberField.setText(""); repsField.setText(""); weightField.setText("");
        previewArea.setText("No sets added yet.");
        if (muscleGroupComboBox.getItemCount() > 0) muscleGroupComboBox.setSelectedIndex(0);
        setLogControlsEnabled(false);
    }

    public void resetIfNeeded() {
        if (workoutData.isEmpty() && currentWorkout == null) resetForm();
    }

    private void setLogControlsEnabled(boolean enabled) {
        muscleGroupComboBox.setEnabled(enabled);
        exerciseComboBox.setEnabled(enabled);
        setNumberField.setEnabled(enabled);
        repsField.setEnabled(enabled);
        weightField.setEnabled(enabled);
        addSetButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }

    private JLabel createLabel(String text) {
        ThemeManager tm = ThemeManager.getInstance();
        JLabel label = new JLabel(text);
        label.setFont(tm.getLabelFont());
        label.setForeground(tm.getLabelColor());
        return label;
    }

    @Override
    public void removeNotify() {
        if (statusAnimator != null) {
            statusAnimator.stop();
        }
        super.removeNotify();
    }

    private void setStatus(String text, boolean loading) {
        statusLabel.setText(text);
        if (loading) {
            startStatusPulse();
        } else {
            stopStatusPulse();
            statusLabel.setForeground(ThemeManager.getInstance().getTextMuted());
        }
    }

    private void startStatusPulse() {
        float start = statusPulse;
        float target = statusPulseForward ? 1f : 0f;
        if (statusAnimator != null) {
            statusAnimator.stop();
        }
        statusAnimator = new Animator(760, Animator.EASE_IN_OUT, eased -> {
            statusPulse = lerp(start, target, eased);
            statusLabel.setForeground(blend(
                ThemeManager.getInstance().getTextMuted(),
                ThemeManager.getInstance().getTextPrimary(),
                0.25f + (statusPulse * 0.35f)
            ));
            statusLabel.repaint();
        }, () -> {
            statusPulse = target;
            statusPulseForward = !statusPulseForward;
            if (isDisplayable() && statusLabel.getText().startsWith("Loading")) {
                startStatusPulse();
            }
        });
        statusAnimator.start();
    }

    private void stopStatusPulse() {
        if (statusAnimator != null) {
            statusAnimator.stop();
        }
        statusPulse = 0f;
        statusPulseForward = true;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static Color blend(Color a, Color b, float t) {
        int red = Math.round(lerp(a.getRed(), b.getRed(), t));
        int green = Math.round(lerp(a.getGreen(), b.getGreen(), t));
        int blue = Math.round(lerp(a.getBlue(), b.getBlue(), t));
        int alpha = Math.round(lerp(a.getAlpha(), b.getAlpha(), t));
        return new Color(red, green, blue, alpha);
    }

    public static class SetEntry {
        public int setNumber; public int reps; public double weight;
        public SetEntry(int s, int r, double w) { setNumber = s; reps = r; weight = w; }
    }
}
