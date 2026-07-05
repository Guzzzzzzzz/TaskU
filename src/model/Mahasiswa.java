package model;

/**
 * Merepresentasikan entitas mahasiswa pemilik task.
 */
public class Mahasiswa {

    private int id;
    private String nama;
    private String nim;
    private String password;

    // --- Constructor ---

    public Mahasiswa(int id, String nama, String nim) {
        this.id   = id;
        this.nama = nama;
        this.nim  = nim;
    }

    public Mahasiswa(int id, String nama, String nim, String password) {
        this.id       = id;
        this.nama     = nama;
        this.nim      = nim;
        this.password = password;
    }

    /** Constructor tanpa ID — untuk insert data baru. */
    public Mahasiswa(String nama, String nim) {
        this.nama = nama;
        this.nim  = nim;
    }

    // --- Getter & Setter ---

    public int getId()            { return id; }
    public void setId(int id)     { this.id = id; }

    public String getNama()               { return nama; }
    public void setNama(String nama)      { this.nama = nama; }

    public String getNim()                { return nim; }
    public void setNim(String nim)        { this.nim = nim; }

    public String getPassword()                   { return password; }
    public void setPassword(String password)      { this.password = password; }

    @Override
    public String toString() {
        return nama + " (" + nim + ")";
    }
}
