package ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import services.AnalyticsService;
import services.SessionManager;
import services.WorkoutService;
import ui.components.CardPanel;
import ui.components.GraphPanel;
import ui.theme.ThemeManager;

public class AnalyticsPanel extends JPanel {
    private final AnalyticsService analyticsService;
    private final WorkoutService workoutService;
    private List<Exercise> exercisesList;

    private JComboBox<String> exerciseCombo;
    private GraphPanel progressChart;
    private GraphPanel volumeChart;
    private JTextArea prArea;
    private JLabel statusLabel;

    public AnalyticsPanel() {
        this.analyticsService = new AnalyticsService();
        this.workoutService = new WorkoutService();
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.getInstance();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel header = new JLabel("Analytics");
        header.setFont(tm.getHeaderFont());
        header.setForeground(tm.getTextPrimary());
        header.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(header);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        statusLabel = new JLabel("Loading analytics...");
        statusLabel.setFont(tm.getSmallFont());
        statusLabel.setForeground(tm.getTextMuted());
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Progress chart card
        CardPanel progressCard = new CardPanel(new BorderLayout());
        progressCard.setAlignmentX(LEFT_ALIGNMENT);

        JPanel progressHeader = new JPanel(new GridBagLayout());
        progressHeader.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 8, 8);

        g.gridx = 0;
        JLabel progressTitle = new JLabel("Strength Progress");
        progressTitle.setFont(tm.getTitleFont());
        progressTitle.setForeground(tm.getTextPrimary());
        progressHeader.add(progressTitle, g);

        g.gridx = 1;
        exerciseCombo = new JComboBox<>();
        exerciseCombo.addItem("Select Exercise");
        tm.styleComboBox(exerciseCombo);
        exerciseCombo.addActionListener(e -> loadProgressChart());
        progressHeader.add(exerciseCombo, g);

        progressCard.add(progressHeader, BorderLayout.NORTH);

        progressChart = new GraphPanel();
        progressChart.setChartType(GraphPanel.ChartType.LINE);
        progressChart.setYLabel("Weight (kg)");
        progressChart.setPreferredSize(new Dimension(400, 200));
        progressCard.add(progressChart, BorderLayout.CENTER);

        mainPanel.add(progressCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Volume + PRs row
        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);

        g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1.0; g.weighty = 1.0;
        g.insets = new Insets(0, 0, 0, 8);

        CardPanel volumeCard = new CardPanel(new BorderLayout());
        JLabel volTitle = new JLabel("Volume Trend");
        volTitle.setFont(tm.getTitleFont());
        volTitle.setForeground(tm.getTextPrimary());
        volTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        volumeCard.add(volTitle, BorderLayout.NORTH);

        volumeChart = new GraphPanel();
        volumeChart.setChartType(GraphPanel.ChartType.BAR);
        volumeChart.setYLabel("Volume");
        volumeChart.setPreferredSize(new Dimension(300, 200));
        volumeCard.add(volumeChart, BorderLayout.CENTER);

        g.gridx = 0;
        row2.add(volumeCard, g);

        CardPanel prCard = new CardPanel(new BorderLayout());
        g.gridx = 1; g.insets = new Insets(0, 0, 0, 0);

        JLabel prTitle = new JLabel("Personal Records");
        prTitle.setFont(tm.getTitleFont());
        prTitle.setForeground(tm.getTextPrimary());
        prTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        prCard.add(prTitle, BorderLayout.NORTH);

        prArea = new JTextArea();
        prArea.setEditable(false);
        prArea.setFont(tm.getMonoFont());
        prArea.setForeground(tm.getTextPrimary());
        prArea.setBackground(tm.getPreviewBg());
        prArea.setText("Loading...");
        JScrollPane prScroll = new JScrollPane(prArea);
        prScroll.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        prScroll.setPreferredSize(new Dimension(250, 200));
        prCard.add(prScroll, BorderLayout.CENTER);

        row2.add(prCard, g);
        mainPanel.add(row2);

        JScrollPane mainScroll = new JScrollPane(mainPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private UUID getUserId() {
        try {
            return UUID.fromString(SessionManager.getInstance().getCurrentUser().getId());
        } catch (Exception e) { return null; }
    }

    public void refresh() {
        statusLabel.setText("Loading analytics...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() { return null; }

            @Override
            protected void done() {
                try {
                    loadExerciseCombo();
                    loadVolumeChart();
                    loadPRs();
                    statusLabel.setText("Analytics loaded");
                } catch (Exception ex) {
                    System.err.println("[ANALYTICS] Refresh failed: " + ex.getMessage());
                    statusLabel.setText("Error loading analytics");
                }
            }
        }.execute();
    }

    private void loadExerciseCombo() {
        try {
            exercisesList = workoutService.getAllExercises();
            exerciseCombo.removeAllItems();
            exerciseCombo.addItem("Select Exercise");
            for (Exercise ex : exercisesList) exerciseCombo.addItem(ex.getName());
            System.out.println("[ANALYTICS] Exercises loaded: " + exercisesList.size());
        } catch (Exception ex) {
            System.err.println("[ANALYTICS] Exercise load failed: " + ex.getMessage());
            exerciseCombo.removeAllItems();
            exerciseCombo.addItem("Error loading");
        }
    }

    private void loadProgressChart() {
        UUID userId = getUserId();
        if (userId == null || exercisesList == null) return;

        int idx = exerciseCombo.getSelectedIndex();
        if (idx <= 0) { progressChart.setData(null); return; }

        UUID exerciseId = exercisesList.get(idx - 1).getId();

        new SwingWorker<List<double[]>, Void>() {
            @Override
            protected List<double[]> doInBackground() {
                return analyticsService.getExerciseProgress(userId, exerciseId);
            }

            @Override
            protected void done() {
                try { progressChart.setData(get()); }
                catch (Exception ex) { progressChart.setData(null); }
            }
        }.execute();
    }

    private void loadVolumeChart() {
        UUID userId = getUserId();
        if (userId == null) return;

        new SwingWorker<List<double[]>, Void>() {
            @Override
            protected List<double[]> doInBackground() {
                return analyticsService.getVolumeTrend(userId);
            }

            @Override
            protected void done() {
                try { volumeChart.setData(get()); }
                catch (Exception ex) { volumeChart.setData(null); }
            }
        }.execute();
    }

    private void loadPRs() {
        UUID userId = getUserId();
        if (userId == null) return;

        new SwingWorker<Map<String, Double>, Void>() {
            @Override
            protected Map<String, Double> doInBackground() {
                return analyticsService.getPRs(userId);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Double> prs = get();
                    StringBuilder sb = new StringBuilder();
                    if (prs.isEmpty()) {
                        sb.append("No records yet.\nStart logging workouts!");
                    } else {
                        for (Map.Entry<String, Double> entry : prs.entrySet()) {
                            String w = (entry.getValue() % 1 == 0)
                                ? String.format("%.0f", entry.getValue()) : String.valueOf(entry.getValue());
                            sb.append(String.format("%-20s %skg\n", entry.getKey(), w));
                        }
                    }
                    prArea.setText(sb.toString());
                } catch (Exception ex) {
                    prArea.setText("Error loading records.");
                }
            }
        }.execute();
    }
}
