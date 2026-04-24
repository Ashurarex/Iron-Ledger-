package ui.components;

import ui.animation.Animator;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import ui.theme.ThemeManager;

public class PrimaryButton extends JButton {
    private float hoverProgress = 0f;
    private float pressProgress = 0f;
    private float rippleProgress = 1f;
    private int rippleX = 0;
    private int rippleY = 0;
    private Animator hoverAnimator;
    private Animator pressAnimator;
    private Animator rippleAnimator;

    public PrimaryButton(String text) {
        super(text);
        setForeground(Color.WHITE);
        setFont(ThemeManager.getInstance().getButtonFont());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(new EmptyBorder(10, 20, 10, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateHover(1f);
            }

            @Override
            public void mouseExited(MouseEvent e)  {
                animateHover(0f);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;
                rippleX = e.getX();
                rippleY = e.getY();
                animatePress(1f, 90);
                animateRipple();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                animatePress(0f, 140);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemeManager tm = ThemeManager.getInstance();

        double scale = 1.0 - (0.03 * pressProgress);
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;
        g2.translate(centerX, centerY);
        g2.scale(scale, scale);
        g2.translate(-centerX, -centerY);

        if (!isEnabled()) {
            setForeground(tm.getDisabledText());
            g2.setColor(tm.getDisabledBg());
        } else {
            Color base = blend(tm.getPrimary(), tm.getPrimaryHover(), hoverProgress);
            g2.setColor(blend(base, tm.getPrimaryPressed(), pressProgress));
            setForeground(Color.WHITE);
        }

        int arc = 14;
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        if (rippleProgress < 1f) {
            Shape oldClip = g2.getClip();
            g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            float alpha = Math.max(0f, (1f - rippleProgress) * 0.22f);
            g2.setColor(new Color(255, 255, 255, Math.min(255, Math.max(0, Math.round(alpha * 255f)))));
            int maxRadius = Math.max(getWidth(), getHeight()) + 24;
            int d = Math.round(maxRadius * rippleProgress);
            g2.fillOval(rippleX - d / 2, rippleY - d / 2, d, d);
            g2.setClip(oldClip);
        }

        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    public void removeNotify() {
        stopAnimator(hoverAnimator);
        stopAnimator(pressAnimator);
        stopAnimator(rippleAnimator);
        super.removeNotify();
    }

    private void animateHover(float target) {
        float start = hoverProgress;
        stopAnimator(hoverAnimator);
        hoverAnimator = new Animator(180, Animator.EASE_OUT_CUBIC, eased -> {
            hoverProgress = lerp(start, target, eased);
            repaint(0, 0, getWidth(), getHeight());
        }, () -> hoverProgress = target);
        hoverAnimator.start();
    }

    private void animatePress(float target, int durationMs) {
        float start = pressProgress;
        stopAnimator(pressAnimator);
        pressAnimator = new Animator(durationMs, Animator.EASE_IN_OUT, eased -> {
            pressProgress = lerp(start, target, eased);
            repaint(0, 0, getWidth(), getHeight());
        }, () -> pressProgress = target);
        pressAnimator.start();
    }

    private void animateRipple() {
        rippleProgress = 0f;
        stopAnimator(rippleAnimator);
        rippleAnimator = new Animator(320, Animator.EASE_OUT_CUBIC, eased -> {
            rippleProgress = eased;
            repaint(0, 0, getWidth(), getHeight());
        }, () -> rippleProgress = 1f);
        rippleAnimator.start();
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
