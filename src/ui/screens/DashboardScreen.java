package ui.screens;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import ui.components.CardPanel;
import ui.components.PrimaryButton;
import services.SessionManager;
import ui.theme.ThemeManager;

public class DashboardScreen extends JFrame {
    public DashboardScreen() {
        super("Iron Ledger");
        initialize();
    }

    private void initialize() {
        ThemeManager tm = ThemeManager.getInstance();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setResizable(false);
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(tm.getBackground());
        setLayout(new BorderLayout());

        String userName = "User";
        if (SessionManager.getInstance().getCurrentUser() != null) {
            models.User user = SessionManager.getInstance().getCurrentUser();
            userName = user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : user.getEmail();
        }
        
        JLabel titleLabel = new JLabel("Welcome, " + userName, SwingConstants.CENTER);
        titleLabel.setFont(tm.getHeaderFont());
        titleLabel.setForeground(tm.getTextPrimary());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerArea = new JPanel(new GridBagLayout());
        centerArea.setOpaque(false);
        
        CardPanel cardPanel = new CardPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        PrimaryButton startWorkoutBtn = new PrimaryButton("Start Workout");
        startWorkoutBtn.addActionListener(e -> {
            if (SessionManager.getInstance().getCurrentUser() == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "You must be logged in to access workouts.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            new WorkoutLoggerScreen().setVisible(true);
        });

        PrimaryButton viewHistoryBtn = new PrimaryButton("View History");
        viewHistoryBtn.addActionListener(e -> {
            if (SessionManager.getInstance().getCurrentUser() == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "You must be logged in to access workouts.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            new WorkoutHistoryScreen().setVisible(true);
        });

        gbc.gridy = 0;
        cardPanel.add(startWorkoutBtn, gbc);
        
        gbc.gridy = 1;
        cardPanel.add(viewHistoryBtn, gbc);

        centerArea.add(cardPanel);
        
        add(centerArea, BorderLayout.CENTER);
    }
}
