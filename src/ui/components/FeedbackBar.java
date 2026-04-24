package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Inline feedback bar that sits at the top of a panel.
 * Shows success/error/info messages with auto-hide.
 */
public class FeedbackBar extends JPanel {
    public enum Type { SUCCESS, ERROR, INFO }

    private final JLabel messageLabel;
    private Timer hideTimer;
    private Type currentType = Type.INFO;

    public FeedbackBar() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setVisible(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(0, 44));

        messageLabel = new JLabel("", SwingConstants.LEFT);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        add(messageLabel, BorderLayout.CENTER);
    }

    public void show(String message, Type type) {
        this.currentType = type;
        messageLabel.setText(message);
        setVisible(true);
        revalidate();
        repaint();

        // Cancel any existing timer
        if (hideTimer != null && hideTimer.isRunning()) {
            hideTimer.stop();
        }

        // Auto-hide after 3 seconds
        hideTimer = new Timer(3000, e -> {
            setVisible(false);
            revalidate();
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    public void showSuccess(String message) { show(message, Type.SUCCESS); }
    public void showError(String message)   { show(message, Type.ERROR);   }
    public void showInfo(String message)     { show(message, Type.INFO);    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg;
        switch (currentType) {
            case SUCCESS: bg = new Color(34, 197, 94);  break;
            case ERROR:   bg = new Color(239, 68, 68);  break;
            default:      bg = new Color(59, 130, 246); break;
        }
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}
