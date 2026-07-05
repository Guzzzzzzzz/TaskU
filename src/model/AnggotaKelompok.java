package model;

/**
 * Merepresentasikan anggota dalam tugas kelompok.
 */
public class AnggotaKelompok {

    private int id;
    private int taskId;
    private String namaAnggota;
    private String peran;

    // --- Constructor ---

    public AnggotaKelompok(int id, int taskId, String namaAnggota, String peran) {
        this.id = id;
        this.taskId = taskId;
        this.namaAnggota = namaAnggota;
        this.peran = peran;
    }

    public AnggotaKelompok(int taskId, String namaAnggota, String peran) {
        this.taskId = taskId;
        this.namaAnggota = namaAnggota;
        this.peran = peran;
    }

    public AnggotaKelompok(String namaAnggota, String peran) {
        this.namaAnggota = namaAnggota;
        this.peran = peran;
    }

    // --- Getter & Setter ---

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getTaskId()                      { return taskId; }
    public void setTaskId(int taskId)           { this.taskId = taskId; }

    public String getNamaAnggota()              { return namaAnggota; }
    public void setNamaAnggota(String nama)     { this.namaAnggota = nama; }

    public String getPeran()                    { return peran; }
    public void setPeran(String peran)          { this.peran = peran; }

    @Override
    public String toString() {
        return namaAnggota + (peran.isEmpty() ? "" : " (" + peran + ")");
    }
}
