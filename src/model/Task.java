package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Merepresentasikan satu entitas tugas mahasiswa.
 */
public class Task {

    public enum Prioritas {
        TINGGI, SEDANG, RENDAH
    }

    public enum Status {
        BELUM, SEDANG, SELESAI
    }

    public enum Jenis {
        INDIVIDU, KELOMPOK
    }

    // --- Atribut ---

    private int id;
    private String judul;
    private String deskripsi;
    private LocalDate deadline;
    private Prioritas prioritas;
    private Status status;
    private Jenis jenis;
    private MataKuliah mataKuliah;
    private Mahasiswa mahasiswa;
    private LocalDateTime createdAt;
    private List<AnggotaKelompok> anggotaList = new ArrayList<>();

    // --- Constructor ---

    /** Digunakan saat membaca data dari database (semua field terisi). */
    public Task(int id, String judul, String deskripsi, LocalDate deadline,
                Prioritas prioritas, Status status, Jenis jenis,
                MataKuliah mataKuliah, Mahasiswa mahasiswa, LocalDateTime createdAt) {
        this.id         = id;
        this.judul      = judul;
        this.deskripsi  = deskripsi;
        this.deadline   = deadline;
        this.prioritas  = prioritas;
        this.status     = status;
        this.jenis      = jenis;
        this.mataKuliah = mataKuliah;
        this.mahasiswa  = mahasiswa;
        this.createdAt  = createdAt;
    }

    /** Digunakan saat menambah task baru (ID auto-generated oleh database). */
    public Task(String judul, String deskripsi, LocalDate deadline,
                Prioritas prioritas, Status status, Jenis jenis,
                MataKuliah mataKuliah, Mahasiswa mahasiswa) {
        this.judul      = judul;
        this.deskripsi  = deskripsi;
        this.deadline   = deadline;
        this.prioritas  = prioritas;
        this.status     = status;
        this.jenis      = jenis;
        this.mataKuliah = mataKuliah;
        this.mahasiswa  = mahasiswa;
    }

    // --- Getter & Setter ---

    public int getId()            { return id; }
    public void setId(int id)     { this.id = id; }

    public String getJudul()                  { return judul; }
    public void setJudul(String judul)        { this.judul = judul; }

    public String getDeskripsi()              { return deskripsi; }
    public void setDeskripsi(String deskripsi){ this.deskripsi = deskripsi; }

    public LocalDate getDeadline()                    { return deadline; }
    public void setDeadline(LocalDate deadline)       { this.deadline = deadline; }

    public Prioritas getPrioritas()                   { return prioritas; }
    public void setPrioritas(Prioritas prioritas)     { this.prioritas = prioritas; }

    public Status getStatus()                         { return status; }
    public void setStatus(Status status)              { this.status = status; }

    public Jenis getJenis()                           { return jenis; }
    public void setJenis(Jenis jenis)                 { this.jenis = jenis; }

    public MataKuliah getMataKuliah()                        { return mataKuliah; }
    public void setMataKuliah(MataKuliah mataKuliah)        { this.mataKuliah = mataKuliah; }

    public Mahasiswa getMahasiswa()                         { return mahasiswa; }
    public void setMahasiswa(Mahasiswa mahasiswa)           { this.mahasiswa = mahasiswa; }

    public LocalDateTime getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)       { this.createdAt = createdAt; }

    public List<AnggotaKelompok> getAnggotaList()                          { return anggotaList; }
    public void setAnggotaList(List<AnggotaKelompok> anggotaList)          { this.anggotaList = anggotaList; }
    public void addAnggota(AnggotaKelompok anggota)                        { this.anggotaList.add(anggota); }

    // --- Business Methods ---

    /** @return jumlah hari tersisa sebelum deadline, atau -1 jika tidak ada deadline */
    public long getDaysRemaining() {
        if (deadline == null) return -1;
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    /** Tugas mendesak jika deadline <= 2 hari ke depan dan belum selesai. */
    public boolean isUrgent() {
        if (deadline == null || status == Status.SELESAI) return false;
        long daysLeft = getDaysRemaining();
        return daysLeft >= 0 && daysLeft <= 2;
    }

    /** Tugas overdue jika deadline sudah lewat dan belum selesai. */
    public boolean isOverdue() {
        if (deadline == null || status == Status.SELESAI) return false;
        return getDaysRemaining() < 0;
    }

    public boolean isCompleted() {
        return status == Status.SELESAI;
    }

    /** Label deadline user-friendly: "Besok", "Hari ini", "2 hari lagi", "Lewat 3 hari!" */
    public String getDeadlineLabel() {
        if (deadline == null) {
            return util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Tanpa deadline" : "No deadline";
        }
        
        long days = getDaysRemaining();
        
        if (days < 0) {
            return util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID 
                ? "Lewat " + Math.abs(days) + " hari!" 
                : "Overdue by " + Math.abs(days) + " days!";
        } else if (days == 0) {
            return util.TranslationManager.get("unit_hari_ini");
        } else if (days == 1) {
            return util.TranslationManager.get("unit_besok");
        } else {
            return days + " " + util.TranslationManager.get("unit_hari_lagi");
        }
    }

    @Override
    public String toString() {
        return "[" + status + "] " + judul
             + " | " + (mataKuliah != null ? mataKuliah.getNama() : (util.TranslationManager.currentLanguage == util.TranslationManager.Language.ID ? "Umum" : "General"))
             + " | Deadline: " + getDeadlineLabel();
    }
}
