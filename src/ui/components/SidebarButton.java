package ui.components;

import ui.animation.Animator;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import ui.theme.ThemeManager;

public class SidebarButton extends JButton {
    private boolean isActive = false;
    private float hoverProgress = 0f;
    private float activeProgress = 0f;
    private Animator hoverAnimator;
    private Animator activeAnimator;

    public SidebarButton(String text) {
        super(text);
        ThemeManager tm = ThemeManager.getInstance();
        setFont(tm.getSidebarFont());
        setForeground(tm.getSidebarText());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(new EmptyBorder(12, 24, 12, 24));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(LEFT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateHover(1f);
            }

            @Override
            public void mouseExited(MouseEvent e)  {
                animateHover(0f);
            }
        });
    }

    public void setActive(boolean active) {
        this.isActive = active;
        float start = activeProgress;
        stopAnimator(activeAnimator);
        activeAnimator = new Animator(200, Animator.EASE_OUT_CUBIC, eased -> {
            activeProgress = lerp(start, active ? 1f : 0f, eased);
            repaint(0, 0, getWidth(), getHeight());
        }, () -> activeProgress = active ? 1f : 0f);
        activeAnimator.start();
    }

    public boolean isActive() { return isActive; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemeManager tm = ThemeManager.getInstance();

        if (activeProgress > 0f) {
            g2.setColor(new Color(59, 130, 246, Math.round(25 * activeProgress)));
            g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 8, 8);
            setForeground(blend(tm.getSidebarText(), tm.getSidebarActive(), activeProgress));

            g2.setColor(tm.getSidebarActive());
            g2.fillRoundRect(0, 6, Math.max(1, Math.round(3 * activeProgress)), getHeight() - 12, 3, 3);
        } else if (hoverProgress > 0f) {
            g2.setColor(new Color(255, 255, 255, Math.round(10 * hoverProgress)));
            g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 8, 8);
            setForeground(blend(tm.getSidebarText(), new Color(226, 232, 240), hoverProgress));
        } else {
            setForeground(tm.getSidebarText());
        }

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void removeNotify() {
        stopAnimator(hoverAnimator);
        stopAnimator(activeAnimator);
        super.removeNotify();
    }

    private void animateHover(float target) {
        float start = hoverProgress;
        stopAnimator(hoverAnimator);
        hoverAnimator = new Animator(160, Animator.EASE_OUT_CUBIC, eased -> {
            hoverProgress = lerp(start, target, eased);
            repaint(0, 0, getWidth(), getHeight());
        }, () -> hoverProgress = target);
        hoverAnimator.start();
    }

    private void stopAnimator(Animator animator) {
        if (animator != null) {
            animator.stop();
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
