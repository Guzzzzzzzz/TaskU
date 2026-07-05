import ui.LoginFrame;

import javax.swing.*;

/**
 * Entry Point aplikasi TaskU.
 */
public class Main {

    public static void main(String[] args) {
        // Set Look and Feel agar tampilan konsisten di semua OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] Gagal set Look and Feel: " + e.getMessage());
        }

        // Jalankan LoginFrame di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
