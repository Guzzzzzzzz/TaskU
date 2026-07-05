package database;

import model.Mahasiswa;
import util.PasswordUtil;
import java.sql.*;

/**
 * MahasiswaDAO — operasi database untuk tabel mahasiswa.
 * Password di-hash menggunakan BCrypt sebelum disimpan.
 */
public class MahasiswaDAO {

    private final Connection conn;

    public MahasiswaDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /** Autentikasi berdasarkan NIM dan password (BCrypt-safe). */
    public Mahasiswa login(String nim, String password) {
        String sql = "SELECT id, nama, nim, password FROM mahasiswa WHERE nim = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nim);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.verify(password, storedHash)) {
                        return new Mahasiswa(
                            rs.getInt("id"),
                            rs.getString("nama"),
                            rs.getString("nim"),
                            storedHash
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MahasiswaDAO] login() error: " + e.getMessage());
        }

        return null;
    }

    /** Daftarkan mahasiswa baru. NIM harus unik. Password di-hash otomatis. */
    public boolean register(Mahasiswa mhs) {

        if (isNimExists(mhs.getNim())) {
            System.err.println("[MahasiswaDAO] register() gagal: NIM sudah terdaftar!");
            return false;
        }

        String sql = "INSERT INTO mahasiswa (nama, nim, password) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mhs.getNama());
            stmt.setString(2, mhs.getNim());
            stmt.setString(3, PasswordUtil.hash(mhs.getPassword()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MahasiswaDAO] register() error: " + e.getMessage());
            return false;
        }
    }


    public boolean isNimExists(String nim) {
        String sql = "SELECT COUNT(*) FROM mahasiswa WHERE nim = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nim);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[MahasiswaDAO] isNimExists() error: " + e.getMessage());
        }
        return false;
    }


    public Mahasiswa getMahasiswaById(int id) {
        String sql = "SELECT id, nama, nim FROM mahasiswa WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Mahasiswa(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("nim")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[MahasiswaDAO] getMahasiswaById() error: " + e.getMessage());
        }
        return null;
    }

    /** Autentikasi berdasarkan nama (username) dan password (BCrypt-safe). */
    public Mahasiswa loginByUsername(String username, String password) {
        String sql = "SELECT id, nama, nim, password FROM mahasiswa WHERE nama = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.verify(password, storedHash)) {
                        return new Mahasiswa(
                            rs.getInt("id"),
                            rs.getString("nama"),
                            rs.getString("nim"),
                            storedHash
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MahasiswaDAO] loginByUsername() error: " + e.getMessage());
        }

        return null;
    }

    /**
     * Reset password berdasarkan NIM — digunakan oleh fitur "Lupa Password".
     * @param nim NIM mahasiswa
     * @param newHashedPassword password baru yang SUDAH di-hash dengan BCrypt
     * @return true jika berhasil di-update
     */
    public boolean resetPasswordByNim(String nim, String newHashedPassword) {
        String sql = "UPDATE mahasiswa SET password = ? WHERE nim = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setString(2, nim);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MahasiswaDAO] resetPasswordByNim() error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update password berdasarkan user ID — digunakan oleh fitur "Ganti Password".
     * @param userId ID mahasiswa
     * @param newHashedPassword password baru yang SUDAH di-hash dengan BCrypt
     * @return true jika berhasil di-update
     */
    public boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE mahasiswa SET password = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MahasiswaDAO] updatePassword() error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ambil stored hash berdasarkan user ID — untuk verifikasi password lama saat ganti password.
     */
    public String getPasswordHashById(int userId) {
        String sql = "SELECT password FROM mahasiswa WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        } catch (SQLException e) {
            System.err.println("[MahasiswaDAO] getPasswordHashById() error: " + e.getMessage());
        }
        return null;
    }
}
