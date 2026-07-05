# 👤 Flowchart User — Aplikasi TaskU

> Flowchart ini menggambarkan alur aplikasi **dari sudut pandang pengguna (user)**.
> Tidak ada kode, SQL, atau nama method — hanya layar, aksi, dan hasil yang dilihat user.

---

## Perbedaan Flowchart Program vs Flowchart User

| | Flowchart Program (Sistem) | Flowchart User |
|---|---|---|
| **Sudut pandang** | Developer / Kode | Pengguna / User |
| **Isi** | SQL query, method, DAO, Service | Layar, tombol, form, pesan |
| **Contoh** | "DAO eksekusi INSERT ke database" | "Tugas baru muncul di daftar" |
| **Tujuan** | Memahami logika kode | Memahami alur penggunaan aplikasi |

---

## 1. Flowchart User — Alur Utama Keseluruhan

```mermaid
flowchart TD
    A([Buka Aplikasi TaskU]) --> B[/Halaman Login ditampilkan/]
    B --> C{Sudah punya akun?}

    C -- Belum --> D[Klik 'Daftar Sekarang']
    D --> E[/Halaman Registrasi ditampilkan/]
    E --> F[Isi Nama, NIM, dan Password]
    F --> G[Klik 'Daftar']
    G --> H{Registrasi berhasil?}
    H -- Tidak --> H2[/Tampil pesan error/]
    H2 --> E
    H -- Ya --> B

    C -- Sudah --> I[Masukkan Username dan Password]
    I --> J[Klik 'Masuk']
    J --> K{Login berhasil?}
    K -- Tidak --> K2[/Tampil pesan error/]
    K2 --> B
    K -- Ya --> L[/Halaman Utama ditampilkan/]

    L --> M{User memilih menu}
    M -- Tambah Tugas --> N[[Alur Tambah Tugas]]
    M -- Klik Tugas --> O[[Alur Lihat Detail]]
    M -- Centang Tugas --> P[[Alur Selesaikan Tugas]]
    M -- Hapus Tugas --> Q[[Alur Hapus Tugas]]
    M -- Filter / Cari --> R[[Alur Filter dan Pencarian]]
    M -- Profil --> S[[Alur Pengaturan Akun]]
    M -- Tutup --> T([Aplikasi Ditutup])

    N --> L
    O --> L
    P --> L
    Q --> L
    R --> L
    S --> L
```

---

## 2. Flowchart User — Login dan Registrasi

```mermaid
flowchart TD
    A([Buka Aplikasi]) --> B[/Halaman Login ditampilkan/]
    B --> C[Masukkan Username]
    C --> D[Masukkan Password]
    D --> E[Klik tombol 'Masuk']
    E --> F{Username dan Password terisi?}
    
    F -- Tidak --> G[/Tampil pesan: 'Username dan Password tidak boleh kosong!'/]
    G --> B
    
    F -- Ya --> H{Data cocok dengan database?}
    H -- Ya --> I[/Halaman Utama terbuka/]
    I --> J([Login Selesai])
    
    H -- Tidak --> K[/Tampil pesan: 'Username atau Password salah!'/]
    K --> L[Password otomatis dikosongkan]
    L --> B

    B --> M{Lupa Password?}
    M -- Ya --> N[Klik 'Lupa Password?']
    N --> O[/Popup muncul: Masukkan NIM/]
    O --> P[Masukkan NIM]
    P --> Q{NIM ditemukan?}
    Q -- Ya --> R[/Password ditampilkan/]
    Q -- Tidak --> S[/Tampil pesan: 'NIM tidak ditemukan'/]
    R --> B
    S --> O

    B --> T{Belum punya akun?}
    T -- Ya --> U[Klik 'Daftar Sekarang']
    U --> V[/Halaman Registrasi terbuka/]
    V --> W[Isi Nama Lengkap, NIM, Password]
    W --> X[Klik 'Daftar']
    X --> Y{NIM sudah terdaftar?}
    Y -- Ya --> Z[/Tampil pesan: 'NIM sudah terdaftar!'/]
    Z --> V
    Y -- Tidak --> AA[/Tampil pesan: 'Pendaftaran berhasil!'/]
    AA --> AB[Otomatis kembali ke Halaman Login]
    AB --> B
```

---

## 3. Flowchart User — Tambah Tugas Baru

```mermaid
flowchart TD
    A([User di Halaman Utama]) --> B[Klik bar '+ Tambah tugas baru...']
    B --> C[/Form Tambah Tugas terbuka/]
    
    C --> D[Ketik Judul Tugas]
    D --> E[Pilih Mata Kuliah dari dropdown]
    E --> E2{Mata kuliah belum ada?}
    E2 -- Ya --> E3[Klik tombol '+ Baru']
    E3 --> E4[/Popup tambah mata kuliah terbuka/]
    E4 --> E5[Ketik nama mata kuliah + pilih warna]
    E5 --> E6[Klik Simpan]
    E6 --> E7[Mata kuliah baru muncul di dropdown]
    E7 --> E
    E2 -- Tidak --> F

    F[Pilih Prioritas: Rendah / Sedang / Tinggi]
    F --> G[Pilih Tanggal Deadline]
    G --> G2{Pakai kalender?}
    G2 -- Ya --> G3[Klik ikon kalender]
    G3 --> G4[/Popup kalender terbuka/]
    G4 --> G5[Klik tanggal yang diinginkan]
    G5 --> H
    G2 -- Tidak --> G6[Ketik manual: DD-MM-YYYY]
    G6 --> H

    H[Pilih Jenis: Individu atau Kelompok]
    H --> I{Jenis Kelompok?}
    I -- Ya --> I2[Isi Nomor Kelompok]
    I2 --> I3[Ketik nama anggota kelompok]
    I3 --> I4{Tambah anggota lagi?}
    I4 -- Ya --> I5[Klik tombol '+ Tambah Anggota']
    I5 --> I3
    I4 -- Tidak --> J
    I -- Tidak --> J
    
    J[Klik tombol 'Simpan']
    J --> K{Semua data valid?}
    K -- Tidak --> L[/Tampil pesan peringatan/]
    L --> C
    K -- Ya --> M[/Form tertutup/]
    M --> N[/Tugas baru muncul di daftar/]
    N --> O[/Angka statistik di dashboard terupdate/]
    O --> P([Kembali ke Halaman Utama])
```

---

## 4. Flowchart User — Kelola Tugas (Detail, Selesai, Hapus)

```mermaid
flowchart TD
    A([User melihat daftar tugas]) --> B{Aksi apa yang dilakukan?}

    B -- Klik kartu tugas --> C[/Popup Detail Tugas terbuka/]
    C --> D[Lihat info: judul, matkul, deadline, prioritas, status, jenis]
    D --> D2{Tugas kelompok?}
    D2 -- Ya --> D3[Lihat daftar anggota kelompok]
    D3 --> D4
    D2 -- Tidak --> D4{Ingin edit deskripsi?}
    D4 -- Ya --> D5[Ketik/ubah deskripsi di area teks]
    D5 --> D6[Klik 'Simpan & Tutup']
    D6 --> D7[/Deskripsi tersimpan, popup tertutup/]
    D4 -- Tidak --> D8[Klik tombol X untuk tutup]
    D7 --> A
    D8 --> A

    B -- Klik lingkaran checkbox --> E{Status saat ini?}
    E -- Belum selesai --> F[/Checkbox berubah jadi centang hijau/]
    F --> F2[/Judul tugas dicoret/]
    F2 --> F3[/Tugas pindah ke bagian 'Selesai'/]
    F3 --> F4[/Angka statistik terupdate/]
    F4 --> A
    E -- Sudah selesai --> G[/Checkbox kembali jadi lingkaran kosong/]
    G --> G2[/Judul tugas normal kembali/]
    G2 --> G3[/Tugas pindah ke bagian 'Belum Selesai'/]
    G3 --> G4[/Angka statistik terupdate/]
    G4 --> A

    B -- Hover ke dot prioritas lalu klik X --> H[/Popup konfirmasi: 'Hapus Tugas?'/]
    H --> I{Pilih apa?}
    I -- Batal --> J[/Popup tertutup, tidak terjadi apa-apa/]
    J --> A
    I -- Ya, Hapus --> K[/Tugas hilang dari daftar/]
    K --> L[/Angka statistik terupdate/]
    L --> A
```

---

## 5. Flowchart User — Filter dan Pencarian

```mermaid
flowchart TD
    A([User di Halaman Utama]) --> B{Ingin menyaring tugas?}

    B -- Filter Status --> C{Pilih filter status}
    C -- Semua --> C1[Klik tombol 'Semua']
    C1 --> C2[/Semua tugas ditampilkan/]
    C -- Belum --> C3[Klik tombol 'Belum']
    C3 --> C4[/Hanya tugas belum selesai yang tampil/]
    C -- Selesai --> C5[Klik tombol 'Selesai']
    C5 --> C6[/Hanya tugas selesai yang tampil/]

    B -- Filter Prioritas --> D{Pilih filter prioritas}
    D -- Tinggi --> D1[Klik dot merah]
    D1 --> D2[/Hanya tugas prioritas tinggi yang tampil/]
    D -- Sedang --> D3[Klik dot kuning]
    D3 --> D4[/Hanya tugas prioritas sedang yang tampil/]
    D -- Rendah --> D5[Klik dot hijau]
    D5 --> D6[/Hanya tugas prioritas rendah yang tampil/]

    B -- Cari Tugas --> E[Klik kolom pencarian]
    E --> F[Ketik kata kunci]
    F --> G[Tekan Enter]
    G --> H[/Tugas yang judulnya cocok ditampilkan/]

    B -- Filter Mata Kuliah --> I[Klik nama mata kuliah di Sidebar]
    I --> J[/Hanya tugas dari mata kuliah tersebut yang tampil/]

    B -- Navigasi Sidebar --> K{Menu yang diklik}
    K -- Semua Tugas --> K1[/Semua tugas ditampilkan/]
    K -- Hari Ini --> K2[/Hanya tugas deadline hari ini yang tampil/]
    K -- Selesai --> K3[/Hanya tugas selesai yang tampil/]

    C2 --> L([Daftar tugas diperbarui])
    C4 --> L
    C6 --> L
    D2 --> L
    D4 --> L
    D6 --> L
    H --> L
    J --> L
    K1 --> L
    K2 --> L
    K3 --> L
```

---

## 6. Flowchart User — Pengaturan Akun (Profil)

```mermaid
flowchart TD
    A([User di Halaman Utama]) --> B[Klik area profil di bagian bawah Sidebar]
    B --> C[/Menu popup muncul: 'Ganti Password' dan 'Logout'/]
    C --> D{Pilih menu apa?}

    D -- Ganti Password --> E[/Form ganti password terbuka/]
    E --> F[Masukkan password lama]
    F --> G[Masukkan password baru]
    G --> H[Klik 'Simpan']
    H --> I{Password lama cocok?}
    I -- Tidak --> J[/Tampil pesan: 'Password lama salah!'/]
    J --> E
    I -- Ya --> K[/Tampil pesan: 'Password berhasil diubah!'/]
    K --> L([Kembali ke Halaman Utama])

    D -- Logout --> M[/Popup konfirmasi: 'Kamu akan kembali ke halaman login'/]
    M --> N{Pilih apa?}
    N -- Batal --> O([Kembali ke Halaman Utama])
    N -- Ya, Logout --> P[/Halaman Utama tertutup/]
    P --> Q[/Halaman Login terbuka/]
    Q --> R([Session berakhir])
```
