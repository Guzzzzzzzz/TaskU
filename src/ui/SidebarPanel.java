package ui;

import model.MataKuliah;
import model.Mahasiswa;
import service.TaskService;
import util.ColorPalette;
import util.TranslationManager;
import ui.components.RoundedPasswordField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * 
 *  Menampilkan:
 *  - Logo + tagline "YOUR ACADEMIC COMPANION"
 *  - Menu navigasi (Semua Tugas, Hari Ini, Selesai) dengan badge count
 *  - Daftar Mata Kuliah dengan warna indikator
 *  - Profil mahasiswa (Nama + NIM) di bagian bawah
 *
 */
public class SidebarPanel extends JPanel {

    private TaskService taskService;
    private MainFrame mainFrame;

    // Menu items (disimpan sebagai field agar bisa di-highlight)
    private JPanel menuSemua;
    private JPanel menuHariIni;
    private JPanel menuSelesai;
    private JPanel activeMenu;
    private int activeMataKuliahId = -1;

    // Menu labels (agar bisa diupdate bahasanya)
    private JLabel labelSemua;
    private JLabel labelHariIni;
    private JLabel labelSelesai;
    // Menu icon panels (digambar via Graphics2D agar konsisten di EXE)
    private JPanel iconSemua;
    private JPanel iconHariIni;
    private JPanel iconSelesai;

    // Badge labels
    private JLabel badgeSemua;
    private JLabel badgeHariIni;
    private JLabel badgeSelesai;

    // Container Mata Kuliah
    private JPanel mkContainer;

    private JLabel menuLabelHeader;
    private JLabel mkLabelHeader;

    public SidebarPanel(TaskService taskService, MainFrame mainFrame, Mahasiswa loggedInUser) {
        this.taskService = taskService;
        this.mainFrame = mainFrame;

        setPreferredSize(new Dimension(260, 0));
        setBackground(ColorPalette.BG_SIDEBAR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorPalette.BORDER_LIGHT));

        // ── Top area (Logo + Menu) ──
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(25, 20, 10, 20));

        // Logo — solid blue
        JLabel logoLabel = new JLabel("TaskU") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(ColorPalette.ACCENT_BLUE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        logoLabel.setFont(ColorPalette.FONT_LOGO);
        logoLabel.setPreferredSize(new Dimension(100, 28));
        logoLabel.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.add(logoLabel);

        topPanel.add(Box.createVerticalStrut(30));

        // Section: MENU
        menuLabelHeader = new JLabel("MENU");
        menuLabelHeader.setFont(ColorPalette.FONT_LABEL);
        menuLabelHeader.setForeground(ColorPalette.TEXT_SECONDARY);
        menuLabelHeader.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.add(menuLabelHeader);
        topPanel.add(Box.createVerticalStrut(8));

        // Menu Items
        badgeSemua = new JLabel("0") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconSemua = createBookIcon(true);
        labelSemua = new JLabel(TranslationManager.get("menu_semua"));
        JPanel menuLabelSemua = createMenuLabelPanel(iconSemua, labelSemua);
        menuSemua = createMenuItem(menuLabelSemua, badgeSemua, true);
        menuSemua.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveMenu(menuSemua);
                mainFrame.showAllTasks();
            }
        });
        topPanel.add(menuSemua);
        topPanel.add(Box.createVerticalStrut(4));

        badgeHariIni = new JLabel("0") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconHariIni = createBookIcon(false);
        labelHariIni = new JLabel(TranslationManager.get("menu_hari_ini"));
        JPanel menuLabelHariIni = createMenuLabelPanel(iconHariIni, labelHariIni);
        menuHariIni = createMenuItem(menuLabelHariIni, badgeHariIni, false);
        menuHariIni.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveMenu(menuHariIni);
                mainFrame.showTodayTasks();
            }
        });
        topPanel.add(menuHariIni);
        topPanel.add(Box.createVerticalStrut(4));

        badgeSelesai = new JLabel("0") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeSelesai.setOpaque(false);
        badgeSelesai.setBackground(Color.WHITE);
        badgeSelesai.setForeground(ColorPalette.TEXT_SECONDARY);
        badgeSelesai.setFont(ColorPalette.FONT_SMALL);
        badgeSelesai.setHorizontalAlignment(SwingConstants.CENTER);
        badgeSelesai.setPreferredSize(new Dimension(28, 18));

        iconSelesai = createBookIcon(false);
        labelSelesai = new JLabel(TranslationManager.get("menu_selesai"));
        JPanel menuLabelSelesai = createMenuLabelPanel(iconSelesai, labelSelesai);
        menuSelesai = createMenuItem(menuLabelSelesai, badgeSelesai, false);
        menuSelesai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveMenu(menuSelesai);
                mainFrame.showCompletedTasks();
            }
        });
        topPanel.add(menuSelesai);

        topPanel.add(Box.createVerticalStrut(25));

        // Section: MATA KULIAH
        mkLabelHeader = new JLabel(TranslationManager.get("menu_matkul"));
        mkLabelHeader.setFont(ColorPalette.FONT_LABEL);
        mkLabelHeader.setForeground(ColorPalette.TEXT_SECONDARY);
        mkLabelHeader.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.add(mkLabelHeader);
        topPanel.add(Box.createVerticalStrut(10));

        mkContainer = new JPanel();
        mkContainer.setOpaque(false);
        mkContainer.setLayout(new BoxLayout(mkContainer, BoxLayout.Y_AXIS));
        mkContainer.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.add(mkContainer);

        refreshMataKuliah();

        add(topPanel, BorderLayout.NORTH);

        // ── Bottom area (Profil Mahasiswa) ──
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorPalette.BORDER_LIGHT));
        bottomPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Mahasiswa mhs = loggedInUser;
        if (mhs != null) {
            // Avatar circle
            JLabel avatar = new JLabel(String.valueOf(mhs.getNama().charAt(0))) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ColorPalette.ACCENT_BLUE_BG);
                    g2.fillOval(0, 0, 40, 40);
                    g2.setColor(ColorPalette.ACCENT_BLUE);
                    g2.setFont(ColorPalette.FONT_HEADING);
                    FontMetrics fm = g2.getFontMetrics();
                    String text = getText();
                    int x = (40 - fm.stringWidth(text)) / 2;
                    int y = (40 + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(text, x, y);
                    g2.dispose();
                }
            };
            avatar.setPreferredSize(new Dimension(40, 40));
            bottomPanel.add(avatar);

            // Nama & NIM
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel namaLabel = new JLabel(mhs.getNama());
            namaLabel.setFont(ColorPalette.FONT_BODY);
            namaLabel.setForeground(ColorPalette.TEXT_PRIMARY);
            infoPanel.add(namaLabel);

            JLabel nimLabel = new JLabel(mhs.getNim());
            nimLabel.setFont(ColorPalette.FONT_SMALL);
            nimLabel.setForeground(ColorPalette.TEXT_SECONDARY);
            infoPanel.add(nimLabel);

            bottomPanel.add(infoPanel);

            // ── Popup Menu (Custom JDialog untuk menghindari icon gap bawaan JMenuItem) ──
            bottomPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showProfilePopup(bottomPanel);
                }
            });
        }

        add(bottomPanel, BorderLayout.SOUTH);

        // Set default active menu
        activeMenu = menuSemua;
    }

    /**
     * Membuat satu item menu sidebar.
     */
    private JPanel createMenuItem(JLabel label, JLabel badge, boolean isActive) {
        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(8, 12, 8, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.setFont(ColorPalette.FONT_BODY);
        item.add(label, BorderLayout.CENTER);

        if (badge != null) {
            badge.setFont(ColorPalette.FONT_BADGE);
            badge.setOpaque(false);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(24, 20));
            badge.setBorder(new EmptyBorder(2, 6, 2, 6));
            item.add(badge, BorderLayout.EAST);
        }

        if (isActive) {
            applyActiveStyle(item);
        } else {
            applyInactiveStyle(item);
        }

        return item;
    }

    /** Overload: menerima JPanel (icon+label) sebagai center component. */
    private JPanel createMenuItem(JPanel labelPanel, JLabel badge, boolean isActive) {
        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(8, 12, 8, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        item.add(labelPanel, BorderLayout.CENTER);

        if (badge != null) {
            badge.setFont(ColorPalette.FONT_BADGE);
            badge.setOpaque(false);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(24, 20));
            badge.setBorder(new EmptyBorder(2, 6, 2, 6));
            item.add(badge, BorderLayout.EAST);
        }

        if (isActive) {
            applyActiveStyle(item);
        } else {
            applyInactiveStyle(item);
        }

        return item;
    }


    /**
     * Set menu yang aktif (dihighlight).
     */
    private void setActiveMenu(JPanel menu) {
        if (activeMenu != null) applyInactiveStyle(activeMenu);
        activeMenu = menu;
        if (activeMenu != null) {
            applyActiveStyle(activeMenu);
            activeMataKuliahId = -1;
            refreshMataKuliah();
        }
    }

    private void applyActiveStyle(JPanel item) {
        item.setBackground(ColorPalette.ACCENT_BLUE_LIGHT);
        applyLabelColors(item, ColorPalette.ACCENT_BLUE, ColorPalette.ACCENT_BLUE, Color.WHITE);
    }

    private void applyInactiveStyle(JPanel item) {
        item.setBackground(ColorPalette.BG_SIDEBAR);
        Color textCol = ColorPalette.TEXT_DARK;
        Color badgeBg = ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : new Color(0xF4, 0xF4, 0xF5);
        Color badgeFg = ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_SECONDARY;
        applyLabelColors(item, textCol, badgeBg, badgeFg);
    }

    /**
     * Rekursif: iterasi semua komponen (termasuk nested JPanel) dan update warna JLabel.
     * Badge dikenali dari preferredSize.width <= 30.
     */
    private void applyLabelColors(Container container, Color textColor, Color badgeBg, Color badgeFg) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                if (label.getPreferredSize().width <= 30) { // Badge
                    label.setBackground(badgeBg);
                    label.setForeground(badgeFg);
                } else { // Teks menu
                    label.setForeground(textColor);
                }
            } else if (c instanceof Container) {
                // Masuk ke nested panel (contoh: menuLabelPanel yang berisi icon + JLabel)
                applyLabelColors((Container) c, textColor, badgeBg, badgeFg);
            }
        }
    }

    /**
     * Memperbarui gaya semua menu item di sidebar (aktif & inaktif) sesuai tema saat ini.
     */
    public void updateMenuStyles() {
        if (menuSemua != null) {
            if (activeMenu == menuSemua) applyActiveStyle(menuSemua);
            else applyInactiveStyle(menuSemua);
        }
        if (menuHariIni != null) {
            if (activeMenu == menuHariIni) applyActiveStyle(menuHariIni);
            else applyInactiveStyle(menuHariIni);
        }
        if (menuSelesai != null) {
            if (activeMenu == menuSelesai) applyActiveStyle(menuSelesai);
            else applyInactiveStyle(menuSelesai);
        }
        refreshMataKuliah();
        repaint();
    }

    /**
     * Membuat item mata kuliah di sidebar.
     */
    private JPanel createMataKuliahItem(MataKuliah mk) {
        boolean isSelected = (activeMataKuliahId == mk.getId());
        Color activeBg = ColorPalette.isDarkMode ? new Color(30, 41, 59) : new Color(240, 245, 255);
        Color hoverBg = ColorPalette.isDarkMode ? new Color(45, 55, 72) : new Color(240, 245, 255);
        
        Color normalText = ColorPalette.TEXT_PRIMARY;
        Color activeText = ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.ACCENT_BLUE;
        Color hoverText = ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.ACCENT_BLUE;

        final boolean[] hovered = {false};

        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(activeBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (hovered[0]) {
                    g2.setColor(hoverBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        // Dot warna (Mini Squircle)
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mk.getWarnaColor());
                int size = 10;
                int y = (getHeight() - size) / 2;
                g2.fillRoundRect(0, y, size, size, 3, 3);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(18, 26));
        leftPanel.add(dot);

        // Nama matkul — truncate if too long, full name as tooltip
        String fullNama = mk.getNama();
        String displayNama = fullNama.length() > 22 ? fullNama.substring(0, 20) + "…" : fullNama;
        JLabel nama = new JLabel(displayNama);
        nama.setFont(ColorPalette.FONT_BODY);
        nama.setForeground(isSelected ? activeText : normalText);
        if (fullNama.length() > 22) {
            nama.setToolTipText(fullNama);
        }
        leftPanel.add(nama);

        item.add(leftPanel, BorderLayout.CENTER);

        // Tombol delete (custom painted X)
        JButton delBtn = new JButton() {
            private boolean isDelHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { isDelHovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { isDelHovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int pad = 7;
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (isDelHovered) {
                    g2.setColor(new Color(220, 50, 50));
                } else {
                    g2.setColor(new Color(0, 0, 0, 0)); // Invisible saat tidak hover
                }
                g2.drawLine(pad, pad, w - pad, h - pad);
                g2.drawLine(w - pad, pad, pad, h - pad);
                g2.dispose();
            }
        };
        delBtn.setBorderPainted(false);
        delBtn.setContentAreaFilled(false);
        delBtn.setFocusPainted(false);
        delBtn.setOpaque(false);
        delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delBtn.setPreferredSize(new Dimension(22, 22));
        item.add(delBtn, BorderLayout.EAST);

        delBtn.addActionListener(e -> {
            showDeleteMkConfirmation(mk);
        });

        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        MouseAdapter hoverLogic = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == delBtn) return;
                setActiveMenu(null);
                activeMataKuliahId = mk.getId();
                refreshMataKuliah();
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(SidebarPanel.this);
                if (topFrame instanceof MainFrame) {
                    ((MainFrame) topFrame).showTasksByMataKuliah(mk.getId());
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                nama.setForeground(isSelected ? activeText : hoverText);
                delBtn.setVisible(true);
                item.repaint();
                delBtn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                nama.setForeground(isSelected ? activeText : normalText);
                item.repaint();
                delBtn.repaint();
            }
        };

        item.addMouseListener(hoverLogic);
        delBtn.addMouseListener(hoverLogic);
        leftPanel.addMouseListener(hoverLogic);
        dot.addMouseListener(hoverLogic);

        return item;
    }

    /**
     * Dialog custom konfirmasi hapus mata kuliah.
     */
    private void showDeleteMkConfirmation(MataKuliah mk) {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        boolean[] confirmed = {false};

        JDialog dialog = new JDialog(topFrame, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 20, 20);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(28, 32, 24, 32));

        // Warning Icon
        JLabel icon = new JLabel("\u26A0\uFE0F");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setBorder(new EmptyBorder(8, 0, 0, 0));
        icon.setAlignmentX(CENTER_ALIGNMENT);
        content.add(icon);
        content.add(Box.createVerticalStrut(12));

        // Title
        JLabel title = new JLabel("Hapus Mata Kuliah?");
        title.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 16));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(8));

        // Nama MK
        JLabel mkName = new JLabel("\"" + mk.getNama() + "\"");
        mkName.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 14));
        mkName.setForeground(ColorPalette.ACCENT_BLUE);
        mkName.setAlignmentX(CENTER_ALIGNMENT);
        content.add(mkName);
        content.add(Box.createVerticalStrut(6));

        // Subtitle
        JLabel subtitle = new JLabel("Tugas terkait ikut terhapus.");
        subtitle.setFont(ColorPalette.FONT_BODY);
        subtitle.setForeground(ColorPalette.TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(24));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(CENTER_ALIGNMENT);

        JButton cancelBtn = new JButton("Batal") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cancelBtn.setFont(ColorPalette.FONT_BUTTON);
        cancelBtn.setForeground(ColorPalette.TEXT_PRIMARY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { cancelBtn.setBackground(new Color(245, 245, 245)); }
            @Override public void mouseExited(MouseEvent e) { cancelBtn.setBackground(Color.WHITE); }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton deleteBtn = new JButton("Ya, Hapus") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        deleteBtn.setFont(ColorPalette.FONT_BUTTON);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(ColorPalette.PRIORITY_HIGH);
        deleteBtn.setPreferredSize(new Dimension(100, 36));
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { deleteBtn.setBackground(new Color(200, 40, 40)); }
            @Override public void mouseExited(MouseEvent e) { deleteBtn.setBackground(ColorPalette.PRIORITY_HIGH); }
        });
        deleteBtn.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        btnRow.add(cancelBtn);
        btnRow.add(deleteBtn);
        content.add(btnRow);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(topFrame);
        dialog.setVisible(true);

        if (confirmed[0]) {
            taskService.deleteMataKuliah(mk.getId());
            if (topFrame instanceof MainFrame) {
                ((MainFrame) topFrame).refreshAll();
            } else {
                refreshMataKuliah();
            }
        }
    }

    /**
     * Update angka badge di sidebar.
     */
    public void refreshBadges() {
        int totalTasks = taskService.getTotalTasks();
        int todayCount = taskService.getTasksToday().size();
        int completedCount = taskService.getTasksByStatus(
            model.Task.Status.SELESAI).size();

        badgeSemua.setText(String.valueOf(totalTasks));
        badgeHariIni.setText(String.valueOf(todayCount));
        badgeSelesai.setText(String.valueOf(completedCount));
        repaint();
    }

    /**
     * Refresh daftar mata kuliah di sidebar.
     */
    public void refreshMataKuliah() {
        mkContainer.removeAll();
        java.util.List<MataKuliah> mataKuliahList = taskService.getAllMataKuliah();
        for (MataKuliah mk : mataKuliahList) {
            JPanel mkItem = createMataKuliahItem(mk);
            mkContainer.add(mkItem);
            mkContainer.add(Box.createVerticalStrut(8));
        }
        mkContainer.revalidate();
        mkContainer.repaint();
    }

    /**
     * Logout — tampilkan konfirmasi custom, tutup MainFrame, buka LoginFrame.
     */
    private void doLogout() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        // ── Custom Confirmation Dialog ──
        JDialog dialog = new JDialog(topFrame, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        // Result holder
        final boolean[] confirmed = {false};

        // Main content panel with rounded corners
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 20, 20);
                // Background
                g2.setColor(ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(28, 32, 24, 32));



        // Title
        JLabel title = new JLabel(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Keluar dari Akun?" : "Logout from Account?");
        title.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 16));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(6));

        // Subtitle
        JLabel subtitle = new JLabel(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Kamu akan kembali ke halaman login." : "You will return to the login page.");
        subtitle.setFont(ColorPalette.FONT_BODY);
        subtitle.setForeground(ColorPalette.TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(22));

        // Button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(CENTER_ALIGNMENT);

        // Cancel button
        JButton cancelBtn = new JButton(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Batal" : "Cancel") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cancelBtn.setFont(ColorPalette.FONT_BUTTON);
        cancelBtn.setForeground(ColorPalette.TEXT_PRIMARY);
        cancelBtn.setBackground(ColorPalette.isDarkMode ? ColorPalette.BG_CARD : Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(125, 36));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { cancelBtn.setBackground(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2B) : new Color(245, 245, 245)); }
            @Override
            public void mouseExited(MouseEvent e) { cancelBtn.setBackground(ColorPalette.isDarkMode ? ColorPalette.BG_CARD : Color.WHITE); }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        // Logout button
        JButton logoutBtn = new JButton(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Ya, Keluar" : "Yes, Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(ColorPalette.FONT_BUTTON);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(ColorPalette.PRIORITY_HIGH);
        logoutBtn.setPreferredSize(new Dimension(125, 36));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(new Color(170, 50, 50)); }
            @Override
            public void mouseExited(MouseEvent e) { logoutBtn.setBackground(ColorPalette.PRIORITY_HIGH); }
        });
        logoutBtn.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        btnRow.add(cancelBtn);
        btnRow.add(logoutBtn);
        content.add(btnRow);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(topFrame);
        dialog.setVisible(true);

        // After dialog closes
        if (confirmed[0]) {
            topFrame.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }

    /**
     * Dialog ganti password.
     */
    private void showChangePasswordDialog() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        JDialog dialog = new JDialog(topFrame, true);
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

        // Icon
        JLabel icon = new JLabel("\uD83D\uDD11");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setBorder(new EmptyBorder(8, 0, 0, 0)); // Padding atas untuk cegah emoji kepotong
        icon.setAlignmentX(CENTER_ALIGNMENT);
        content.add(icon);
        content.add(Box.createVerticalStrut(10));

        // Title
        JLabel title = new JLabel(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Ganti Password" : "Change Password");
        title.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 17));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(24));

        // Form (Vertical Stack Style)
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(CENTER_ALIGNMENT);

        String[] labels = TranslationManager.currentLanguage == TranslationManager.Language.ID ? 
            new String[]{"Password Lama", "Password Baru", "Konfirmasi Password"} : 
            new String[]{"Old Password", "New Password", "Confirm Password"};
        JPasswordField[] fields = new JPasswordField[3];

        for (int i = 0; i < 3; i++) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(280, 54));
            
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
            lbl.setForeground(ColorPalette.TEXT_SECONDARY);
            lbl.setBorder(new EmptyBorder(0, 2, 6, 0));
            
            fields[i] = new RoundedPasswordField("");
            fields[i].setFont(ColorPalette.FONT_BODY);
            fields[i].setBackground(ColorPalette.isDarkMode ? new Color(0x2E, 0x2E, 0x2E) : new Color(250, 250, 250));
            fields[i].setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
            fields[i].setCaretColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
            
            row.add(lbl, BorderLayout.NORTH);
            row.add(fields[i], BorderLayout.CENTER);
            
            form.add(row);
            if (i < 2) form.add(Box.createVerticalStrut(14));
        }

        JPasswordField oldPass = fields[0];
        JPasswordField newPass = fields[1];
        JPasswordField confirmPass = fields[2];

        content.add(form);
        content.add(Box.createVerticalStrut(30));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(CENTER_ALIGNMENT);

        JButton cancelBtn = new JButton(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Batal" : "Cancel") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cancelBtn.setFont(ColorPalette.FONT_BUTTON);
        cancelBtn.setForeground(ColorPalette.TEXT_PRIMARY);
        cancelBtn.setBackground(ColorPalette.isDarkMode ? ColorPalette.BG_CARD : Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { cancelBtn.setBackground(ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2B) : new Color(245, 245, 245)); }
            @Override
            public void mouseExited(MouseEvent e) { cancelBtn.setBackground(ColorPalette.isDarkMode ? ColorPalette.BG_CARD : Color.WHITE); }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton(TranslationManager.currentLanguage == TranslationManager.Language.ID ? "Simpan" : "Save") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        saveBtn.setFont(ColorPalette.FONT_BUTTON);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(ColorPalette.BTN_ACTIVE);
        saveBtn.setPreferredSize(new Dimension(100, 36));
        saveBtn.setBorderPainted(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { saveBtn.setBackground(new Color(30, 80, 200)); }
            @Override
            public void mouseExited(MouseEvent e) { saveBtn.setBackground(ColorPalette.BTN_ACTIVE); }
        });
        // Result label untuk feedback inline
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(ColorPalette.FONT_SMALL);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(resultLabel);
        content.add(Box.createVerticalStrut(8));

        saveBtn.addActionListener(e -> {
            String oldP = new String(oldPass.getPassword());
            String newP = new String(newPass.getPassword());
            String confP = new String(confirmPass.getPassword());

            Mahasiswa user = taskService.getLoggedInUser();

            // Ambil hash terbaru dari database untuk verifikasi
            database.MahasiswaDAO dao = new database.MahasiswaDAO();
            String storedHash = dao.getPasswordHashById(user.getId());

            if (storedHash == null || !util.PasswordUtil.verify(oldP, storedHash)) {
                resultLabel.setForeground(ColorPalette.PRIORITY_HIGH);
                resultLabel.setText("Password lama salah!");
                return;
            }
            if (newP.isEmpty() || newP.length() < 4) {
                resultLabel.setForeground(ColorPalette.PRIORITY_HIGH);
                resultLabel.setText("Password baru minimal 4 karakter!");
                return;
            }
            if (!newP.equals(confP)) {
                resultLabel.setForeground(ColorPalette.PRIORITY_HIGH);
                resultLabel.setText("Konfirmasi password tidak cocok!");
                return;
            }

            // Hash password baru dan update via DAO
            String newHash = util.PasswordUtil.hash(newP);
            if (dao.updatePassword(user.getId(), newHash)) {
                user.setPassword(newHash);
                resultLabel.setForeground(new Color(16, 130, 70));
                resultLabel.setText("✓ Password berhasil diubah!");
                // Auto-close setelah 1.5 detik
                Timer timer = new Timer(1500, evt -> dialog.dispose());
                timer.setRepeats(false);
                timer.start();
            } else {
                resultLabel.setForeground(ColorPalette.PRIORITY_HIGH);
                resultLabel.setText("Gagal mengubah password!");
            }
        });


        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        content.add(btnRow);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(topFrame);
        dialog.setVisible(true);
    }

    /**
     * Tampilkan popup profile menggunakan custom JDialog.
     * JMenuItem selalu mereserve space untuk icon di kiri,
     * jadi kita pakai JLabel di dalam JDialog untuk kontrol penuh.
     */
    private void showProfilePopup(JPanel anchor) {
        JDialog popup = new JDialog((Frame) SwingUtilities.getWindowAncestor(this));
        popup.setUndecorated(true);
        popup.setBackground(new Color(0, 0, 0, 0));
        popup.setAlwaysOnTop(true);

        Color popupBg = ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE;
        Color borderCol = ColorPalette.isDarkMode ? new Color(0x3A, 0x3A, 0x3A) : ColorPalette.BORDER_LIGHT;
        Color changePassHover = ColorPalette.isDarkMode ? new Color(0x2E, 0x2E, 0x2E) : new Color(240, 240, 245);
        Color logoutHover = ColorPalette.isDarkMode ? new Color(0x4A, 0x20, 0x20) : new Color(255, 235, 235);
        Color sepCol = ColorPalette.isDarkMode ? new Color(0x33, 0x33, 0x33) : new Color(230, 230, 230);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(popupBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(borderCol);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(6, 6, 6, 6));
        content.setOpaque(false);

        // Item: Ganti Password
        JPanel changePassItem = createPopupItem("KEY", TranslationManager.get("btn_ganti_password"), ColorPalette.TEXT_PRIMARY, changePassHover);
        changePassItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.dispose();
                showChangePasswordDialog();
            }
        });
        content.add(changePassItem);

        // Separator 1
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(sepCol);
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        content.add(sep1);

        // Item: Ganti Bahasa
        JPanel languageItem = createPopupItem("GLOBE", TranslationManager.get("btn_ganti_bahasa"), ColorPalette.TEXT_PRIMARY, changePassHover);
        languageItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.dispose();
                if (TranslationManager.currentLanguage == TranslationManager.Language.ID) {
                    TranslationManager.currentLanguage = TranslationManager.Language.EN;
                } else {
                    TranslationManager.currentLanguage = TranslationManager.Language.ID;
                }
                taskService.saveSettings(ColorPalette.isDarkMode, TranslationManager.currentLanguage.name());
                if (mainFrame != null) {
                    mainFrame.updateLanguage();
                }
            }
        });
        content.add(languageItem);

        // Separator 2
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(sepCol);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        content.add(sep2);

        // Item: Keluar
        JPanel logoutItem = createPopupItem("LOGOUT", TranslationManager.get("btn_keluar"), ColorPalette.PRIORITY_HIGH, logoutHover);
        logoutItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.dispose();
                doLogout();
            }
        });
        content.add(logoutItem);

        popup.setContentPane(content);
        popup.pack();

        // Posisi popup di atas anchor, center
        Point loc = anchor.getLocationOnScreen();
        int x = loc.x + (anchor.getWidth() - popup.getWidth()) / 2;
        int y = loc.y - popup.getHeight() - 4;
        popup.setLocation(x, y);

        // Auto-close saat klik di luar
        popup.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                popup.dispose();
            }
        });

        popup.setVisible(true);
    }

    /**
     * Helper: Membuat item popup dengan ikon yang digambar via Graphics2D.
     * iconType: "KEY", "GLOBE", "LOGOUT"
     */
    private JPanel createPopupItem(String iconType, String text, Color textColor, Color hoverColor) {
        JPanel item = new JPanel(new BorderLayout(8, 0)) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(hoverColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(8, 12, 8, 16));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        // Icon panel (16x16 drawn via Graphics2D)
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(textColor);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                switch (iconType) {
                    case "KEY":
                        // Circle (head of key)
                        g2.drawOval(cx - 6, cy - 4, 7, 7);
                        // Shaft
                        g2.drawLine(cx + 1, cy, cx + 7, cy);
                        // Teeth
                        g2.drawLine(cx + 5, cy, cx + 5, cy + 2);
                        g2.drawLine(cx + 7, cy, cx + 7, cy + 2);
                        break;
                    case "GLOBE":
                        // Outer circle
                        g2.drawOval(cx - 6, cy - 6, 12, 12);
                        // Horizontal line
                        g2.drawLine(cx - 6, cy, cx + 6, cy);
                        // Vertical ellipse lines
                        g2.drawArc(cx - 3, cy - 6, 6, 12, 0, 360);
                        break;
                    case "LOGOUT":
                        // Bracket/Door frame (open on the right side)
                        g2.drawLine(cx - 5, cy - 5, cx - 5, cy + 5);
                        g2.drawLine(cx - 5, cy - 5, cx + 1, cy - 5);
                        g2.drawLine(cx - 5, cy + 5, cx + 1, cy + 5);
                        // Arrow pointing right (shaft + smaller arrow head)
                        g2.drawLine(cx - 2, cy, cx + 5, cy); // Shaft
                        g2.drawLine(cx + 2, cy - 3, cx + 5, cy); // Top head wing
                        g2.drawLine(cx + 2, cy + 3, cx + 5, cy); // Bottom head wing
                        break;

                    default:
                        // Dot fallback
                        g2.fillOval(cx - 3, cy - 3, 6, 6);
                }
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(16, 16));

        JLabel label = new JLabel(text);
        label.setFont(ColorPalette.FONT_BODY);
        label.setForeground(textColor);

        item.add(iconPanel, BorderLayout.WEST);
        item.add(label, BorderLayout.CENTER);

        return item;
    }

    /**
     * Memperbarui teks sidebar panel saat bahasa diubah.
     */
    public void updateLanguage() {
        if (menuLabelHeader != null) menuLabelHeader.setText("MENU");
        if (mkLabelHeader != null) mkLabelHeader.setText(TranslationManager.get("menu_matkul"));

        if (labelSemua != null) labelSemua.setText(TranslationManager.get("menu_semua"));
        if (labelHariIni != null) labelHariIni.setText(TranslationManager.get("menu_hari_ini"));
        if (labelSelesai != null) labelSelesai.setText(TranslationManager.get("menu_selesai"));

        repaint();
    }

    /** Membuat icon panel buku yang digambar via Graphics2D — tidak bergantung pada font emoji. */
    private JPanel createBookIcon(boolean active) {
        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Warna adaptif: aktif = biru, tidak aktif = secondary text sesuai tema
                boolean isActive = (this.getParent() != null && this.getParent().getParent() == activeMenu);
                Color iconColor = isActive
                    ? ColorPalette.ACCENT_BLUE
                    : ColorPalette.TEXT_SECONDARY;
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                // Cover buku (kiri)
                g2.drawRoundRect(cx - 7, cy - 6, 5, 11, 2, 2);
                // Cover buku (kanan)
                g2.drawRoundRect(cx - 2, cy - 6, 8, 11, 2, 2);
                // Spine / garis tengah
                g2.drawLine(cx - 2, cy - 5, cx - 2, cy + 4);
                // Lines in book (halaman)
                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, cy - 3, cx + 4, cy - 3);
                g2.drawLine(cx, cy, cx + 4, cy);
                g2.drawLine(cx, cy + 3, cx + 4, cy + 3);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(16, 16));
        return icon;
    }

    /** Membungkus icon panel + JLabel dalam satu JPanel horizontal. */
    private JPanel createMenuLabelPanel(JPanel iconPanel, JLabel label) {
        // Set warna awal agar tidak default hitam saat dark mode
        label.setFont(ColorPalette.FONT_BODY);
        label.setForeground(ColorPalette.TEXT_DARK);
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.add(iconPanel, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }
}
