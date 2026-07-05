# Pseudocode Per Fitur Utama — Aplikasi TaskU

---

## Fitur 1: Registrasi Akun Baru

```
PROGRAM Fitur_Registrasi

DEKLARASI
    nama        : STRING
    nim         : STRING
    password    : STRING
    cekNim      : INTEGER
    berhasil    : BOOLEAN

BEGIN
    INPUT nama
    INPUT nim
    INPUT password

    IF nama = "" OR nim = "" OR password = "" THEN
        OUTPUT "Semua field wajib diisi!"
        RETURN
    END IF

    IF LENGTH(password) < 4 THEN
        OUTPUT "Password minimal 4 karakter!"
        RETURN
    END IF

    cekNim ← Query "SELECT COUNT(*) FROM mahasiswa WHERE nim = ?"
    IF cekNim > 0 THEN
        OUTPUT "NIM sudah terdaftar! Silakan login."
        RETURN
    END IF

    berhasil ← Query "INSERT INTO mahasiswa (nama, nim, password) VALUES (?, ?, ?)"

    IF berhasil = TRUE THEN
        OUTPUT "Registrasi berhasil! Mengalihkan..."
        TUNGGU 1.5 detik
        BUKA Halaman Login
    ELSE
        OUTPUT "Registrasi gagal. Coba lagi."
    END IF
END
```

---

## Fitur 2: Login

```
PROGRAM Fitur_Login

DEKLARASI
    username    : STRING
    password    : STRING
    mahasiswa   : OBJECT Mahasiswa

BEGIN
    INPUT username
    INPUT password

    IF username = "" OR password = "" THEN
        OUTPUT "Username dan Password tidak boleh kosong!"
        RETURN
    END IF

    mahasiswa ← Query "SELECT * FROM mahasiswa WHERE nama = ? AND password = ?"

    IF mahasiswa != NULL THEN
        TUTUP Halaman Login
        BUKA Halaman Utama DENGAN DATA mahasiswa
    ELSE
        OUTPUT "Username atau Password salah!"
        KOSONGKAN field password
    END IF
END
```

---

## Fitur 3: Lupa Password

```
PROGRAM Fitur_Lupa_Password

DEKLARASI
    nim         : STRING
    password    : STRING

BEGIN
    INPUT nim

    IF nim = "" THEN
        OUTPUT "NIM tidak boleh kosong!"
        RETURN
    END IF

    password ← Query "SELECT password FROM mahasiswa WHERE nim = ?"

    IF password != NULL THEN
        OUTPUT "Password Anda: " + password
    ELSE
        OUTPUT "NIM tidak ditemukan!"
    END IF
END
```

---

## Fitur 4: Dashboard Statistik

```
PROGRAM Fitur_Dashboard_Statistik

DEKLARASI
    total       : INTEGER
    selesai     : INTEGER
    mendesak    : INTEGER
    kategori    : INTEGER
    persentase  : INTEGER
    pesan       : STRING

BEGIN
    total     ← Query "SELECT COUNT(*) FROM task WHERE mahasiswa_id = ?"
    selesai   ← Query "SELECT COUNT(*) FROM task WHERE mahasiswa_id = ? AND status = 'SELESAI'"
    mendesak  ← Query "SELECT COUNT(*) FROM task WHERE mahasiswa_id = ? AND deadline <= 2 hari dari sekarang AND status != 'SELESAI'"
    kategori  ← Query "SELECT COUNT(DISTINCT mata_kuliah_id) FROM task WHERE mahasiswa_id = ?"

    persentase ← (selesai / total) * 100

    OUTPUT Kartu 1: "TOTAL TUGAS: " + total
    OUTPUT Kartu 2: "SELESAI: " + selesai + " (" + persentase + "% dari total)"
    OUTPUT Kartu 3: "MENDESAK: " + mendesak
    OUTPUT Kartu 4: "KATEGORI: " + kategori

    IF mendesak > 0 THEN
        pesan ← getDeadlineWarningMessage()
        TAMPILKAN Banner Peringatan DENGAN pesan
    ELSE
        SEMBUNYIKAN Banner Peringatan
    END IF
END
```

---

## Fitur 5: Tambah Tugas Baru

```
PROGRAM Fitur_Tambah_Tugas

DEKLARASI
    judul           : STRING
    mataKuliah      : OBJECT MataKuliah
    prioritas       : STRING
    deadline        : DATE
    deadlineStr     : STRING
    jenisTugas      : STRING
    daftarAnggota   : LIST OF AnggotaKelompok
    nomorKelompok   : STRING
    namaAnggota     : STRING
    hasilId         : INTEGER

BEGIN
    INPUT judul
    INPUT mataKuliah
    INPUT prioritas
    INPUT deadlineStr
    INPUT jenisTugas

    IF judul = "" THEN
        OUTPUT "Judul tugas tidak boleh kosong!"
        RETURN
    END IF

    IF mataKuliah = NULL THEN
        OUTPUT "Mata kuliah belum dipilih!"
        RETURN
    END IF

    IF deadlineStr != "" THEN
        deadline ← parseDate(deadlineStr, "DD-MM-YYYY")
        IF deadline = INVALID THEN
            OUTPUT "Format deadline salah! Gunakan DD-MM-YYYY"
            RETURN
        END IF
    END IF

    daftarAnggota ← List kosong
    IF jenisTugas = "KELOMPOK" THEN
        INPUT nomorKelompok
        IF nomorKelompok != "" THEN
            TAMBAH {nama: nomorKelompok, peran: "#GROUP_NUM#"} KE daftarAnggota
        END IF

        UNTUK SETIAP fieldAnggota LAKUKAN
            INPUT namaAnggota
            IF namaAnggota != "" THEN
                TAMBAH {nama: namaAnggota, peran: ""} KE daftarAnggota
            END IF
        END UNTUK
    END IF

    hasilId ← Query "INSERT INTO task (judul, deskripsi, deadline, prioritas, status, jenis, mata_kuliah_id, mahasiswa_id) 
                      VALUES (?, '', ?, ?, 'BELUM', ?, ?, ?)"

    UNTUK SETIAP anggota DALAM daftarAnggota LAKUKAN
        Query "INSERT INTO anggota_kelompok (task_id, nama_anggota, peran) VALUES (?, ?, ?)"
    END UNTUK

    IF hasilId > 0 THEN
        TUTUP form tambah tugas
        REFRESH seluruh tampilan
    ELSE
        OUTPUT "Gagal menyimpan tugas!"
    END IF
END
```

---

## Fitur 6: Lihat Detail Tugas & Edit Deskripsi

```
PROGRAM Fitur_Lihat_Detail_dan_Edit_Deskripsi

DEKLARASI
    task            : OBJECT Task
    daftarAnggota   : LIST OF AnggotaKelompok
    deskripsiBaru   : STRING

BEGIN
    OUTPUT "Judul: " + task.judul
    OUTPUT "Mata Kuliah: " + task.mataKuliah.nama
    OUTPUT "Deadline: " + task.deadline
    OUTPUT "Prioritas: " + task.prioritas
    OUTPUT "Status: " + task.status
    OUTPUT "Jenis: " + task.jenis

    IF task.jenis = "KELOMPOK" THEN
        daftarAnggota ← Query "SELECT * FROM anggota_kelompok WHERE task_id = ?"
        UNTUK SETIAP anggota DALAM daftarAnggota LAKUKAN
            OUTPUT "Anggota: " + anggota.nama
        END UNTUK
    END IF

    TAMPILKAN area teks berisi task.deskripsi

    INPUT deskripsiBaru

    IF deskripsiBaru != task.deskripsi THEN
        Query "UPDATE task SET deskripsi = ? WHERE id = ?"
        task.deskripsi ← deskripsiBaru
    END IF

    TUTUP dialog detail
END
```

---

## Fitur 7: Tandai Tugas Selesai / Belum Selesai

```
PROGRAM Fitur_Tandai_Selesai

DEKLARASI
    task        : OBJECT Task
    statusBaru  : STRING
    checked     : BOOLEAN

BEGIN
    checked ← NOT checked

    IF checked = TRUE THEN
        statusBaru ← "SELESAI"
    ELSE
        statusBaru ← "BELUM"
    END IF

    Query "UPDATE task SET status = ? WHERE id = ?"
    task.status ← statusBaru

    IF statusBaru = "SELESAI" THEN
        CORET teks judul tugas
        UBAH warna teks jadi abu-abu
        UBAH checkbox jadi centang hijau
    ELSE
        KEMBALIKAN teks judul normal
        UBAH warna teks jadi hitam
        UBAH checkbox jadi lingkaran kosong
    END IF

    TUNGGU 300 milidetik
    REFRESH seluruh tampilan
END
```

---

## Fitur 8: Hapus Tugas

```
PROGRAM Fitur_Hapus_Tugas

DEKLARASI
    task        : OBJECT Task
    confirmed   : BOOLEAN

BEGIN
    OUTPUT "Hapus Tugas?"
    OUTPUT "Tugas ini akan dihapus secara permanen"
    OUTPUT Tombol: [Batal] [Ya, Hapus]

    INPUT confirmed

    IF confirmed = FALSE THEN
        TUTUP dialog
        RETURN
    END IF

    Query "DELETE FROM anggota_kelompok WHERE task_id = ?"
    Query "DELETE FROM task WHERE id = ?"

    REFRESH seluruh tampilan
END
```

---

## Fitur 9: Filter & Pencarian Tugas

```
PROGRAM Fitur_Filter_dan_Pencarian

DEKLARASI
    viewAktif       : STRING
    filterStatus    : STRING
    filterPrioritas : STRING
    keyword         : STRING
    daftarTugas     : LIST OF Task
    hasilFilter     : LIST OF Task
    tugas           : OBJECT Task
    cocokStatus     : BOOLEAN
    cocokPrioritas  : BOOLEAN
    cocokKeyword    : BOOLEAN

BEGIN
    SWITCH viewAktif:
        CASE "ALL":
            daftarTugas ← Query "SELECT * FROM task WHERE mahasiswa_id = ?"
        CASE "TODAY":
            daftarTugas ← Query "SELECT * FROM task WHERE mahasiswa_id = ? AND deadline = hari ini"
        CASE "COMPLETED":
            daftarTugas ← Query "SELECT * FROM task WHERE mahasiswa_id = ? AND status = 'SELESAI'"
        CASE "MATKUL":
            daftarTugas ← Query "SELECT * FROM task WHERE mahasiswa_id = ? AND mata_kuliah_id = ?"
    END SWITCH

    hasilFilter ← List kosong

    UNTUK SETIAP tugas DALAM daftarTugas LAKUKAN
        cocokStatus    ← (filterStatus = NULL) OR (tugas.status = filterStatus)
        cocokPrioritas ← (filterPrioritas = NULL) OR (tugas.prioritas = filterPrioritas)
        cocokKeyword   ← (keyword = "") OR (tugas.judul MENGANDUNG keyword)

        IF cocokStatus AND cocokPrioritas AND cocokKeyword THEN
            TAMBAH tugas KE hasilFilter
        END IF
    END UNTUK

    UNTUK SETIAP tugas DALAM hasilFilter LAKUKAN
        TAMPILKAN kartu tugas di layar
    END UNTUK
END
```

---

## Fitur 10: Logout

```
PROGRAM Fitur_Logout

DEKLARASI
    confirmed   : BOOLEAN

BEGIN
    OUTPUT "Keluar dari Akun?"
    OUTPUT "Kamu akan kembali ke halaman login."
    OUTPUT Tombol: [Batal] [Ya, Keluar]

    INPUT confirmed

    IF confirmed = FALSE THEN
        TUTUP dialog
        RETURN
    END IF

    TUTUP Halaman Utama
    BUKA Halaman Login
END
```
