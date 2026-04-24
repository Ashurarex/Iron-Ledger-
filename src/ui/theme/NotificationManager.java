package ui.theme;

import ui.animation.Animator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Lightweight toast notification system.
 * Shows a small floating panel at the top-right of the parent frame.
 * Does NOT take over the screen.
 */
public class NotificationManager {
    public enum Type { SUCCESS, ERROR, INFO }

    private static JFrame parentFrame;
    private static ToastPanel currentToast;

    public static void setParentFrame(JFrame frame) {
        parentFrame = frame;
    }

    public static void show(String message, Type type) {
        SwingUtilities.invokeLater(() -> showInternal(message, type));
    }

    private static void showInternal(String message, Type type) {
        if (parentFrame == null) return;

        if (currentToast != null) {
            currentToast.dismissImmediately();
            currentToast = null;
        }

        ToastPanel toast = new ToastPanel(message, type);
        int toastW = 320;
        int toastH = 48;
        toast.setSize(toastW, toastH);

        int frameW = parentFrame.getLayeredPane().getWidth();
        int x = frameW - toastW - 20;
        int y = 64; // below navbar
        if (x < 20) x = 20;

        currentToast = toast;
        parentFrame.getLayeredPane().add(toast, javax.swing.JLayeredPane.POPUP_LAYER);
        toast.showAnimated(parentFrame.getLayeredPane(), x, y, () -> {
            if (currentToast == toast) {
                currentToast = null;
            }
        });
    }

    private static class ToastPanel extends JPanel {
        private final Type type;
        private final JLabel label;
        private float visibilityProgress = 0f;
        private int anchorX;
        private int anchorY;
        private Animator animator;
        private Timer hideTimer;

        ToastPanel(String message, Type type) {
            this.type = type;
            setLayout(new java.awt.BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            label = new JLabel(message, SwingConstants.LEFT);
            label.setForeground(Color.WHITE);
            label.setFont(Typography.BODY_BOLD);
            add(label, java.awt.BorderLayout.CENTER);
        }

        void showAnimated(JLayeredPane parent, int x, int y, Runnable onRemoved) {
            anchorX = x;
            anchorY = y;
            updateVisualState(0f, parent);

            animateTo(1f, 220, Animator.EASE_OUT_CUBIC, parent, () -> {
                hideTimer = new Timer(2800, e -> animateTo(0f, 220, Animator.EASE_IN_OUT, parent, () -> removeFromParent(parent, onRemoved)));
                hideTimer.setRepeats(false);
                hideTimer.start();
            });
        }

        void dismissImmediately() {
            if (hideTimer != null) {
                hideTimer.stop();
            }
            if (animator != null) {
                animator.stop();
            }
            if (getParent() != null) {
                Rectangle oldBounds = getBounds();
                getParent().remove(this);
                getParent().repaint(oldBounds.x, oldBounds.y, oldBounds.width, oldBounds.height);
            }
        }

        private void animateTo(float target, int durationMs, Animator.Easing easing, JLayeredPane parent, Runnable completion) {
            float start = visibilityProgress;
            if (animator != null) {
                animator.stop();
            }
            animator = new Animator(durationMs, easing, eased -> {
                float progress = lerp(start, target, eased);
                updateVisualState(progress, parent);
            }, completion);
            animator.start();
        }

        private void updateVisualState(float progress, JLayeredPane parent) {
            Rectangle oldBounds = getBounds();
            visibilityProgress = progress;
            int x = anchorX + Math.round((1f - progress) * 18f);
            int y = anchorY - Math.round((1f - progress) * 14f);
            setLocation(x, y);
            Rectangle repaintBounds = oldBounds.union(new Rectangle(x, y, getWidth(), getHeight()));
            parent.repaint(repaintBounds.x, repaintBounds.y, repaintBounds.width, repaintBounds.height);
        }

        private void removeFromParent(JLayeredPane parent, Runnable onRemoved) {
            Rectangle oldBounds = getBounds();
            parent.remove(this);
            parent.repaint(oldBounds.x, oldBounds.y, oldBounds.width, oldBounds.height);
            if (onRemoved != null) {
                onRemoved.run();
            }
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, visibilityProgress)));
            super.paint(g2);
            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 10, 10);

            Color bg;
            switch (type) {
                case SUCCESS: bg = new Color(34, 197, 94);  break;
                case ERROR:   bg = new Color(239, 68, 68);  break;
                default:      bg = new Color(59, 130, 246); break;
            }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 10, 10);
            g2.dispose();
        }

        private static float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }
    }
}
