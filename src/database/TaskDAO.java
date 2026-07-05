package database;

import model.AnggotaKelompok;
import model.Mahasiswa;
import model.MataKuliah;
import model.Task;
import model.Task.Jenis;
import model.Task.Prioritas;
import model.Task.Status;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskDAO — Data Access Object (SQLite).
 * Semua query di-filter berdasarkan mahasiswa_id (data isolation).
 */
public class TaskDAO {

    private final Connection conn;
    private final AnggotaKelompokDAO anggotaDAO;

    public TaskDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
        this.anggotaDAO = new AnggotaKelompokDAO();
    }

    // --- Helper ---

    /** Konversi baris database ke objek Task. */

    private Task mapRowToTask(ResultSet rs) throws SQLException {

        MataKuliah mk = null;
        int mkId = rs.getInt("mata_kuliah_id");
        if (!rs.wasNull() && mkId > 0) {
            mk = new MataKuliahDAO().getById(mkId);
        }


        Mahasiswa mhs = null;
        int mhsId = rs.getInt("mahasiswa_id");
        if (!rs.wasNull() && mhsId > 0) {
            mhs = new MahasiswaDAO().getMahasiswaById(mhsId);
        }


        LocalDate deadline = null;
        String deadlineStr = rs.getString("deadline");
        if (deadlineStr != null && !deadlineStr.isEmpty()) {
            deadline = LocalDate.parse(deadlineStr);
        }


        LocalDateTime createdAt = null;
        String createdStr = rs.getString("created_at");
        if (createdStr != null && !createdStr.isEmpty()) {
            createdAt = LocalDateTime.parse(createdStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        Task task = new Task(
            rs.getInt("id"),
            rs.getString("judul"),
            rs.getString("deskripsi"),
            deadline,
            Prioritas.valueOf(rs.getString("prioritas")),
            Status.valueOf(rs.getString("status")),
            Jenis.valueOf(rs.getString("jenis")),
            mk, mhs, createdAt
        );

        // Load anggota kelompok jika jenis == KELOMPOK
        if (task.getJenis() == Jenis.KELOMPOK) {
            List<AnggotaKelompok> anggota = anggotaDAO.getByTaskId(task.getId());
            task.setAnggotaList(anggota);
        }

        return task;
    }

    // --- Read ---


    public List<Task> getAllTasks(int mahasiswaId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE mahasiswa_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] getAllTasks() error: " + e.getMessage());
        }
        return tasks;
    }


    public List<Task> getTasksByMataKuliah(int mahasiswaId, int mkId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE mahasiswa_id = ? AND mata_kuliah_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            stmt.setInt(2, mkId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] getTasksByMataKuliah() error: " + e.getMessage());
        }
        return tasks;
    }

    public List<Task> getTasksByStatus(int mahasiswaId, Status status) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE mahasiswa_id = ? AND status = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            stmt.setString(2, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] getTasksByStatus() error: " + e.getMessage());
        }
        return tasks;
    }


    public List<Task> getTasksByPriority(int mahasiswaId, Prioritas prioritas) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE mahasiswa_id = ? AND prioritas = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            stmt.setString(2, prioritas.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] getTasksByPriority() error: " + e.getMessage());
        }
        return tasks;
    }


    public List<Task> searchTasks(int mahasiswaId, String keyword) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE mahasiswa_id = ? AND judul LIKE ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            stmt.setString(2, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] searchTasks() error: " + e.getMessage());
        }
        return tasks;
    }


    public List<Task> getTasksToday(int mahasiswaId) {
        List<Task> tasks = new ArrayList<>();
        String today = LocalDate.now().toString();
        String sql = "SELECT * FROM task WHERE mahasiswa_id = ? AND deadline = ? AND status != 'SELESAI'";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            stmt.setString(2, today);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] getTasksToday() error: " + e.getMessage());
        }
        return tasks;
    }

    // --- Statistik ---


    public int getTotalTasks(int mahasiswaId) {
        String sql = "SELECT COUNT(*) FROM task WHERE mahasiswa_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] getTotalTasks() error: " + e.getMessage());
        }
        return 0;
    }


    public int getCompletedCount(int mahasiswaId) {
        String sql = "SELECT COUNT(*) FROM task WHERE mahasiswa_id = ? AND status = 'SELESAI'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] getCompletedCount() error: " + e.getMessage());
        }
        return 0;
    }


    public int getUrgentCount(int mahasiswaId) {
        String twoDaysLater = LocalDate.now().plusDays(2).toString();
        String today = LocalDate.now().toString();
        String sql = "SELECT COUNT(*) FROM task WHERE mahasiswa_id = ? AND status != 'SELESAI' "
                   + "AND deadline IS NOT NULL AND deadline >= ? AND deadline <= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mahasiswaId);
            stmt.setString(2, today);
            stmt.setString(3, twoDaysLater);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] getUrgentCount() error: " + e.getMessage());
        }
        return 0;
    }

    // --- Create / Update / Delete ---

    /** Tambah tugas baru. @return ID yang baru dibuat, atau -1 jika gagal. */
    public int addTask(Task task) {
        String sql = "INSERT INTO task (judul, deskripsi, deadline, prioritas, status, jenis, mata_kuliah_id, mahasiswa_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, task.getJudul());
            stmt.setString(2, task.getDeskripsi());
            stmt.setString(3, task.getDeadline() != null ? task.getDeadline().toString() : null);
            stmt.setString(4, task.getPrioritas().name());
            stmt.setString(5, task.getStatus().name());
            stmt.setString(6, task.getJenis().name());
            if (task.getMataKuliah() != null) {
                stmt.setInt(7, task.getMataKuliah().getId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setInt(8, task.getMahasiswa().getId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);

                    // Jika kelompok, simpan anggota juga
                    if (task.getJenis() == Jenis.KELOMPOK && !task.getAnggotaList().isEmpty()) {
                        anggotaDAO.insertAll(newId, task.getAnggotaList());
                    }

                    return newId;
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDAO] addTask() error: " + e.getMessage());
        }
        return -1;
    }


    public boolean updateStatus(int taskId, Status newStatus) {
        String sql = "UPDATE task SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TaskDAO] updateStatus() error: " + e.getMessage());
            return false;
        }
    }


    public boolean updateDeskripsi(int taskId, String deskripsi) {
        String sql = "UPDATE task SET deskripsi = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, deskripsi);
            stmt.setInt(2, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TaskDAO] updateDeskripsi() error: " + e.getMessage());
            return false;
        }
    }

    /** Hapus tugas beserta anggota kelompoknya. */
    public boolean deleteTask(int taskId) {
        // Hapus anggota kelompok dulu (karena FK constraint)
        anggotaDAO.deleteByTaskId(taskId);

        String sql = "DELETE FROM task WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TaskDAO] deleteTask() error: " + e.getMessage());
            return false;
        }
    }
}
