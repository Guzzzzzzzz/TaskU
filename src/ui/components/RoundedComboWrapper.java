package ui.components;

import util.ColorPalette;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 *  RoundedComboWrapper — Utility untuk membungkus JComboBox
 *  dalam panel rounded dengan chevron custom
 *
 */
public class RoundedComboWrapper {

    /**
     * Membungkus JComboBox dalam JPanel rounded yang sudah di-style.
     * Termasuk custom chevron arrow, transparent background, 
     * dan custom ListCellRenderer.
     */
    public static JPanel wrap(JComboBox<?> combo) {
        Color baseBg = ColorPalette.isDarkMode ? new Color(0x2A, 0x2A, 0x2A) : Color.WHITE;
        Color baseBorder = ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : ColorPalette.BORDER_LIGHT;

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(baseBg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(baseBorder);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);

        combo.setOpaque(false);
        combo.setBackground(new Color(0, 0, 0, 0));
        combo.setForeground(ColorPalette.TEXT_PRIMARY);
        combo.setFocusable(false);
        combo.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));

        // Custom UI dengan chevron halus, custom popup, dan custom scrollbar
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int w = getWidth(), h = getHeight();
                        int cx = w / 2, cy = h / 2;
                        g2.setColor(ColorPalette.TEXT_SECONDARY);
                        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                        g2.dispose();
                    }
                };
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setPreferredSize(new Dimension(28, 28));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { }

            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    @Override
                    protected JScrollPane createScroller() {
                        JScrollPane scrollPane = super.createScroller();
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                        scrollPane.setOpaque(false);
                        scrollPane.getViewport().setOpaque(false);
                        
                        // Custom Scrollbar UI
                        JScrollBar vsb = scrollPane.getVerticalScrollBar();
                        vsb.setOpaque(false);
                        vsb.setUI(new BasicScrollBarUI() {
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
                                // Track background is transparent
                            }
                            @Override
                            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                                    return;
                                }
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                // Muted dark grey line matching main dashboard
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
                        return scrollPane;
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(baseBg);
                        // Paint rounded background inside shadow border
                        g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 8, 8);
                        g2.dispose();
                    }
                };

                // Add drop shadow and clean border to the popup JPopupMenu itself
                Color popupBorderCol = ColorPalette.isDarkMode ? new Color(0x44, 0x44, 0x44) : new Color(226, 232, 240);
                popup.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 0, 0, 15), 4), // soft shadow
                    new javax.swing.border.AbstractBorder() {
                        @Override
                        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(popupBorderCol);
                            g2.drawRoundRect(x + 4, y + 4, width - 9, height - 9, 8, 8);
                            g2.dispose();
                        }
                        @Override
                        public Insets getBorderInsets(Component c) {
                            return new Insets(8, 8, 8, 8); // outer padding + border inset
                        }
                    }
                ));
                popup.setOpaque(false);
                popup.getList().setBackground(baseBg);
                popup.getList().setOpaque(true);
                return popup;
            }
        });

        // Custom renderer modern
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(ColorPalette.FONT_BODY);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                if (isSelected) {
                    lbl.setBackground(ColorPalette.isDarkMode ? new Color(0x1E, 0x29, 0x3B) : new Color(235, 242, 255));
                    lbl.setForeground(ColorPalette.isDarkMode ? Color.WHITE : ColorPalette.ACCENT_BLUE);
                } else {
                    lbl.setBackground(baseBg);
                    lbl.setForeground(ColorPalette.TEXT_PRIMARY);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });

        wrapper.add(combo, BorderLayout.CENTER);
        return wrapper;
    }
}
