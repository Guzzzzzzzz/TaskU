package ui.components;

import util.ColorPalette;

import javax.swing.*;
import java.awt.*;

/**
 *
 *  Digunakan untuk tombol sekunder: "Batal", "Tutup"
 *
 */
public class OutlineButton extends JButton {

    public OutlineButton(String text) {
        super(text);
        setFont(ColorPalette.FONT_BUTTON);
        setForeground(ColorPalette.TEXT_PRIMARY);
        setPreferredSize(new Dimension(100, 38));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(ColorPalette.BG_CARD);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        // Border
        g2.setColor(ColorPalette.BORDER_LIGHT);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

        // Text
        g2.setColor(ColorPalette.TEXT_PRIMARY);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}
