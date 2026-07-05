package util;

import java.awt.*;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Utility untuk memuat icon aplikasi TaskU.
 * Catatan: Java tidak bisa membaca .ico, harus .png.
 */
public class AppIcon {

    private static Image cachedIcon = null;

    /** Icon di-cache agar hanya dimuat satu kali. */
    public static Image getIcon() {
        if (cachedIcon == null) {
            try {
                URL iconUrl = AppIcon.class.getClassLoader().getResource("resources/app-icon.png");
                if (iconUrl != null) {
                    cachedIcon = ImageIO.read(iconUrl);
                }
            } catch (Exception e) {
                System.err.println("[AppIcon] Gagal memuat icon: " + e.getMessage());
            }
        }
        return cachedIcon;
    }


    public static void apply(JFrame frame) {
        Image icon = getIcon();
        if (icon != null) {
            frame.setIconImage(icon);
        }
    }


    public static void apply(JDialog dialog) {
        Image icon = getIcon();
        if (icon != null) {
            dialog.setIconImage(icon);
        }
    }
}
