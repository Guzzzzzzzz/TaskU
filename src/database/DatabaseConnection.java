package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton — mengelola koneksi tunggal ke database SQLite.
 * Tabel dibuat otomatis saat pertama kali aplikasi dijalankan.
 */
public class DatabaseConnection {


    private static final String DB_FILE = "tasku.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;


    private static DatabaseConnection instance = null;
    private Connection connection = null;


    private DatabaseConnection() {
        try {

            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(URL);

            // Aktifkan foreign key support (SQLite default-nya OFF)
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            System.out.println("[DB] Koneksi ke database SQLite berhasil! (" + DB_FILE + ")");

            initializeTables();

        } catch (ClassNotFoundException e) {
            System.err.println("[DB] SQLite JDBC Driver tidak ditemukan: " + e.getMessage());
            System.err.println("[DB] Pastikan file sqlite-jdbc-*.jar ada di folder /lib");
        } catch (SQLException e) {
            System.err.println("[DB] Gagal terhubung ke database: " + e.getMessage());
        }
    }


    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }


    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DB] Koneksi terputus, mencoba reconnect...");
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error cek status koneksi: " + e.getMessage());
        }
        return connection;
    }


    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                instance = null;
                System.out.println("[DB] Koneksi database ditutup.");
            } catch (SQLException e) {
                System.err.println("[DB] Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }



    private void initializeTables() {
        try (Statement stmt = connection.createStatement()) {


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mahasiswa (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nama TEXT NOT NULL,
                    nim TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL
                )
            """);


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mata_kuliah (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nama TEXT NOT NULL,
                    warna TEXT DEFAULT '#2D5F2D',
                    mahasiswa_id INTEGER NOT NULL,
                    FOREIGN KEY (mahasiswa_id) REFERENCES mahasiswa(id)
                )
            """);


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS task (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    judul TEXT NOT NULL,
                    deskripsi TEXT,
                    deadline TEXT,
                    prioritas TEXT DEFAULT 'SEDANG',
                    status TEXT DEFAULT 'BELUM',
                    jenis TEXT DEFAULT 'INDIVIDU',
                    mata_kuliah_id INTEGER,
                    mahasiswa_id INTEGER NOT NULL,
                    created_at TEXT DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY (mata_kuliah_id) REFERENCES mata_kuliah(id),
                    FOREIGN KEY (mahasiswa_id) REFERENCES mahasiswa(id)
                )
            """);


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS anggota_kelompok (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    task_id INTEGER NOT NULL,
                    nama_anggota TEXT NOT NULL,
                    peran TEXT DEFAULT '',
                    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    mahasiswa_id INTEGER PRIMARY KEY,
                    is_dark_mode INTEGER DEFAULT 0,
                    language TEXT DEFAULT 'ID',
                    FOREIGN KEY (mahasiswa_id) REFERENCES mahasiswa(id)
                )
            """);

            System.out.println("[DB] Tabel berhasil diinisialisasi.");

        } catch (SQLException e) {
            System.err.println("[DB] Gagal membuat tabel: " + e.getMessage());
        }

        // Migrasi password plain-text ke BCrypt (one-time, otomatis)
        migratePasswords();
    }

    /**
     * Migrasi otomatis: Deteksi password plain-text dan hash ke BCrypt.
     * Password yang sudah di-hash (dimulai "$2a$") akan di-skip.
     * Aman dipanggil berulang kali — idempotent.
     */
    private void migratePasswords() {
        try {
            String selectSql = "SELECT id, password FROM mahasiswa";
            String updateSql = "UPDATE mahasiswa SET password = ? WHERE id = ?";

            try (Statement selectStmt = connection.createStatement();
                 ResultSet rs = selectStmt.executeQuery(selectSql)) {

                int migrated = 0;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String pw = rs.getString("password");

                    // Skip jika sudah BCrypt hash
                    if (pw != null && util.PasswordUtil.isBCryptHash(pw)) {
                        continue;
                    }

                    // Hash plain-text password
                    String hashed = util.PasswordUtil.hash(pw);
                    try (java.sql.PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setString(1, hashed);
                        updateStmt.setInt(2, id);
                        updateStmt.executeUpdate();
                    }
                    migrated++;
                }

                if (migrated > 0) {
                    System.out.println("[DB] Migrasi selesai: " + migrated + " password berhasil di-hash ke BCrypt.");
                }
            }

        } catch (SQLException e) {
            System.err.println("[DB] Migrasi password gagal: " + e.getMessage());
        }
    }
}
