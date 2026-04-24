package ui.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import ui.theme.ThemeManager;

public class CardPanel extends JPanel {

    public CardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));
    }

    public CardPanel() {
        this(new java.awt.FlowLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemeManager tm = ThemeManager.getInstance();

        // Shadow
        g2.setColor(new java.awt.Color(0, 0, 0, 12));
        g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);

        // Card background
        g2.setColor(tm.getCardBg());
        g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);

        // Border
        g2.setColor(tm.getCardBorder());
        g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
