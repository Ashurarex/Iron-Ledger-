package ui.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import ui.theme.ThemeManager;

public class StyledTextField extends JTextField {

    public StyledTextField() { super(); init(); }
    public StyledTextField(String text) { super(text); init(); }

    private void init() {
        ThemeManager tm = ThemeManager.getInstance();
        setFont(tm.getBodyFont());
        setBorder(new EmptyBorder(8, 10, 8, 10));
        setOpaque(false);
        setForeground(tm.getInputText());
        setCaretColor(tm.getInputText());
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e)   { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemeManager tm = ThemeManager.getInstance();

        // Background fill
        g2.setColor(tm.getInputBg());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        g2.dispose();

        // Update text color dynamically (for theme switch)
        setForeground(tm.getInputText());
        setCaretColor(tm.getInputText());
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemeManager tm = ThemeManager.getInstance();

        if (hasFocus()) {
            // Draw focus ring (2px blue border)
            g2.setColor(tm.getInputFocusBorder());
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
        } else {
            g2.setColor(tm.getInputBorder());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        }
        g2.dispose();
    }
}
