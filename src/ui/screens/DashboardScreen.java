package ui.screens;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class DashboardScreen extends JFrame {
    public DashboardScreen() {
        super("Iron Ledger");
        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new java.awt.BorderLayout());

        JLabel titleLabel = new JLabel("Iron Ledger Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 20, 20));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(150, 350, 150, 350));

        javax.swing.JButton startWorkoutBtn = new javax.swing.JButton("Start Workout");
        startWorkoutBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        startWorkoutBtn.addActionListener(e -> {
            if (services.SessionManager.getInstance().getCurrentUser() == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "You must be logged in to access workouts.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            new WorkoutLoggerScreen().setVisible(true);
        });

        javax.swing.JButton viewHistoryBtn = new javax.swing.JButton("View History");
        viewHistoryBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        viewHistoryBtn.addActionListener(e -> {
            if (services.SessionManager.getInstance().getCurrentUser() == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "You must be logged in to access workouts.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            new WorkoutHistoryScreen().setVisible(true);
        });

        buttonPanel.add(startWorkoutBtn);
        buttonPanel.add(viewHistoryBtn);

        add(buttonPanel, java.awt.BorderLayout.CENTER);
    }
}
