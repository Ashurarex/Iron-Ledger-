package ui.theme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
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
    private static JPanel currentToast;

    public static void setParentFrame(JFrame frame) {
        parentFrame = frame;
    }

    public static void show(String message, Type type) {
        SwingUtilities.invokeLater(() -> showInternal(message, type));
    }

    private static void showInternal(String message, Type type) {
        if (parentFrame == null) return;

        // Remove existing toast if present
        if (currentToast != null) {
            parentFrame.getLayeredPane().remove(currentToast);
            currentToast = null;
        }

        ToastPanel toast = new ToastPanel(message, type);
        int toastW = 320;
        int toastH = 48;
        toast.setSize(toastW, toastH);

        // Position at top-right of the frame's content area
        int frameW = parentFrame.getLayeredPane().getWidth();
        int x = frameW - toastW - 20;
        int y = 64; // below navbar
        if (x < 20) x = 20;
        toast.setLocation(x, y);

        currentToast = toast;
        parentFrame.getLayeredPane().add(toast, javax.swing.JLayeredPane.POPUP_LAYER);
        parentFrame.getLayeredPane().repaint();

        // Auto-hide after 3 seconds
        Timer timer = new Timer(3000, e -> {
            if (currentToast == toast) {
                parentFrame.getLayeredPane().remove(toast);
                parentFrame.getLayeredPane().repaint();
                currentToast = null;
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private static class ToastPanel extends JPanel {
        private final Type type;

        ToastPanel(String message, Type type) {
            this.type = type;
            setLayout(new java.awt.BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            JLabel label = new JLabel(message, SwingConstants.LEFT);
            label.setForeground(Color.WHITE);
            label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            add(label, java.awt.BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 10, 10);

            // Background
            Color bg;
            switch (type) {
                case SUCCESS: bg = new Color(34, 197, 94);  break;
                case ERROR:   bg = new Color(239, 68, 68);  break;
                default:      bg = new Color(59, 130, 246); break;
            }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
