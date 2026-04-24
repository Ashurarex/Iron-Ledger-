package ui;

import ui.animation.Animator;

import java.awt.AlphaComposite;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
import ui.theme.Typography;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLayeredPane contentHost;
    private TransitionOverlay transitionOverlay;
    private JPanel sidebarPanel;
    private JPanel navbarPanel;
    private JLabel navTitleLabel;
    private JLabel navUserLabel;
    private JButton themeToggleBtn;
    private final List<SidebarButton> sidebarButtons = new ArrayList<>();
    private final Map<String, JPanel> routePanels = new LinkedHashMap<>();
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
        contentPanel.setOpaque(true);
        contentPanel.setBackground(tm.getBackground());
        transitionOverlay = new TransitionOverlay();
        contentHost = new JLayeredPane() {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                contentPanel.setBounds(0, 0, width, height);
                transitionOverlay.setBounds(0, 0, width, height);
            }
        };
        contentHost.add(contentPanel, JLayeredPane.DEFAULT_LAYER);
        contentHost.add(transitionOverlay, JLayeredPane.PALETTE_LAYER);

        // Dashboard is always loaded; others lazy
        dashboardPanel = new DashboardPanel(
            () -> navigateTo("workout"),
            () -> navigateTo("history"),
            () -> navigateTo("analytics")
        );
        registerRoutePanel("dashboard", dashboardPanel);
        registerRoutePanel("workout", createLoadingPanel());
        registerRoutePanel("history", createLoadingPanel());
        registerRoutePanel("analytics", createLoadingPanel());
        registerRoutePanel("exercises", createLoadingPanel());

        root.add(contentHost, BorderLayout.CENTER);
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
        JLabel lbl = new LoadingLabel("Loading...", tm.getTitleFont());
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
        navTitleLabel.setFont(tm.getNavbarFont());
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
        themeToggleBtn.setFont(Typography.SMALL_BOLD);
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
        avatar.setFont(Typography.BODY_BOLD);
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
        String previousRoute = currentRoute;

        ensurePanelLoaded(route);

        if (transitionOverlay.isVisible()) {
            contentPanel.setVisible(true);
            transitionOverlay.clear();
        }

        // Reset workout panel when navigating away
        if ("workout".equals(currentRoute) && !"workout".equals(route) && workoutPanel != null) {
            workoutPanel.resetIfNeeded();
        }

        // Refresh data panels
        if ("history".equals(route) && historyPanel != null) historyPanel.loadWorkouts();
        if ("analytics".equals(route) && analyticsPanel != null) analyticsPanel.refresh();

        JPanel currentPanel = routePanels.get(previousRoute);
        JPanel targetPanel = routePanels.get(route);
        boolean animate = currentPanel != null
            && targetPanel != null
            && currentPanel != targetPanel
            && contentPanel.getWidth() > 0
            && contentPanel.getHeight() > 0
            && isShowing();
        BufferedImage fromImage = animate ? snapshotComponent(currentPanel) : null;

        cardLayout.show(contentPanel, route);
        contentPanel.revalidate();
        contentPanel.repaint();

        BufferedImage toImage = animate ? snapshotComponent(targetPanel) : null;
        currentRoute = route;

        updateSidebarSelection(route);

        ThemeManager.getInstance().setLastScreen(route);

        if (animate && fromImage != null && toImage != null) {
            contentPanel.setVisible(false);
            transitionOverlay.start(fromImage, toImage, resolveDirection(previousRoute, route), () -> contentPanel.setVisible(true));
        } else {
            transitionOverlay.clear();
        }
    }

    private void ensurePanelLoaded(String route) {
        switch (route) {
            case "workout":
                if (workoutPanel == null) {
                    workoutPanel = new WorkoutLoggerPanel();
                    registerRoutePanel("workout", workoutPanel);
                }
                break;
            case "history":
                if (historyPanel == null) {
                    historyPanel = new WorkoutHistoryPanel();
                    registerRoutePanel("history", historyPanel);
                }
                break;
            case "analytics":
                if (analyticsPanel == null) {
                    analyticsPanel = new AnalyticsPanel();
                    registerRoutePanel("analytics", analyticsPanel);
                }
                break;
            case "exercises":
                if (exercisePanel == null) {
                    exercisePanel = new ExerciseLibraryPanel();
                    registerRoutePanel("exercises", exercisePanel);
                }
                break;
        }
    }

    // ── Theme ───────────────────────────────────────────────────────────────

    private void handleThemeChange() {
        ThemeManager tm = ThemeManager.getInstance();

        contentPanel.setVisible(true);
        transitionOverlay.clear();

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

        navbarPanel.repaint();
        sidebarPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();
        contentHost.repaint();
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

    private void registerRoutePanel(String route, JPanel panel) {
        JPanel previous = routePanels.put(route, panel);
        if (previous != null) {
            contentPanel.remove(previous);
        }
        contentPanel.add(panel, route);
    }

    private void updateSidebarSelection(String route) {
        for (int i = 0; i < sidebarButtons.size() && i < routeKeys.length; i++) {
            sidebarButtons.get(i).setActive(routeKeys[i].equals(route));
        }
    }

    private int resolveDirection(String previousRoute, String targetRoute) {
        int previousIndex = indexOfRoute(previousRoute);
        int targetIndex = indexOfRoute(targetRoute);
        if (previousIndex < 0 || targetIndex < 0) {
            return 1;
        }
        return targetIndex >= previousIndex ? 1 : -1;
    }

    private int indexOfRoute(String route) {
        if (route == null) {
            return -1;
        }
        for (int i = 0; i < routeKeys.length; i++) {
            if (routeKeys[i].equals(route)) {
                return i;
            }
        }
        return -1;
    }

    private BufferedImage snapshotComponent(Component component) {
        if (component == null || contentPanel.getWidth() <= 0 || contentPanel.getHeight() <= 0) {
            return null;
        }

        component.setSize(contentPanel.getSize());
        component.doLayout();

        BufferedImage image = new BufferedImage(contentPanel.getWidth(), contentPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        component.paint(g2);
        g2.dispose();
        return image;
    }

    private static class LoadingLabel extends JLabel {
        private float pulseProgress = 0f;
        private boolean directionForward = true;
        private Animator pulseAnimator;

        LoadingLabel(String text, Font font) {
            super(text, SwingConstants.CENTER);
            setFont(font);
            setForeground(ThemeManager.getInstance().getTextMuted());
        }

        @Override
        public void addNotify() {
            super.addNotify();
            startPulse();
        }

        @Override
        public void removeNotify() {
            if (pulseAnimator != null) {
                pulseAnimator.stop();
            }
            super.removeNotify();
        }

        @Override
        protected void paintComponent(Graphics g) {
            ThemeManager tm = ThemeManager.getInstance();
            setForeground(blend(tm.getTextMuted(), tm.getTextPrimary(), 0.35f + (pulseProgress * 0.25f)));
            super.paintComponent(g);
        }

        private void startPulse() {
            float start = pulseProgress;
            float target = directionForward ? 1f : 0f;
            if (pulseAnimator != null) {
                pulseAnimator.stop();
            }
            pulseAnimator = new Animator(800, Animator.EASE_IN_OUT, eased -> {
                pulseProgress = lerp(start, target, eased);
                repaint(0, 0, getWidth(), getHeight());
            }, () -> {
                pulseProgress = target;
                directionForward = !directionForward;
                if (isDisplayable()) {
                    startPulse();
                }
            });
            pulseAnimator.start();
        }
    }

    private final class TransitionOverlay extends JPanel {
        private BufferedImage fromImage;
        private BufferedImage toImage;
        private float progress = 1f;
        private int direction = 1;
        private Animator animator;
        private Runnable completionCallback;

        TransitionOverlay() {
            setOpaque(false);
            setVisible(false);
        }

        void start(BufferedImage fromImage, BufferedImage toImage, int direction, Runnable completionCallback) {
            clear();
            this.fromImage = fromImage;
            this.toImage = toImage;
            this.direction = direction == 0 ? 1 : direction;
            this.completionCallback = completionCallback;
            this.progress = 0f;
            setVisible(true);

            animator = new Animator(210, Animator.EASE_OUT_CUBIC, eased -> {
                progress = eased;
                repaint(0, 0, getWidth(), getHeight());
            }, this::finishTransition);
            animator.start();
        }

        void clear() {
            if (animator != null) {
                animator.stop();
                animator = null;
            }
            fromImage = null;
            toImage = null;
            progress = 1f;
            completionCallback = null;
            if (isVisible()) {
                setVisible(false);
                repaint(0, 0, getWidth(), getHeight());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (fromImage == null || toImage == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int slideDistance = Math.max(24, getWidth() / 12);
            int outgoingX = Math.round(-direction * slideDistance * progress);
            int incomingX = Math.round(direction * slideDistance * (1f - progress));

            paintImage(g2, fromImage, outgoingX, 1f - progress);
            paintImage(g2, toImage, incomingX, Math.min(1f, 0.15f + (progress * 0.85f)));
            g2.dispose();
        }

        private void finishTransition() {
            animator = null;
            setVisible(false);
            fromImage = null;
            toImage = null;
            repaint(0, 0, getWidth(), getHeight());

            Runnable callback = completionCallback;
            completionCallback = null;
            if (callback != null) {
                callback.run();
            }
        }

        private void paintImage(Graphics2D g2, BufferedImage image, int x, float alpha) {
            if (image == null || alpha <= 0f) {
                return;
            }
            Graphics2D copy = (Graphics2D) g2.create();
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
            copy.drawImage(image, x, 0, null);
            copy.dispose();
        }
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
}
