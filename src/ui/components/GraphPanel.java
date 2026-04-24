package ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import ui.theme.ThemeManager;

public class GraphPanel extends JPanel {
    public enum ChartType { LINE, BAR }

    private List<double[]> data; // each entry: [x, y]
    private ChartType chartType = ChartType.LINE;
    private String yLabel = "";

    public GraphPanel() {
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));
    }

    public void setData(List<double[]> data) { this.data = data; repaint(); }
    public void setChartType(ChartType type) { this.chartType = type; repaint(); }
    public void setYLabel(String label) { this.yLabel = label; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemeManager tm = ThemeManager.getInstance();

        int pad = 50;
        int w = getWidth() - pad * 2;
        int h = getHeight() - pad * 2;

        if (data == null || data.isEmpty() || w <= 0 || h <= 0) {
            g2.setColor(tm.getTextMuted());
            g2.setFont(tm.getBodyFont());
            g2.drawString("No data available", getWidth() / 2 - 50, getHeight() / 2);
            g2.dispose();
            return;
        }

        // Find bounds
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (double[] d : data) { minY = Math.min(minY, d[1]); maxY = Math.max(maxY, d[1]); }
        if (maxY == minY) { maxY = minY + 1; }

        // Draw grid lines
        g2.setColor(tm.getCardBorder());
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = pad + h - (int)(h * ((double)i / gridLines));
            g2.drawLine(pad, y, pad + w, y);
            double val = minY + (maxY - minY) * ((double)i / gridLines);
            g2.setColor(tm.getTextMuted());
            g2.setFont(tm.getSmallFont());
            g2.drawString(String.format("%.0f", val), 5, y + 4);
            g2.setColor(tm.getCardBorder());
        }

        // Y axis label
        if (!yLabel.isEmpty()) {
            g2.setColor(tm.getTextSecondary());
            g2.setFont(tm.getSmallFont());
            g2.drawString(yLabel, pad, pad - 8);
        }

        if (chartType == ChartType.LINE) {
            drawLineChart(g2, pad, w, h, minY, maxY);
        } else {
            drawBarChart(g2, pad, w, h, minY, maxY);
        }

        g2.dispose();
    }

    private void drawLineChart(Graphics2D g2, int pad, int w, int h, double minY, double maxY) {
        ThemeManager tm = ThemeManager.getInstance();
        int n = data.size();

        // Fill area under line
        int[] xPoints = new int[n + 2];
        int[] yPoints = new int[n + 2];
        for (int i = 0; i < n; i++) {
            xPoints[i] = pad + (int)(w * ((double)i / Math.max(n - 1, 1)));
            yPoints[i] = pad + h - (int)(h * ((data.get(i)[1] - minY) / (maxY - minY)));
        }
        xPoints[n] = xPoints[n - 1];
        yPoints[n] = pad + h;
        xPoints[n + 1] = xPoints[0];
        yPoints[n + 1] = pad + h;
        g2.setColor(new Color(59, 130, 246, 30));
        g2.fillPolygon(xPoints, yPoints, n + 2);

        // Draw line
        g2.setColor(tm.getPrimary());
        g2.setStroke(new java.awt.BasicStroke(2.5f));
        for (int i = 1; i < n; i++) {
            g2.drawLine(xPoints[i - 1], yPoints[i - 1], xPoints[i], yPoints[i]);
        }

        // Draw dots
        for (int i = 0; i < n; i++) {
            g2.setColor(Color.WHITE);
            g2.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
            g2.setColor(tm.getPrimary());
            g2.drawOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
        }
    }

    private void drawBarChart(Graphics2D g2, int pad, int w, int h, double minY, double maxY) {
        ThemeManager tm = ThemeManager.getInstance();
        int n = data.size();
        int barWidth = Math.max(w / (n * 2), 8);
        int gap = Math.max(barWidth / 2, 4);

        for (int i = 0; i < n; i++) {
            int x = pad + (int)(w * ((double)i / Math.max(n, 1))) + gap;
            int barH = (int)(h * ((data.get(i)[1] - Math.min(minY, 0)) / (maxY - Math.min(minY, 0))));
            int y = pad + h - barH;

            g2.setColor(new Color(59, 130, 246, 180));
            g2.fillRoundRect(x, y, barWidth, barH, 4, 4);
            g2.setColor(tm.getPrimary());
            g2.drawRoundRect(x, y, barWidth, barH, 4, 4);
        }
    }
}
