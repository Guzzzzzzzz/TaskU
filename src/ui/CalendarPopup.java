package ui;

import util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 *
 *  Fitur:
 *  - Navigasi bulan (prev / next)
 *  - Highlight hari ini (biru)
 *  - Klik tanggal → set ke JTextField target
 *  - Tombol "Hari Ini" dan "Hapus"
 *
 */
public class CalendarPopup extends JPopupMenu {

    private YearMonth currentMonth;
    private JTextField targetField;
    private JPanel calendarGrid;
    private JLabel monthLabel;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String[] DAY_NAMES = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

    public CalendarPopup(JTextField targetField) {
        this.targetField = targetField;
        this.currentMonth = YearMonth.now();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_LIGHT),
            new EmptyBorder(8, 8, 8, 8)
        ));
        setBackground(ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE);
        setOpaque(false);

        // ── Header: < Month Year > ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JButton prevBtn = createNavButton(true);
        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });

        JButton nextBtn = createNavButton(false);
        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 13));
        monthLabel.setForeground(ColorPalette.TEXT_PRIMARY);

        header.add(prevBtn, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Calendar Grid ──
        calendarGrid = new JPanel(new GridLayout(0, 7, 2, 2));
        calendarGrid.setOpaque(false);
        add(calendarGrid, BorderLayout.CENTER);

        // ── Footer: Clear / Today ──
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 0, 0, 0));

        JButton clearBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Hapus" : "Clear");
        clearBtn.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 11));
        clearBtn.setForeground(ColorPalette.TEXT_SECONDARY);
        clearBtn.setBorderPainted(false);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            targetField.setText("DD-MM-YYYY");
            targetField.setForeground(ColorPalette.TEXT_SECONDARY);
            setVisible(false);
        });
        footer.add(clearBtn, BorderLayout.WEST);

        JButton todayBtn = new JButton(util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Hari Ini" : "Today");
        todayBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 11));
        todayBtn.setForeground(ColorPalette.ACCENT_BLUE);
        todayBtn.setBorderPainted(false);
        todayBtn.setContentAreaFilled(false);
        todayBtn.setFocusPainted(false);
        todayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        todayBtn.addActionListener(e -> {
            selectDate(LocalDate.now());
        });
        footer.add(todayBtn, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);

        refreshCalendar();
    }

    private void refreshCalendar() {
        setBackground(ColorPalette.isDarkMode ? new Color(0x24, 0x24, 0x24) : Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_LIGHT),
            new EmptyBorder(8, 8, 8, 8)
        ));
        calendarGrid.removeAll();

        monthLabel.setText(currentMonth.getMonth().toString().substring(0, 1)
            + currentMonth.getMonth().toString().substring(1).toLowerCase()
            + " " + currentMonth.getYear());

        // Day name headers
        for (String day : DAY_NAMES) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 11));
            lbl.setForeground(ColorPalette.TEXT_SECONDARY);
            lbl.setPreferredSize(new Dimension(32, 24));
            calendarGrid.add(lbl);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int startDow = firstDay.getDayOfWeek().getValue() % 7; // Sun=0
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        // Previous month padding
        YearMonth prevMonth = currentMonth.minusMonths(1);
        int prevDays = prevMonth.lengthOfMonth();
        for (int i = 0; i < startDow; i++) {
            int day = prevDays - startDow + 1 + i;
            JLabel lbl = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            lbl.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 12));
            lbl.setForeground(ColorPalette.isDarkMode ? new Color(0x55, 0x55, 0x55) : ColorPalette.TEXT_LIGHT);
            lbl.setPreferredSize(new Dimension(32, 28));
            calendarGrid.add(lbl);
        }

        // Current month days
        for (int d = 1; d <= daysInMonth; d++) {
            final int day = d;
            LocalDate date = currentMonth.atDay(day);
            boolean isToday = date.equals(today);

            JLabel lbl = new JLabel(String.valueOf(day), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isToday) {
                        g2.setColor(ColorPalette.ACCENT_BLUE);
                        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lbl.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 12));
            lbl.setForeground(isToday ? Color.WHITE : ColorPalette.TEXT_PRIMARY);
            lbl.setPreferredSize(new Dimension(32, 28));
            lbl.setOpaque(false);
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectDate(currentMonth.atDay(day));
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isToday) {
                        lbl.setForeground(ColorPalette.ACCENT_BLUE);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isToday) {
                        lbl.setForeground(ColorPalette.TEXT_PRIMARY);
                    }
                }
            });
            calendarGrid.add(lbl);
        }

        // Fill remaining cells to make it always exactly 42 date cells (6 rows)
        int totalCells = startDow + daysInMonth;
        int maxDateCells = 42;
        int remaining = maxDateCells - totalCells;
        for (int i = 1; i <= remaining; i++) {
            JLabel lbl = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            lbl.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 12));
            lbl.setForeground(ColorPalette.isDarkMode ? new Color(0x55, 0x55, 0x55) : ColorPalette.TEXT_LIGHT);
            lbl.setPreferredSize(new Dimension(32, 28));
            calendarGrid.add(lbl);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
        if (!isVisible()) {
            pack();
        }
    }

    private void selectDate(LocalDate date) {
        targetField.setText(date.format(FMT));
        targetField.setForeground(ColorPalette.TEXT_PRIMARY);
        setVisible(false);
    }

    private JButton createNavButton(boolean isLeft) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                
                int w = getWidth();
                int h = getHeight();
                
                g2.setColor(ColorPalette.TEXT_PRIMARY);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int cx = w / 2;
                int cy = h / 2;
                
                if (isLeft) {
                    // Draw chevron left: <
                    g2.drawLine(cx + 2, cy - 4, cx - 2, cy);
                    g2.drawLine(cx - 2, cy, cx + 2, cy + 4);
                } else {
                    // Draw chevron right: >
                    g2.drawLine(cx - 2, cy - 4, cx + 2, cy);
                    g2.drawLine(cx + 2, cy, cx - 2, cy + 4);
                }
                
                g2.dispose();
            }
        };
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(30, 24));
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
