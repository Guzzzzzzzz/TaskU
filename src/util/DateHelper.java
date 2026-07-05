package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Utility untuk format tanggal bahasa Indonesia.
 */
public class DateHelper {

    private static Locale getLocale() {
        return TranslationManager.currentLanguage == TranslationManager.Language.ID 
            ? Locale.of("id", "ID") 
            : Locale.US;
    }

    /**
     * Format tanggal lengkap: "SENIN, 13 APRIL 2026"
     */
    public static String formatFull(LocalDate date) {
        if (date == null) return "-";
        String hari = date.getDayOfWeek().getDisplayName(TextStyle.FULL, getLocale()).toUpperCase();
        String bulan = date.getMonth().getDisplayName(TextStyle.FULL, getLocale()).toUpperCase();
        return hari + ", " + date.getDayOfMonth() + " " + bulan + " " + date.getYear();
    }

    /**
     * Format tanggal pendek: "14 Apr"
     */
    public static String formatShort(LocalDate date) {
        if (date == null) return "-";
        String bulan = date.getMonth().getDisplayName(TextStyle.SHORT, getLocale());
        // Capitalize first letter
        bulan = bulan.substring(0, 1).toUpperCase() + bulan.substring(1);
        return date.getDayOfMonth() + " " + bulan;
    }

    /**
     * Format untuk input: "yyyy-MM-dd"
     */
    public static String formatISO(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Parse string ISO ke LocalDate
     */
    public static LocalDate parseISO(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    // Private constructor
    private DateHelper() {}
}
