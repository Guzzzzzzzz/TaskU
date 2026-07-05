package ui;

import model.Task;
import model.MataKuliah;
import service.TaskService;
import util.ColorPalette;
import util.DateHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 *
 *  Layout (GridBagLayout, 1 column):
 *  ┌─────────────────────────────────────────┐
 *  │ Task Title                          ●→× │  Row 0: title + priority dot/delete
 *  │ [PBO] [Kelompok]                        │  Row 1: colored badges
 *  │ Deadline: Hari ini                      │  Row 2: deadline
 *  └─────────────────────────────────────────┘
 */
public class TaskCardPanel extends JPanel {

    private boolean isHovered = false;
    private Color currentBorderColor = ColorPalette.BORDER_CARD;

    public TaskCardPanel(Task task, TaskService taskService, MainFrame mainFrame) {

        setOpaque(false);
        setLayout(new BorderLayout());
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect on entire card
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                currentBorderColor = ColorPalette.BORDER_HOVER;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                Point p = getMousePosition(true);
                if (p == null) {
                    isHovered = false;
                    currentBorderColor = ColorPalette.BORDER_CARD;
                    repaint();
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    new TaskDetailDialog(mainFrame, task, taskService).setVisible(true);
                });
            }
        });

        // Inner content (GridBagLayout)
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Row 0: Checkbox + Title + Priority Dot
        gbc.gridy = 0;
        JPanel titleRow = new JPanel(new BorderLayout(10, 0));
        titleRow.setOpaque(false);

        // Custom checkbox circle (left side)
        boolean done = task.isCompleted();
        CheckCircle checkCircle = new CheckCircle(done, task, taskService, mainFrame);
        titleRow.add(checkCircle, BorderLayout.WEST);

        // Title label with strikethrough if completed
        JLabel judulLabel = new JLabel(done
            ? "<html><s>" + task.getJudul() + "</s></html>"
            : task.getJudul());
        judulLabel.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 14));
        judulLabel.setForeground(done ? ColorPalette.TEXT_SECONDARY : ColorPalette.TEXT_PRIMARY);
        checkCircle.setTitleLabel(judulLabel);
        titleRow.add(judulLabel, BorderLayout.CENTER);

        if (!done) {
            Color prioColor = switch (task.getPrioritas()) {
                case TINGGI -> ColorPalette.PRIORITY_HIGH;
                case SEDANG -> ColorPalette.PRIORITY_MEDIUM;
                case RENDAH -> ColorPalette.PRIORITY_LOW;
            };
            JComponent dotButton = new PriorityDotButton(prioColor, task, taskService, mainFrame);
            titleRow.add(dotButton, BorderLayout.EAST);
        }

        content.add(titleRow, gbc);

        // Row 1: Badges (Mata Kuliah + Jenis)
        gbc.gridy = 1;
        JPanel badgeRow = new JPanel();
        badgeRow.setLayout(new BoxLayout(badgeRow, BoxLayout.X_AXIS));
        badgeRow.setOpaque(false);
        badgeRow.setBorder(BorderFactory.createEmptyBorder(5, 0, 4, 0));

        // BUG 1 FIX: Colored badge for mata kuliah
        if (task.getMataKuliah() != null) {
            MataKuliah mk = task.getMataKuliah();
            Color mkColor = mk.getWarnaColor();
            badgeRow.add(createColoredBadge(mk.getNama(), mkColor));
            badgeRow.add(Box.createRigidArea(new Dimension(6, 0)));
        }

        // Neutral badge for jenis
        String jenisText = util.TranslationManager.get("jenis_individu");
        if (task.getJenis() == Task.Jenis.KELOMPOK) {
            jenisText = util.TranslationManager.get("jenis_kelompok");
            if (task.getAnggotaList() != null) {
                for (model.AnggotaKelompok ak : task.getAnggotaList()) {
                    if ("#GROUP_NUM#".equals(ak.getPeran())) {
                        jenisText += " " + ak.getNamaAnggota();
                        break;
                    }
                }
            }
        }
        badgeRow.add(createNeutralBadge(jenisText));
        badgeRow.add(Box.createHorizontalGlue());

        content.add(badgeRow, gbc);

        // Row 2: Deadline
        gbc.gridy = 2;
        JPanel deadlinePanel = new JPanel();
        deadlinePanel.setLayout(new BoxLayout(deadlinePanel, BoxLayout.X_AXIS));
        deadlinePanel.setOpaque(false);
        deadlinePanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        if (task.getDeadline() != null) {
            JLabel deadlinePrefix = new JLabel("Deadline: ");
            deadlinePrefix.setFont(ColorPalette.FONT_BODY_SM);
            deadlinePrefix.setForeground(ColorPalette.TEXT_SECONDARY);
            deadlinePanel.add(deadlinePrefix);

            if (task.isCompleted()) {
                JLabel deadlineValue = new JLabel(DateHelper.formatShort(task.getDeadline()));
                deadlineValue.setFont(ColorPalette.FONT_BODY_SM);
                deadlineValue.setForeground(ColorPalette.TEXT_SECONDARY);
                deadlinePanel.add(deadlineValue);
            } else {
                long daysLeft = task.getDaysRemaining();
                String dateText;
                Color deadlineColor;

                if (daysLeft < 0) {
                    dateText = util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Lewat " + Math.abs(daysLeft) + " hari!" : "Overdue by " + Math.abs(daysLeft) + " days!";
                    deadlineColor = ColorPalette.PRIORITY_HIGH;
                } else if (daysLeft == 0) {
                    dateText = util.TranslationManager.get("unit_hari_ini");
                    deadlineColor = ColorPalette.PRIORITY_HIGH;
                } else if (daysLeft == 1) {
                    dateText = util.TranslationManager.get("unit_besok");
                    deadlineColor = ColorPalette.PRIORITY_HIGH;
                } else if (daysLeft <= 3) {
                    dateText = daysLeft + " " + util.TranslationManager.get("unit_hari_lagi");
                    deadlineColor = ColorPalette.PRIORITY_MEDIUM;
                } else {
                    dateText = DateHelper.formatShort(task.getDeadline());
                    deadlineColor = ColorPalette.ACCENT_BLUE;
                }

                JLabel deadlineValue = new JLabel(dateText);
                deadlineValue.setFont(new Font("Plus Jakarta Sans", daysLeft <= 1 ? Font.BOLD : Font.PLAIN, 13));
                deadlineValue.setForeground(deadlineColor);
                deadlinePanel.add(deadlineValue);
            }
        } else {
            JLabel noDeadline = new JLabel("Deadline: " + util.TranslationManager.get("unit_tidak_ada"));
            noDeadline.setFont(ColorPalette.FONT_BODY_SM);
            noDeadline.setForeground(ColorPalette.TEXT_LIGHT);
            deadlinePanel.add(noDeadline);
        }

        content.add(deadlinePanel, gbc);

        add(content, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isHovered ? ColorPalette.BG_HOVER : ColorPalette.BG_CARD);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(currentBorderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
    }


    private JLabel createColoredBadge(String text, Color mkColor) {
        Color bgColor = mkColor; // Solid pastel color

        // Calculate background brightness using the YIQ formula
        double brightness = (bgColor.getRed() * 299 + bgColor.getGreen() * 587 + bgColor.getBlue() * 114) / 1000.0;
        
        // Dynamic text color: Solid dark zinc-900 (#18181B) for light backgrounds, Solid White (#FFFFFF) for dark backgrounds
        Color textColor = (brightness > 135) ? new Color(0x18, 0x18, 0x1B) : Color.WHITE;

        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Solid background with fully rounded corners (pill shape)
                int radius = getHeight();
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                
                // Optional: Draw a very subtle 1px border if the background is extremely light/near-white
                if (brightness > 220) {
                    g2.setColor(new Color(0, 0, 0, 30)); // 12% opacity black border
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                }
                
                // Solid main text (no outlines/shadows)
                g2.setColor(textColor);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        badge.setFont(ColorPalette.FONT_BADGE);
        badge.setOpaque(false);
        FontMetrics fm = badge.getFontMetrics(ColorPalette.FONT_BADGE);
        // Padding: +20 horizontal (spacious but balanced), 24 height
        badge.setPreferredSize(new Dimension(fm.stringWidth(text) + 20, 24));
        return badge;
    }


    private JLabel createNeutralBadge(String text) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Solid light-gray background with fully rounded corners (pill shape) - Adaptive for Dark Mode
                g2.setColor(ColorPalette.isDarkMode ? new Color(255, 255, 255, 25) : new Color(0xF4, 0xF4, 0xF5));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                
                // Fine light border for neutral badge
                g2.setColor(ColorPalette.BORDER_LIGHT);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                
                g2.setColor(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.TEXT_DARK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        badge.setFont(ColorPalette.FONT_BADGE);
        badge.setOpaque(false);
        FontMetrics fm = badge.getFontMetrics(ColorPalette.FONT_BADGE);
        // Padding: +20 horizontal, 24 height
        badge.setPreferredSize(new Dimension(fm.stringWidth(text) + 20, 24));
        return badge;
    }


    private static class CheckCircle extends JComponent {
        private boolean checked;
        private boolean hovered = false;
        private JLabel titleLabel;
        private static final Color COLOR_DONE   = new Color(0x22, 0xC5, 0x5E); // green-500
        private static final Color COLOR_BORDER = new Color(0xD1, 0xD5, 0xDB); // gray-300

        public CheckCircle(boolean checked, Task task, TaskService taskService, MainFrame mainFrame) {
            this.checked = checked;
            setPreferredSize(new Dimension(22, 22));
            setMinimumSize(new Dimension(22, 22));
            setMaximumSize(new Dimension(22, 22));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { CheckCircle.this.hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { CheckCircle.this.hovered = false; repaint(); }
                @Override
                public void mouseClicked(MouseEvent e) {
                    e.consume(); // prevent card open
                    CheckCircle.this.checked = !CheckCircle.this.checked;
                    Task.Status newStatus = CheckCircle.this.checked ? Task.Status.SELESAI : Task.Status.BELUM;
                    taskService.updateStatus(task, newStatus);
                    // Update title label
                    if (CheckCircle.this.titleLabel != null) {
                        if (CheckCircle.this.checked) {
                            CheckCircle.this.titleLabel.setText("<html><s>" + task.getJudul() + "</s></html>");
                            CheckCircle.this.titleLabel.setForeground(ColorPalette.TEXT_SECONDARY);
                        } else {
                            CheckCircle.this.titleLabel.setText(task.getJudul());
                            CheckCircle.this.titleLabel.setForeground(ColorPalette.TEXT_PRIMARY);
                        }
                    }
                    repaint();
                    // Refresh parent after short delay to re-sort tasks
                    Timer t = new Timer(300, ev -> mainFrame.refreshAll());
                    t.setRepeats(false);
                    t.start();
                }
            });
        }

        public void setTitleLabel(JLabel label) {
            this.titleLabel = label;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int size = 18;
            int ox = (w - size) / 2, oy = (h - size) / 2;

            if (checked) {
                // Filled green circle
                g2.setColor(COLOR_DONE);
                g2.fillOval(ox, oy, size, size);
                // Checkmark
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xp = {ox + 4, ox + 7, ox + 14};
                int[] yp = {oy + 9, oy + 13, oy + 5};
                g2.drawPolyline(xp, yp, 3);
            } else {
                // Empty circle with border
                Color border = hovered ? new Color(0x9C, 0xA3, 0xAF) : COLOR_BORDER;
                g2.setColor(hovered ? new Color(0xF3, 0xF4, 0xF6) : Color.WHITE);
                g2.fillOval(ox, oy, size, size);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(ox, oy, size - 1, size - 1);
            }
            g2.dispose();
        }
    }


    private static class PriorityDotButton extends JComponent {

        private boolean hovered = false;
        private final Color prioColor;
        private static final Color DELETE_BG = new Color(0xFE, 0xE2, 0xE2); // #FEE2E2
        private static final Color DELETE_FG = new Color(0xEF, 0x44, 0x44); // #EF4444

        public PriorityDotButton(Color prioColor, Task task, TaskService taskService, MainFrame mainFrame) {
            this.prioColor = prioColor;
            setPreferredSize(new Dimension(22, 22));
            setToolTipText(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Hapus tugas" : "Delete task");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    e.consume(); // prevent card click
                    showDeleteConfirmation(task, taskService, mainFrame,
                        (JFrame) SwingUtilities.getWindowAncestor(PriorityDotButton.this));
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            if (hovered) {
                // Hover: 18px circle, red-light bg, custom painted X
                int size = 18;
                int ox = (w - size) / 2, oy = (h - size) / 2;
                g2.setColor(DELETE_BG);
                g2.fillOval(ox, oy, size, size);
                
                // Draw custom X
                g2.setColor(DELETE_FG);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 5; // padding relative to the 18px circle
                g2.drawLine(ox + pad, oy + pad, ox + size - pad, oy + size - pad);
                g2.drawLine(ox + size - pad, oy + pad, ox + pad, oy + size - pad);
            } else {
                // Default: 10px solid circle
                int size = 10;
                int ox = (w - size) / 2, oy = (h - size) / 2;
                g2.setColor(prioColor);
                g2.fillOval(ox, oy, size, size);
            }

            g2.dispose();
        }

        private void showDeleteConfirmation(Task task, TaskService taskService, MainFrame mainFrame, JFrame topFrame) {
            boolean[] confirmed = {false};

            JDialog dialog = new JDialog(topFrame, true);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));

            JPanel panel = new JPanel() {
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
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(28, 32, 24, 32));

            JLabel icon = new JLabel("\u26A0\uFE0F");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            icon.setAlignmentX(CENTER_ALIGNMENT);
            panel.add(icon);
            panel.add(Box.createVerticalStrut(12));

            JLabel titleLabel = new JLabel(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Hapus Tugas?" : "Delete Task?");
            titleLabel.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 15));
            titleLabel.setForeground(ColorPalette.TEXT_PRIMARY);
            titleLabel.setAlignmentX(CENTER_ALIGNMENT);
            panel.add(titleLabel);
            panel.add(Box.createVerticalStrut(4));

            JLabel nameLabel = new JLabel("\"" + task.getJudul() + "\"");
            nameLabel.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 15));
            nameLabel.setForeground(ColorPalette.ACCENT_BLUE);
            nameLabel.setAlignmentX(CENTER_ALIGNMENT);
            panel.add(nameLabel);
            panel.add(Box.createVerticalStrut(18));

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            btnRow.setOpaque(false);
            btnRow.setAlignmentX(CENTER_ALIGNMENT);

            JButton batalBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Batal" : "Cancel") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ColorPalette.BG_MAIN);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(ColorPalette.BORDER_LIGHT);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.setColor(getForeground());
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            batalBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
            batalBtn.setPreferredSize(new Dimension(110, 34));
            batalBtn.setContentAreaFilled(false);
            batalBtn.setBorderPainted(false);
            batalBtn.setFocusPainted(false);
            batalBtn.setOpaque(false);
            batalBtn.setForeground(ColorPalette.TEXT_PRIMARY);
            batalBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            batalBtn.addActionListener(e -> dialog.dispose());
            btnRow.add(batalBtn);

            JButton hapusBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Ya, Hapus" : "Yes, Delete") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ColorPalette.BTN_DELETE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            hapusBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 12));
            hapusBtn.setPreferredSize(new Dimension(110, 34));
            hapusBtn.setContentAreaFilled(false);
            hapusBtn.setBorderPainted(false);
            hapusBtn.setFocusPainted(false);
            hapusBtn.setOpaque(false);
            hapusBtn.setForeground(Color.WHITE);
            hapusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            hapusBtn.addActionListener(e -> { confirmed[0] = true; dialog.dispose(); });
            btnRow.add(hapusBtn);

            panel.add(btnRow);
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(topFrame);
            dialog.setVisible(true);

            if (confirmed[0]) {
                taskService.deleteTask(task.getId());
                mainFrame.refreshAll();
            }
        }
    }
}
