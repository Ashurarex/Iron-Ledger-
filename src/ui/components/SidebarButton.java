package ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
    private boolean isHovered = false;

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
            @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
        });
    }

    public void setActive(boolean active) {
        this.isActive = active;
        repaint();
    }

    public boolean isActive() { return isActive; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemeManager tm = ThemeManager.getInstance();

        if (isActive) {
            g2.setColor(new Color(59, 130, 246, 25));
            g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 8, 8);
            setForeground(tm.getSidebarActive());

            // Active indicator bar
            g2.setColor(tm.getSidebarActive());
            g2.fillRoundRect(0, 6, 3, getHeight() - 12, 3, 3);
        } else if (isHovered) {
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 8, 8);
            setForeground(new Color(226, 232, 240));
        } else {
            setForeground(tm.getSidebarText());
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
