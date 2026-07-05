package ui.components;

import util.ColorPalette;

import javax.swing.*;
import java.awt.*;

/**
 *
 *  Digunakan untuk tombol aksi utama: "Masuk", "Daftar", "Simpan"
 *
 */
public class GradientButton extends JButton {

    public GradientButton(String text) {
        super(text);
        setFont(ColorPalette.FONT_BUTTON);
        setForeground(Color.WHITE);
        setPreferredSize(new Dimension(120, 38));
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

        GradientPaint gp = new GradientPaint(
            0, 0, ColorPalette.ACCENT_BLUE,
            getWidth(), 0, ColorPalette.ACCENT_CYAN
        );
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        // Text
        g2.setColor(Color.WHITE);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}
