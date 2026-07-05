package ui.components;

import java.awt.*;
import javax.swing.*;

/**
 * RoundedPanel — Reusable rounded-corner panel with optional border.
 * Uses Graphics2D.fillRoundRect for anti-aliased corners.
 */
public class RoundedPanel extends JPanel {

    private Color bgColor;
    private Color borderColor;
    private int radius;
    private int borderWidth;

    public RoundedPanel(int radius) {
        this(radius, Color.WHITE, null, 0);
    }

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, null, 0);
    }

    public RoundedPanel(int radius, Color bgColor, Color borderColor) {
        this(radius, bgColor, borderColor, 1);
    }

    public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderWidth) {
        this.radius = radius;
        this.bgColor = bgColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        setOpaque(false);
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        // Border
        if (borderColor != null && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }

        g2.dispose();
    }
}
