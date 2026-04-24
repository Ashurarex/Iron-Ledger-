package ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import services.SessionManager;
import ui.components.CardPanel;
import ui.components.PrimaryButton;
import ui.theme.ThemeManager;

public class DashboardPanel extends JPanel {
    private final Runnable onStartWorkout;
    private final Runnable onViewHistory;
    private final Runnable onViewAnalytics;

    public DashboardPanel(Runnable onStartWorkout, Runnable onViewHistory, Runnable onViewAnalytics) {
        this.onStartWorkout = onStartWorkout;
        this.onViewHistory = onViewHistory;
        this.onViewAnalytics = onViewAnalytics;
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.getInstance();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // Welcome header
        String userName = "User";
        if (SessionManager.getInstance().getCurrentUser() != null) {
            models.User user = SessionManager.getInstance().getCurrentUser();
            userName = (user.getFullName() != null && !user.getFullName().isEmpty())
                ? user.getFullName() : user.getEmail();
        }

        JLabel welcomeLabel = new JLabel("Welcome back, " + userName + "!");
        welcomeLabel.setFont(tm.getHeaderFont());
        welcomeLabel.setForeground(tm.getTextPrimary());
        welcomeLabel.setAlignmentX(LEFT_ALIGNMENT);
        container.add(welcomeLabel);

        container.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel subtitleLabel = new JLabel("Ready to crush your workout today?");
        subtitleLabel.setFont(tm.getBodyFont());
        subtitleLabel.setForeground(tm.getTextSecondary());
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        container.add(subtitleLabel);

        container.add(Box.createRigidArea(new Dimension(0, 32)));

        // Action cards row
        JPanel cardsRow = new JPanel(new GridBagLayout());
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        cardsRow.add(createActionCard(
            "Start Workout", "Create a new workout session and log your exercises.",
            "Begin", onStartWorkout
        ), gbc);

        gbc.gridx = 1;
        cardsRow.add(createActionCard(
            "View History", "Review your past workouts and track consistency.",
            "Open", onViewHistory
        ), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        cardsRow.add(createActionCard(
            "Analytics", "See your strength progression and personal records.",
            "Explore", onViewAnalytics
        ), gbc);

        container.add(cardsRow);

        add(container, BorderLayout.CENTER);
    }

    private CardPanel createActionCard(String title, String description, String buttonText, Runnable action) {
        ThemeManager tm = ThemeManager.getInstance();
        CardPanel card = new CardPanel(new BorderLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(tm.getTitleFont());
        titleLabel.setForeground(tm.getTextPrimary());
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(titleLabel);

        content.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel descLabel = new JLabel("<html><body style='width:150px'>" + description + "</body></html>");
        descLabel.setFont(tm.getSmallFont());
        descLabel.setForeground(tm.getTextSecondary());
        descLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(descLabel);

        content.add(Box.createRigidArea(new Dimension(0, 16)));

        PrimaryButton btn = new PrimaryButton(buttonText);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> action.run());
        content.add(btn);

        card.add(content, BorderLayout.CENTER);
        return card;
    }
}
