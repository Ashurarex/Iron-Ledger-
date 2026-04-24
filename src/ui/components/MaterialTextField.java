package ui.components;

import ui.animation.Animator;
import ui.theme.ThemeManager;

import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MaterialTextField extends JPanel {
    private static final int DEFAULT_ANIM_MS = 180;

    private final JLabel floatingLabel;
    private final JTextField textField;
    private final boolean passwordField;

    private int animDurationMs = DEFAULT_ANIM_MS;
    private Animator animator;

    private float labelProgress = 0f;
    private float focusProgress = 0f;

    private float labelStart = 0f;
    private float labelTarget = 0f;
    private float focusStart = 0f;
    private float focusTarget = 0f;

    private final Runnable themeListener = () -> {
        updateColors();
        repaint();
    };

    public MaterialTextField(String label) {
        this(label, false);
    }

    public MaterialTextField(String label, boolean isPasswordField) {
        this.passwordField = isPasswordField;
        setLayout(null);
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 8, 10));
        setPreferredSize(new Dimension(260, 52));
        setMinimumSize(new Dimension(140, 52));

        floatingLabel = new JLabel(label);
        floatingLabel.setOpaque(false);
        floatingLabel.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        floatingLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                textField.requestFocusInWindow();
            }
        });
        add(floatingLabel);

        textField = isPasswordField ? new JPasswordField() : new JTextField();
        textField.setOpaque(false);
        textField.setBorder(new EmptyBorder(0, 0, 0, 0));
        textField.setColumns(10);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                animateTo(1f, 1f);
            }

            @Override
            public void focusLost(FocusEvent e) {
                animateTo(shouldFloat() ? 1f : 0f, 0f);
            }
        });
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onTextChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onTextChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onTextChanged(); }
        });
        add(textField);

        updateColors();
        syncInitialState();
    }

    private void syncInitialState() {
        float target = shouldFloat() ? 1f : 0f;
        labelProgress = target;
        focusProgress = textField.hasFocus() ? 1f : 0f;
        labelTarget = labelProgress;
        focusTarget = focusProgress;
        updateLabelStyle();
        revalidate();
        repaint();
    }

    private void onTextChanged() {
        animateTo(shouldFloat() ? 1f : 0f, textField.hasFocus() ? 1f : 0f);
    }

    private boolean shouldFloat() {
        return textField.hasFocus() || !getText().trim().isEmpty();
    }

    public void setAnimationDurationMs(int ms) {
        animDurationMs = Math.max(80, ms);
    }

    private void animateTo(float newLabelTarget, float newFocusTarget) {
        newLabelTarget = clamp01(newLabelTarget);
        newFocusTarget = clamp01(newFocusTarget);

        if (approxEquals(labelTarget, newLabelTarget) && approxEquals(focusTarget, newFocusTarget)) {
            return;
        }

        labelStart = labelProgress;
        focusStart = focusProgress;
        labelTarget = newLabelTarget;
        focusTarget = newFocusTarget;

        if (animator != null) {
            animator.stop();
        }

        animator = new Animator(animDurationMs, Animator.EASE_OUT_CUBIC, eased -> {
            labelProgress = lerp(labelStart, labelTarget, eased);
            focusProgress = lerp(focusStart, focusTarget, eased);
            updateLabelStyle();
            doLayout();
            repaint(0, 0, getWidth(), getHeight());
        }, () -> {
            labelProgress = labelTarget;
            focusProgress = focusTarget;
            updateLabelStyle();
            repaint(0, 0, getWidth(), getHeight());
        });
        animator.start();
    }

    private void updateColors() {
        ThemeManager tm = ThemeManager.getInstance();
        textField.setFont(tm.getBodyFont());
        textField.setForeground(tm.getInputText());
        textField.setCaretColor(tm.getInputText());
        floatingLabel.setFont(tm.getBodyFont());
    }

    private void updateLabelStyle() {
        ThemeManager tm = ThemeManager.getInstance();
        Font body = tm.getBodyFont();
        Font small = tm.getSmallFont();

        float startSize = body.getSize2D();
        float endSize = Math.min(startSize, small.getSize2D());
        float size = lerp(startSize, endSize, labelProgress);

        floatingLabel.setFont(body.deriveFont(size));
        Color inactive = tm.getPlaceholderText();
        Color active = tm.getPrimary();
        floatingLabel.setForeground(blend(inactive, active, labelProgress));
    }

    @Override
    public void doLayout() {
        int left = getInsets().left;
        int right = getInsets().right;
        int top = getInsets().top;
        int bottom = getInsets().bottom;

        int w = Math.max(0, getWidth() - left - right);
        int h = Math.max(0, getHeight() - top - bottom);

        int underlineY = top + h - 2;
        int fieldHeight = Math.min(26, Math.max(20, textField.getPreferredSize().height));
        int fieldY = underlineY - 6 - fieldHeight;
        int fieldX = left;

        textField.setBounds(fieldX, fieldY, w, fieldHeight);

        Dimension labelPref = floatingLabel.getPreferredSize();
        int labelUpY = top;
        int labelDownY = fieldY + 4;
        int labelY = Math.round(lerp(labelDownY, labelUpY, labelProgress));
        floatingLabel.setBounds(fieldX, labelY, Math.min(w, labelPref.width), labelPref.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ThemeManager tm = ThemeManager.getInstance();
        textField.setForeground(tm.getInputText());
        textField.setCaretColor(tm.getInputText());

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int left = getInsets().left;
        int right = getInsets().right;
        int bottom = getInsets().bottom;
        int w = getWidth() - left - right;
        int y = getHeight() - bottom;

        Color base = tm.getInputBorder();
        Color focus = tm.getInputFocusBorder();
        Color line = blend(base, focus, focusProgress);
        float thickness = lerp(1f, 2f, focusProgress);

        if (!isEnabled()) {
            line = tm.getDisabledBg();
        }

        g2.setColor(line);
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(left, y - 1, left + Math.max(0, w), y - 1);

        g2.dispose();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ThemeManager.getInstance().addListener(themeListener);
        syncInitialState();
    }

    @Override
    public void removeNotify() {
        ThemeManager.getInstance().removeListener(themeListener);
        if (animator != null) {
            animator.stop();
        }
        super.removeNotify();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
        floatingLabel.setEnabled(enabled);
        repaint();
    }

    public JTextField getTextField() {
        return textField;
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
        syncInitialState();
    }

    public void setEditable(boolean editable) {
        textField.setEditable(editable);
    }

    @Override
    public boolean requestFocusInWindow() {
        return textField.requestFocusInWindow();
    }

    @Override
    public void requestFocus() {
        textField.requestFocus();
    }

    public char[] getPassword() {
        if (!passwordField) return new char[0];
        return ((JPasswordField) textField).getPassword();
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static boolean approxEquals(float a, float b) {
        return Math.abs(a - b) < 0.001f;
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
