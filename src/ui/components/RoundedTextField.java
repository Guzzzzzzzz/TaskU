package ui.components;

import util.ColorPalette;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 *
 *  Fitur:
 *  - Rounded corners (sudut membulat, arc bisa diatur)
 *  - Placeholder text (teks abu-abu di dalam field)
 *  - Focus highlight (border biru saat diklik)
 *  - Realtime repaint saat mengetik
 *
 */
public class RoundedTextField extends JTextField {

    private String placeholder;
    private boolean focused = false;
    private final int arc;

    /** Constructor dengan arc default 16 */
    public RoundedTextField(String placeholder) {
        this(placeholder, 16);
    }

    /** Constructor dengan arc custom */
    public RoundedTextField(String placeholder, int arc) {
        this.placeholder = placeholder;
        this.arc = arc;
        setOpaque(false);
        setFont(ColorPalette.FONT_BODY);
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        setPreferredSize(new Dimension(260, 40));

        // Dark mode adaptations
        setBackground(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : Color.WHITE);
        setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        setCaretColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);

        // FocusListener untuk highlight border
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });

        // Repaint saat teks diketik atau dihapus
        getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { repaint(); }
            public void removeUpdate(DocumentEvent e) { repaint(); }
            public void changedUpdate(DocumentEvent e) { repaint(); }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();

        super.paintComponent(g);

        // Placeholder text (hanya tampil jika kosong)
        if (getText().isEmpty()) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g3.setColor(ColorPalette.isDarkMode ? new Color(0x88, 0x88, 0x88) : ColorPalette.TEXT_LIGHT);
            g3.setFont(getFont());
            FontMetrics fm = g3.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g3.drawString(placeholder, getInsets().left, y);
            g3.dispose();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (focused) {
            g2.setColor(ColorPalette.ACCENT_BLUE);
            g2.setStroke(new BasicStroke(1.8f));
        } else {
            g2.setColor(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1.2f));
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
    }
}
