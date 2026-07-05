package util;

import java.awt.Color;
import java.awt.Font;

/**
 * Konstanta warna dan font untuk seluruh GUI.
 */
public class ColorPalette {

    public static boolean isDarkMode = false;

    private static class DynamicColor extends Color {
        private final Color lightColor;
        private final Color darkColor;

        public DynamicColor(Color lightColor, Color darkColor) {
            super(lightColor.getRGB());
            this.lightColor = lightColor;
            this.darkColor = darkColor;
        }

        private Color getActiveColor() {
            return isDarkMode ? darkColor : lightColor;
        }

        @Override public int getRed() { return getActiveColor().getRed(); }
        @Override public int getGreen() { return getActiveColor().getGreen(); }
        @Override public int getBlue() { return getActiveColor().getBlue(); }
        @Override public int getAlpha() { return getActiveColor().getAlpha(); }
        @Override public int getRGB() { return getActiveColor().getRGB(); }

        @Override
        public Color brighter() {
            return new DynamicColor(lightColor.brighter(), darkColor.brighter());
        }

        @Override
        public Color darker() {
            return new DynamicColor(lightColor.darker(), darkColor.darker());
        }
    }

    // Background
    public static final Color BG_MAIN       = new DynamicColor(new Color(0xF4, 0xF4, 0xF5), new Color(0x12, 0x12, 0x12));  // zinc-100 vs deep gray-900 (#121212)
    public static final Color BG_SIDEBAR    = new DynamicColor(Color.WHITE, new Color(0x1E, 0x1E, 0x1E));                    // white vs elevated gray-800 (#1E1E1E)
    public static final Color BG_CARD       = new DynamicColor(Color.WHITE, new Color(0x1E, 0x1E, 0x1E));                    // white vs elevated gray-800 (#1E1E1E)
    public static final Color BG_INPUT      = new DynamicColor(Color.WHITE, new Color(0x2D, 0x2D, 0x2D));                    // white vs input gray
    public static final Color BG_HOVER      = new DynamicColor(new Color(0xFA, 0xFA, 0xFA), new Color(0x2E, 0x2E, 0x2E));  // zinc-50 vs hover gray

    // Border
    public static final Color BORDER_LIGHT  = new DynamicColor(new Color(0xE4, 0xE4, 0xE7), new Color(0x2E, 0x2E, 0x2E));  // zinc-200 vs border gray
    public static final Color BORDER_CARD   = new DynamicColor(new Color(0xE4, 0xE4, 0xE7), new Color(0x2E, 0x2E, 0x2E));  // zinc-200 vs border gray
    public static final Color BORDER_HOVER  = new DynamicColor(new Color(0x93, 0xC5, 0xFD), new Color(0x3B, 0x82, 0xF6));  // blue-300 vs blue-500

    // Text
    public static final Color TEXT_PRIMARY   = new DynamicColor(new Color(0x18, 0x18, 0x1B), new Color(0xFA, 0xFA, 0xFA));  // zinc-900 vs zinc-50
    public static final Color TEXT_SECONDARY = new DynamicColor(new Color(0x71, 0x71, 0x7A), new Color(0xD4, 0xD4, 0xD8));  // zinc-500 vs brighter zinc-300 (#D4D4D8)
    public static final Color TEXT_LIGHT     = new DynamicColor(new Color(0xA1, 0xA1, 0xAA), new Color(0x71, 0x71, 0x7A));  // zinc-400 vs zinc-500
    public static final Color TEXT_WHITE     = Color.WHITE;
    public static final Color TEXT_DARK      = new DynamicColor(new Color(0x3F, 0x3F, 0x46), new Color(0xE4, 0xE4, 0xE7));  // zinc-700 vs zinc-200

    // Accent / Brand
    public static final Color ACCENT_BLUE       = new DynamicColor(new Color(0x3B, 0x82, 0xF6), new Color(0x3B, 0x82, 0xF6));  // blue-500
    public static final Color ACCENT_BLUE_LIGHT = new DynamicColor(new Color(0xEF, 0xF6, 0xFF), new Color(0x1E, 0x29, 0x3B));  // blue-50 vs blue-900 (smooth selected state)
    public static final Color ACCENT_BLUE_BG    = new DynamicColor(new Color(0xDB, 0xEA, 0xFE), new Color(0x1E, 0x29, 0x3B));  // blue-100 vs blue-950
    public static final Color ACCENT_CYAN       = new Color(0x06, 0xB6, 0xD4);  // (legacy)
    public static final Color ACCENT_GREEN      = new Color(0x22, 0xC5, 0x5E);  // green-500
    public static final Color ACCENT_GREEN_DARK = new Color(0x2D, 0x5F, 0x2D);  // legacy
    public static final Color ACCENT_GREEN_LIGHT = new Color(0xE8, 0xF5, 0xE9); // legacy

    // Prioritas
    public static final Color PRIORITY_HIGH   = new Color(0xEF, 0x44, 0x44);  // red-500
    public static final Color PRIORITY_MEDIUM = new Color(0xF5, 0x9E, 0x0B);  // amber-500
    public static final Color PRIORITY_LOW    = new Color(0x22, 0xC5, 0x5E);  // green-500

    // Priority light backgrounds
    public static final Color RED_LIGHT_BG   = new DynamicColor(new Color(0xFE, 0xF2, 0xF2), new Color(0x45, 0x1A, 0x1A));  // red-50 vs red-900/dark
    public static final Color AMBER_LIGHT_BG = new DynamicColor(new Color(0xFF, 0xFB, 0xEB), new Color(0x45, 0x2E, 0x1A));  // amber-50 vs amber-900/dark

    // Warning Banner
    public static final Color WARNING_BG     = new DynamicColor(new Color(0xFE, 0xFC, 0xE8), new Color(250, 202, 21, 25));  // yellow-50 vs 10% opacity yellow-400
    public static final Color WARNING_BORDER = new DynamicColor(new Color(0xFD, 0xE0, 0x47), new Color(250, 202, 21));       // yellow-300 vs solid yellow-400
    public static final Color WARNING_TEXT   = new DynamicColor(new Color(0x71, 0x3F, 0x12), new Color(254, 240, 138));      // yellow-900 vs bright yellow-200

    // Mata Kuliah Badge
    public static final Color MK_PBO        = new Color(0x22, 0xC5, 0x5E);  // green
    public static final Color MK_BASDAT     = new Color(0xF5, 0x9E, 0x0B);  // amber
    public static final Color MK_MATEMATIKA = new Color(0xEF, 0x44, 0x44);  // red
    public static final Color MK_JARINGAN   = new Color(0x3B, 0x82, 0xF6);  // blue

    // Button
    public static final Color BTN_ACTIVE   = new Color(0x3B, 0x82, 0xF6);  // blue
    public static final Color BTN_INACTIVE = new DynamicColor(new Color(0xF4, 0xF4, 0xF5), new Color(0x27, 0x27, 0x2A));  // zinc-100 vs zinc-800
    public static final Color BTN_ADD      = new Color(0x3B, 0x82, 0xF6);  // blue
    public static final Color BTN_DELETE   = new Color(0xEF, 0x44, 0x44);  // red
    public static final Color BTN_DARK     = new DynamicColor(new Color(0x18, 0x18, 0x1B), new Color(0xFA, 0xFA, 0xFA));  // zinc-900 vs zinc-50 (active filter pill)

    // Font
    // All fonts: Plus Jakarta Sans
    // Headline : 18–20px / weight 600 (Bold)
    // Body     : 13–14px / weight 400 (Plain)
    // Label    : 11–12px / weight 500 (Bold approximation)

    static {
        loadAndRegisterFont("fonts/PlusJakartaSans-Regular.ttf");
        loadAndRegisterFont("fonts/PlusJakartaSans-Bold.ttf");
        loadAndRegisterFont("fonts/Inter-Regular.ttf");
        loadAndRegisterFont("fonts/Inter-Bold.ttf");
        loadAndRegisterFont("fonts/JetBrainsMono-Regular.ttf");
        loadAndRegisterFont("fonts/JetBrainsMono-Bold.ttf");
    }

    private static void loadAndRegisterFont(String fontPath) {
        try {
            // 1. Coba dari classpath (JAR / bin/)
            java.io.InputStream is = ColorPalette.class.getClassLoader().getResourceAsStream(fontPath);

            // 2. Fallback: src/<fontPath> (saat run dari IDE dengan working dir = project root)
            if (is == null) {
                java.io.File fontFile = new java.io.File("src/" + fontPath);
                if (fontFile.exists()) {
                    is = new java.io.FileInputStream(fontFile);
                }
            }

            // 3. Fallback: langsung dari working dir (jika bin/ di-set sebagai working dir)
            if (is == null) {
                java.io.File fontFile = new java.io.File(fontPath);
                if (fontFile.exists()) {
                    is = new java.io.FileInputStream(fontFile);
                }
            }

            if (is != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, is);
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                is.close();
            } else {
                System.err.println("[ColorPalette] Font file tidak ditemukan: " + fontPath);
            }
        } catch (Exception e) {
            System.err.println("[ColorPalette] Gagal meload font: " + fontPath);
            e.printStackTrace();
        }
    }

    // Typography System

    // Headline variants
    public static final Font FONT_TITLE       = new Font("Plus Jakarta Sans", Font.BOLD, 28);
    public static final Font FONT_TITLE_ITALIC = new Font("Plus Jakarta Sans", Font.BOLD | Font.ITALIC, 28);
    public static final Font FONT_HEADING      = new Font("Plus Jakarta Sans", Font.BOLD, 20);
    public static final Font FONT_LOGO         = new Font("Plus Jakarta Sans", Font.BOLD, 22);
    public static final Font FONT_STAT_NUM     = new Font("Plus Jakarta Sans", Font.BOLD, 32);

    // Body variants
    public static final Font FONT_SUBTITLE  = new Font("Plus Jakarta Sans", Font.PLAIN, 14);
    public static final Font FONT_BODY      = new Font("Plus Jakarta Sans", Font.PLAIN, 14);
    public static final Font FONT_BODY_SM   = new Font("Plus Jakarta Sans", Font.PLAIN, 13);

    // Label / Meta variants
    public static final Font FONT_LABEL   = new Font("Plus Jakarta Sans", Font.BOLD, 11);
    public static final Font FONT_SMALL   = new Font("Plus Jakarta Sans", Font.PLAIN, 11);
    public static final Font FONT_BUTTON  = new Font("Plus Jakarta Sans", Font.BOLD, 14);
    public static final Font FONT_TAGLINE = new Font("Plus Jakarta Sans", Font.PLAIN, 9);
    public static final Font FONT_BADGE   = new Font("Plus Jakarta Sans", Font.BOLD, 11);

    // Private constructor
    private ColorPalette() {}
}
