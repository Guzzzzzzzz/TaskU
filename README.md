# TaskU — Setup Database (Fase 1)

## Prasyarat

| Software | Versi | Link |
|----------|-------|------|
| Java JDK | 17+ | https://www.oracle.com/java/technologies/downloads/ |
| MySQL Server | 8.0+ | https://dev.mysql.com/downloads/mysql/ |
| MySQL Connector/J | 8.x | https://dev.mysql.com/downloads/connector/j/ |

---

## Langkah Setup

### 1. Install MySQL & Jalankan Server
Pastikan MySQL server sudah berjalan. Cek dengan:
```bash
mysql -u root -p
```

### 2. Jalankan SQL Schema
```bash
mysql -u root -p < sql/tasku_schema.sql
```
Atau buka file `sql/tasku_schema.sql` di MySQL Workbench / phpMyAdmin dan jalankan.

### 3. Tambahkan JDBC Driver ke Project
- Download `mysql-connector-j-x.x.x.jar` dari link di atas
- Taruh di folder `lib/`
- Tambahkan ke classpath project:
  - **IDE (IntelliJ/Eclipse/NetBeans)** → klik kanan project → Add Library → pilih .jar
  - **Compile manual** → `javac -cp "lib/mysql-connector-j-*.jar" ...`

### 4. Sesuaikan Konfigurasi di `DatabaseConnection.java`
```java
private static final String HOST     = "localhost";   // alamat MySQL server
private static final String PORT     = "3306";        // port default MySQL
private static final String DB_NAME  = "tasku_db";
private static final String USERNAME = "root";
private static final String PASSWORD = "";            // ← ganti dengan password kamu
```

---

## Struktur Database

```
tasku_db
├── mahasiswa        (id, nama, nim)
├── mata_kuliah      (id, nama, warna)
└── task             (id, judul, deskripsi, deadline, prioritas,
                      status, jenis, mata_kuliah_id, mahasiswa_id,
                      created_at)
```

---

## Verifikasi Database Berhasil Dibuat

Jalankan query berikut di MySQL:
```sql
USE tasku_db;
SELECT * FROM mahasiswa;
SELECT * FROM mata_kuliah;
SELECT * FROM task;
```

Harusnya muncul:
- 1 data mahasiswa (Arya Mahendra / 22SI001)
- 4 mata kuliah (PBO, Basis Data, Matematika, Jaringan)
- 7 contoh task

---

## File yang Dibuat di Fase 1

| File | Keterangan |
|------|-----------|
| `sql/tasku_schema.sql` | Script SQL untuk membuat semua tabel & data awal |
| `src/database/DatabaseConnection.java` | Koneksi JDBC dengan Singleton Pattern |
| `src/database/TaskDAO.java` | CRUD untuk tabel task (9 method) |
| `src/database/MataKuliahDAO.java` | Read/insert untuk tabel mata_kuliah |
| `src/database/MahasiswaDAO.java` | CRUD untuk tabel mahasiswa |

---

## Konsep PBO di Fase 1

| Konsep | Implementasi |
|--------|-------------|
| **Encapsulation** | Private fields + Public methods di semua DAO class |
| **Abstraction** | DAO menyembunyikan detail SQL dari layer lain |
| **Singleton Pattern** | `DatabaseConnection.getInstance()` — hanya 1 koneksi DB |
