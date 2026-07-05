package util;

import java.util.HashMap;
import java.util.Map;

public class TranslationManager {
    public enum Language {
        ID, EN
    }

    public static Language currentLanguage = Language.ID; // Default to Indonesian

    private static final Map<String, Map<Language, String>> translations = new HashMap<>();

    static {
        // Sidebar / Menu
        add("menu_semua", "Semua Tugas", "All Tasks");
        add("menu_hari_ini", "Hari Ini", "Today");
        add("menu_selesai", "Selesai", "Completed");
        add("menu_matkul", "MATA KULIAH", "COURSES");
        add("btn_tambah", "Tambah", "Add");
        add("btn_ganti_password", "Ganti Password", "Change Password");
        add("btn_keluar", "Keluar", "Logout");
        add("btn_ganti_bahasa", "Bahasa: Indonesia", "Language: English");
        add("welcome_msg", "Selamat datang", "Welcome");
        
        // Stats Dashboard
        add("stat_total", "TOTAL TUGAS", "TOTAL TASKS");
        add("stat_semester", "Semester ini", "This semester");
        add("stat_selesai_header", "SELESAI", "COMPLETED");
        add("stat_dari_total", "dari total", "of total");
        add("stat_mendesak", "MENDESAK", "URGENT");
        add("stat_deadline_minggu", "Deadline minggu ini", "Deadline this week");
        add("stat_kategori", "KATEGORI", "CATEGORIES");
        add("stat_matkul", "Mata kuliah", "Courses");
        
        // Filter & Search
        add("filter_semua", "Semua", "All");
        add("filter_belum", "Belum", "Pending");
        add("filter_selesai", "Selesai", "Done");
        add("filter_tinggi", "Tinggi", "High");
        add("filter_sedang", "Sedang", "Medium");
        add("filter_rendah", "Rendah", "Low");
        add("placeholder_search", "search", "search");
        add("placeholder_tambah", "Tambah tugas baru...", "Add new task...");
        
        // Section Titles
        add("header_belum", "BELUM SELESAI", "PENDING");
        add("header_selesai", "SELESAI", "COMPLETED");
        add("header_tugas", "TUGAS", "TASKS");

        // Detail dialog & Add task dialog
        add("dialog_tambah_tugas", "Tambah Tugas Baru", "Add New Task");
        add("label_judul_tugas", "Judul Tugas", "Task Title");
        add("label_pilih_matkul", "Pilih Mata Kuliah", "Select Course");
        add("label_deadline", "Deadline", "Deadline");
        add("label_prioritas", "Prioritas", "Priority");
        add("label_jenis_tugas", "Jenis Tugas", "Task Type");
        add("jenis_individu", "Individu", "Individual");
        add("jenis_kelompok", "Kelompok", "Group");
        add("label_nomor_kelompok", "Nomor Kelompok", "Group Number");
        add("label_anggota_kelompok", "Anggota Kelompok", "Group Members");
        add("label_status", "Status", "Status");
        
        add("btn_simpan_tutup", "Simpan & Tutup", "Save & Close");
        add("label_deskripsi", "Deskripsi:", "Description:");
        
        // Priorities
        add("priority_high", "TINGGI", "HIGH");
        add("priority_medium", "SEDANG", "MEDIUM");
        add("priority_low", "RENDAH", "LOW");
        
        // Statuses
        add("status_belum", "BELUM", "PENDING");
        add("status_sedang", "SEDANG", "IN PROGRESS");
        add("status_selesai", "SELESAI", "COMPLETED");
        
        // Units / Labels
        add("unit_hari_lagi", "hari lagi", "days left");
        add("unit_lewat", "Lewat", "Overdue");
        add("unit_hari", "hari", "days");
        add("unit_hari_ini", "Hari ini", "Today");
        add("unit_besok", "Besok", "Tomorrow");
        add("unit_tidak_ada", "Tidak ada", "None");
    }

    private static void add(String key, String idVal, String enVal) {
        Map<Language, String> map = new HashMap<>();
        map.put(Language.ID, idVal);
        map.put(Language.EN, enVal);
        translations.put(key, map);
    }

    public static String get(String key) {
        Map<Language, String> map = translations.get(key);
        if (map != null) {
            String val = map.get(currentLanguage);
            if (val != null) return val;
        }
        return key;
    }
}
