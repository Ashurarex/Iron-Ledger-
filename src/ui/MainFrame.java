package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import services.SessionManager;
import ui.components.SidebarButton;
import ui.panels.AnalyticsPanel;
import ui.panels.DashboardPanel;
import ui.panels.ExerciseLibraryPanel;
import ui.panels.WorkoutHistoryPanel;
import ui.panels.WorkoutLoggerPanel;
import ui.theme.NotificationManager;
import ui.theme.ThemeManager;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JPanel navbarPanel;
    private JLabel navTitleLabel;
    private JLabel navUserLabel;
    private JButton themeToggleBtn;
    private final List<SidebarButton> sidebarButtons = new ArrayList<>();
    private final String[] routeKeys = {"dashboard", "workout", "history", "analytics", "exercises"};

    // Lazy-loaded panels
    private DashboardPanel dashboardPanel;
    private WorkoutLoggerPanel workoutPanel;
    private WorkoutHistoryPanel historyPanel;
    private AnalyticsPanel analyticsPanel;
    private ExerciseLibraryPanel exercisePanel;
    private String currentRoute = "";

    public MainFrame() {
        super("Iron Ledger");
        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        // Register frame for toast notifications (uses built-in layered pane)
        NotificationManager.setParentFrame(this);

        ThemeManager tm = ThemeManager.getInstance();

        // Simple BorderLayout content pane — no OverlayLayout tricks
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(tm.getBackground());

        // Navbar
        navbarPanel = createNavbar();
        root.add(navbarPanel, BorderLayout.NORTH);

        // Sidebar
        sidebarPanel = createSidebar();
        root.add(sidebarPanel, BorderLayout.WEST);

        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(tm.getBackground());

        // Dashboard is always loaded; others lazy
        dashboardPanel = new DashboardPanel(
            () -> navigateTo("workout"),
            () -> navigateTo("history"),
            () -> navigateTo("analytics")
        );
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(createLoadingPanel(), "workout");
        contentPanel.add(createLoadingPanel(), "history");
        contentPanel.add(createLoadingPanel(), "analytics");
        contentPanel.add(createLoadingPanel(), "exercises");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);

        // Theme change listener
        tm.addListener(this::handleThemeChange);

        // Navigate to saved last screen
        navigateTo(tm.getLastScreen());
    }

    private JPanel createLoadingPanel() {
        ThemeManager tm = ThemeManager.getInstance();
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(tm.getBackground());
        JLabel lbl = new JLabel("Loading...", SwingConstants.CENTER);
        lbl.setFont(tm.getTitleFont());
        lbl.setForeground(tm.getTextMuted());
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ── Navbar ──────────────────────────────────────────────────────────────

    private JPanel createNavbar() {
        ThemeManager tm = ThemeManager.getInstance();

        JPanel navbar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                ThemeManager t = ThemeManager.getInstance();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(t.getNavbarBg());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(t.getNavbarBorder());
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        navbar.setOpaque(false);
        navbar.setPreferredSize(new Dimension(0, 56));
        navbar.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // Left: App title
        navTitleLabel = new JLabel("Iron Ledger");
        navTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        navTitleLabel.setForeground(tm.getTextPrimary());
        navbar.add(navTitleLabel, BorderLayout.WEST);

        // Right: theme toggle + user info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        rightPanel.setOpaque(false);

        // Theme toggle button — wide enough for both labels
        themeToggleBtn = new JButton(tm.isDark() ? "Light Mode" : "Dark Mode") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ThemeManager t = ThemeManager.getInstance();
                if (getModel().isRollover()) {
                    g2.setColor(t.isDark() ? new Color(55, 65, 81) : new Color(229, 231, 235));
                } else {
                    g2.setColor(t.isDark() ? new Color(31, 41, 55) : new Color(243, 244, 246));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        themeToggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        themeToggleBtn.setForeground(tm.getTextPrimary());
        themeToggleBtn.setFocusPainted(false);
        themeToggleBtn.setBorderPainted(false);
        themeToggleBtn.setContentAreaFilled(false);
        themeToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeToggleBtn.setPreferredSize(new Dimension(100, 32));
        themeToggleBtn.addActionListener(e -> ThemeManager.getInstance().toggleTheme());
        rightPanel.add(themeToggleBtn);

        // User avatar circle
        String userName = getUserName();
        String initials = userName.length() > 0 ? userName.substring(0, 1).toUpperCase() : "U";
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getInstance().getPrimary());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setPreferredSize(new Dimension(36, 36));
        rightPanel.add(avatar);

        navUserLabel = new JLabel(userName);
        navUserLabel.setFont(tm.getBodyFont());
        navUserLabel.setForeground(tm.getTextPrimary());
        rightPanel.add(navUserLabel);

        navbar.add(rightPanel, BorderLayout.EAST);
        return navbar;
    }

    // ── Sidebar ─────────────────────────────────────────────────────────────

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.getInstance().getSidebarBg());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        String[] labels = {"Dashboard", "Start Workout", "History", "Analytics", "Exercises"};
        for (int i = 0; i < labels.length; i++) {
            final String route = routeKeys[i];
            SidebarButton btn = new SidebarButton(labels[i]);
            btn.addActionListener(e -> navigateTo(route));
            sidebarButtons.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public void navigateTo(String route) {
        if (route == null) route = "dashboard";

        ensurePanelLoaded(route);

        // Reset workout panel when navigating away
        if ("workout".equals(currentRoute) && !"workout".equals(route) && workoutPanel != null) {
            workoutPanel.resetIfNeeded();
        }

        // Refresh data panels
        if ("history".equals(route) && historyPanel != null) historyPanel.loadWorkouts();
        if ("analytics".equals(route) && analyticsPanel != null) analyticsPanel.refresh();

        currentRoute = route;
        cardLayout.show(contentPanel, route);

        for (int i = 0; i < sidebarButtons.size() && i < routeKeys.length; i++) {
            sidebarButtons.get(i).setActive(routeKeys[i].equals(route));
        }

        ThemeManager.getInstance().setLastScreen(route);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void ensurePanelLoaded(String route) {
        switch (route) {
            case "workout":
                if (workoutPanel == null) {
                    workoutPanel = new WorkoutLoggerPanel();
                    contentPanel.add(workoutPanel, "workout");
                }
                break;
            case "history":
                if (historyPanel == null) {
                    historyPanel = new WorkoutHistoryPanel();
                    contentPanel.add(historyPanel, "history");
                }
                break;
            case "analytics":
                if (analyticsPanel == null) {
                    analyticsPanel = new AnalyticsPanel();
                    contentPanel.add(analyticsPanel, "analytics");
                }
                break;
            case "exercises":
                if (exercisePanel == null) {
                    exercisePanel = new ExerciseLibraryPanel();
                    contentPanel.add(exercisePanel, "exercises");
                }
                break;
        }
    }

    // ── Theme ───────────────────────────────────────────────────────────────

    private void handleThemeChange() {
        ThemeManager tm = ThemeManager.getInstance();

        // Update navbar elements
        navTitleLabel.setForeground(tm.getTextPrimary());
        navUserLabel.setForeground(tm.getTextPrimary());
        themeToggleBtn.setText(tm.isDark() ? "Light Mode" : "Dark Mode");
        themeToggleBtn.setForeground(tm.getTextPrimary());

        // Update content panel background
        contentPanel.setBackground(tm.getBackground());
        getContentPane().setBackground(tm.getBackground());

        // Recursively apply theme colors to ALL components
        tm.applyThemeToTree(contentPanel);

        // Force full repaint of everything
        navbarPanel.repaint();
        sidebarPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();
        repaint();
    }

    private String getUserName() {
        String userName = "User";
        try {
            if (SessionManager.getInstance().getCurrentUser() != null) {
                models.User user = SessionManager.getInstance().getCurrentUser();
                userName = (user.getFullName() != null && !user.getFullName().isEmpty())
                    ? user.getFullName() : user.getEmail();
            }
        } catch (Exception e) {
            System.err.println("[MAIN] Failed to get user name: " + e.getMessage());
        }
        return userName;
    }
}
