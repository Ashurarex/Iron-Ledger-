package ui.screens;

import java.awt.BorderLayout;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import models.Workout;
import models.Exercise;
import services.WorkoutService;
import ui.components.CardPanel;
import ui.components.MaterialComboBox;
import ui.components.MaterialTextField;
import ui.components.PrimaryButton;
import ui.theme.ThemeManager;

public class WorkoutLoggerScreen extends JFrame {
    private final WorkoutService workoutService;
    private Workout currentWorkout;
    private List<Exercise> exercisesList;
    private Map<String, UUID> exerciseMap;
    
    private Map<UUID, List<SetEntry>> workoutData;

    private MaterialTextField nameField;
    private PrimaryButton createButton;

    private MaterialComboBox<String> muscleGroupComboBox;
    private MaterialComboBox<String> exerciseComboBox;
    private MaterialTextField setNumberField;
    private MaterialTextField repsField;
    private MaterialTextField weightField;
    private PrimaryButton addLogButton;
    private PrimaryButton finishButton;
    private JTextArea exerciseListArea;

    public WorkoutLoggerScreen() {
        super("Log Workout");
        this.workoutService = new WorkoutService();
        this.exerciseMap = new HashMap<>();
        this.workoutData = new LinkedHashMap<>();
        initialize();
    }

    private void initialize() {
        ThemeManager tm = ThemeManager.getInstance();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(550, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(tm.getBackground());
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // A. Header
        JLabel headerLabel = new JLabel("Log Workout");
        headerLabel.setFont(tm.getHeaderFont());
        headerLabel.setForeground(tm.getTextPrimary());
        headerLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // B. Workout Info Card
        CardPanel setupCard = new CardPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 2;
        nameField = new MaterialTextField("Workout Name");
        setupCard.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        createButton = new PrimaryButton("Create Workout");
        createButton.addActionListener(e -> handleCreateWorkout());
        setupCard.add(createButton, gbc);
        
        mainPanel.add(setupCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // C. Exercise Builder Card
        CardPanel builderCard = new CardPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        builderCard.add(createLabel("Muscle Group:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        muscleGroupComboBox = new MaterialComboBox<>();
        builderCard.add(muscleGroupComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        builderCard.add(createLabel("Exercise:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        exerciseComboBox = new MaterialComboBox<>();
        builderCard.add(exerciseComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 1.0; gbc.gridwidth = 2;
        setNumberField = new MaterialTextField("Set Number");
        builderCard.add(setNumberField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 1.0; gbc.gridwidth = 2;
        repsField = new MaterialTextField("Reps");
        builderCard.add(repsField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 1.0; gbc.gridwidth = 2;
        weightField = new MaterialTextField("Weight (kg)");
        builderCard.add(weightField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        addLogButton = new PrimaryButton("Add Set");
        addLogButton.addActionListener(e -> handleAddSet());
        builderCard.add(addLogButton, gbc);
        
        mainPanel.add(builderCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Load exercises
        try {
            exercisesList = workoutService.getAllExercises();
            Set<String> muscleGroups = exercisesList.stream()
                .map(Exercise::getMuscleGroup)
                .filter(mg -> mg != null && !mg.isEmpty())
                .collect(Collectors.toSet());

            muscleGroupComboBox.addItem("Select Muscle Group");
            for (String mg : muscleGroups) {
                muscleGroupComboBox.addItem(mg);
            }

            muscleGroupComboBox.addActionListener(e -> {
                String selectedGroup = (String) muscleGroupComboBox.getSelectedItem();
                updateExerciseDropdown(selectedGroup);
            });

        } catch (Exception ex) {
            exercisesList = new java.util.ArrayList<>();
            muscleGroupComboBox.addItem("Error loading data");
        }

        // D. Session Preview Card
        CardPanel previewCard = new CardPanel(new BorderLayout());
        JLabel previewLabel = createLabel("Session Preview");
        previewLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        previewCard.add(previewLabel, BorderLayout.NORTH);
        
        exerciseListArea = new JTextArea();
        exerciseListArea.setEditable(false);
        exerciseListArea.setFont(tm.getMonoFont());
        exerciseListArea.setForeground(tm.getInputText());
        exerciseListArea.setBackground(tm.getPreviewBg());
        JScrollPane scrollPane = new JScrollPane(exerciseListArea);
        scrollPane.setPreferredSize(new Dimension(450, 150));
        scrollPane.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        previewCard.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(previewCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // E. Action Buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        finishButton = new PrimaryButton("Save / Finish Workout");
        finishButton.addActionListener(e -> handleFinish());
        bottomPanel.add(finishButton);
        
        mainPanel.add(bottomPanel);

        JScrollPane mainScroll = new JScrollPane(mainPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        add(mainScroll, BorderLayout.CENTER);

        setLogControlsEnabled(false);
    }

    private JLabel createLabel(String text) {
        ThemeManager tm = ThemeManager.getInstance();
        JLabel label = new JLabel(text);
        label.setFont(tm.getLabelFont());
        label.setForeground(tm.getLabelColor());
        return label;
    }

    private void updateExerciseDropdown(String selectedGroup) {
        exerciseComboBox.removeAllItems();
        exerciseMap.clear();

        if (selectedGroup == null || selectedGroup.equals("Select Muscle Group")) {
            exerciseComboBox.addItem("Select Exercise");
            return;
        }

        List<Exercise> filtered = exercisesList.stream()
            .filter(exercise -> exercise.getMuscleGroup() != null && exercise.getMuscleGroup().equals(selectedGroup))
            .collect(Collectors.toList());

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

    private void handleCreateWorkout() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Workout name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (currentWorkout == null) {
                currentWorkout = workoutService.createWorkout(name);
            }
            nameField.setEnabled(false);
            createButton.setEnabled(false);
            setLogControlsEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddSet() {
        try {
            int setNumber = Integer.parseInt(setNumberField.getText().trim());
            int reps = Integer.parseInt(repsField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());

            if (setNumber < 1) {
                JOptionPane.showMessageDialog(this, "Set number must be >= 1", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (reps <= 0) {
                JOptionPane.showMessageDialog(this, "Reps must be > 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (weight < 0) {
                JOptionPane.showMessageDialog(this, "Weight must be >= 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedExerciseName = (String) exerciseComboBox.getSelectedItem();
            if (selectedExerciseName == null || selectedExerciseName.equals("Select Exercise") || selectedExerciseName.equals("No exercises found")) {
                JOptionPane.showMessageDialog(this, "Please select an exercise.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UUID exerciseId = exerciseMap.get(selectedExerciseName);
            if (exerciseId == null) {
                JOptionPane.showMessageDialog(this, "Invalid exercise selection.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SetEntry entry = new SetEntry(setNumber, reps, weight);
            workoutData.computeIfAbsent(exerciseId, k -> new ArrayList<>()).add(entry);

            setNumberField.setText("");
            repsField.setText("");
            weightField.setText("");
            setNumberField.requestFocusInWindow();

            updateExerciseListDisplay();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Set, Reps, and Weight.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateExerciseListDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, List<SetEntry>> entry : workoutData.entrySet()) {
            UUID exerciseId = entry.getKey();
            String exerciseName = getExerciseNameById(exerciseId);
            sb.append(exerciseName).append("\n");
            
            for (SetEntry setEntry : entry.getValue()) {
                String formattedWeight = (setEntry.weight % 1 == 0) ? String.format("%.0f", setEntry.weight) : String.valueOf(setEntry.weight);
                sb.append(String.format("Set %d: %d reps, %skg\n", setEntry.setNumber, setEntry.reps, formattedWeight));
            }
            sb.append("\n");
        }
        exerciseListArea.setText(sb.toString());
    }

    private String getExerciseNameById(UUID id) {
        if (exercisesList != null) {
            for (Exercise ex : exercisesList) {
                if (ex.getId().equals(id)) {
                    return ex.getName();
                }
            }
        }
        return "Unknown Exercise";
    }

    private void handleFinish() {
        if (workoutData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sets added to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        if (currentWorkout == null) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Workout name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                currentWorkout = workoutService.createWorkout(name);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to create workout: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            for (Map.Entry<UUID, List<SetEntry>> entry : workoutData.entrySet()) {
                UUID exerciseId = entry.getKey();
                for (SetEntry setEntry : entry.getValue()) {
                    workoutService.addLog(currentWorkout.getId(), exerciseId, setEntry.setNumber, setEntry.reps, setEntry.weight);
                }
            }
            JOptionPane.showMessageDialog(this, "Workout saved successfully!");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving logs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setLogControlsEnabled(boolean enabled) {
        muscleGroupComboBox.setEnabled(enabled);
        exerciseComboBox.setEnabled(enabled);
        setNumberField.setEnabled(enabled);
        repsField.setEnabled(enabled);
        weightField.setEnabled(enabled);
        addLogButton.setEnabled(enabled);
        finishButton.setEnabled(enabled);
    }

    public static class SetEntry {
        public int setNumber;
        public int reps;
        public double weight;

        public SetEntry(int setNumber, int reps, double weight) {
            this.setNumber = setNumber;
            this.reps = reps;
            this.weight = weight;
        }
    }
}
