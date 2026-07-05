package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility untuk hashing dan verifikasi password menggunakan BCrypt.
 * BCrypt secara otomatis meng-generate salt unik per password dan
 * menyimpannya bersama hash, sehingga tidak perlu kolom salt terpisah.
 */
public class PasswordUtil {

    /** Cost factor — semakin tinggi semakin aman tapi lebih lambat. 12 = standar industri. */
    private static final int BCRYPT_ROUNDS = 12;

    /**
     * Hash password plain-text menjadi BCrypt hash string.
     * @param plainPassword password asli dari user
     * @return hash string (60 karakter) yang aman untuk disimpan di database
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifikasi apakah plain password cocok dengan hash yang tersimpan.
     * @param plainPassword password yang diketik user saat login
     * @param hashedPassword hash yang tersimpan di database
     * @return true jika cocok
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Hash format tidak valid (misalnya masih plain-text dari migrasi lama)
            // Fallback: bandingkan langsung sebagai plain-text
            return plainPassword.equals(hashedPassword);
        }
    }

    /**
     * Cek apakah string sudah berupa BCrypt hash.
     * BCrypt hash selalu dimulai dengan "$2a$", "$2b$", atau "$2y$" dan panjangnya 60 karakter.
     */
    public static boolean isBCryptHash(String password) {
        return password != null
            && password.length() == 60
            && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}
