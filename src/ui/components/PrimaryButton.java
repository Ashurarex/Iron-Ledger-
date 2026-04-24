package ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import ui.theme.ThemeManager;

public class PrimaryButton extends JButton {
    private boolean isHovered = false;

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
            @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemeManager tm = ThemeManager.getInstance();

        if (!isEnabled()) {
            g2.setColor(tm.getDisabledBg());
            setForeground(tm.getDisabledText());
        } else if (getModel().isPressed()) {
            g2.setColor(tm.getPrimaryPressed());
            setForeground(Color.WHITE);
        } else if (isHovered) {
            g2.setColor(tm.getPrimaryHover());
            setForeground(Color.WHITE);
        } else {
            g2.setColor(tm.getPrimary());
            setForeground(Color.WHITE);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}
