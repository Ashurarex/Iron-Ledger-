package ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import ui.animation.Animator;
import ui.theme.ThemeManager;

public class MaterialComboBox<T> extends JComboBox<T> {
    private static final int DEFAULT_ANIM_MS = 180;

    private int animDurationMs = DEFAULT_ANIM_MS;
    private Animator animator;

    private float focusProgress = 0f;
    private float focusStart = 0f;
    private float focusTarget = 0f;

    private final Runnable themeListener = () -> {
        refreshTheme();
        repaint();
    };

    public MaterialComboBox() {
        super();
        init();
    }

    public MaterialComboBox(T[] items) {
        super(items);
        init();
    }

    private void init() {
        setOpaque(false);
        setFocusable(true);
        setBorder(new EmptyBorder(10, 10, 8, 30));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new ArrowButton();
            }

            @Override
            protected BasicComboPopup createPopup() {
                BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                popup.setBorder(BorderFactory.createLineBorder(ThemeManager.getInstance().getCardBorder()));
                JList<?> list = popup.getList();
                list.setBorder(new EmptyBorder(6, 0, 6, 0));
                list.setBackground(ThemeManager.getInstance().getCardBg());
                JScrollPane scroller = (JScrollPane) popup.getComponent(0);
                scroller.setBorder(BorderFactory.createEmptyBorder());
                JViewport viewport = scroller.getViewport();
                if (viewport != null) {
                    viewport.setBackground(ThemeManager.getInstance().getCardBg());
                }
                return popup;
            }
        });

        setRenderer(new MaterialRenderer());

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                animateFocusTo(1f);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!isPopupVisible()) animateFocusTo(0f);
            }
        });

        addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { animateFocusTo(1f); }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { if (!hasFocus()) animateFocusTo(0f); }
            @Override public void popupMenuCanceled(PopupMenuEvent e) { if (!hasFocus()) animateFocusTo(0f); }
        });

        refreshTheme();
    }

    public void setAnimationDurationMs(int ms) {
        animDurationMs = Math.max(80, ms);
    }

    public void refreshTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        setForeground(tm.getInputText());
        setFont(tm.getBodyFont());
        setBackground(tm.getInputBg());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ThemeManager.getInstance().addListener(themeListener);
        focusProgress = hasFocus() ? 1f : 0f;
        focusTarget = focusProgress;
        repaint();
    }

    @Override
    public void removeNotify() {
        ThemeManager.getInstance().removeListener(themeListener);
        if (animator != null) {
            animator.stop();
        }
        super.removeNotify();
    }

    private void animateFocusTo(float newTarget) {
        newTarget = clamp01(newTarget);
        if (Math.abs(focusTarget - newTarget) < 0.001f) return;

        focusStart = focusProgress;
        focusTarget = newTarget;
        if (animator != null) {
            animator.stop();
        }

        animator = new Animator(animDurationMs, Animator.EASE_OUT_CUBIC, eased -> {
            focusProgress = lerp(focusStart, focusTarget, eased);
            repaint(0, 0, getWidth(), getHeight());
        }, () -> {
            focusProgress = focusTarget;
            repaint(0, 0, getWidth(), getHeight());
        });
        animator.start();
    }

    @Override
    protected void paintBorder(Graphics g) {
        ThemeManager tm = ThemeManager.getInstance();
        Color base = tm.getInputBorder();
        Color focus = tm.getInputFocusBorder();
        Color line = blend(base, focus, focusProgress);
        float thickness = lerp(1f, 2f, focusProgress);

        if (!isEnabled()) {
            line = tm.getDisabledBg();
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(line);
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int y = getHeight() - 1;
        g2.drawLine(0, y, getWidth(), y);
        g2.dispose();
    }

    private static final class ArrowButton extends JButton {
        ArrowButton() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder());
            setFocusable(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ThemeManager tm = ThemeManager.getInstance();

            Color arrow = tm.getTextSecondary();
            if (!isEnabled()) arrow = tm.getDisabledText();
            g2.setColor(arrow);

            int w = getWidth();
            int h = getHeight();
            int size = Math.min(10, Math.min(w, h) - 4);
            int cx = w / 2;
            int cy = h / 2 + 1;

            int[] xs = { cx - size / 2, cx + size / 2, cx };
            int[] ys = { cy - size / 4, cy - size / 4, cy + size / 3 };
            g2.fillPolygon(xs, ys, 3);
            g2.dispose();
        }
    }

    private final class MaterialRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            ThemeManager tm = ThemeManager.getInstance();
            setFont(tm.getBodyFont());
            setBorder(new EmptyBorder(8, 10, 8, 10));

            if (index == -1) {
                setOpaque(false);
                setForeground(tm.getInputText());
                return this;
            }

            setOpaque(true);
            list.setBackground(tm.getCardBg());
            list.setForeground(tm.getTextPrimary());

            if (isSelected) {
                setBackground(tm.getPrimary());
                setForeground(Color.WHITE);
            } else {
                setBackground(tm.getCardBg());
                setForeground(tm.getTextPrimary());
            }

            return this;
        }
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static Color blend(Color a, Color b, float t) {
        t = clamp01(t);
        int r = (int) Math.round(lerp(a.getRed(), b.getRed(), t));
        int g = (int) Math.round(lerp(a.getGreen(), b.getGreen(), t));
        int bl = (int) Math.round(lerp(a.getBlue(), b.getBlue(), t));
        int alpha = (int) Math.round(lerp(a.getAlpha(), b.getAlpha(), t));
        return new Color(r, g, bl, alpha);
    }
}
