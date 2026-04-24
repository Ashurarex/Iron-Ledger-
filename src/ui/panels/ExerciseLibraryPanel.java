package ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

import models.Exercise;
import services.ExerciseService;
import ui.components.CardPanel;
import ui.components.MaterialComboBox;
import ui.theme.ThemeManager;

public class ExerciseLibraryPanel extends JPanel {
    private final ExerciseService exerciseService;
    private List<Exercise> allExercises;
    private List<Exercise> filteredExercises;

    private MaterialComboBox<String> muscleFilter;
    private MaterialComboBox<String> equipmentFilter;
    private MaterialComboBox<String> difficultyFilter;
    private JList<Exercise> exerciseList;
    private JTextArea detailsArea;

    public ExerciseLibraryPanel() {
        this.exerciseService = new ExerciseService();
        this.allExercises = new ArrayList<>();
        this.filteredExercises = new ArrayList<>();
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
        loadData();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.getInstance();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header
        JLabel header = new JLabel("Exercise Library");
        header.setFont(tm.getHeaderFont());
        header.setForeground(tm.getTextPrimary());
        header.setAlignmentX(LEFT_ALIGNMENT);
        container.add(header);
        container.add(Box.createRigidArea(new Dimension(0, 16)));

        // Filters
        CardPanel filterCard = new CardPanel(new GridBagLayout());
        filterCard.setAlignmentX(LEFT_ALIGNMENT);
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        filterCard.add(createLabel("Muscle"), g);
        g.gridx = 1; g.weightx = 1.0;
        muscleFilter = new MaterialComboBox<>(); muscleFilter.addItem("All");
        filterCard.add(muscleFilter, g);

        g.gridx = 2; g.weightx = 0;
        filterCard.add(createLabel("Equipment"), g);
        g.gridx = 3; g.weightx = 1.0;
        equipmentFilter = new MaterialComboBox<>(); equipmentFilter.addItem("All");
        filterCard.add(equipmentFilter, g);

        g.gridx = 4; g.weightx = 0;
        filterCard.add(createLabel("Difficulty"), g);
        g.gridx = 5; g.weightx = 1.0;
        difficultyFilter = new MaterialComboBox<>(); difficultyFilter.addItem("All");
        filterCard.add(difficultyFilter, g);

        muscleFilter.addActionListener(e -> applyFilters());
        equipmentFilter.addActionListener(e -> applyFilters());
        difficultyFilter.addActionListener(e -> applyFilters());

        container.add(filterCard);
        container.add(Box.createRigidArea(new Dimension(0, 16)));

        // Content: list + details
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);
        content.setAlignmentX(LEFT_ALIGNMENT);

        // Left: Exercise list
        CardPanel listCard = new CardPanel(new BorderLayout());
        exerciseList = new JList<>();
        exerciseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        exerciseList.setFont(tm.getBodyFont());
        exerciseList.setBackground(tm.getCardBg());
        exerciseList.setForeground(tm.getTextPrimary());
        exerciseList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Exercise) {
                    Exercise ex = (Exercise) value;
                    setText(ex.getName());
                }
                if (isSelected) {
                    setBackground(tm.getPrimary());
                    setForeground(java.awt.Color.WHITE);
                } else {
                    setBackground(tm.getCardBg());
                    setForeground(tm.getTextPrimary());
                }
                setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
        exerciseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showExerciseDetails();
        });

        JScrollPane listScroll = new JScrollPane(exerciseList);
        listScroll.setPreferredSize(new Dimension(250, 0));
        listScroll.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        listCard.add(listScroll, BorderLayout.CENTER);
        content.add(listCard, BorderLayout.WEST);

        // Right: Details
        CardPanel detailCard = new CardPanel(new BorderLayout());
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(tm.getBodyFont());
        detailsArea.setForeground(tm.getTextPrimary());
        detailsArea.setBackground(tm.getPreviewBg());
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailScroll = new JScrollPane(detailsArea);
        detailScroll.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        detailCard.add(detailScroll, BorderLayout.CENTER);
        content.add(detailCard, BorderLayout.CENTER);

        container.add(content);
        add(container, BorderLayout.CENTER);
    }

    private void loadData() {
        try {
            allExercises = exerciseService.getAllExercises();
            filteredExercises = new ArrayList<>(allExercises);

            // Populate filter dropdowns
            Set<String> muscles = allExercises.stream().map(Exercise::getMuscleGroup).filter(s -> s != null && !s.isEmpty()).collect(Collectors.toSet());
            Set<String> equips = allExercises.stream().map(Exercise::getEquipment).filter(s -> s != null && !s.isEmpty()).collect(Collectors.toSet());
            Set<String> diffs = allExercises.stream().map(Exercise::getDifficulty).filter(s -> s != null && !s.isEmpty()).collect(Collectors.toSet());

            for (String s : muscles) muscleFilter.addItem(s);
            for (String s : equips) equipmentFilter.addItem(s);
            for (String s : diffs) difficultyFilter.addItem(s);

            exerciseList.setListData(filteredExercises.toArray(new Exercise[0]));
        } catch (Exception ex) {
            System.err.println("Failed to load exercises: " + ex.getMessage());
        }
    }

    private void applyFilters() {
        String muscle = getSelectedFilter(muscleFilter);
        String equip = getSelectedFilter(equipmentFilter);
        String diff = getSelectedFilter(difficultyFilter);

        filteredExercises = exerciseService.filterExercises(muscle, equip, diff);
        exerciseList.setListData(filteredExercises.toArray(new Exercise[0]));
        detailsArea.setText("");
    }

    private String getSelectedFilter(javax.swing.JComboBox<String> combo) {
        String val = (String) combo.getSelectedItem();
        return (val == null || val.equals("All")) ? null : val;
    }

    private void showExerciseDetails() {
        Exercise selected = exerciseList.getSelectedValue();
        if (selected == null) { detailsArea.setText(""); return; }

        ExerciseService.ExerciseGuidance guidance = exerciseService.getGuidance(selected.getName());
        List<Exercise> substitutions = exerciseService.getSubstitutions(selected.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(selected.getName().toUpperCase()).append(" ===\n\n");
        sb.append("Muscle Group:  ").append(nvl(selected.getMuscleGroup())).append("\n");
        sb.append("Equipment:     ").append(nvl(selected.getEquipment())).append("\n");
        sb.append("Difficulty:    ").append(nvl(selected.getDifficulty())).append("\n");

        sb.append("\n-- How to Perform --\n");
        int i = 1;
        for (String step : guidance.getSteps()) sb.append("  ").append(i++).append(". ").append(step).append("\n");

        sb.append("\n-- Tips --\n");
        for (String tip : guidance.getTips()) sb.append("  * ").append(tip).append("\n");

        sb.append("\n-- Common Mistakes --\n");
        for (String m : guidance.getMistakes()) sb.append("  x ").append(m).append("\n");

        if (!substitutions.isEmpty()) {
            sb.append("\n-- Substitutions --\n");
            for (Exercise sub : substitutions) {
                sb.append("  > ").append(sub.getName());
                if (sub.getEquipment() != null) sb.append(" (").append(sub.getEquipment()).append(")");
                sb.append("\n");
            }
        }

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }

    private String nvl(String s) { return s == null || s.isEmpty() ? "-" : s; }

    private JLabel createLabel(String text) {
        ThemeManager tm = ThemeManager.getInstance();
        JLabel l = new JLabel(text);
        l.setFont(tm.getLabelFont());
        l.setForeground(tm.getTextSecondary());
        return l;
    }
}
