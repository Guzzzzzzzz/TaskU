package ui;

import util.ColorPalette;
import util.TranslationManager;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/**
 *
 *  Layout (BorderLayout):
 *  [Semua][Belum][Selesai]          [Tinggi][Sedang][Rendah][🔍 search]
 *  ← LEFT (WEST)                                    RIGHT (EAST) →
 */
public class FilterPanel extends JPanel {

    // Tombol filter status
    private JButton btnSemua, btnBelum, btnSelesai;
    private JButton activeStatusBtn;

    // Search field
    private JTextField searchField;

    public FilterPanel(MainFrame mainFrame) {

        setOpaque(false);
        setLayout(new BorderLayout());
        setAlignmentX(LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // LEFT group: Status filters
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftGroup.setOpaque(false);

        btnSemua = createFilterButton(TranslationManager.get("filter_semua"), true);
        btnBelum = createFilterButton(TranslationManager.get("filter_belum"), false);
        btnSelesai = createFilterButton(TranslationManager.get("filter_selesai"), false);
        activeStatusBtn = btnSemua;

        btnSemua.addActionListener(e -> { activeStatusBtn = btnSemua; setActiveStatus(); mainFrame.filterByStatus(null); });
        btnBelum.addActionListener(e -> { activeStatusBtn = btnBelum; setActiveStatus(); mainFrame.filterByStatus("BELUM"); });
        btnSelesai.addActionListener(e -> { activeStatusBtn = btnSelesai; setActiveStatus(); mainFrame.filterByStatus("SELESAI"); });

        leftGroup.add(btnSemua);
        leftGroup.add(btnBelum);
        leftGroup.add(btnSelesai);

        add(leftGroup, BorderLayout.WEST);

        // RIGHT group: Priority filters + Search
        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightGroup.setOpaque(false);

        rightGroup.add(new PrioritySegmentedControl(mainFrame));

        // Search field
        searchField = new JTextField(14) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        searchField.setFont(ColorPalette.FONT_BODY_SM);
        searchField.setForeground(ColorPalette.TEXT_LIGHT);
        searchField.setText(TranslationManager.get("placeholder_search"));
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        searchField.setCaretColor(ColorPalette.TEXT_PRIMARY); // Agar kursor caret terlihat di dark mode
        searchField.setPreferredSize(new Dimension(180, 34));
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(TranslationManager.get("placeholder_search"))) {
                    searchField.setText("");
                    searchField.setForeground(ColorPalette.TEXT_PRIMARY);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(TranslationManager.get("placeholder_search"));
                    searchField.setForeground(ColorPalette.TEXT_LIGHT);
                }
            }
        });
        searchField.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.equals(TranslationManager.get("placeholder_search"))) keyword = "";
            mainFrame.searchTasks(keyword);
        });

        rightGroup.add(searchField);

        add(rightGroup, BorderLayout.EAST);
    }

    private JButton createFilterButton(String text, boolean isActive) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (!getBackground().equals(ColorPalette.BTN_DARK) && !getBackground().equals(ColorPalette.ACCENT_BLUE)) {
                    g2.setColor(ColorPalette.BORDER_LIGHT);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                }
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(ColorPalette.FONT_BODY_SM);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(70, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isActive) {
            btn.setBackground(ColorPalette.isDarkMode ? ColorPalette.ACCENT_BLUE : ColorPalette.BTN_DARK);
            btn.setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.BG_SIDEBAR);
        } else {
            btn.setBackground(ColorPalette.BG_CARD);
            btn.setForeground(ColorPalette.TEXT_DARK);
        }

        return btn;
    }


    private class PrioritySegmentedControl extends JPanel {
        private String[] dbValues = {"TINGGI", "SEDANG", "RENDAH"};
        private Color[] colors = {
            ColorPalette.PRIORITY_HIGH,
            ColorPalette.PRIORITY_MEDIUM,
            ColorPalette.PRIORITY_LOW
        };
        private int selectedIndex = -1; // -1 means none selected (Semua)
        private MainFrame mainFrame;

        public PrioritySegmentedControl(MainFrame mainFrame) {
            this.mainFrame = mainFrame;
            setOpaque(false);
            setPreferredSize(new Dimension(210, 32));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int segW = getWidth() / dbValues.length;
                    int idx = e.getX() / segW;
                    if (idx >= 0 && idx < dbValues.length) {
                        if (selectedIndex == idx) {
                            selectedIndex = -1; // toggle off
                            mainFrame.filterByPriority(null);
                        } else {
                            selectedIndex = idx;
                            mainFrame.filterByPriority(dbValues[idx]);
                        }
                        repaint();
                    }
                }
            });
        }

        private String getOptionText(int i) {
            if (i == 0) return TranslationManager.get("filter_tinggi");
            if (i == 1) return TranslationManager.get("filter_sedang");
            return TranslationManager.get("filter_rendah");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int segW = w / dbValues.length;
            int arc = 14;
            int pad = 2;

            // Track background (Dynamic)
            g2.setColor(ColorPalette.BTN_INACTIVE);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Track border
            g2.setColor(ColorPalette.BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Draw pills and text
            g2.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12)); 
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < dbValues.length; i++) {
                int px = i * segW + pad;
                int py = pad;
                int pw = segW - pad * 2;
                int ph = h - pad * 2;
                String text = getOptionText(i);

                if (i == selectedIndex) {
                    Color c = colors[i];
                    // Faded background (alpha ~15%)
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 35)); 
                    g2.fillRoundRect(px, py, pw, ph, arc - 2, arc - 2);
                    
                    // Selected text color
                    g2.setColor(c);
                } else {
                    // Default text color
                    g2.setColor(ColorPalette.TEXT_SECONDARY);
                }

                int tx = i * segW + (segW - fm.stringWidth(text)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);
            }
            g2.dispose();
        }
    }

    /**
     * Set tombol status filter yang aktif.
     */
    private void setActiveStatus() {
        for (JButton b : new JButton[]{btnSemua, btnBelum, btnSelesai}) {
            b.setBackground(ColorPalette.BG_CARD);
            b.setForeground(ColorPalette.TEXT_DARK);
        }
        if (activeStatusBtn != null) {
            activeStatusBtn.setBackground(ColorPalette.isDarkMode ? ColorPalette.ACCENT_BLUE : ColorPalette.BTN_DARK);
            activeStatusBtn.setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.BG_SIDEBAR);
        }
        repaint();
    }

    /**
     * Memperbarui warna tombol filter saat tema di-toggle.
     */
    public void updateTheme() {
        setActiveStatus();
    }

    /**
     * Memperbarui teks filter saat bahasa diubah.
     */
    public void updateLanguage() {
        if (btnSemua != null) btnSemua.setText(TranslationManager.get("filter_semua"));
        if (btnBelum != null) btnBelum.setText(TranslationManager.get("filter_belum"));
        if (btnSelesai != null) btnSelesai.setText(TranslationManager.get("filter_selesai"));
        
        if (searchField != null) {
            String currentText = searchField.getText();
            if (currentText.equals("search") || currentText.equals("cari") || currentText.isEmpty()) {
                searchField.setText(TranslationManager.get("placeholder_search"));
            }
        }
        repaint();
    }
}
