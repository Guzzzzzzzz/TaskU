package database;

import model.MataKuliah;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MataKuliahDAO — mata kuliah bersifat per-user (filter by mahasiswa_id).
 */
public class MataKuliahDAO {

    private final Connection conn;

    public MataKuliahDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }


    public List<MataKuliah> getAllByMahasiswa(int mahasiswaId) {
        List<MataKuliah> list = new ArrayList<>();
        String sql = "SELECT id, nama, warna FROM mata_kuliah WHERE mahasiswa_id = ? ORDER BY nama";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new MataKuliah(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("warna")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MataKuliahDAO] getAllByMahasiswa() error: " + e.getMessage());
        }
        return list;
    }


    public MataKuliah getById(int id) {
        String sql = "SELECT id, nama, warna FROM mata_kuliah WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MataKuliah(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("warna")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[MataKuliahDAO] getById() error: " + e.getMessage());
        }
        return null;
    }

    /** Tambah mata kuliah baru. @return ID baru. */
    public int insert(String nama, String warna, int mahasiswaId) {
        String sql = "INSERT INTO mata_kuliah (nama, warna, mahasiswa_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nama);
            stmt.setString(2, warna);
            stmt.setInt(3, mahasiswaId);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MataKuliahDAO] insert() error: " + e.getMessage());
        }
        return -1;
    }

    /** Hapus mata kuliah + semua task terkait (cascade manual). */
    public boolean delete(int mataKuliahId) {
        try {
            conn.setAutoCommit(false);

            // 1. Hapus anggota_kelompok dari task yang terkait matkul ini
            String delAnggota = """
                DELETE FROM anggota_kelompok WHERE task_id IN (
                    SELECT id FROM task WHERE mata_kuliah_id = ?
                )
            """;
            try (PreparedStatement stmt = conn.prepareStatement(delAnggota)) {
                stmt.setInt(1, mataKuliahId);
                stmt.executeUpdate();
            }

            // 2. Hapus semua task yang terkait matkul ini
            String delTask = "DELETE FROM task WHERE mata_kuliah_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(delTask)) {
                stmt.setInt(1, mataKuliahId);
                stmt.executeUpdate();
            }

            // 3. Hapus mata kuliah itu sendiri
            String delMK = "DELETE FROM mata_kuliah WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(delMK)) {
                stmt.setInt(1, mataKuliahId);
                int affected = stmt.executeUpdate();
                conn.commit();
                conn.setAutoCommit(true);
                return affected > 0;
            }

        } catch (SQLException e) {
            System.err.println("[MataKuliahDAO] delete() error: " + e.getMessage());
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("[MataKuliahDAO] rollback error: " + ex.getMessage());
            }
            return false;
        }
    }
}
