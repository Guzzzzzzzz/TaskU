package ui;

import service.TaskService;
import util.ColorPalette;
import util.DateHelper;
import util.TranslationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 */
public class DashboardPanel extends JPanel {

    private TaskService taskService;
    private model.Mahasiswa mahasiswa;

    // Labels statistik (disimpan agar bisa di-update)
    private JLabel totalLabel;
    private JLabel completedLabel;
    private JLabel completedPctLabel;
    private JLabel urgentLabel;
    private JLabel categoryLabel;
    private JPanel warningBanner;
    private JLabel warningLabel;

    private JLabel greetingLabel;
    private JLabel dateLabel;

    // Cards titles and subtitles
    private JLabel card1Title, card1Sub;
    private JLabel card2Title;
    private JLabel card3Title, card3Sub;
    private JLabel card4Title, card4Sub;

    public DashboardPanel(TaskService taskService) {
        this.taskService = taskService;
        this.mahasiswa = taskService.getLoggedInUser();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 5, 0));
        setAlignmentX(LEFT_ALIGNMENT);

        // Top Header Wrapper (BorderLayout) to push the toggle to the right
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setAlignmentX(LEFT_ALIGNMENT);
        topHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Header text: Judul + Tanggal
        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.Y_AXIS));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(LEFT_ALIGNMENT);

        // Greeting
        String userName = mahasiswa != null ? mahasiswa.getNama() : "";
        greetingLabel = new JLabel(TranslationManager.get("welcome_msg") + ", " + userName + "!");
        greetingLabel.setFont(ColorPalette.FONT_TITLE);
        greetingLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        greetingLabel.setAlignmentX(LEFT_ALIGNMENT);
        headerRow.add(greetingLabel);

        headerRow.add(Box.createVerticalStrut(4));

        // Tanggal hari ini
        dateLabel = new JLabel(DateHelper.formatFull(LocalDate.now()));
        dateLabel.setFont(ColorPalette.FONT_SUBTITLE);
        dateLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        dateLabel.setAlignmentX(LEFT_ALIGNMENT);
        headerRow.add(dateLabel);

        topHeader.add(headerRow, BorderLayout.WEST);

        // Custom Theme Toggle Switch Capsule (Smooth slider animation)
        ui.components.ThemeToggleSwitch themeToggle = new ui.components.ThemeToggleSwitch(ColorPalette.isDarkMode);

        JPanel toggleWrap = new JPanel(new GridBagLayout());
        toggleWrap.setOpaque(false);
        toggleWrap.add(themeToggle);
        topHeader.add(toggleWrap, BorderLayout.EAST);

        add(topHeader);
        add(Box.createVerticalStrut(24));

        // Statistik Cards (4 kartu)
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Kartu 1: Total Tugas
        totalLabel = new JLabel("0");
        card1Title = new JLabel(TranslationManager.get("stat_total"));
        card1Sub = new JLabel(TranslationManager.get("stat_semester"));
        JPanel card1 = createStatCard(card1Title, totalLabel, card1Sub);

        // Kartu 2: Selesai
        completedLabel = new JLabel("0");
        completedPctLabel = new JLabel("0% " + (TranslationManager.currentLanguage == TranslationManager.Language.ID ? "dari total" : "of total"));
        card2Title = new JLabel(TranslationManager.get("stat_selesai_header"));
        JPanel card2 = createStatCard(card2Title, completedLabel, null);
        JPanel card2Inner = (JPanel) card2.getComponent(0);
        completedPctLabel.setFont(ColorPalette.FONT_BODY_SM);
        completedPctLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        card2Inner.add(completedPctLabel);

        // Kartu 3: Mendesak
        urgentLabel = new JLabel("0");
        card3Title = new JLabel(TranslationManager.get("stat_mendesak"));
        card3Sub = new JLabel(TranslationManager.get("stat_deadline_minggu"));
        JPanel card3 = createStatCard(card3Title, urgentLabel, card3Sub);

        // Kartu 4: Kategori
        categoryLabel = new JLabel("0");
        card4Title = new JLabel(TranslationManager.get("stat_kategori"));
        card4Sub = new JLabel(TranslationManager.get("stat_matkul"));
        JPanel card4 = createStatCard(card4Title, categoryLabel, card4Sub);

        statsRow.add(card1);
        statsRow.add(card2);
        statsRow.add(card3);
        statsRow.add(card4);

        add(statsRow);
        add(Box.createVerticalStrut(16));

        // Warning Banner
        warningBanner = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.WARNING_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Border
                g2.setColor(ColorPalette.WARNING_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        warningBanner.setOpaque(false);
        warningBanner.setAlignmentX(LEFT_ALIGNMENT);
        warningBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        warningBanner.setPreferredSize(new Dimension(0, 48));
        warningBanner.setBorder(new EmptyBorder(0, 16, 0, 16));

        JLabel warningIcon = new JLabel("\u26A0\uFE0F");
        warningIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        warningIcon.setForeground(ColorPalette.WARNING_TEXT);
        warningIcon.setVerticalAlignment(JLabel.CENTER);
        warningIcon.setHorizontalAlignment(JLabel.CENTER);
        warningBanner.add(warningIcon, BorderLayout.WEST);

        warningLabel = new JLabel("");
        warningLabel.setFont(ColorPalette.FONT_BODY);
        warningLabel.setForeground(ColorPalette.WARNING_TEXT);
        warningLabel.setVerticalAlignment(JLabel.CENTER);
        warningBanner.add(warningLabel, BorderLayout.CENTER);

        add(warningBanner);
        add(Box.createVerticalStrut(16));

        // Load data awal
        refreshStats();
    }

    /**
     * Membuat satu kartu statistik.
     */
    private JPanel createStatCard(JLabel titleLbl, JLabel valueLabel, JLabel subLbl) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ColorPalette.BORDER_CARD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(20, 24, 20, 24));

        titleLbl.setFont(ColorPalette.FONT_LABEL);
        titleLbl.setForeground(ColorPalette.TEXT_SECONDARY);
        inner.add(titleLbl);

        inner.add(Box.createVerticalStrut(6));

        valueLabel.setFont(ColorPalette.FONT_STAT_NUM);
        valueLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        inner.add(valueLabel);

        if (subLbl != null) {
            inner.add(Box.createVerticalStrut(2));
            subLbl.setFont(ColorPalette.FONT_BODY_SM);
            subLbl.setForeground(ColorPalette.TEXT_SECONDARY);
            inner.add(subLbl);
        }

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refresh semua angka statistik dari database.
     */
    public void refreshStats() {
        int total = taskService.getTotalTasks();
        int completed = taskService.getCompletedCount();
        int pct = taskService.getCompletionPercentage();
        int urgent = taskService.getUrgentCount();
        int category = taskService.getCategoryCount();

        totalLabel.setText(String.valueOf(total));
        completedLabel.setText(String.valueOf(completed));
        
        String pctText = TranslationManager.currentLanguage == TranslationManager.Language.ID 
            ? pct + "% dari total"
            : pct + "% of total";
        completedPctLabel.setText(pctText);
        
        urgentLabel.setText(String.valueOf(urgent));
        categoryLabel.setText(String.valueOf(category));

        // Update warning banner
        String warning = taskService.getDeadlineWarningMessage();
        if (warning != null) {
            warningLabel.setText(warning);
            warningBanner.setVisible(true);
        } else {
            warningBanner.setVisible(false);
        }

        repaint();
    }

    /**
     * Memperbarui bahasa teks panel dashboard secara dinamis.
     */
    public void updateLanguage() {
        if (greetingLabel != null && mahasiswa != null) {
            greetingLabel.setText(TranslationManager.get("welcome_msg") + ", " + mahasiswa.getNama() + "!");
        }
        if (dateLabel != null) {
            dateLabel.setText(DateHelper.formatFull(LocalDate.now()));
        }

        if (card1Title != null) card1Title.setText(TranslationManager.get("stat_total"));
        if (card1Sub != null) card1Sub.setText(TranslationManager.get("stat_semester"));

        if (card2Title != null) card2Title.setText(TranslationManager.get("stat_selesai_header"));

        if (card3Title != null) card3Title.setText(TranslationManager.get("stat_mendesak"));
        if (card3Sub != null) card3Sub.setText(TranslationManager.get("stat_deadline_minggu"));

        if (card4Title != null) card4Title.setText(TranslationManager.get("stat_kategori"));
        if (card4Sub != null) card4Sub.setText(TranslationManager.get("stat_matkul"));

        refreshStats();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
