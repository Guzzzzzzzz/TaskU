package ui;

import ui.components.RoundedComboWrapper;
import util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 
 * User bisa memasukkan nama matkul dan pilih warna badge.
 */
public class AddMataKuliahDialog extends JDialog {

    private JTextField namaField;
    private String selectedColorHex;
    private boolean confirmed = false;

    // Pilihan warna yang tersedia
    private static final String[][] WARNA_OPTIONS = {
        {"Crimson Rose",    "#F43F5E"},
        {"Coral",           "#F87171"},
        {"Sunset Orange",   "#FB923C"},
        {"Golden Amber",    "#F59E0B"},
        {"Lime Green",      "#84CC16"},
        {"Emerald Mint",    "#10B981"},
        {"Misty Teal",      "#2DD4BF"},
        {"Aqua Teal",       "#06B6D4"},
        {"Sky Breeze",      "#38BDF8"},
        {"Cyber Blue",      "#3B82F6"},
        {"Midnight Indigo", "#6366F1"},
        {"Electric Purple", "#A855F7"},
        {"Lavender",        "#C084FC"},
        {"Wild Fuchsia",    "#D946EF"},
        {"Hot Pink",        "#EC4899"},
        {"Cyber Slate",     "#475569"}
    };

    public AddMataKuliahDialog(Frame parent) {
        super(parent, util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Tambah Mata Kuliah" : "Add Course", true);
        
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparan untuk sudut membulat

        setSize(400, 420);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Root panel untuk menggambar background rounded dengan shadow
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Soft shadow
                for (int i = 5; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 6 * (6 - i)));
                    g2.fill(new java.awt.geom.RoundRectangle2D.Float(i, i, getWidth() - i * 2, getHeight() - i * 2, 28, 28));
                }
                // Card background
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth() - 6, getHeight() - 6, 28, 28));
                g2.dispose();
            }
        };
        rootPanel.setOpaque(false);
        rootPanel.setBorder(new EmptyBorder(0, 0, 6, 6));

        // Custom Header (Draggable + Close Button + Separator)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(28, 28, 20, 28));

        JLabel title = new JLabel(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Mata Kuliah Baru" : "New Course");
        title.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 20));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        JComponent closeBtn = createCloseButton();
        JPanel closeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeWrap.setOpaque(false);
        closeWrap.add(closeBtn);
        headerPanel.add(closeWrap, BorderLayout.EAST);

        // Dragging logic
        java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
            int pX, pY;
            @Override public void mousePressed(java.awt.event.MouseEvent e) { pX = e.getX(); pY = e.getY(); }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) { 
                setLocation(getLocation().x + e.getX() - pX, getLocation().y + e.getY() - pY); 
            }
        };
        headerPanel.addMouseListener(ma);
        headerPanel.addMouseMotionListener(ma);

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.add(headerPanel, BorderLayout.CENTER);
        headerWrapper.add(createSeparator(), BorderLayout.SOUTH);

        rootPanel.add(headerWrapper, BorderLayout.NORTH);

        // Main content (center)
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Nama Matkul
        JLabel namaLabel = new JLabel(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Nama Mata Kuliah" : "Course Name");
        namaLabel.setFont(ColorPalette.FONT_BUTTON);
        namaLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        namaLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(namaLabel);
        panel.add(Box.createVerticalStrut(4));

        namaField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        namaField.setOpaque(false);
        namaField.setBackground(ColorPalette.isDarkMode ? new Color(0x2E, 0x2E, 0x2E) : new Color(0xF4, 0xF4, 0xF5));
        namaField.setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        namaField.setCaretColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        namaField.setFont(ColorPalette.FONT_BODY);
        namaField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        namaField.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        namaField.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(namaField);
        panel.add(Box.createVerticalStrut(12));

        // Warna Badge
        JLabel warnaLabel = new JLabel(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Warna Badge" : "Badge Color");
        warnaLabel.setFont(ColorPalette.FONT_BUTTON);
        warnaLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        warnaLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(warnaLabel);
        panel.add(Box.createVerticalStrut(8));

        JPanel colorPickerRow = new JPanel(new GridLayout(0, 8, 4, 8));
        colorPickerRow.setOpaque(false);
        colorPickerRow.setAlignmentX(LEFT_ALIGNMENT);
        // Set maximum size so it doesn't stretch vertically
        colorPickerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        java.util.List<ColorCircleButton> colorButtons = new java.util.ArrayList<>();
        for (String[] option : WARNA_OPTIONS) {
            Color color = Color.decode(option[1]);
            ColorCircleButton btn = new ColorCircleButton(color, option[1]);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    for (ColorCircleButton cb : colorButtons) {
                        cb.setSelected(false);
                    }
                    btn.setSelected(true);
                    selectedColorHex = btn.getHexCode();
                }
            });
            colorButtons.add(btn);
            colorPickerRow.add(btn);
        }

        // Default: select first color
        colorButtons.get(0).setSelected(true);
        selectedColorHex = colorButtons.get(0).getHexCode();

        panel.add(colorPickerRow);
        panel.add(Box.createVerticalStrut(15));

        // Tombol
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(createSeparator(), BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(LEFT_ALIGNMENT);
        btnPanel.setBorder(new EmptyBorder(16, 28, 28, 28));

        JButton cancelBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Batal" : "Cancel") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(ColorPalette.TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        cancelBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
        cancelBtn.setOpaque(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Simpan" : "Save") {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (hovered) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        saveBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setOpaque(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setPreferredSize(new Dimension(140, 36));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Error label
        JLabel mkErrorLabel = new JLabel(" ");
        mkErrorLabel.setFont(ColorPalette.FONT_SMALL);
        mkErrorLabel.setForeground(ColorPalette.PRIORITY_HIGH);
        mkErrorLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(mkErrorLabel);
        panel.add(Box.createVerticalStrut(6));

        saveBtn.addActionListener(e -> {
            if (namaField.getText().trim().isEmpty()) {
                mkErrorLabel.setText(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Nama mata kuliah tidak boleh kosong!" : "Course name cannot be empty!");
                return;
            }
            confirmed = true;
            dispose();
        });
        btnPanel.add(saveBtn);

        bottomWrapper.add(btnPanel, BorderLayout.CENTER);
        rootPanel.add(bottomWrapper, BorderLayout.SOUTH);

        // Enter shortcut
        namaField.addActionListener(e -> saveBtn.doClick());

        contentPanel.add(panel, BorderLayout.NORTH);
        rootPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    /** Apakah user menekan Simpan? */
    public boolean isConfirmed() {
        return confirmed;
    }

    /** Ambil nama matkul yang diinput. */
    public String getNamaMatkul() {
        return namaField.getText().trim();
    }

    public String getWarnaHex() {
        return selectedColorHex;
    }


    private class ColorCircleButton extends JComponent {
        private Color color;
        private String hexCode;
        private boolean selected;

        public ColorCircleButton(Color color, String hexCode) {
            this.color = color;
            this.hexCode = hexCode;
            this.selected = false;
            setPreferredSize(new Dimension(36, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double d = 20.0; // inner dot diameter
            double cx = getWidth() / 2.0;
            double cy = getHeight() / 2.0;

            // Fill inner dot
            java.awt.geom.Ellipse2D.Double innerDot = new java.awt.geom.Ellipse2D.Double(cx - d / 2.0, cy - d / 2.0, d,
                    d);
            g2.setColor(color);
            g2.fill(innerDot);

            if (selected) {
                // Outer ring logic: use exact center to avoid AWT integer rounding offsets
                double gap = 3.0; // gap between dot and ring
                double strokeWidth = 2.0;

                // Diameter of the ring's center path
                double D = d + (gap * 2.0) + strokeWidth;
                java.awt.geom.Ellipse2D.Double outerRing = new java.awt.geom.Ellipse2D.Double(cx - D / 2.0,
                        cy - D / 2.0, D, D);

                g2.setColor(color);
                g2.setStroke(new BasicStroke((float) strokeWidth));
                g2.draw(outerRing);
            } else {
                // Default light border
                g2.setColor(new Color(228, 228, 231)); // #E4E4E7
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(innerDot);
            }

            g2.dispose();
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        public String getHexCode() {
            return hexCode;
        }
    }


    private JComponent createSeparator() {
        return new JComponent() {
            @Override
            public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 1); }
            @Override
            public Dimension getPreferredSize() { return new Dimension(100, 1); }
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ColorPalette.BORDER_LIGHT);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
    }

    private JComponent createCloseButton() {
        JButton btn = new JButton() {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int pad = 10;
                
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (hovered) {
                    g2.setColor(ColorPalette.PRIORITY_HIGH);
                } else {
                    g2.setColor(ColorPalette.TEXT_LIGHT);
                }
                
                g2.drawLine(pad, pad, w - pad, h - pad);
                g2.drawLine(w - pad, pad, pad, h - pad);
                g2.dispose();
            }
        };
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(32, 32));
        btn.addActionListener(e -> dispose());
        return btn;
    }
}
