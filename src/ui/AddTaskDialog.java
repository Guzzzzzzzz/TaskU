package ui;

import model.AnggotaKelompok;
import model.MataKuliah;
import model.Task.Jenis;
import model.Task.Prioritas;
import service.TaskService;
import ui.components.RoundedTextField;
import ui.components.GradientButton;
import ui.components.OutlineButton;
import ui.components.RoundedComboWrapper;
import ui.components.SegmentedControl;
import util.ColorPalette;
import util.TranslationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 *  Layout vertikal modern:
 *  - Judul tugas (rounded + placeholder)
 *  - Mata kuliah (rounded combo + tombol "+ Baru")
 *  - Prioritas (Segmented Control: Rendah / Sedang / Tinggi)
 *  - Deadline (rounded + calendar icon) — full width
 *  - Jenis (radio: Individu / Kelompok)
 *  - Panel anggota kelompok (muncul jika Kelompok)
 * 
 *              Polymorphism (override paintComponent), Event Handling
 */
public class AddTaskDialog extends JDialog {

    private TaskService taskService;
    private boolean confirmed = false;

    // Form fields
    private JTextField judulField;
    private JComboBox<String> matkulCombo;
    String selectedPrioritas = "Sedang"; // diset oleh SegmentedControl
    private JTextField deadlineField;
    private JTextField nomorKelompokField;

    // Jenis tugas — card toggle state
    private String selectedJenis = "INDIVIDU"; // default Individu
    private JPanel individuCard;
    private JPanel kelompokCard;
    private boolean individuSelected = true;
    private boolean kelompokSelected = false;

    // Anggota kelompok
    private JLabel nomorKelompokLabel;
    private JPanel nomorKelompokWrapper;
    private JPanel anggotaSection;  // entire section: header + list
    private JPanel anggotaListPanel; // actual list of member rows
    private List<JTextField> anggotaFields = new ArrayList<>();

    // Data
    private List<MataKuliah> matkulList;

    // Inline Mata Kuliah Form components
    private JPanel inlineMkPanel;
    private JTextField inlineMkNamaField;
    private String inlineMkSelectedColorHex;
    private JLabel inlineMkErrorLabel;
    private List<ColorCircleButton> inlineColorButtons;
    private JLabel previewBadge;

    // Pilihan warna yang tersedia
    private static final String[][] WARNA_OPTIONS = {
        {"Soft Red",            "#F28B82"},
        {"Soft Coral",          "#FFAB91"},
        {"Soft Orange",         "#FFCC80"},
        {"Soft Amber",          "#FFE082"},
        {"Soft Lime Green",     "#C5E1A5"},
        {"Soft Mint Green",     "#A5D6A7"},
        {"Soft Teal/Cyan",      "#80CBC4"},
        {"Soft Cyan Blue",      "#80DEEA"},
        {"Soft Sky Blue",       "#90CAF9"},
        {"Soft Cornflower Blue","#9FA8DA"},
        {"Soft Indigo/Blurple", "#B39DDB"},
        {"Soft Purple/Violet",  "#CE93D8"},
        {"Soft Lavender",       "#E1BEE7"},
        {"Soft Pink/Orchid",    "#F48FB1"},
        {"Soft Rose Pink",      "#FF8A80"}
    };

    // Format tanggal Indonesia
    private static final DateTimeFormatter FORMAT_ID = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    private SegmentedControl prioControl;

    public AddTaskDialog(Frame parent, TaskService taskService) {
        super(parent, util.TranslationManager.get("dialog_tambah_tugas"), true);
        this.taskService = taskService;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparan untuk sudut membulat

        setSize(400, 540);
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
                Color bg = ColorPalette.isDarkMode ? new Color(0x18, 0x18, 0x18) : Color.WHITE;
                g2.setColor(bg);
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

        JLabel title = new JLabel(util.TranslationManager.get("dialog_tambah_tugas"));
        title.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 20));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        JComponent closeBtn = createCloseButton();
        JPanel closeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeWrap.setOpaque(false);
        closeWrap.add(closeBtn);
        headerPanel.add(closeWrap, BorderLayout.EAST);

        // Dragging logic
        MouseAdapter ma = new MouseAdapter() {
            int pX, pY;
            @Override public void mousePressed(MouseEvent e) { pX = e.getX(); pY = e.getY(); }
            @Override public void mouseDragged(MouseEvent e) { 
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

        // Main panel (scrollable)
        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Judul Tugas
        main.add(createLabel(util.TranslationManager.get("label_judul_tugas")));
        main.add(Box.createVerticalStrut(6));
        judulField = new RoundedTextField(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Contoh: Tugas Makalah" : "e.g., Paper Assignment", 16);
        judulField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        judulField.setAlignmentX(LEFT_ALIGNMENT);
        main.add(judulField);
        main.add(Box.createVerticalStrut(16));

        // Mata Kuliah
        main.add(createLabel(util.TranslationManager.get("label_pilih_matkul")));
        main.add(Box.createVerticalStrut(6));

        JPanel mkRow = new JPanel(new BorderLayout(8, 0));
        mkRow.setOpaque(false);
        mkRow.setAlignmentX(LEFT_ALIGNMENT);
        mkRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        matkulCombo = new JComboBox<>();
        matkulCombo.setFont(ColorPalette.FONT_BODY);
        matkulCombo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        refreshMatkulDropdown();

        JPanel matkulWrapper = RoundedComboWrapper.wrap(matkulCombo);
        
        // Custom renderer to add a Squircle color indicator next to subject names
        matkulCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setFont(ColorPalette.FONT_BODY);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                
                if (isSelected) {
                    lbl.setBackground(ColorPalette.isDarkMode ? new Color(0x1E, 0x29, 0x3B) : new Color(235, 242, 255));
                    lbl.setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.ACCENT_BLUE);
                } else {
                    lbl.setBackground(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : Color.WHITE);
                    lbl.setForeground(ColorPalette.TEXT_PRIMARY);
                }
                lbl.setOpaque(true);
                
                String itemText = (value != null) ? value.toString() : "";
                Color badgeColor = null;
                
                if (matkulList != null && !itemText.equals("Pilih Mata Kuliah") && !itemText.isEmpty()) {
                    for (MataKuliah mk : matkulList) {
                        if (mk.getNama().equals(itemText)) {
                            badgeColor = mk.getWarnaColor();
                            break;
                        }
                    }
                }
                
                if (badgeColor != null) {
                    final Color color = badgeColor;
                    lbl.setIcon(new Icon() {
                        @Override
                        public void paintIcon(Component c, Graphics g, int x, int y) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(color);
                            g2.fillRoundRect(x, y, 10, 10, 3, 3);
                            g2.dispose();
                        }
                        @Override
                        public int getIconWidth() { return 10; }
                        @Override
                        public int getIconHeight() { return 10; }
                    });
                    lbl.setIconTextGap(8);
                } else {
                    lbl.setIcon(null);
                }
                return lbl;
            }
        });

        mkRow.add(matkulWrapper, BorderLayout.CENTER);

        // Tombol + Baru (capsule)
        JButton addMkBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "+ Baru" : "+ New") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        addMkBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 11));
        addMkBtn.setBackground(ColorPalette.ACCENT_BLUE);
        addMkBtn.setForeground(Color.WHITE);
        addMkBtn.setOpaque(false);
        addMkBtn.setContentAreaFilled(false);
        addMkBtn.setBorderPainted(false);
        addMkBtn.setFocusPainted(false);
        addMkBtn.setPreferredSize(new Dimension(80, 36));
        addMkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMkBtn.addActionListener(e -> {
            toggleInlineForm(true);
        });
        mkRow.add(addMkBtn, BorderLayout.EAST);

        main.add(mkRow);
        main.add(Box.createVerticalStrut(16));

        // Inline Mata Kuliah Form Panel
        inlineMkPanel = createInlineMataKuliahPanel();
        main.add(inlineMkPanel);

        // Prioritas (Segmented Control)
        main.add(createLabel(util.TranslationManager.get("label_prioritas")));
        main.add(Box.createVerticalStrut(6));
        prioControl = new SegmentedControl(new String[]{
            util.TranslationManager.get("filter_tinggi"),
            util.TranslationManager.get("filter_sedang"),
            util.TranslationManager.get("filter_rendah")
        }, 1);
        prioControl.setChangeListener((selected, idx) -> selectedPrioritas = selected);
        prioControl.setAlignmentX(LEFT_ALIGNMENT);
        main.add(prioControl);
        main.add(Box.createVerticalStrut(16));

        // Deadline (full width, rounded + calendar icon)
        main.add(createLabel(util.TranslationManager.get("label_deadline")));
        main.add(Box.createVerticalStrut(6));

        // Wadah utama rounded — menggambar border sendiri
        JPanel deadWrapper = new JPanel(new BorderLayout()) {
            private boolean focused = false;
            {
                setOpaque(false);
                setPreferredSize(new Dimension(100, 38));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                setAlignmentX(LEFT_ALIGNMENT);
            }
            @SuppressWarnings("unused")
            public void setFocused(boolean f) { this.focused = f; repaint(); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background putih vs gelap rounded
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : Color.WHITE);
                g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                // Border
                if (focused) {
                    g2.setColor(ColorPalette.ACCENT_BLUE);
                    g2.setStroke(new BasicStroke(1.6f));
                } else {
                    g2.setColor(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_LIGHT);
                    g2.setStroke(new BasicStroke(1.2f));
                }
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                g2.dispose();
            }
        };

        // TextField polos dengan placeholder custom
        deadlineField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(ColorPalette.isDarkMode ? new Color(0x88, 0x88, 0x88) : ColorPalette.TEXT_LIGHT);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString("DD-MM-YYYY", getInsets().left, y);
                    g2.dispose();
                }
            }
        };
        deadlineField.setOpaque(false);
        deadlineField.setFont(ColorPalette.FONT_BODY);
        deadlineField.setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        deadlineField.setCaretColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        deadlineField.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 4));
        deadlineField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                // Akses method setFocused via reflection-free approach
                ((JPanel) deadlineField.getParent()).repaint();
                try {
                    deadWrapper.getClass().getMethod("setFocused", boolean.class).invoke(deadWrapper, true);
                } catch (Exception ignored) {}
            }
            @Override public void focusLost(FocusEvent e) {
                try {
                    deadWrapper.getClass().getMethod("setFocused", boolean.class).invoke(deadWrapper, false);
                } catch (Exception ignored) {}
            }
        });

        ((javax.swing.text.AbstractDocument) deadlineField.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            private boolean formatting = false;

            @Override
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                if (formatting) {
                    super.insertString(fb, offset, string, attr);
                    return;
                }
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String proposedText = currentText.substring(0, offset) + string + currentText.substring(offset);
                applyFormat(fb, proposedText);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                if (formatting) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String proposedText = currentText.substring(0, offset) + (text == null ? "" : text) + currentText.substring(offset + length);
                boolean isDeletion = (length > 0 && (text == null || text.isEmpty()));
                if (isDeletion) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    applyFormat(fb, proposedText);
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws javax.swing.text.BadLocationException {
                if (formatting) {
                    super.remove(fb, offset, length);
                    return;
                }
                super.remove(fb, offset, length);
            }

            private void applyFormat(FilterBypass fb, String proposedText) throws javax.swing.text.BadLocationException {
                String digits = proposedText.replaceAll("[^0-9]", "");
                StringBuilder cleanDigits = new StringBuilder();
                
                for (int i = 0; i < digits.length(); i++) {
                    char c = digits.charAt(i);
                    int pos = cleanDigits.length();
                    
                    if (pos == 0) {
                        if (c >= '4' && c <= '9') {
                            cleanDigits.append('0').append(c);
                        } else if (c >= '0' && c <= '3') {
                            cleanDigits.append(c);
                        }
                    } else if (pos == 1) {
                        char firstChar = cleanDigits.charAt(0);
                        if (firstChar == '3') {
                            if (c == '0' || c == '1') {
                                cleanDigits.append(c);
                            }
                        } else if (firstChar == '0') {
                            if (c >= '1' && c <= '9') {
                                cleanDigits.append(c);
                            }
                        } else {
                            cleanDigits.append(c);
                        }
                    } else if (pos == 2) {
                        if (c >= '2' && c <= '9') {
                            cleanDigits.append('0').append(c);
                        } else if (c == '0' || c == '1') {
                            cleanDigits.append(c);
                        }
                    } else if (pos == 3) {
                        char firstMonthChar = cleanDigits.charAt(2);
                        if (firstMonthChar == '1') {
                            if (c >= '0' && c <= '2') {
                                cleanDigits.append(c);
                            }
                        } else if (firstMonthChar == '0') {
                            if (c >= '1' && c <= '9') {
                                cleanDigits.append(c);
                            }
                        } else {
                            cleanDigits.append(c);
                        }
                    } else if (pos >= 4 && pos < 8) {
                        cleanDigits.append(c);
                    }
                }
                
                String finalDigits = cleanDigits.toString();
                StringBuilder sb = new StringBuilder();
                int len = finalDigits.length();
                if (len > 0) {
                    sb.append(finalDigits.substring(0, Math.min(len, 2)));
                }
                if (len >= 2) {
                    sb.append("-");
                    if (len > 2) {
                        sb.append(finalDigits.substring(2, Math.min(len, 4)));
                    }
                }
                if (len >= 4) {
                    sb.append("-");
                    if (len > 4) {
                        sb.append(finalDigits.substring(4, Math.min(len, 8)));
                    }
                }
                String formatted = sb.toString();
                formatting = true;
                fb.replace(0, fb.getDocument().getLength(), formatted, null);
                formatting = false;
            }
        });

        // Calendar icon button (hand-drawn)
        JButton calIcon = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int cx = w / 2, cy = h / 2;
                g2.setColor(ColorPalette.TEXT_SECONDARY);
                
                int sz = 16;
                int x = cx - sz / 2, y = cy - sz / 2 + 1;
                g2.setStroke(new BasicStroke(1.6f)); // Ketebalan garis
                
                // Body (Kotak utama kalender dengan ujung membulat)
                g2.drawRoundRect(x, y + 2, sz, sz - 2, 4, 4);
                
                // Top clips (Dua garis tegak di bagian atas untuk spiral kalender)
                g2.drawLine(x + 4, y, x + 4, y + 4);
                g2.drawLine(x + sz - 4, y, x + sz - 4, y + 4);
                
                // Horizontal line (Garis pembatas header bulan dan tanggal)
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(x + 2, y + 7, x + sz - 2, y + 7);
                
                // Dots (Tiga titik di bagian bawah merepresentasikan tanggal-tanggal)
                g2.fillOval(x + 3, y + 10, 3, 3);
                g2.fillOval(x + 7, y + 10, 3, 3);
                g2.fillOval(x + 11, y + 10, 3, 3);
                g2.dispose();
            }
        };
        calIcon.setPreferredSize(new Dimension(36, 36));
        calIcon.setOpaque(false);
        calIcon.setContentAreaFilled(false);
        calIcon.setBorderPainted(false);
        calIcon.setFocusPainted(false);
        calIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calIcon.addActionListener(e -> {
            CalendarPopup cal = new CalendarPopup(deadlineField);
            cal.show(deadWrapper, 0, deadWrapper.getHeight() + 2);
        });

        deadWrapper.add(deadlineField, BorderLayout.CENTER);
        deadWrapper.add(calIcon, BorderLayout.EAST);
        main.add(deadWrapper);
        main.add(Box.createVerticalStrut(16));

        // Jenis Tugas (Card Toggle Buttons)
        main.add(createLabel(util.TranslationManager.get("label_jenis_tugas")));
        main.add(Box.createVerticalStrut(6));

        JPanel jenisTugasRow = new JPanel(new GridLayout(1, 2, 10, 0));
        jenisTugasRow.setOpaque(false);
        jenisTugasRow.setAlignmentX(LEFT_ALIGNMENT);
        jenisTugasRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        jenisTugasRow.setPreferredSize(new Dimension(0, 52));

        individuCard = createJenisCard(util.TranslationManager.get("jenis_individu"), false, true);
        kelompokCard = createJenisCard(util.TranslationManager.get("jenis_kelompok"), true, false);

        individuCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedJenis = "INDIVIDU";
                individuSelected = true;
                kelompokSelected = false;
                individuCard.repaint();
                kelompokCard.repaint();
                updateKelompokFieldsVisibility(false);
            }
        });
        kelompokCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedJenis = "KELOMPOK";
                kelompokSelected = true;
                individuSelected = false;
                individuCard.repaint();
                kelompokCard.repaint();
                updateKelompokFieldsVisibility(true);
            }
        });

        jenisTugasRow.add(individuCard);
        jenisTugasRow.add(kelompokCard);
        main.add(jenisTugasRow);
        main.add(Box.createVerticalStrut(16));

        // Nomor Kelompok
        nomorKelompokLabel = createLabel(util.TranslationManager.get("label_nomor_kelompok"));
        main.add(nomorKelompokLabel);
        main.add(Box.createVerticalStrut(6));

        nomorKelompokWrapper = new JPanel(new BorderLayout()) {
            private boolean focused = false;
            {
                setOpaque(false);
                setPreferredSize(new Dimension(100, 44));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                setAlignmentX(LEFT_ALIGNMENT);
            }
            @SuppressWarnings("unused")
            public void setFocused(boolean f) { this.focused = f; repaint(); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (focused) {
                    g2.setColor(ColorPalette.ACCENT_BLUE);
                    g2.setStroke(new BasicStroke(1.5f));
                } else {
                    g2.setColor(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_CARD);
                    g2.setStroke(new BasicStroke(1.5f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        nomorKelompokField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(ColorPalette.isDarkMode ? new Color(0x88, 0x88, 0x88) : ColorPalette.TEXT_LIGHT);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Contoh: 3" : "e.g., 3", getInsets().left, y);
                    g2.dispose();
                }
            }
        };
        nomorKelompokField.setOpaque(false);
        nomorKelompokField.setFont(ColorPalette.FONT_BODY);
        nomorKelompokField.setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        nomorKelompokField.setCaretColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        nomorKelompokField.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        nomorKelompokField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                try { nomorKelompokWrapper.getClass().getMethod("setFocused", boolean.class).invoke(nomorKelompokWrapper, true); } catch (Exception ignored) {}
            }
            @Override public void focusLost(FocusEvent e) {
                try { nomorKelompokWrapper.getClass().getMethod("setFocused", boolean.class).invoke(nomorKelompokWrapper, false); } catch (Exception ignored) {}
            }
        });
        nomorKelompokWrapper.add(nomorKelompokField, BorderLayout.CENTER);
        main.add(nomorKelompokWrapper);
        main.add(Box.createVerticalStrut(16));

        // Anggota Kelompok Section
        anggotaSection = new JPanel();
        anggotaSection.setOpaque(false);
        anggotaSection.setLayout(new BoxLayout(anggotaSection, BoxLayout.Y_AXIS));
        anggotaSection.setAlignmentX(LEFT_ALIGNMENT);

        // Header: "Anggota Kelompok" + "+ Tambah" button
        JPanel angHeader = new JPanel(new BorderLayout());
        angHeader.setOpaque(false);
        angHeader.setAlignmentX(LEFT_ALIGNMENT);
        angHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel angLabel = new JLabel(util.TranslationManager.get("label_anggota_kelompok"));
        angLabel.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 15));
        angLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        angHeader.add(angLabel, BorderLayout.WEST);

        JButton addAngBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "+ Tambah" : "+ Add") {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? new Color(0x25, 0x63, 0xEB) : ColorPalette.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        addAngBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
        addAngBtn.setForeground(Color.WHITE);
        addAngBtn.setOpaque(false);
        addAngBtn.setContentAreaFilled(false);
        addAngBtn.setBorderPainted(false);
        addAngBtn.setFocusPainted(false);
        addAngBtn.setPreferredSize(new Dimension(100, 30));
        addAngBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addAngBtn.addActionListener(e -> addAnggotaRow());
        angHeader.add(addAngBtn, BorderLayout.EAST);

        anggotaSection.add(angHeader);
        anggotaSection.add(Box.createVerticalStrut(8));

        // Member list container (bordered)
        anggotaListPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x33, 0x33, 0x33) : ColorPalette.BORDER_CARD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        anggotaListPanel.setOpaque(false);
        anggotaListPanel.setLayout(new BoxLayout(anggotaListPanel, BoxLayout.Y_AXIS));
        anggotaListPanel.setAlignmentX(LEFT_ALIGNMENT);

        anggotaSection.add(anggotaListPanel);

        main.add(anggotaSection);

        // Add initial member row
        addAnggotaRow();

        JScrollPane scrollPane = new JScrollPane(main);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        
        JScrollBar vsb = scrollPane.getVerticalScrollBar();
        vsb.setOpaque(false);
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton jb = new JButton();
                jb.setPreferredSize(new Dimension(0, 0));
                jb.setMinimumSize(new Dimension(0, 0));
                jb.setMaximumSize(new Dimension(0, 0));
                return jb;
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = isThumbRollover() ? new Color(85, 85, 85) : new Color(136, 136, 136);
                g2.setColor(color);
                int w = 3;
                int x = thumbBounds.x + (thumbBounds.width - w) / 2;
                g2.fillRoundRect(x, thumbBounds.y, w, thumbBounds.height, w, w);
                g2.dispose();
            }
            @Override
            public Dimension getPreferredSize(JComponent c) {
                Dimension d = super.getPreferredSize(c);
                return new Dimension(4, d.height);
            }
        });

        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(createSeparator(), BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);
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
        saveBtn.addActionListener(e -> doSave());
        btnPanel.add(saveBtn);

        bottomWrapper.add(btnPanel, BorderLayout.CENTER);

        rootPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(bottomWrapper, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);

        // Update visibilitas sesuai state default
        updateKelompokFieldsVisibility(kelompokSelected);

        // Force scroll ke atas saat dialog pertama kali dibuka
        SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(0);
        });
    }


    private void updateKelompokFieldsVisibility(boolean show) {
        nomorKelompokLabel.setVisible(show);
        nomorKelompokWrapper.setVisible(show);
        anggotaSection.setVisible(show);
        revalidate();
        repaint();
    }


    private void addAnggotaRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 40));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        int nomor = anggotaFields.size() + 1;
        JLabel nomorLabel = new JLabel(nomor + ". ");
        nomorLabel.setFont(ColorPalette.FONT_BODY);
        nomorLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        nomorLabel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 4));
        row.add(nomorLabel, BorderLayout.WEST);

        JTextField namaF = new JTextField();
        namaF.setFont(ColorPalette.FONT_BODY);
        namaF.setForeground(ColorPalette.TEXT_PRIMARY);
        namaF.setCaretColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        namaF.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        namaF.setOpaque(false);
        row.add(namaF, BorderLayout.CENTER);

        // × delete button (custom painted)
        JButton delBtn = new JButton() {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? ColorPalette.PRIORITY_HIGH : ColorPalette.TEXT_SECONDARY);
                g2.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 16));
                FontMetrics fm = g2.getFontMetrics();
                String sym = "\u00D7";
                int x = (getWidth() - fm.stringWidth(sym)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(sym, x, y);
                g2.dispose();
            }
        };
        delBtn.setOpaque(false);
        delBtn.setContentAreaFilled(false);
        delBtn.setBorderPainted(false);
        delBtn.setFocusPainted(false);
        delBtn.setPreferredSize(new Dimension(32, 40));
        delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> {
            anggotaListPanel.remove(row);
            anggotaFields.remove(namaF);
            renumberAnggotaRows();
            anggotaListPanel.revalidate();
            anggotaListPanel.repaint();
        });
        row.add(delBtn, BorderLayout.EAST);

        // Separator: only if not the first row
        if (!anggotaFields.isEmpty()) {
            row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorPalette.BORDER_CARD));
        }

        anggotaFields.add(namaF);
        anggotaListPanel.add(row);
        anggotaListPanel.revalidate();
        anggotaListPanel.repaint();

        // Scroll to bottom so user sees the new row
        SwingUtilities.invokeLater(() -> {
            Container parent = anggotaListPanel.getParent();
            while (parent != null && !(parent instanceof JScrollPane)) {
                parent = parent.getParent();
            }
            if (parent instanceof JScrollPane) {
                JScrollBar sb = ((JScrollPane) parent).getVerticalScrollBar();
                sb.setValue(sb.getMaximum());
            }
        });
    }

    private void renumberAnggotaRows() {
        Component[] rows = anggotaListPanel.getComponents();
        for (int i = 0; i < rows.length; i++) {
            JPanel row = (JPanel) rows[i];
            Component west = ((BorderLayout) row.getLayout()).getLayoutComponent(BorderLayout.WEST);
            if (west instanceof JLabel) {
                ((JLabel) west).setText((i + 1) + ". ");
            }
            // Fix separator: first row no top border, rest have top border
            if (i == 0) {
                row.setBorder(null);
            } else {
                row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorPalette.BORDER_CARD));
            }
        }
    }


    private JPanel createJenisCard(String label, boolean isKelompok, boolean selected) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean sel = isKelompok ? kelompokSelected : individuSelected;

                // Background
                if (sel) {
                    g2.setColor(ColorPalette.isDarkMode ? new Color(0x1E, 0x29, 0x3B) : new Color(235, 242, 255));
                } else {
                    g2.setColor(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Border
                if (sel) {
                    g2.setColor(new Color(59, 130, 246));
                } else {
                    g2.setColor(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : new Color(228, 228, 231));
                }
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Icon + Text (centered manually)
                int contentY = getHeight() / 2; // vertical center
                // Calculate horizontal centering
                g2.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                
                int iconW = 20;
                int gap = 8;
                int textW = fm.stringWidth(label);
                int totalW = iconW + gap + textW;
                
                int startX = (getWidth() - totalW) / 2;
                int cx = startX + iconW / 2;
                int textX = startX + iconW + gap;
                
                // Draw Icon
                Color textCol;
                if (sel) {
                    textCol = ColorPalette.isDarkMode ? Color.WHITE : new Color(29, 78, 216);
                } else {
                    textCol = ColorPalette.TEXT_PRIMARY;
                }
                g2.setColor(textCol);
                if (isKelompok) {
                    paintGroupIcon(g2, cx, contentY);
                } else {
                    paintPersonIcon(g2, cx, contentY);
                }

                // Draw Text
                int textY = contentY + fm.getAscent() / 2 - 2; // baseline centered
                g2.drawString(label, textX, textY);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(0, 52);
            }
        };
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return card;
    }

    /** 👥 Group Icon */
    /** 👥 Group Icon */
    private void paintGroupIcon(Graphics2D g2, int cx, int cy) {
        g2.setStroke(new BasicStroke(1.4f));
        // Back person
        g2.drawOval(cx + 4, cy - 8, 5, 5);
        g2.drawArc(cx + 2, cy - 2, 9, 7, 0, 180);
        // Front person
        g2.drawOval(cx - 5, cy - 7, 7, 7);
        g2.drawArc(cx - 8, cy + 1, 13, 10, 0, 180);
    }

    /** 👤 Single Person Icon */
    private void paintPersonIcon(Graphics2D g2, int cx, int cy) {
        g2.setStroke(new BasicStroke(1.4f));
        // Head centered at cy - 3 (so full icon is centered around cy)
        g2.drawOval(cx - 4, cy - 7, 8, 8);
        // Body
        g2.drawArc(cx - 7, cy + 2, 14, 11, 0, 180);
    }


    private void doSave() {
        String judul = judulField.getText().trim();
        if (judul.isEmpty()) {
            showCustomWarning(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Judul tugas tidak boleh kosong!" : "Task title cannot be empty!");
            return;
        }

        // Matkul
        MataKuliah selectedMk = null;
        int mkIdx = matkulCombo.getSelectedIndex();
        if (mkIdx > 0 && mkIdx <= matkulList.size()) {
            selectedMk = matkulList.get(mkIdx - 1);
        }

        if (selectedMk == null) {
            showCustomWarning(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Mata kuliah belum dipilih!<br>Silakan pilih mata kuliah terlebih dahulu." : "Course not selected!<br>Please choose a course first.");
            return;
        }

        // Prioritas (dari segmented control)
        String prioStr = prioControl.getSelected().toUpperCase();
        Prioritas prio;
        if (prioStr.equals(util.TranslationManager.get("filter_tinggi").toUpperCase())) {
            prio = Prioritas.TINGGI;
        } else if (prioStr.equals(util.TranslationManager.get("filter_rendah").toUpperCase())) {
            prio = Prioritas.RENDAH;
        } else {
            prio = Prioritas.SEDANG;
        }

        // Deadline (format dd-mm-yyyy → LocalDate)
        LocalDate deadline = null;
        String deadlineStr = deadlineField.getText().trim();
        if (!deadlineStr.isEmpty()) {
            try {
                deadline = LocalDate.parse(deadlineStr, FORMAT_ID);
            } catch (DateTimeParseException e) {
                showCustomWarning(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Format deadline salah!<br>Gunakan DD-MM-YYYY (Contoh: 20-04-2026)" : "Incorrect deadline format!<br>Use DD-MM-YYYY (Example: 20-04-2026)");
                return;
            }
        }

        // Jenis
        Jenis jenis = selectedJenis.equals("KELOMPOK") ? Jenis.KELOMPOK : Jenis.INDIVIDU;

        // Anggota & Deskripsi
        List<AnggotaKelompok> anggotaList = new ArrayList<>();
        String deskripsi = "";
        
        if (jenis == Jenis.KELOMPOK) {
            String nomorKlp = nomorKelompokField.getText().trim();
            if (!nomorKlp.isEmpty()) {
                anggotaList.add(new AnggotaKelompok(nomorKlp, "#GROUP_NUM#"));
            }
            
            for (JTextField namaF : anggotaFields) {
                String nama = namaF.getText().trim();
                if (!nama.isEmpty()) {
                    anggotaList.add(new AnggotaKelompok(nama, ""));
                }
            }
        }

        int result = taskService.addNewTask(judul, deskripsi, selectedMk, prio, deadline, jenis, anggotaList);
        if (result > 0) {
            confirmed = true;
            dispose();
        } else {
            showCustomWarning("Gagal menyimpan tugas!");
        }
    }

    public boolean isConfirmed() { return confirmed; }


    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ColorPalette.FONT_BUTTON);
        l.setForeground(ColorPalette.TEXT_PRIMARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void refreshMatkulDropdown() {
        matkulList = taskService.getAllMataKuliah();
        matkulCombo.removeAllItems();
        matkulCombo.addItem(util.TranslationManager.get("label_pilih_matkul"));
        for (MataKuliah mk : matkulList) {
            matkulCombo.addItem(mk.getNama());
        }
    }




    /**
     * Dialog peringatan custom agar tidak menggunakan JOptionPane bawaan yang jelek.
     */
    private void showCustomWarning(String message) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 20, 20);
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Warning Icon
        JLabel icon = new JLabel("\u26A0\uFE0F");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setBorder(new EmptyBorder(8, 0, 0, 0));
        icon.setAlignmentX(CENTER_ALIGNMENT);
        content.add(icon);
        content.add(Box.createVerticalStrut(12));

        // Message
        String fontName = ColorPalette.FONT_BODY.getFamily();
        JLabel msgLabel = new JLabel("<html><div style='text-align: center; font-family: \"" + fontName + "\";'>" 
            + message.replace("\n", "<br>") + "</div></html>");
        msgLabel.setFont(ColorPalette.FONT_BODY);
        msgLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        msgLabel.setAlignmentX(CENTER_ALIGNMENT);
        content.add(msgLabel);
        content.add(Box.createVerticalStrut(24));

        // Button OK
        JButton okBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Mengerti" : "Understood") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        okBtn.setFont(ColorPalette.FONT_BUTTON);
        okBtn.setForeground(Color.WHITE);
        okBtn.setBackground(ColorPalette.ACCENT_BLUE);
        okBtn.setPreferredSize(new Dimension(100, 36));
        okBtn.setMaximumSize(new Dimension(100, 36));
        okBtn.setAlignmentX(CENTER_ALIGNMENT);
        okBtn.setBorderPainted(false);
        okBtn.setContentAreaFilled(false);
        okBtn.setFocusPainted(false);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { okBtn.setBackground(new Color(30, 80, 200)); }
            @Override public void mouseExited(MouseEvent e) { okBtn.setBackground(ColorPalette.ACCENT_BLUE); }
        });
        okBtn.addActionListener(e -> dialog.dispose());

        content.add(okBtn);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private JComponent createSeparator() {
        return new JComponent() {
            @Override
            public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 1); }
            @Override
            public Dimension getPreferredSize() { return new Dimension(100, 1); }
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ColorPalette.isDarkMode ? new Color(0x33, 0x33, 0x33) : ColorPalette.BORDER_LIGHT);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
    }

    private JComponent createCloseButton() {
        JButton btn = new JButton() {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
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

    private void toggleInlineForm(boolean show) {
        inlineMkPanel.setVisible(show);
        if (show) {
            setSize(400, 800);
            inlineMkNamaField.requestFocusInWindow();
        } else {
            setSize(400, 540);
            inlineMkNamaField.setText("");
            inlineMkErrorLabel.setText(" ");
            if (inlineColorButtons != null && !inlineColorButtons.isEmpty()) {
                for (ColorCircleButton cb : inlineColorButtons) {
                    cb.setSelected(false);
                    if (cb.isCustom) {
                        cb.customChosenColor = null;
                        cb.hexCode = "#000000";
                    }
                }
                inlineColorButtons.get(0).setSelected(true);
                inlineMkSelectedColorHex = inlineColorButtons.get(0).getHexCode();
            }
            if (previewBadge != null) {
                updatePreviewBadge("", inlineMkSelectedColorHex);
            }
        }
        setLocationRelativeTo(getOwner());
        revalidate();
        repaint();
    }

    private void updatePreviewBadge(String text, String colorHex) {
        String displayName = text;
        if (text == null || text.isEmpty()) {
            displayName = "Nama Matkul";
        } else if (text.length() > 20) {
            displayName = text.substring(0, 17) + "...";
        }
        previewBadge.setText(displayName);
        
        Color color = Color.decode(colorHex);
        previewBadge.setBackground(color);
        
        // Calculate relative luminance / brightness to dynamically set contrasting text color
        double brightness = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        Color textColor = brightness > 180 ? new Color(0x20, 0x21, 0x24) : Color.WHITE;
        previewBadge.setForeground(textColor);
        
        FontMetrics fm = previewBadge.getFontMetrics(previewBadge.getFont());
        int w = fm.stringWidth(displayName) + 20;
        int h = 24;
        
        // Constraint to never exceed the text field's relative width (e.g. 320px)
        int maxAllowedWidth = 320;
        if (w > maxAllowedWidth) {
            w = maxAllowedWidth;
        }
        
        Dimension size = new Dimension(w, h);
        previewBadge.setPreferredSize(size);
        previewBadge.setMaximumSize(size);
        previewBadge.setMinimumSize(size);
        
        previewBadge.revalidate();
        previewBadge.repaint();
    }

    private JPanel createInlineMataKuliahPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : new Color(0xF4, 0xF4, 0xF5)); // BG_SECTION
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel inlineMkLabel = new JLabel(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Nama Mata Kuliah" : "Course Name");
        inlineMkLabel.setFont(ColorPalette.FONT_BUTTON);
        inlineMkLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        inlineMkLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(inlineMkLabel);
        card.add(Box.createVerticalStrut(6));

        inlineMkNamaField = new JTextField() {
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
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        inlineMkNamaField.setOpaque(false);
        inlineMkNamaField.setBackground(ColorPalette.isDarkMode ? new Color(0x33, 0x33, 0x33) : Color.WHITE);
        inlineMkNamaField.setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        inlineMkNamaField.setCaretColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        inlineMkNamaField.setFont(ColorPalette.FONT_BODY);
        inlineMkNamaField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        inlineMkNamaField.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        inlineMkNamaField.setAlignmentX(LEFT_ALIGNMENT);
        ((javax.swing.text.AbstractDocument) inlineMkNamaField.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int overLimit = (currentLength + text.length() - length) - 30;
                if (overLimit <= 0) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    int insertLength = text.length() - overLimit;
                    if (insertLength > 0) {
                        super.replace(fb, offset, length, text.substring(0, insertLength), attrs);
                    }
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int overLimit = (currentLength + string.length()) - 30;
                if (overLimit <= 0) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    int insertLength = string.length() - overLimit;
                    if (insertLength > 0) {
                        super.insertString(fb, offset, string.substring(0, insertLength), attr);
                    }
                }
            }
        });
        inlineMkNamaField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
            private void updatePreview() {
                SwingUtilities.invokeLater(() -> {
                    updatePreviewBadge(inlineMkNamaField.getText().trim(), inlineMkSelectedColorHex);
                });
            }
        });
        card.add(inlineMkNamaField);
        card.add(Box.createVerticalStrut(12));

        JLabel inlineWarnaLabel = new JLabel(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Warna Badge" : "Badge Color");
        inlineWarnaLabel.setFont(ColorPalette.FONT_BUTTON);
        inlineWarnaLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        inlineWarnaLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(inlineWarnaLabel);
        card.add(Box.createVerticalStrut(8));

        JPanel colorPickerRow = new JPanel(new GridLayout(0, 8, 4, 8));
        colorPickerRow.setOpaque(false);
        colorPickerRow.setAlignmentX(LEFT_ALIGNMENT);
        colorPickerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        inlineColorButtons = new java.util.ArrayList<>();
        for (String[] option : WARNA_OPTIONS) {
            Color color = Color.decode(option[1]);
            ColorCircleButton btn = new ColorCircleButton(color, option[1]);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    for (ColorCircleButton cb : inlineColorButtons) {
                        cb.setSelected(false);
                        if (cb.isCustom) {
                            cb.customChosenColor = null;
                            cb.hexCode = "#000000";
                            cb.repaint();
                        }
                    }
                    btn.setSelected(true);
                    inlineMkSelectedColorHex = btn.getHexCode();
                    updatePreviewBadge(inlineMkNamaField.getText().trim(), inlineMkSelectedColorHex);
                }
            });
            inlineColorButtons.add(btn);
            colorPickerRow.add(btn);
        }

        // Add 16th Custom Color Squircle Button
        ColorCircleButton customBtn = new ColorCircleButton(Color.BLACK, "#000000", true);
        customBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                Color popBg = ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE;
                Color popBorder = ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : new Color(228, 228, 231);
                popup.setBorder(BorderFactory.createLineBorder(popBorder, 1));
                popup.setBackground(popBg);

                Color initial = (customBtn.customChosenColor != null) ? customBtn.customChosenColor : Color.decode("#F28B82");
                
                CustomColorPickerPanel pickerPanel = new CustomColorPickerPanel(initial, chosen -> {
                    customBtn.customChosenColor = chosen;
                    String hex = String.format("#%02X%02X%02X", chosen.getRed(), chosen.getGreen(), chosen.getBlue());
                    customBtn.hexCode = hex;

                    for (ColorCircleButton cb : inlineColorButtons) {
                        cb.setSelected(false);
                    }
                    customBtn.setSelected(true);
                    inlineMkSelectedColorHex = hex;
                    updatePreviewBadge(inlineMkNamaField.getText().trim(), inlineMkSelectedColorHex);
                });

                popup.add(pickerPanel);
                
                // Position popup cleanly to the right of the "+" button, or fallback to bottom left-shifted if screen space is tight
                Point screenLoc = null;
                try {
                    screenLoc = customBtn.getLocationOnScreen();
                } catch (java.awt.IllegalComponentStateException ignored) {}

                int popupWidth = popup.getPreferredSize().width;
                int popupHeight = popup.getPreferredSize().height;
                int x;
                int y;

                if (screenLoc != null) {
                    GraphicsConfiguration gc = customBtn.getGraphicsConfiguration();
                    Rectangle screenBounds = (gc != null) ? gc.getBounds() : new Rectangle(0, 0, 1920, 1080);
                    
                    if (screenLoc.x + customBtn.getWidth() + 8 + popupWidth <= screenBounds.x + screenBounds.width) {
                        // Position to the right, centered vertically relative to customBtn
                        x = customBtn.getWidth() + 8;
                        y = -(popupHeight - customBtn.getHeight()) / 2;
                    } else {
                        // Position underneath, shifted left to keep Batal/Simpan buttons visible
                        x = customBtn.getWidth() - popupWidth;
                        y = customBtn.getHeight() + 4;
                    }
                } else {
                    // Fallback to right side positioning
                    x = customBtn.getWidth() + 8;
                    y = -(popupHeight - customBtn.getHeight()) / 2;
                }

                popup.show(customBtn, x, y);
            }
        });
        inlineColorButtons.add(customBtn);
        colorPickerRow.add(customBtn);

        inlineColorButtons.get(0).setSelected(true);
        inlineMkSelectedColorHex = inlineColorButtons.get(0).getHexCode();

        card.add(colorPickerRow);
        card.add(Box.createVerticalStrut(12));

        // Live Preview Badge Section
        JLabel previewTitleLabel = new JLabel(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Pratinjau Badge:" : "Badge Preview:");
        previewTitleLabel.setFont(ColorPalette.FONT_BUTTON);
        previewTitleLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        previewTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(previewTitleLabel);
        card.add(Box.createVerticalStrut(6));

        JPanel previewWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        previewWrapper.setOpaque(false);
        previewWrapper.setAlignmentX(LEFT_ALIGNMENT);
        
        previewBadge = new JLabel(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Nama Matkul" : "Course Name") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        previewBadge.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 11));
        previewBadge.setOpaque(false);
        previewBadge.setBackground(Color.decode(inlineMkSelectedColorHex));
        // Set initial contrasting foreground color
        Color initBgColor = Color.decode(inlineMkSelectedColorHex);
        double initBrightness = (0.299 * initBgColor.getRed() + 0.587 * initBgColor.getGreen() + 0.114 * initBgColor.getBlue());
        previewBadge.setForeground(initBrightness > 180 ? new Color(0x20, 0x21, 0x24) : Color.WHITE);
        String previewText = TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Nama Matkul" : "Course Name";
        FontMetrics fm = previewBadge.getFontMetrics(previewBadge.getFont());
        Dimension previewSize = new Dimension(fm.stringWidth(previewText) + 20, 24);
        previewBadge.setPreferredSize(previewSize);
        previewBadge.setMaximumSize(previewSize);
        previewBadge.setMinimumSize(previewSize);
        
        previewWrapper.add(previewBadge);
        card.add(previewWrapper);
        card.add(Box.createVerticalStrut(12));

        inlineMkErrorLabel = new JLabel(" ");
        inlineMkErrorLabel.setFont(ColorPalette.FONT_SMALL);
        inlineMkErrorLabel.setForeground(ColorPalette.PRIORITY_HIGH);
        inlineMkErrorLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(inlineMkErrorLabel);
        card.add(Box.createVerticalStrut(6));

        JPanel inlineBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        inlineBtnRow.setOpaque(false);
        inlineBtnRow.setAlignmentX(LEFT_ALIGNMENT);
        inlineBtnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        inlineBtnRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JButton inlineCancelBtn = new JButton(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Batal" : "Cancel") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(ColorPalette.TEXT_SECONDARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        inlineCancelBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 11));
        inlineCancelBtn.setOpaque(false);
        inlineCancelBtn.setContentAreaFilled(false);
        inlineCancelBtn.setBorderPainted(false);
        inlineCancelBtn.setFocusPainted(false);
        inlineCancelBtn.setPreferredSize(new Dimension(70, 30));
        inlineCancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inlineCancelBtn.addActionListener(e -> toggleInlineForm(false));
        inlineBtnRow.add(inlineCancelBtn);

        JButton inlineSaveBtn = new JButton(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Simpan" : "Save") {
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
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        inlineSaveBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 11));
        inlineSaveBtn.setOpaque(false);
        inlineSaveBtn.setContentAreaFilled(false);
        inlineSaveBtn.setBorderPainted(false);
        inlineSaveBtn.setFocusPainted(false);
        inlineSaveBtn.setPreferredSize(new Dimension(90, 30));
        inlineSaveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inlineSaveBtn.addActionListener(e -> {
            String nama = inlineMkNamaField.getText().trim();
            if (nama.isEmpty()) {
                inlineMkErrorLabel.setText(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Nama mata kuliah tidak boleh kosong!" : "Course name cannot be empty!");
                return;
            }
            taskService.addMataKuliah(nama, inlineMkSelectedColorHex);
            refreshMatkulDropdown();

            // Select the newly created Mata Kuliah
            for (int i = 0; i < matkulCombo.getItemCount(); i++) {
                if (matkulCombo.getItemAt(i).equals(nama)) {
                    matkulCombo.setSelectedIndex(i);
                    break;
                }
            }

            if (SwingUtilities.getWindowAncestor(this) instanceof MainFrame) {
                ((MainFrame) SwingUtilities.getWindowAncestor(this)).refreshAll();
            }
            toggleInlineForm(false);
        });
        inlineBtnRow.add(inlineSaveBtn);

        card.add(inlineBtnRow);
        panel.add(card);

        inlineMkNamaField.addActionListener(e -> inlineSaveBtn.doClick());

        panel.setVisible(false); // Default hidden initially
        return panel;
    }

    private class ColorCircleButton extends JComponent {
        private Color color;
        private String hexCode;
        private boolean selected;
        private boolean hovered;
        private boolean isCustom;
        private Color customChosenColor = null;

        public ColorCircleButton(Color color, String hexCode) {
            this(color, hexCode, false);
        }

        public ColorCircleButton(Color color, String hexCode, boolean isCustom) {
            this.color = color;
            this.hexCode = hexCode;
            this.selected = false;
            this.hovered = false;
            this.isCustom = isCustom;
            setPreferredSize(new Dimension(32, 32));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            double d = (selected || hovered) ? 22.0 : 18.0;
            double cx = getWidth() / 2.0;
            double cy = getHeight() / 2.0;
            
            // Corner radius of 6px for 18px size, 7px for 22px size
            double cornerRadius = (selected || hovered) ? 7.0 : 6.0;

            // Fill inner squircle (rounded rectangle)
            java.awt.geom.RoundRectangle2D.Double squircle = new java.awt.geom.RoundRectangle2D.Double(
                cx - d / 2.0, cy - d / 2.0, d, d, cornerRadius * 2, cornerRadius * 2
            );

            if (isCustom && customChosenColor == null) {
                // Paint beautiful rainbow gradient
                float x = (float)(cx - d / 2.0);
                float y = (float)(cy - d / 2.0);
                float size = (float)d;
                LinearGradientPaint gp = new LinearGradientPaint(
                    x, y, x + size, y + size,
                    new float[]{0.0f, 0.33f, 0.66f, 1.0f},
                    new Color[]{
                        new Color(242, 139, 130), // Soft Red
                        new Color(255, 204, 128), // Soft Orange
                        new Color(165, 214, 167), // Soft Green
                        new Color(144, 202, 249)  // Soft Blue
                    }
                );
                g2.setPaint(gp);
                g2.fill(squircle);
            } else {
                Color drawColor = (isCustom && customChosenColor != null) ? customChosenColor : color;
                g2.setColor(drawColor);
                g2.fill(squircle);
            }

            if (selected) {
                // White checkmark inside the squircle
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                double startX = cx - 3.5;
                double startY = cy + 0.5;
                double midX = cx - 0.5;
                double midY = cy + 3.5;
                double endX = cx + 4.5;
                double endY = cy - 2.5;

                java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
                path.moveTo(startX, startY);
                path.lineTo(midX, midY);
                path.lineTo(endX, endY);
                g2.draw(path);
            } else {
                if (isCustom && customChosenColor == null) {
                    // Draw a plus "+" sign in the center of rainbow button
                    g2.setColor(new Color(100, 116, 139)); // Slate color for "+"
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine((int)(cx - 3.0), (int)cy, (int)(cx + 3.0), (int)cy);
                    g2.drawLine((int)cx, (int)(cy - 3.0), (int)cx, (int)(cy + 3.0));
                } else {
                    // Default light border
                    g2.setColor(new Color(228, 228, 231)); // #E4E4E7
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(squircle);
                }
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

    private class CustomColorPickerPanel extends JPanel {
        private float hue = 0.0f;
        private float saturation = 1.0f;
        private float brightness = 1.0f;
        
        private SVGradientBox svBox;
        private HueSlider hueSlider;
        private JTextField hexField;
        
        private java.util.function.Consumer<Color> colorChangeListener;
        private boolean updatingFromUI = false;

        public CustomColorPickerPanel(Color initialColor, java.util.function.Consumer<Color> listener) {
            this.colorChangeListener = listener;
            
            // Set initial HSB
            float[] hsb = Color.RGBtoHSB(initialColor.getRed(), initialColor.getGreen(), initialColor.getBlue(), null);
            this.hue = hsb[0];
            this.saturation = hsb[1];
            this.brightness = hsb[2];

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            setBackground(ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE); // White clean card matching TaskU

            // 1. SV Gradient Box
            svBox = new SVGradientBox();
            svBox.setPreferredSize(new Dimension(160, 120));
            svBox.setMinimumSize(new Dimension(160, 120));
            svBox.setMaximumSize(new Dimension(160, 120));
            svBox.setAlignmentX(CENTER_ALIGNMENT);
            add(svBox);
            add(Box.createVerticalStrut(10));

            // 2. Hue Slider
            hueSlider = new HueSlider();
            hueSlider.setPreferredSize(new Dimension(160, 18));
            hueSlider.setMinimumSize(new Dimension(160, 18));
            hueSlider.setMaximumSize(new Dimension(160, 18));
            hueSlider.setAlignmentX(CENTER_ALIGNMENT);
            add(hueSlider);
            add(Box.createVerticalStrut(10));

            // 3. Hex Input Field
            JPanel hexRow = new JPanel(new BorderLayout(6, 0));
            hexRow.setOpaque(false);
            hexRow.setAlignmentX(CENTER_ALIGNMENT);
            hexRow.setMaximumSize(new Dimension(160, 32));

            JLabel hashLabel = new JLabel("#");
            hashLabel.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 13));
            hashLabel.setForeground(new Color(100, 116, 139));
            hexRow.add(hashLabel, BorderLayout.WEST);

            hexField = new JTextField(7) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(32, 33, 36)); // Dark background
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    super.paintComponent(g2);
                    g2.dispose();
                }
            };
            hexField.setOpaque(false);
            hexField.setBackground(new Color(0, 0, 0, 0));
            hexField.setForeground(Color.WHITE);
            hexField.setCaretColor(Color.WHITE);
            hexField.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            hexField.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
            hexField.setHorizontalAlignment(JTextField.CENTER);

            // Document listener to handle manual editing
            hexField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void check() {
                    if (updatingFromUI) return;
                    String text = hexField.getText().trim();
                    if (text.startsWith("#")) {
                        text = text.substring(1);
                    }
                    if (text.length() == 6) {
                        try {
                            Color c = Color.decode("#" + text);
                            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                            hue = hsb[0];
                            saturation = hsb[1];
                            brightness = hsb[2];
                            
                            updatingFromUI = true;
                            svBox.repaint();
                            hueSlider.repaint();
                            fireColorChange();
                            updatingFromUI = false;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { check(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { check(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { check(); }
            });

            hexRow.add(hexField, BorderLayout.CENTER);
            add(hexRow);

            updateHexField();
        }

        private void fireColorChange() {
            Color chosenColor = Color.getHSBColor(hue, saturation, brightness);
            if (colorChangeListener != null) {
                colorChangeListener.accept(chosenColor);
            }
        }

        private void updateHexField() {
            if (updatingFromUI) return;
            updatingFromUI = true;
            Color current = Color.getHSBColor(hue, saturation, brightness);
            String hex = String.format("%02X%02X%02X", current.getRed(), current.getGreen(), current.getBlue());
            hexField.setText(hex);
            updatingFromUI = false;
        }

        private class SVGradientBox extends JComponent {
            public SVGradientBox() {
                java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        updateHSB(e.getX(), e.getY());
                    }
                    @Override
                    public void mouseDragged(java.awt.event.MouseEvent e) {
                        updateHSB(e.getX(), e.getY());
                    }
                };
                addMouseListener(ma);
                addMouseMotionListener(ma);
            }

            private void updateHSB(int mx, int my) {
                double s = (double) mx / getWidth();
                double v = 1.0 - ((double) my / getHeight());
                
                saturation = (float) Math.max(0.0, Math.min(1.0, s));
                brightness = (float) Math.max(0.0, Math.min(1.0, v));
                
                repaint();
                updateHexField();
                fireColorChange();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Draw rounded background clipping
                g2d.setClip(new java.awt.geom.RoundRectangle2D.Double(0, 0, w, h, 8, 8));

                // Horizontal gradient: White to Hue
                Color hueColor = Color.getHSBColor(hue, 1.0f, 1.0f);
                GradientPaint horiz = new GradientPaint(0, 0, Color.WHITE, w, 0, hueColor);
                g2d.setPaint(horiz);
                g2d.fillRect(0, 0, w, h);

                // Vertical gradient: Transparent to Black
                GradientPaint vert = new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, h, Color.BLACK);
                g2d.setPaint(vert);
                g2d.fillRect(0, 0, w, h);

                // Clear clip
                g2d.setClip(null);

                // Draw Squircle border around SV Box
                g2d.setColor(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : new Color(228, 228, 231)); // #E4E4E7
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

                // Draw thumb indicator (ring)
                double tx = saturation * w;
                double ty = (1.0 - brightness) * h;

                // Determine thumb ring color based on brightness for contrast
                g2d.setColor(brightness < 0.5 ? Color.WHITE : Color.BLACK);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval((int)(tx - 5), (int)(ty - 5), 10, 10);
                
                g2d.dispose();
            }
        }

        private class HueSlider extends JComponent {
            public HueSlider() {
                java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        updateHue(e.getX());
                    }
                    @Override
                    public void mouseDragged(java.awt.event.MouseEvent e) {
                        updateHue(e.getX());
                    }
                };
                addMouseListener(ma);
                addMouseMotionListener(ma);
            }

            private void updateHue(int mx) {
                int pad = 6;
                int w = getWidth();
                int trackW = w - (pad * 2);
                if (trackW <= 0) return;

                int clampedX = Math.max(pad, Math.min(w - pad, mx));
                double hVal = (double) (clampedX - pad) / trackW;
                hue = (float) Math.max(0.0, Math.min(1.0, hVal));
                
                repaint();
                svBox.repaint();
                updateHexField();
                fireColorChange();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int pad = 6;
                int trackW = w - (pad * 2);

                // Draw rainbow gradient track
                float[] fractions = {0.0f, 0.17f, 0.33f, 0.5f, 0.67f, 0.83f, 1.0f};
                Color[] colors = {
                    Color.getHSBColor(0.0f, 1f, 1f),
                    Color.getHSBColor(0.17f, 1f, 1f),
                    Color.getHSBColor(0.33f, 1f, 1f),
                    Color.getHSBColor(0.5f, 1f, 1f),
                    Color.getHSBColor(0.67f, 1f, 1f),
                    Color.getHSBColor(0.83f, 1f, 1f),
                    Color.getHSBColor(1.0f, 1f, 1f)
                };
                LinearGradientPaint huePaint = new LinearGradientPaint(pad, 0, pad + trackW, 0, fractions, colors);
                g2d.setPaint(huePaint);
                // Render modern pill track inside the padded bounds
                g2d.fillRoundRect(pad, 5, trackW, h - 10, 4, 4);

                // Draw pill/capsule white vertical thumb handle
                double tx = pad + (hue * trackW);
                int thumbW = 8;
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect((int)(tx - thumbW / 2.0), 1, thumbW, h - 2, 4, 4);
                
                g2d.setColor(new Color(156, 163, 175)); // gray-400 border for handle
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect((int)(tx - thumbW / 2.0), 1, thumbW, h - 2, 4, 4);

                g2d.dispose();
            }
        }
    }
}
