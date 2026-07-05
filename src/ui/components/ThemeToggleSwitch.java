package ui.components;

import util.ColorPalette;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Custom Toggle Switch (Slider Button) berbentuk kapsul untuk fitur Dark Mode/Light Mode.
 * Mengikuti spesifikasi visual:
 * - Day Mode: Background light gray, teks "DAY MODE" di kiri, Knob matahari di kanan.
 * - Night Mode: Background dark, teks "NIGHT MODE" di kanan, Knob bulan & bintang di kiri.
 * - Animasi geser smooth saat berpindah state.
 */
public class ThemeToggleSwitch extends JComponent {
    private boolean isDark;
    
    // Animasi sliding
    private double animationProgress; // 0.0 (Night Mode - Kiri) ke 1.0 (Day Mode - Kanan)
    private Timer timer;
    private final double ANIMATION_SPEED = 0.12; // Kecepatan easing (Lerp)
    
    public ThemeToggleSwitch(boolean startDark) {
        this.isDark = startDark;
        this.animationProgress = startDark ? 0.0 : 1.0;
        
        setPreferredSize(new Dimension(118, 36));
        setMinimumSize(new Dimension(118, 36));
        setMaximumSize(new Dimension(118, 36));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggle();
            }
        });
        
        // Timer animasi 60fps (16ms interval)
        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double target = isDark ? 0.0 : 1.0;
                if (Math.abs(animationProgress - target) < 0.01) {
                    animationProgress = target;
                    timer.stop();
                } else {
                    // Smooth Linear Interpolation Easing
                    animationProgress += (target - animationProgress) * ANIMATION_SPEED;
                }
                repaint();
            }
        });
    }
    
    private void toggle() {
        isDark = !isDark;
        
        // Panggil action toggle tema global di MainFrame
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof ui.MainFrame) {
            ((ui.MainFrame) window).toggleTheme();
        }
        
        // Mulai animasi
        if (timer.isRunning()) {
            timer.stop();
        }
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        int arc = h; // Round rect berbentuk kapsul penuh
        
        // 1. Gambar Background Kapsul (Interpolasi Warna)
        Color lightBg = new Color(228, 228, 231); // zinc-200 (Day Mode)
        Color darkBg = new Color(9, 9, 11);        // zinc-950 (Night Mode)
        
        int r = (int) (darkBg.getRed() + (lightBg.getRed() - darkBg.getRed()) * animationProgress);
        int gr = (int) (darkBg.getGreen() + (lightBg.getGreen() - darkBg.getGreen()) * animationProgress);
        int b = (int) (darkBg.getBlue() + (lightBg.getBlue() - darkBg.getBlue()) * animationProgress);
        
        g2.setColor(new Color(r, gr, b));
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        
        // Border luar kapsul tipis semi-transparan
        g2.setColor(ColorPalette.BORDER_LIGHT);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        
        // 2. Gambar Teks dengan Efek Fading
        g2.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        
        // Teks "DAY MODE" (Fading berdasarkan progress menuju 1.0)
        if (animationProgress > 0.15) {
            int alpha = (int) (255 * (animationProgress - 0.15) / 0.85);
            alpha = Math.max(0, Math.min(255, alpha));
            g2.setColor(new Color(24, 24, 27, alpha)); // zinc-900
            // Lebih dekat ke knob kanan (knob ada di kanan saat Day Mode)
            int tx = 14;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString("DAY MODE", tx, ty);
        }
        
        // Teks "NIGHT MODE" (Fading berdasarkan progress menuju 0.0)
        if (animationProgress < 0.85) {
            int alpha = (int) (255 * (0.85 - animationProgress) / 0.85);
            alpha = Math.max(0, Math.min(255, alpha));
            g2.setColor(new Color(250, 250, 250, alpha)); // zinc-50
            // Lebih dekat ke knob kiri (knob ada di kiri saat Night Mode)
            int tx = w - fm.stringWidth("NIGHT MODE") - 12;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString("NIGHT MODE", tx, ty);
        }

        
        // 3. Gambar Knob (Bulatan/Thumb)
        int margin = 3;
        int knobDim = h - margin * 2;
        int startX = margin;
        int endX = w - knobDim - margin;
        int knobX = (int) (startX + (endX - startX) * animationProgress);
        int knobY = margin;
        
        // Drop shadow tipis untuk knob
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillOval(knobX, knobY + 1, knobDim, knobDim);
        
        // Knob putih solid
        g2.setColor(Color.WHITE);
        g2.fillOval(knobX, knobY, knobDim, knobDim);
        
        // Outline tipis knob
        g2.setColor(new Color(0, 0, 0, 25));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(knobX, knobY, knobDim, knobDim);
        
        // 4. Gambar Ikon di Tengah Knob (Blending Matahari & Bulan)
        int kcx = knobX + knobDim / 2;
        int kcy = knobY + knobDim / 2;
        
        // Ikon Matahari (Tampil saat Day Mode)
        if (animationProgress > 0.05) {
            Graphics2D gSun = (Graphics2D) g2.create();
            gSun.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) animationProgress));
            gSun.setColor(new Color(24, 24, 27)); // Hitam gelap
            
            // Pusat matahari
            gSun.fillOval(kcx - 4, kcy - 4, 8, 8);
            
            // Sinar matahari (8 arah)
            gSun.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int r1 = 6;
            int r2 = 8;
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                int x1 = (int) (kcx + r1 * Math.cos(angle));
                int y1 = (int) (kcy + r1 * Math.sin(angle));
                int x2 = (int) (kcx + r2 * Math.cos(angle));
                int y2 = (int) (kcy + r2 * Math.sin(angle));
                gSun.drawLine(x1, y1, x2, y2);
            }
            gSun.dispose();
        }
        
        // Ikon Bulan & Bintang (Tampil saat Night Mode)
        if (animationProgress < 0.95) {
            Graphics2D gMoon = (Graphics2D) g2.create();
            gMoon.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1.0 - animationProgress)));
            gMoon.setColor(new Color(24, 24, 27)); // Hitam gelap
            
            // Bulan sabit
            java.awt.geom.Area moon = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(kcx - 6, kcy - 6, 12, 12));
            java.awt.geom.Area cutter = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(kcx - 2, kcy - 7, 12, 12));
            moon.subtract(cutter);
            gMoon.fill(moon);
            
            // Bintang-bintang kecil (kilau) di samping bulan sabit
            int sx1 = kcx + 3;
            int sy1 = kcy - 4;
            gMoon.drawLine(sx1, sy1 - 1, sx1, sy1 + 1);
            gMoon.drawLine(sx1 - 1, sy1, sx1 + 1, sy1);
            
            int sx2 = kcx + 1;
            int sy2 = kcy - 1;
            gMoon.drawLine(sx2, sy2 - 1, sx2, sy2 + 1);
            gMoon.drawLine(sx2 - 1, sy2, sx2 + 1, sy2);
            
            gMoon.dispose();
        }
        
        g2.dispose();
    }
}
