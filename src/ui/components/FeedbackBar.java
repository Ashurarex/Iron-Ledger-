package ui.components;

import ui.animation.Animator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import ui.theme.Typography;

/**
 * Inline feedback bar that sits at the top of a panel.
 * Shows success/error/info messages with auto-hide.
 */
public class FeedbackBar extends JPanel {
    public enum Type { SUCCESS, ERROR, INFO }

    private final JLabel messageLabel;
    private Timer hideTimer;
    private Animator visibilityAnimator;
    private Type currentType = Type.INFO;
    private float visibilityProgress = 0f;

    public FeedbackBar() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setVisible(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(0, 44));

        messageLabel = new JLabel("", SwingConstants.LEFT);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(Typography.BODY_BOLD);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        add(messageLabel, BorderLayout.CENTER);
    }

    public void show(String message, Type type) {
        this.currentType = type;
        messageLabel.setText(message);
        setVisible(true);
        revalidate();
        animateVisibility(1f);

        if (hideTimer != null && hideTimer.isRunning()) {
            hideTimer.stop();
        }

        hideTimer = new Timer(3000, e -> {
            animateVisibility(0f);
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    public void showSuccess(String message) { show(message, Type.SUCCESS); }
    public void showError(String message)   { show(message, Type.ERROR);   }
    public void showInfo(String message)     { show(message, Type.INFO);    }

    @Override
    public void paint(Graphics g) {
        if (!isVisible() && visibilityProgress <= 0f) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        int offsetY = Math.round((1f - visibilityProgress) * -10f);
        g2.translate(0, offsetY);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, visibilityProgress)));
        super.paint(g2);
        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (visibilityProgress <= 0f) return;
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
    }

    @Override
    public void removeNotify() {
        if (hideTimer != null) {
            hideTimer.stop();
        }
        if (visibilityAnimator != null) {
            visibilityAnimator.stop();
        }
        super.removeNotify();
    }

    private void animateVisibility(float target) {
        float start = visibilityProgress;
        if (visibilityAnimator != null) {
            visibilityAnimator.stop();
        }
        visibilityAnimator = new Animator(180, Animator.EASE_OUT_CUBIC, eased -> {
            visibilityProgress = lerp(start, target, eased);
            repaint(0, 0, getWidth(), getHeight());
        }, () -> {
            visibilityProgress = target;
            if (target <= 0f) {
                setVisible(false);
                revalidate();
            }
        });
        visibilityAnimator.start();
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
