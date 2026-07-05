package database;

import java.sql.*;

/**
 * SettingsDAO — Menyimpan dan memuat preferensi pengguna (Dark Mode & Bahasa) per mahasiswa ke database.
 */
public class SettingsDAO {
    private final Connection conn;

    public SettingsDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Mengambil status dark mode mahasiswa.
     */
    public boolean isDarkMode(int mahasiswaId) {
        String sql = "SELECT is_dark_mode FROM settings WHERE mahasiswa_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("is_dark_mode") == 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("[SettingsDAO] isDarkMode() error: " + e.getMessage());
        }
        return false; // default light mode
    }

    /**
     * Mengambil preferensi bahasa mahasiswa.
     */
    public String getLanguage(int mahasiswaId) {
        String sql = "SELECT language FROM settings WHERE mahasiswa_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("language");
                }
            }
        } catch (SQLException e) {
            System.err.println("[SettingsDAO] getLanguage() error: " + e.getMessage());
        }
        return "ID"; // default Bahasa Indonesia
    }

    /**
     * Menyimpan atau memperbarui preferensi bahasa & tema mahasiswa.
     */
    public void saveSettings(int mahasiswaId, boolean isDarkMode, String language) {
        String sql = "INSERT OR REPLACE INTO settings (mahasiswa_id, is_dark_mode, language) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            stmt.setInt(2, isDarkMode ? 1 : 0);
            stmt.setString(3, language);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SettingsDAO] saveSettings() error: " + e.getMessage());
        }
    }
}
