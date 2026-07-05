package ui;

import database.DatabaseConnection;
import model.Mahasiswa;
import model.Task;
import model.Task.Prioritas;
import model.Task.Status;
import service.TaskService;
import util.AppIcon;
import util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 
 * Arsitektur:
 * ┌───────────────────────────────────────────────────────────┐
 * │ MainFrame (JFrame) │
 * │ ┌────────────┬──────────────────────────────────────────┐│
 * │ │ │ ┌──────────────────────────────────────┐││
 * │ │ Sidebar │ │ Dashboard (Statistik + Warning) │││
 * │ │ Panel │ ├──────────────────────────────────────┤││
 * │ │ │ │ AddTask (Form) │││
 * │ │ (WEST) │ ├──────────────────────────────────────┤││
 * │ │ │ │ Filter (Tombol Filter + Search) │││
 * │ │ │ ├──────────────────────────────────────┤││
 * │ │ │ │ TaskList (Daftar Kartu Tugas) │││
 * │ │ │ │ [scrollable] │││
 * │ │ │ └──────────────────────────────────────┘││
 * │ └────────────┴──────────────────────────────────────────┘│
 * └───────────────────────────────────────────────────────────┘
 * 
 */
public class MainFrame extends JFrame {

    private TaskService taskService;

    // Panel-panel utama
    private SidebarPanel sidebarPanel;
    private DashboardPanel dashboardPanel;
    private AddTaskPanel addTaskPanel;
    private FilterPanel filterPanel;
    private TaskListPanel taskListPanel;

    // Mahasiswa yang sedang login
    private Mahasiswa loggedInUser;

    // State: filter yang sedang aktif
    private String currentView = "ALL"; // ALL, TODAY, COMPLETED, MATKUL
    private int currentMatkulId = -1;

    // Cache base tasks to prevent excessive database queries on filtering
    private List<Task> cachedBaseTasks = null;

    // Debounce timer — coalesces rapid filter clicks into one UI rebuild
    private javax.swing.Timer filterDebounceTimer;

    // State tambahan untuk filter terpadu
    private String filterStatus = null;
    private String filterPriority = null;
    private String filterKeyword = "";

    public MainFrame(Mahasiswa loggedIn) {
        this.loggedInUser = loggedIn;

        // Inisialisasi Service
        taskService = new TaskService(loggedInUser);

        // Load Settings Persisten dari Database
        ColorPalette.isDarkMode = taskService.isDarkMode();
        String savedLang = taskService.getLanguage();
        try {
            util.TranslationManager.currentLanguage = util.TranslationManager.Language.valueOf(savedLang.toUpperCase());
        } catch (Exception ex) {
            util.TranslationManager.currentLanguage = util.TranslationManager.Language.ID;
        }

        // Debounce timer: 60ms delay. Pada setiap trigger, timer di-restart.
        // Hanya panggilan TERAKHIR dalam seri spam-click yang benar-benar dieksekusi.
        filterDebounceTimer = new javax.swing.Timer(60, e -> executeFilter());
        filterDebounceTimer.setRepeats(false);

        // Setup JFrame
        setTitle("TaskU");
        AppIcon.apply(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ColorPalette.BG_MAIN);
        setLayout(new BorderLayout());

        // Tutup koneksi DB saat window ditutup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseConnection.getInstance().closeConnection();
            }
        });

        // Sidebar (WEST)
        sidebarPanel = new SidebarPanel(taskService, this, loggedInUser);
        add(sidebarPanel, BorderLayout.WEST);

        // Content Area (CENTER)
        class ScrollablePanel extends JPanel implements Scrollable {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }
            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 16;
            }
            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 32;
            }
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true; // Lock width to viewport width to prevent layout shifts
            }
            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }

        JPanel contentWrapper = new ScrollablePanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.Y_AXIS));
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(new EmptyBorder(32, 32, 20, 32));

        // Dashboard
        dashboardPanel = new DashboardPanel(taskService);
        dashboardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentWrapper.add(dashboardPanel);

        // Add Task Form
        addTaskPanel = new AddTaskPanel(taskService, this);
        addTaskPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentWrapper.add(addTaskPanel);
        contentWrapper.add(Box.createVerticalStrut(10));

        // Filter
        filterPanel = new FilterPanel(this);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentWrapper.add(filterPanel);
        contentWrapper.add(Box.createVerticalStrut(15));

        // Task List
        taskListPanel = new TaskListPanel(taskService, this);
        taskListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentWrapper.add(taskListPanel);

        JScrollPane scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setBorder(null);
        scrollPane.setBackground(ColorPalette.BG_MAIN);
        scrollPane.getViewport().setBackground(ColorPalette.BG_MAIN);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);


        JScrollBar vsb = scrollPane.getVerticalScrollBar();
        vsb.setOpaque(false);
        vsb.setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton jb = new JButton();
                jb.setPreferredSize(new Dimension(0, 0));
                jb.setMinimumSize(new Dimension(0, 0));
                jb.setMaximumSize(new Dimension(0, 0));
                return jb;
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                // Track tipis semi-transparan
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color trackColor = ColorPalette.isDarkMode
                    ? new Color(255, 255, 255, 18)
                    : new Color(0, 0, 0, 12);
                g2.setColor(trackColor);
                int w = 4;
                int x = trackBounds.x + (trackBounds.width - w) / 2;
                g2.fillRoundRect(x, trackBounds.y + 2, w, trackBounds.height - 4, w, w);
                g2.dispose();
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color thumbColor = isThumbRollover()
                    ? (ColorPalette.isDarkMode ? new Color(180, 180, 180) : new Color(80, 80, 80))
                    : (ColorPalette.isDarkMode ? new Color(120, 120, 120) : new Color(140, 140, 140));
                g2.setColor(thumbColor);
                int w = 4;
                int x = thumbBounds.x + (thumbBounds.width - w) / 2;
                g2.fillRoundRect(x, thumbBounds.y + 2, w, thumbBounds.height - 4, w, w);
                g2.dispose();
            }
            @Override
            public Dimension getPreferredSize(JComponent c) {
                return new Dimension(8, Integer.MAX_VALUE);
            }
        });

        add(scrollPane, BorderLayout.CENTER);


        // Load Data Awal
        loadAllTasks();
        sidebarPanel.refreshBadges();
    }


    /** Tampilkan semua tugas. */
    public void showAllTasks() {
        currentView = "ALL";
        cachedBaseTasks = null; // Invalidate cache on view change
        applyCombinedFilters();
    }

    /** Tampilkan tugas hari ini saja. */
    public void showTodayTasks() {
        currentView = "TODAY";
        cachedBaseTasks = null; // Invalidate cache on view change
        applyCombinedFilters();
    }

    /** Tampilkan tugas yang sudah selesai saja. */
    public void showCompletedTasks() {
        currentView = "COMPLETED";
        cachedBaseTasks = null; // Invalidate cache on view change
        applyCombinedFilters();
    }


    /** Filter berdasarkan status. null = semua. */
    public void filterByStatus(String status) {
        this.filterStatus = status;
        applyCombinedFilters();
    }

    /** Filter berdasarkan prioritas. */
    public void filterByPriority(String prioritas) {
        this.filterPriority = prioritas;
        applyCombinedFilters();
    }

    /** Filter berdasarkan Mata Kuliah. */
    public void showTasksByMataKuliah(int mkId) {
        currentView = "MATKUL";
        currentMatkulId = mkId;
        cachedBaseTasks = null; // Invalidate cache on view change
        applyCombinedFilters();
    }

    /** Cari tugas berdasarkan keyword. */
    public void searchTasks(String keyword) {
        this.filterKeyword = keyword == null ? "" : keyword;
        applyCombinedFilters();
    }


    /**
     * Debounced filter trigger.
     * Setiap panggilan me-restart timer 60ms. Jika user spam-click,
     * hanya klik TERAKHIR yang benar-benar memicu rebuild UI.
     */
    private void applyCombinedFilters() {
        filterDebounceTimer.restart();
    }

    /**
     * Eksekusi filter yang sebenarnya — dipanggil oleh debounce timer.
     * Melakukan in-memory filtering dari cachedBaseTasks dan rebuild TaskListPanel.
     */
    private void executeFilter() {
        if (cachedBaseTasks == null) {
            switch (currentView) {
                case "TODAY":
                    cachedBaseTasks = taskService.getTasksToday();
                    break;
                case "COMPLETED":
                    cachedBaseTasks = taskService.getTasksByStatus(Status.SELESAI);
                    break;
                case "MATKUL":
                    cachedBaseTasks = taskService.getTasksByMataKuliah(currentMatkulId);
                    break;
                default:
                    cachedBaseTasks = taskService.getAllTasks();
                    break;
            }
        }

        List<Task> filtered = new java.util.ArrayList<>();
        for (Task t : cachedBaseTasks) {
            boolean matchStatus = (filterStatus == null) || t.getStatus().name().equalsIgnoreCase(filterStatus);
            boolean matchPriority = (filterPriority == null)
                    || t.getPrioritas().name().equalsIgnoreCase(filterPriority);
            boolean matchSearch = filterKeyword.isEmpty()
                    || t.getJudul().toLowerCase().contains(filterKeyword.toLowerCase());

            if (matchStatus && matchPriority && matchSearch) {
                filtered.add(t);
            }
        }
        taskListPanel.loadTasks(filtered);
    }


    /** Reload semua data — dipanggil setelah add/delete/toggle task. */
    public void refreshAll() {
        dashboardPanel.refreshStats();
        sidebarPanel.refreshBadges();
        sidebarPanel.refreshMataKuliah();

        // Invalidate cache since database contents mutated
        cachedBaseTasks = null;

        // Terapkan semua filter ke view yang sedang aktif
        applyCombinedFilters();
    }

    /** Load semua tasks. (Dipanggil waktu inisialisasi awal) */
    private void loadAllTasks() {
        currentView = "ALL";
        applyCombinedFilters();
    }

    /** Toggle antara Light Mode dan Dark Mode secara global. */
    public void toggleTheme() {
        ColorPalette.isDarkMode = !ColorPalette.isDarkMode;

        // Simpan setting persisten ke database
        taskService.saveSettings(ColorPalette.isDarkMode, util.TranslationManager.currentLanguage.name());

        // Update warna container utama
        getContentPane().setBackground(ColorPalette.BG_MAIN);

        // Cari JScrollPane dan perbarui background viewport & container didalamnya
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) c;
                sp.setBackground(ColorPalette.BG_MAIN);
                sp.getViewport().setBackground(ColorPalette.BG_MAIN);
                sp.getVerticalScrollBar().setBackground(ColorPalette.BG_MAIN);
                Component view = sp.getViewport().getView();
                if (view instanceof JComponent) {
                    JComponent jc = (JComponent) view;
                    jc.setBackground(ColorPalette.BG_MAIN);
                }
            }
        }


        // Update sidebar panel background & border
        if (sidebarPanel != null) {
            sidebarPanel.setBackground(ColorPalette.BG_SIDEBAR);
            sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorPalette.BORDER_LIGHT));
            sidebarPanel.updateMenuStyles();
            sidebarPanel.repaint();
        }

        // Update filter panel theme
        if (filterPanel != null) {
            filterPanel.updateTheme();
        }

        // Repaint seluruh frame secara rekursif
        this.repaint();
        this.revalidate();
    }

    /** Memperbarui bahasa teks di seluruh aplikasi secara dinamis. */
    public void updateLanguage() {
        if (sidebarPanel != null) sidebarPanel.updateLanguage();
        if (dashboardPanel != null) dashboardPanel.updateLanguage();
        if (filterPanel != null) filterPanel.updateLanguage();
        if (addTaskPanel != null) addTaskPanel.updateLanguage();
        
        // Refresh daftar tugas agar jika ada label "Hari ini", "Besok", "kemarin" dll, bahasanya ter-update
        refreshAll();
    }
}
