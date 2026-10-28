package prolab.salespredict.data;

/**
 * Veri seti üzerinde sayısal dönüştürme ve normalizasyon işlemlerini yürüten sınıftır.
 */
public class PreProcessor {

    /**
     * Min-Max Normalizasyonu: Veriyi [0, 1] arasına çeker. [cite: 85-87]
     * Formül: (değer - min) / (max - min)
     */
    public double normalize(double value, double min, double max) {
        if (max == min) {
            return 0.0; // Payda sıfır olamaz, tüm değerler aynıysa 0 döndür.
        }
        return (value - min) / (max - min);
    }

    /**
     * Cinsiyet verisini sayısal değere dönüştürür (Label Encoding). [cite: 83]
     * Female -> 0.0, Male -> 1.0
     */
    public double encodeGender(String gender) {
        if (gender == null) return 0.5; // Eksik veri kontrolü
        
        String cleanGender = gender.trim().toLowerCase();
        if (cleanGender.equals("female") || cleanGender.equals("kadın")) {
            return 0.0;
        } else if (cleanGender.equals("male") || cleanGender.equals("erkek")) {
            return 1.0;
        }
        return 0.5; // Tanımlanamayan değerler için nötr değer.
    }

    /**
     * Şehir veya Marka isimlerini sayısal ID'lere dönüştürür. [cite: 84, 97]
     * Makineler sadece sayılarla matematiksel işlem yapabilir. [cite: 81]
     */
    public double encodeLabel(String label) {
        if (label == null || label.isEmpty()) return 0.0;
        
        // Basit bir Label Encoding mantığı: Metnin hash kodunu kullanır.
        // % 100 yaparak değer aralığını kontrol altında tutuyoruz.
        return Math.abs(label.trim().hashCode() % 100);
    }
}