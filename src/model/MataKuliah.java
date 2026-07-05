package model;

import java.awt.Color;

/**
 * Merepresentasikan kategori mata kuliah untuk setiap task.
 * Setiap mata kuliah memiliki warna unik sebagai badge di GUI.
 */
public class MataKuliah {

    private int id;
    private String nama;
    private String warna;   // Kode warna Hex (contoh: "#2D5F2D")

    // --- Constructor ---

    public MataKuliah(int id, String nama, String warna) {
        this.id    = id;
        this.nama  = nama;
        this.warna = warna;
    }

    /** Constructor tanpa ID — untuk insert data baru. */
    public MataKuliah(String nama, String warna) {
        this.nama  = nama;
        this.warna = warna;
    }

    // --- Getter & Setter ---

    public int getId()            { return id; }
    public void setId(int id)     { this.id = id; }

    public String getNama()               { return nama; }
    public void setNama(String nama)      { this.nama = nama; }

    public String getWarna()              { return warna; }
    public void setWarna(String warna)    { this.warna = warna; }

    // --- Utility ---

    /** Konversi kode Hex ke java.awt.Color untuk rendering badge. */
    public Color getWarnaColor() {
        try {
            return Color.decode(this.warna);
        } catch (NumberFormatException e) {
            return Color.GRAY;   // Fallback jika kode hex tidak valid
        }
    }

    /** Diperlukan agar JComboBox menampilkan nama, bukan alamat memori. */
    @Override
    public String toString() {
        return nama;
    }
}
