package database;

import model.AnggotaKelompok;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AnggotaKelompokDAO — operasi CRUD untuk anggota kelompok.
 */
public class AnggotaKelompokDAO {

    private final Connection conn;

    public AnggotaKelompokDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }


    public List<AnggotaKelompok> getByTaskId(int taskId) {
        List<AnggotaKelompok> list = new ArrayList<>();
        String sql = "SELECT id, task_id, nama_anggota, peran FROM anggota_kelompok WHERE task_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AnggotaKelompok(
                        rs.getInt("id"),
                        rs.getInt("task_id"),
                        rs.getString("nama_anggota"),
                        rs.getString("peran")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnggotaKelompokDAO] getByTaskId() error: " + e.getMessage());
        }
        return list;
    }


    public boolean insert(AnggotaKelompok anggota) {
        String sql = "INSERT INTO anggota_kelompok (task_id, nama_anggota, peran) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, anggota.getTaskId());
            stmt.setString(2, anggota.getNamaAnggota());
            stmt.setString(3, anggota.getPeran());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AnggotaKelompokDAO] insert() error: " + e.getMessage());
            return false;
        }
    }


    public void insertAll(int taskId, List<AnggotaKelompok> anggotaList) {
        String sql = "INSERT INTO anggota_kelompok (task_id, nama_anggota, peran) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (AnggotaKelompok a : anggotaList) {
                stmt.setInt(1, taskId);
                stmt.setString(2, a.getNamaAnggota());
                stmt.setString(3, a.getPeran());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("[AnggotaKelompokDAO] insertAll() error: " + e.getMessage());
        }
    }


    public boolean deleteByTaskId(int taskId) {
        String sql = "DELETE FROM anggota_kelompok WHERE task_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("[AnggotaKelompokDAO] deleteByTaskId() error: " + e.getMessage());
            return false;
        }
    }
}
