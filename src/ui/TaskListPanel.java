package ui;

import model.Task;
import service.TaskService;
import util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 */
public class TaskListPanel extends JPanel {

    private TaskService taskService;
    private MainFrame mainFrame;

    public TaskListPanel(TaskService taskService, MainFrame mainFrame) {
        this.taskService = taskService;
        this.mainFrame = mainFrame;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
    }

    /**
     * Memuat ulang daftar tugas dari list yang diberikan.
     */
    public void loadTasks(List<Task> tasks) {
        // Suppress intermediate layout passes during rebuild
        setVisible(false);
        removeAll();

        // Pisahkan tugas belum selesai dan selesai
        List<Task> incomplete = tasks.stream()
                .filter(t -> t.getStatus() != Task.Status.SELESAI)
                .collect(java.util.stream.Collectors.toList());

        List<Task> completed = tasks.stream()
                .filter(t -> t.getStatus() == Task.Status.SELESAI)
                .collect(java.util.stream.Collectors.toList());

        // Section: Belum Selesai
        if (!incomplete.isEmpty()) {
            add(createSectionHeader(util.TranslationManager.get("header_belum"), incomplete.size()));
            add(Box.createVerticalStrut(12));
            add(createTaskGrid(incomplete));
            add(Box.createVerticalStrut(20));
        }

        // Section: Selesai
        if (!completed.isEmpty()) {
            add(createSectionHeader(util.TranslationManager.get("header_selesai"), completed.size()));
            add(Box.createVerticalStrut(12));
            add(createTaskGrid(completed));
        }

        // Jika tidak ada tugas sama sekali
        if (tasks.isEmpty()) {
            add(Box.createVerticalStrut(40));
            JLabel emptyLabel = new JLabel(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Belum Terdapat Tugas" : "No Tasks Found");
            emptyLabel.setFont(ColorPalette.FONT_BODY);
            emptyLabel.setForeground(ColorPalette.TEXT_SECONDARY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Bungkus dengan panel agar text benar-benar center secara horizontal tanpa mengecilkan parent
            JPanel emptyWrapper = new JPanel(new BorderLayout());
            emptyWrapper.setOpaque(false);
            emptyWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            emptyWrapper.setAlignmentX(LEFT_ALIGNMENT);
            emptyWrapper.add(emptyLabel, BorderLayout.CENTER);
            
            add(emptyWrapper);
        }

        // Spacer di bawah
        add(Box.createVerticalStrut(30));

        // Single batched layout pass
        setVisible(true);
        revalidate();
        repaint();
    }

    /**
     * Membuat 2-column grid dari daftar tugas.
     */
    private JPanel createTaskGrid(List<Task> tasks) {
        int rows = (int) Math.ceil(tasks.size() / 2.0);
        JPanel grid = new JPanel(new GridLayout(rows, 2, 12, 12));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);

        for (Task task : tasks) {
            grid.add(new TaskCardPanel(task, taskService, mainFrame));
        }

        // If odd number, add empty filler
        if (tasks.size() % 2 != 0) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            grid.add(filler);
        }

        // Constrain max height
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, grid.getPreferredSize().height));

        return grid;
    }

    /**
     * Membuat header section (contoh: "BELUM SELESAI — 4 TUGAS").
     */
    private JPanel createSectionHeader(String title, int count) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel label = new JLabel(title + " — " + count + " " + util.TranslationManager.get("header_tugas"));
        label.setFont(ColorPalette.FONT_LABEL);
        label.setForeground(ColorPalette.TEXT_SECONDARY);
        header.add(label);

        return header;
    }
}
