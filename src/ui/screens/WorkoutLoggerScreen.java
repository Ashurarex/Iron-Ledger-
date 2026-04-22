package ui.screens;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import models.Workout;
import services.WorkoutService;

public class WorkoutLoggerScreen extends JFrame {
    private final WorkoutService workoutService;
    private Workout currentWorkout;
    private java.util.List<models.Exercise> exercisesList;

    private JTextField nameField;
    private JButton createButton;

    private JComboBox<String> exerciseComboBox;
    private JTextField setNumberField;
    private JTextField repsField;
    private JTextField weightField;
    private JButton addLogButton;
    private JButton finishButton;

    public WorkoutLoggerScreen() {
        super("Log Workout");
        this.workoutService = new WorkoutService();
        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(new JLabel("Workout Name:"));
        nameField = new JTextField();
        topPanel.add(nameField);

        createButton = new JButton("Create Workout");
        createButton.addActionListener(e -> handleCreateWorkout());
        topPanel.add(new JLabel());
        topPanel.add(createButton);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        centerPanel.add(new JLabel("Exercise:"));
        try {
            exercisesList = workoutService.getAllExercises();
            String[] exerciseNames = new String[exercisesList.size()];
            for (int i = 0; i < exercisesList.size(); i++) {
                exerciseNames[i] = exercisesList.get(i).getName();
            }
            exerciseComboBox = new JComboBox<>(exerciseNames);
        } catch (Exception ex) {
            exercisesList = new java.util.ArrayList<>();
            exerciseComboBox = new JComboBox<>(new String[]{"Error loading exercises"});
        }
        centerPanel.add(exerciseComboBox);

        centerPanel.add(new JLabel("Set Number:"));
        setNumberField = new JTextField();
        centerPanel.add(setNumberField);

        centerPanel.add(new JLabel("Reps:"));
        repsField = new JTextField();
        centerPanel.add(repsField);

        centerPanel.add(new JLabel("Weight (kg/lbs):"));
        weightField = new JTextField();
        centerPanel.add(weightField);

        addLogButton = new JButton("Add Log");
        addLogButton.addActionListener(e -> handleAddLog());
        centerPanel.add(new JLabel());
        centerPanel.add(addLogButton);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        finishButton = new JButton("Save / Finish Workout");
        finishButton.addActionListener(e -> handleFinish());
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setLogControlsEnabled(false);
    }

    private void handleCreateWorkout() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Workout name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            currentWorkout = workoutService.createWorkout(name);
            JOptionPane.showMessageDialog(this, "Workout created successfully.");
            nameField.setEnabled(false);
            createButton.setEnabled(false);
            setLogControlsEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddLog() {
        if (currentWorkout == null) {
            return;
        }

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

            if (exercisesList == null || exercisesList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No exercises available.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int selectedIndex = exerciseComboBox.getSelectedIndex();
            if (selectedIndex < 0) {
                return;
            }
            UUID exerciseId = exercisesList.get(selectedIndex).getId();

            System.out.println("[DEBUG] Saving log: WorkoutId=" + currentWorkout.getId() + ", ExerciseId=" + exerciseId);

            workoutService.addLog(currentWorkout.getId(), exerciseId, setNumber, reps, weight);
            JOptionPane.showMessageDialog(this, "Log added successfully.");

            setNumberField.setText("");
            repsField.setText("");
            weightField.setText("");

            setNumberField.requestFocusInWindow();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Set, Reps, and Weight.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleFinish() {
        dispose();
    }

    private void setLogControlsEnabled(boolean enabled) {
        exerciseComboBox.setEnabled(enabled);
        setNumberField.setEnabled(enabled);
        repsField.setEnabled(enabled);
        weightField.setEnabled(enabled);
        addLogButton.setEnabled(enabled);
        finishButton.setEnabled(enabled);
    }
}
