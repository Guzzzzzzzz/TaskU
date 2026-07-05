# TaskU — Initial Release (v1.0.0)

TaskU adalah aplikasi desktop To-Do List modern berbasis Java yang dirancang untuk membantu mahasiswa mengelola tugas, deadline, dan tugas kelompok secara produktif dan aman.

## Fitur Utama

- **Task & Course Management**: Atur tugas berdasarkan mata kuliah dan filter dengan mudah.
- **Smart Calendar & Deadline**: Pengingat tenggat waktu yang terintegrasi dan visualisasi prioritas.
- **Modern Adaptive UI**: Tampilan minimalis, responsif, dan elegan menggunakan bahasa desain *Pastel Squircle*.
- **Dark & Light Mode**: Transisi mode gelap dan terang yang mulus untuk kenyamanan mata.

## Keamanan & Keandalan

- **BCrypt Password Hashing**: Mengamankan kata sandi pengguna dengan algoritma hashing standard industri (BCrypt).
- **Secure Authentication Flow**: Logika login, registrasi, dan reset password aman yang mencegah kebocoran data.
- **Local SQLite Database**: Penyimpanan data lokal yang cepat, portabel, tanpa memerlukan konfigurasi database server yang rumit.

## Cara Instalasi & Menjalankan Aplikasi

### Menggunakan Executable (.exe)
1. Unduh berkas `TaskU-Windows.zip` dari bagian **Releases** di repositori GitHub ini.
2. Ekstrak berkas `.zip` tersebut.
3. Jalankan `TaskU.exe`.
4. Buat akun baru pada halaman Register, lalu masuk untuk mulai mengelola tugas Anda.

### Menjalankan dari Source Code (Development)
Jika Anda ingin menjalankan aplikasi dari kode sumber:
1. Pastikan Anda memiliki JDK 17 atau yang lebih baru terinstal.
2. Jalankan skrip `run.bat` untuk mengompilasi dan membuka aplikasi secara langsung.
3. Gunakan skrip `build-exe.bat` jika ingin memaketkan ulang aplikasi menjadi berkas desktop `.exe`.
