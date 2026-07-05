@echo off
REM ═══════════════════════════════════════════════════════════════
REM  build-exe.bat — Build TaskU menjadi aplikasi desktop (.exe)
REM ═══════════════════════════════════════════════════════════════
REM
REM  Cara pakai: Klik 2x file ini, atau jalankan dari terminal.
REM  Hasil akhir: folder "dist\TaskU" berisi TaskU.exe
REM
REM  Prasyarat: JDK 14+ dengan jpackage (sudah termasuk default)
REM ═══════════════════════════════════════════════════════════════

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo  ╔═══════════════════════════════════════╗
echo  ║   TaskU Desktop App Builder           ║
echo  ╚═══════════════════════════════════════╝
echo.

REM ── Step 0: Bersihkan build sebelumnya ─────────────────────────
echo [1/4] Membersihkan build sebelumnya...
if exist build rmdir /s /q build
if exist dist  rmdir /s /q dist
mkdir build\classes
mkdir build\jar
echo       Done.
echo.

REM ── Step 1: Compile semua source ───────────────────────────────
echo [2/4] Compile source code...
javac -d build\classes ^
  -cp "lib\sqlite-jdbc-3.45.3.0.jar;lib\slf4j-api-2.0.12.jar;lib\slf4j-nop-2.0.12.jar;lib\jbcrypt-0.4.jar" ^
  src\util\*.java ^
  src\model\*.java ^
  src\database\*.java ^
  src\service\*.java ^
  src\ui\components\*.java ^
  src\ui\*.java ^
  src\Main.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo  [ERROR] Kompilasi gagal! Periksa error di atas.
    pause
    exit /b 1
)

REM Copy resources (icon, dll) ke folder classes
if exist src\resources (
    xcopy /s /i /q src\resources build\classes\resources >nul
)

REM Copy fonts ke dalam JAR agar Plus Jakarta Sans dimuat dengan benar di EXE
if exist src\fonts (
    xcopy /s /i /q src\fonts build\classes\fonts >nul
)

echo       Done. (0 errors)
echo.

REM ── Step 2: Buat Fat JAR (semua library digabung) ──────────────
echo [3/4] Membuat Fat JAR...

REM Ekstrak library JAR ke build\classes
pushd build\classes
jar xf ..\..\lib\sqlite-jdbc-3.45.3.0.jar
jar xf ..\..\lib\slf4j-api-2.0.12.jar
jar xf ..\..\lib\slf4j-nop-2.0.12.jar
jar xf ..\..\lib\jbcrypt-0.4.jar
REM Hapus file META-INF dari library agar tidak konflik
if exist META-INF rmdir /s /q META-INF
popd

REM Buat manifest
echo Main-Class: Main> build\MANIFEST.MF

REM Buat JAR
jar cfm build\jar\TaskU.jar build\MANIFEST.MF -C build\classes .

if %ERRORLEVEL% neq 0 (
    echo.
    echo  [ERROR] Pembuatan JAR gagal!
    pause
    exit /b 1
)
echo       Done. (build\jar\TaskU.jar)
echo.

REM ── Step 3: jpackage → Desktop App ────────────────────────────
echo [4/4] Membangun aplikasi desktop dengan jpackage...
echo       (Ini memakan waktu 1-3 menit, harap tunggu...)
echo.

jpackage ^
  --type app-image ^
  --name TaskU ^
  --input build\jar ^
  --main-jar TaskU.jar ^
  --main-class Main ^
  --dest dist ^
  --app-version 1.0.0 ^
  --icon assets\app-icon.ico ^
  --vendor "TaskU Team" ^
  --description "TaskU - Aplikasi Manajemen Tugas Mahasiswa" ^
  --java-options "--enable-preview" ^
  --java-options "-Dfile.encoding=UTF-8"

if %ERRORLEVEL% neq 0 (
    echo.
    echo  [ERROR] jpackage gagal! Cek error di atas.
    pause
    exit /b 1
)

echo.
echo  ╔═══════════════════════════════════════════════════════╗
echo  ║  BUILD BERHASIL!                                     ║
echo  ║                                                      ║
echo  ║  Aplikasi berada di: dist\TaskU\TaskU.exe             ║
echo  ║                                                      ║
echo  ║  Kamu bisa:                                          ║
echo  ║  1. Jalankan langsung: dist\TaskU\TaskU.exe           ║
echo  ║  2. ZIP folder dist\TaskU\ lalu kirim ke teman       ║
echo  ║  3. Buat shortcut di Desktop                         ║
echo  ╚═══════════════════════════════════════════════════════╝
echo.

REM Buka folder hasil
explorer dist\TaskU

pause
