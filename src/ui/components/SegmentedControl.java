package ui.components;

import util.ColorPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 *  Digunakan untuk memilih opsi dari daftar (contoh: Prioritas).
 *  Tampilan: track abu-abu dengan pill putih yang bergeser.
 *
 */
public class SegmentedControl extends JPanel {

    private String[] options;
    private int selectedIndex;
    private final int arc = 14;
    private ChangeListener changeListener;

    /** Interface untuk callback ketika pilihan berubah */
    public interface ChangeListener {
        void onSelectionChanged(String selected, int index);
    }

    public SegmentedControl(String[] options, int defaultIndex) {
        this.options = options;
        this.selectedIndex = defaultIndex;
        setOpaque(false);
        setPreferredSize(new Dimension(100, 36));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int segW = getWidth() / options.length;
                int idx = e.getX() / segW;
                if (idx >= 0 && idx < options.length) {
                    selectedIndex = idx;
                    repaint();
                    if (changeListener != null) {
                        changeListener.onSelectionChanged(options[idx], idx);
                    }
                }
            }
        });
    }

    public void setChangeListener(ChangeListener listener) {
        this.changeListener = listener;
    }

    public String getSelected() {
        return options[selectedIndex];
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private Color getColorForIndex(int index) {
        switch (index) {
            case 0: return ColorPalette.PRIORITY_HIGH;    // Tinggi / High
            case 1: return ColorPalette.PRIORITY_MEDIUM;  // Sedang / Medium
            case 2: return ColorPalette.PRIORITY_LOW;     // Rendah / Low
            default: return ColorPalette.ACCENT_BLUE;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int segW = w / options.length;
        int pad = 2;

        // Track background (abu-abu terang vs abu-abu gelap)
        g2.setColor(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : new Color(245, 245, 245));
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // Track border
        g2.setColor(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_LIGHT);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        // Draw pills
        for (int i = 0; i < options.length; i++) {
            int px = i * segW + pad;
            int py = pad;
            int pw = segW - pad * 2;
            int ph = h - pad * 2;

            if (i == selectedIndex) {
                Color c = getColorForIndex(i);
                // Faded background (alpha ~15%)
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 35)); 
                g2.fillRoundRect(px, py, pw, ph, arc - 2, arc - 2);
                
                // Selected text color
                g2.setColor(c);
            } else {
                // Default text color
                g2.setColor(ColorPalette.TEXT_SECONDARY);
            }

            g2.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tx = i * segW + (segW - fm.stringWidth(options[i])) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(options[i], tx, ty);
        }

        g2.dispose();
    }
}
