package ui;

import model.AnggotaKelompok;
import model.MataKuliah;
import model.Task;
import util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 *
 */
import service.TaskService;

public class TaskDetailDialog extends JDialog {

    // Warna khusus dialog ini
    private static final Color BG_SECTION = new Color(0xF4, 0xF4, 0xF5);
    private static final Color AVATAR_BLUE = new Color(59, 130, 246);
    private static final Color AVATAR_GREEN = new Color(34, 197, 94);
    private static final Color AVATAR_PURPLE = new Color(147, 51, 234);
    private static final Color AVATAR_ORANGE = new Color(234, 138, 51);
    private static final Color AVATAR_PINK = new Color(220, 60, 120);
    private static final Color[] AVATAR_COLORS = {
        AVATAR_BLUE, AVATAR_GREEN, AVATAR_PURPLE, AVATAR_ORANGE, AVATAR_PINK
    };

    public TaskDetailDialog(JFrame owner, Task task, TaskService taskService) {
        super(owner, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Main Card Panel (rounded + shadow)
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Soft shadow
                for (int i = 5; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 6 * (6 - i)));
                    g2.fill(new RoundRectangle2D.Float(i, i, getWidth() - i * 2, getHeight() - i * 2, 28, 28));
                }
                // Card background
                Color bg = ColorPalette.isDarkMode ? new Color(0x18, 0x18, 0x18) : Color.WHITE;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 6, getHeight() - 6, 28, 28));
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(0, 0, 6, 6));

        // Custom Header (Draggable + Close Button + Separator)
        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(28, 28, 20, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel judulLabel = new JLabel(task.getJudul());
        judulLabel.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 20));
        judulLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        judulLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(judulLabel);

        String mkNama = task.getMataKuliah() != null ? task.getMataKuliah().getNama() : (util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Umum" : "General");
        JLabel mkLabel = new JLabel(mkNama);
        mkLabel.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 14));
        mkLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        mkLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(mkLabel);

        headerPanel.add(titleBlock, BorderLayout.CENTER);

        // Close Button (X)
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

        mainPanel.add(headerWrapper, BorderLayout.NORTH);

        // Scrollable Content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 28, 24, 28));


        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 16, 16));
        gridPanel.setOpaque(false);
        gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        gridPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // Deadline
        String deadlineStr = task.getDeadline() != null
            ? task.getDeadline().toString() + "\n(" + task.getDeadlineLabel() + ")"
            : util.TranslationManager.get("unit_tidak_ada");
        gridPanel.add(createInfoCard(util.TranslationManager.get("label_deadline"), deadlineStr, ColorPalette.TEXT_PRIMARY, IconType.CALENDAR));

        // Prioritas
        Color priColor = getPriorityColor(task.getPrioritas());
        String prioText;
        switch (task.getPrioritas()) {
            case TINGGI: prioText = util.TranslationManager.get("priority_high"); break;
            case RENDAH: prioText = util.TranslationManager.get("priority_low"); break;
            default: prioText = util.TranslationManager.get("priority_medium"); break;
        }
        gridPanel.add(createInfoCard(util.TranslationManager.get("label_prioritas"), prioText, priColor, IconType.FLAG));

        // Status
        String statusText;
        switch (task.getStatus()) {
            case BELUM: statusText = util.TranslationManager.get("status_belum"); break;
            case SEDANG: statusText = util.TranslationManager.get("status_sedang"); break;
            default: statusText = util.TranslationManager.get("status_selesai"); break;
        }
        gridPanel.add(createInfoCard(util.TranslationManager.get("label_status"), statusText, ColorPalette.TEXT_PRIMARY, IconType.STATUS));

        // Jenis Tugas
        String jenisTugasText;
        if (task.getJenis() == Task.Jenis.KELOMPOK) {
            jenisTugasText = util.TranslationManager.get("jenis_kelompok");
        } else {
            jenisTugasText = util.TranslationManager.get("jenis_individu");
        }
        IconType jenisIcon = IconType.PERSON;
        if (task.getJenis() == Task.Jenis.KELOMPOK) {
            String groupNum = "";
            for (model.AnggotaKelompok ak : task.getAnggotaList()) {
                if ("#GROUP_NUM#".equals(ak.getPeran())) {
                    groupNum = " " + ak.getNamaAnggota();
                    break;
                }
            }
            if (!groupNum.isEmpty()) {
                jenisTugasText += groupNum;
            } else {
                // Fallback (for older tasks or tasks without group number)
                int validCount = (int) task.getAnggotaList().stream().filter(ak -> !"#GROUP_NUM#".equals(ak.getPeran())).count();
                if (validCount > 0) {
                    jenisTugasText += " " + validCount;
                }
            }
            jenisIcon = IconType.GROUP;
        }
        gridPanel.add(createInfoCard(util.TranslationManager.get("label_jenis_tugas"), jenisTugasText, ColorPalette.TEXT_PRIMARY, jenisIcon));

        content.add(gridPanel);
        content.add(Box.createVerticalStrut(20));


        JPanel descCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgSec = ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : BG_SECTION;
                g2.setColor(bgSec);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
            @Override
            public Dimension getMaximumSize() {
                // Memaksa tinggi maksimal = tinggi ideal (tidak melar ke bawah)
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        descCard.setOpaque(false);
        descCard.setLayout(new BoxLayout(descCard, BoxLayout.Y_AXIS));
        descCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        descCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descTitle = new JLabel(util.TranslationManager.get("label_deskripsi"));
        descTitle.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
        descTitle.setForeground(ColorPalette.TEXT_SECONDARY);
        descTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        descCard.add(descTitle);
        descCard.add(Box.createVerticalStrut(6));

        String descText = (task.getDeskripsi() != null && !task.getDeskripsi().isEmpty())
            ? task.getDeskripsi() : "";
        JTextArea descArea = new JTextArea(descText) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        descArea.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 13));
        descArea.setForeground(ColorPalette.TEXT_PRIMARY);
        descArea.setCaretColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(true); // Deskripsi bisa di-edit
        descArea.setOpaque(false);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        descArea.insert("\n", descArea.getCaretPosition());
                        e.consume();
                    } else {
                        e.consume(); // Prevent default newline insert
                        String newDesc = descArea.getText().trim();
                        if (!newDesc.equals(task.getDeskripsi() != null ? task.getDeskripsi() : "")) {
                            taskService.updateDeskripsi(task, newDesc);
                        }
                        dispose(); // Close dialog on enter
                    }
                }
            }
        });
        descCard.add(descArea);

        content.add(descCard);


        if (task.getJenis() == Task.Jenis.KELOMPOK) {
            content.add(Box.createVerticalStrut(20));

            // Filter out the dummy member
            List<model.AnggotaKelompok> list = new java.util.ArrayList<>();
            if (task.getAnggotaList() != null) {
                for (model.AnggotaKelompok ak : task.getAnggotaList()) {
                    if (!"#GROUP_NUM#".equals(ak.getPeran())) {
                        list.add(ak);
                    }
                }
            }

            JLabel anggotaTitle = new JLabel((util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Anggota: " : "Members: ") + list.size());
            anggotaTitle.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
            anggotaTitle.setForeground(ColorPalette.TEXT_SECONDARY);
            anggotaTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(anggotaTitle);
            content.add(Box.createVerticalStrut(10));

            if (list.isEmpty()) {
                JLabel empty = new JLabel(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Belum ada anggota yang ditambahkan." : "No members added yet.");
                empty.setFont(new Font("Plus Jakarta Sans", Font.ITALIC, 12));
                empty.setForeground(ColorPalette.TEXT_LIGHT);
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(empty);
            } else {
                for (int i = 0; i < list.size(); i++) {
                    model.AnggotaKelompok ak = list.get(i);
                    content.add(createAnggotaRow(ak, i));
                    content.add(Box.createVerticalStrut(8));
                }
            }
        }


        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(createSeparator(), BorderLayout.NORTH);

        JPanel bottomInner = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomInner.setOpaque(false);
        bottomInner.setBorder(new EmptyBorder(16, 28, 28, 28));

        // Simpan & Tutup Button
        JButton tutupBtn = createTutupButton(task, taskService, descArea);
        bottomInner.add(tutupBtn);

        bottomWrapper.add(bottomInner, BorderLayout.CENTER);
        mainPanel.add(bottomWrapper, BorderLayout.SOUTH);

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // Hide scrollbar visually but keep scroll functionality
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setSize(400, 540);
        setLocationRelativeTo(owner);
    }


    private enum IconType { CALENDAR, FLAG, STATUS, GROUP, PERSON }


    private JPanel createInfoCard(String label, String value, Color valueColor, IconType iconType) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setOpaque(false);

        // Icon wrapper
        JPanel iconWrapper = new JPanel(new GridBagLayout());
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(24, 0)); // fixed width

        // Icon (hand-drawn)
        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;

                switch (iconType) {
                    case CALENDAR:
                        paintCalendarIcon(g2, cx, cy);
                        break;
                    case FLAG:
                        paintFlagIcon(g2, cx, cy, valueColor);
                        break;
                    case STATUS:
                        paintCheckIcon(g2, cx, cy);
                        break;
                    case GROUP:
                        paintGroupIcon(g2, cx, cy);
                        break;
                    case PERSON:
                        paintPersonIcon(g2, cx, cy);
                        break;
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        iconWrapper.add(icon);

        card.add(iconWrapper, BorderLayout.WEST);

        // Text block
        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 11));
        labelLbl.setForeground(ColorPalette.TEXT_SECONDARY);
        labelLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        textBlock.add(labelLbl);
        textBlock.add(Box.createRigidArea(new Dimension(0, 2)));

        // Handle multi-line value (split by \n)
        String[] lines = value.split("\n");
        for (String line : lines) {
            JLabel valueLbl = new JLabel(line);
            valueLbl.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
            valueLbl.setForeground(valueColor);
            valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            textBlock.add(valueLbl);
        }

        card.add(textBlock, BorderLayout.CENTER);
        return card;
    }


    /** 📅 Calendar Icon */
    private void paintCalendarIcon(Graphics2D g2, int cx, int cy) {
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(ColorPalette.TEXT_PRIMARY);
        
        // Body (8px up from center, 8px down)
        g2.drawRoundRect(cx - 8, cy - 7, 16, 14, 3, 3);
        // Top hooks
        g2.drawLine(cx - 4, cy - 8, cx - 4, cy - 10);
        g2.drawLine(cx + 4, cy - 8, cx + 4, cy - 10);
        // Date dots
        for (int i = 0; i < 3; i++) {
            g2.fillOval(cx - 4 + (i * 4), cy + 2, 2, 2);
        }
    }

    /** 🚩 Flag / Priority Icon */
    private void paintFlagIcon(Graphics2D g2, int cx, int cy, Color priorityColor) {
        g2.setStroke(new BasicStroke(1.6f));
        // Pole
        g2.setColor(ColorPalette.TEXT_PRIMARY);
        g2.drawLine(cx - 6, cy - 8, cx - 6, cy + 8);
        // Flag fill
        g2.setColor(priorityColor);
        java.awt.geom.Path2D flag = new java.awt.geom.Path2D.Float();
        flag.moveTo(cx - 5, cy - 7);
        flag.lineTo(cx + 6, cy - 3);
        flag.lineTo(cx - 5, cy + 1);
        flag.closePath();
        g2.fill(flag);
    }

    /** ⏳ Status Icon (circle with progress) */
    private void paintCheckIcon(Graphics2D g2, int cx, int cy) {
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(ColorPalette.TEXT_PRIMARY);
        // Circle
        g2.drawOval(cx - 8, cy - 8, 16, 16);
        // Checkmark
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx - 3, cy, cx - 1, cy + 3);
        g2.drawLine(cx - 1, cy + 3, cx + 4, cy - 4);
    }

    /** 👥 Group Icon */
    private void paintGroupIcon(Graphics2D g2, int cx, int cy) {
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(ColorPalette.TEXT_PRIMARY);
        
        // BACK person (smaller, shifted right)
        g2.drawOval(cx + 4, cy - 8, 5, 5);
        g2.drawArc(cx + 2, cy - 2, 9, 7, 0, 180);
        
        // FRONT person (larger, shifted left)
        g2.drawOval(cx - 5, cy - 7, 7, 7);
        g2.drawArc(cx - 8, cy + 1, 13, 10, 0, 180);
    }

    /** 👤 Single Person Icon */
    private void paintPersonIcon(Graphics2D g2, int cx, int cy) {
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(ColorPalette.TEXT_PRIMARY);
        // Head
        g2.drawOval(cx - 4, cy - 8, 8, 8);
        // Body
        g2.drawArc(cx - 7, cy + 1, 14, 11, 0, 180);
    }


    private JPanel createAnggotaRow(AnggotaKelompok ak, int index) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Avatar circle with initials
        Color avatarColor = AVATAR_COLORS[index % AVATAR_COLORS.length];
        String initials = getInitials(ak.getNamaAnggota());

        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Circle
                g2.setColor(avatarColor);
                g2.fillOval(2, 2, 32, 32);
                // Initials
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        row.add(avatar, BorderLayout.WEST);

        // Name + Role
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel namaLbl = new JLabel(ak.getNamaAnggota());
        namaLbl.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 13));
        namaLbl.setForeground(ColorPalette.TEXT_PRIMARY);
        namaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(namaLbl);

        String peran = ak.getPeran();
        if (peran != null && !peran.isEmpty()) {
            JLabel peranLbl = new JLabel(peran);
            peranLbl.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 11));
            peranLbl.setForeground(ColorPalette.TEXT_SECONDARY);
            peranLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            textPanel.add(peranLbl);
        }

        row.add(textPanel, BorderLayout.CENTER);
        return row;
    }


    private JPanel createStatusPill(Task task) {
        JPanel pill = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int pillH = 8;
                int y = (h - pillH) / 2;

                // Background track
                Color trackBg = ColorPalette.isDarkMode ? new Color(0x33, 0x33, 0x33) : new Color(230, 233, 240);
                g2.setColor(trackBg);
                g2.fillRoundRect(0, y, w, pillH, pillH, pillH);

                // Progress fill
                float progress = 0f;
                Color fillColor = ColorPalette.ACCENT_BLUE;
                switch (task.getStatus()) {
                    case BELUM:
                        progress = 0.05f;
                        fillColor = ColorPalette.TEXT_LIGHT;
                        break;
                    case SEDANG:
                        progress = 0.5f;
                        fillColor = ColorPalette.ACCENT_BLUE;
                        break;
                    case SELESAI:
                        progress = 1.0f;
                        fillColor = ColorPalette.ACCENT_GREEN;
                        break;
                }

                int fillW = Math.max(pillH, (int)(w * progress));
                g2.setColor(fillColor);
                g2.fillRoundRect(0, y, fillW, pillH, pillH, pillH);

                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.setPreferredSize(new Dimension(140, 36));
        return pill;
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

    private JPanel createSeparator() {
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Color sepColor = ColorPalette.isDarkMode ? new Color(0x33, 0x33, 0x33) : new Color(235, 238, 245);
                g2.setColor(sepColor);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        sep.setOpaque(false);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private JButton createTutupButton(Task task, TaskService taskService, JTextArea descArea) {
        JButton btn = new JButton(util.TranslationManager.get("btn_simpan_tutup")) {
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

                // Solid blue background
                g2.setColor(ColorPalette.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Hover overlay
                if (hovered) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            String newDesc = descArea.getText().trim();
            if (!newDesc.equals(task.getDeskripsi() != null ? task.getDeskripsi() : "")) {
                taskService.updateDeskripsi(task, newDesc);
            }
            dispose();
        });
        return btn;
    }


    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }


    private Color getPriorityColor(Task.Prioritas p) {
        switch (p) {
            case TINGGI: return ColorPalette.PRIORITY_HIGH;
            case SEDANG: return ColorPalette.PRIORITY_MEDIUM;
            case RENDAH: return ColorPalette.PRIORITY_LOW;
            default:     return ColorPalette.TEXT_PRIMARY;
        }
    }
}
