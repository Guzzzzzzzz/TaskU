package ui;

import service.TaskService;
import util.ColorPalette;

import javax.swing.*;
import java.awt.*;

/**
 * 
 *  Tampilan bersih: hanya tombol "+ Tambah Tugas Baru"
 *  Saat diklik → buka AddTaskDialog (popup form lengkap)
 */
public class AddTaskPanel extends JPanel {

    private TaskService taskService;
    private MainFrame mainFrame;
    private JLabel label;
    private JButton addBtn;

    public AddTaskPanel(TaskService taskService, MainFrame mainFrame) {
        this.taskService = taskService;
        this.mainFrame = mainFrame;

        setOpaque(false);
        setLayout(new BorderLayout());
        setAlignmentX(LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Quick-add bar
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(ColorPalette.BORDER_CARD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setLayout(new BorderLayout(12, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 12));
        bar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Placeholder label
        label = new JLabel(util.TranslationManager.get("placeholder_tambah"));
        label.setFont(ColorPalette.FONT_BODY);
        label.setForeground(ColorPalette.TEXT_LIGHT);
        bar.add(label, BorderLayout.CENTER);

        // Tombol Tambah — solid blue, rounded 8px
        addBtn = new JButton(util.TranslationManager.get("btn_tambah")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.ACCENT_BLUE);
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
        addBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 14));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setOpaque(false);
        addBtn.setPreferredSize(new Dimension(100, 36));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> openAddDialog());
        bar.add(addBtn, BorderLayout.EAST);

        // Klik anywhere di bar juga buka dialog
        bar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openAddDialog();
            }
        });

        add(bar, BorderLayout.CENTER);
    }

    public void updateLanguage() {
        if (label != null) {
            label.setText(util.TranslationManager.get("placeholder_tambah"));
        }
        if (addBtn != null) {
            addBtn.setText(util.TranslationManager.get("btn_tambah"));
        }
    }

    private void openAddDialog() {
        AddTaskDialog dialog = new AddTaskDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), taskService);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            mainFrame.refreshAll();
        }
    }

    /**
     * Refresh dropdown (compatibility — dipanggil dari MainFrame).
     */
    public void refreshMatkulDropdown() {
        // Dropdown sekarang ada di AddTaskDialog, jadi method ini kosong.
        // Tetap ada agar MainFrame tidak error.
    }
}
