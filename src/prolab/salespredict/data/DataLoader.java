package prolab.salespredict.data;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import prolab.salespredict.model.UserRecord;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Veri yükleme sorumluluğunu üstlenen sınıf.
 * Hem .xlsx (Apache POI) hem .csv formatlarını destekler.
 * Hatalar yutulamaz; Exception olarak çağırana iletilir.
 */
public class DataLoader {

    /**
     * Dosyayı okur ve UserRecord listesi döndürür.
     * @param filePath Dosya yolu (.xlsx veya .csv)
     * @return Okunan kayıtların listesi
     * @throws DataLoadException Dosya okunamadığında veya format hatalı olduğunda
     */
    public List<UserRecord> loadData(String filePath) throws DataLoadException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new DataLoadException("Dosya yolu boş olamaz!");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new DataLoadException("Dosya bulunamadı: " + filePath);
        }

        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xlsx")) {
            return loadFromExcel(file);
        } else if (lowerPath.endsWith(".csv")) {
            return loadFromCSV(file);
        } else {
            throw new DataLoadException(
                "Desteklenmeyen dosya formatı! Sadece .xlsx ve .csv kabul edilir.\n" +
                "Seçilen dosya: " + file.getName()
            );
        }
    }

    // ==================== EXCEL (.xlsx) OKUYUCU ====================
    private List<UserRecord> loadFromExcel(File file) throws DataLoadException {
        List<UserRecord> userList = new ArrayList<>();
        PreProcessor preProcessor = new PreProcessor();
        int skippedRows = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // İlk sayfayı al
            Iterator<Row> rowIterator = sheet.iterator();

            // Başlık satırını atla
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                try {
                    // Sütunları güvenli bir şekilde al (F=5, G=6, H=7, L=11, M=12)
                    
                    // test için yazdım
                    // System.out.println("Satır okunuyor...");
                    double amount = row.getCell(5).getNumericCellValue(); // F
                    double price = row.getCell(6).getNumericCellValue();  // G
                    double totalRaw = row.getCell(7).getNumericCellValue(); // H

                    String brand = row.getCell(11).getStringCellValue();   // L
                    String category = row.getCell(12).getStringCellValue(); // M

                    // null veya boş gelirse patlamaması için atlıyoruz
                    if (category == null || category.isEmpty()) {
                        skippedRows++;
                        
                        //System.out.println("kategori boş geçti");
                        continue;
                    }

                    // Yönergedeki 3.3 Min-Max olayı
                    double encodedBrand = Math.abs(brand.hashCode() % 100);
                    
                    double normalizedAge = preProcessor.normalize(price, 0, 50000);
                    double normalizedCity = preProcessor.normalize(encodedBrand, 0, 100);
                    double normalizedGender = preProcessor.normalize(amount, 0, 1000);
                    double normalizedSpending = preProcessor.normalize(totalRaw, 0, 1000000);

                    userList.add(new UserRecord(normalizedAge, normalizedCity, normalizedGender, normalizedSpending, category));
                } catch (Exception e) {
                    // Hatalı veya boş satırları atla, sayacı artır
                    skippedRows++;
                    continue;
                }
            }
        } catch (Exception e) {
            throw new DataLoadException(
                "Excel dosyası okunurken hata oluştu!\n" +
                "Dosya: " + file.getName() + "\n" +
                "Detay: " + e.getMessage()
            );
        }

        if (userList.isEmpty()) {
            throw new DataLoadException(
                "Dosya okundu ancak geçerli kayıt bulunamadı!\n" +
                "Atlanan satır sayısı: " + skippedRows + "\n" +
                "Dosyadaki sütun yapısını kontrol edin."
            );
        }

        System.out.println("[DataLoader] Excel yüklendi: " + userList.size() +
                " kayıt başarılı, " + skippedRows + " satır atlandı.");
        return userList;
    }

    // ==================== CSV OKUYUCU ====================
    private List<UserRecord> loadFromCSV(File file) throws DataLoadException {
        List<UserRecord> userList = new ArrayList<>();
        PreProcessor preProcessor = new PreProcessor();
        int skippedRows = 0;
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;

            // Başlık satırını atla
            if ((line = br.readLine()) != null) lineNumber++;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    // Virgül veya noktalı virgül ile ayır
                    String[] columns = line.contains(";") ? line.split(";") : line.split(",");

                    if (columns.length < 13) {
                        skippedRows++;
                        continue;
                    }

                    // Sütunları güvenli bir şekilde al (F=5, G=6, H=7, L=11, M=12)
                    double amount = parseDouble(columns[5].trim());
                    double price = parseDouble(columns[6].trim());
                    double totalRaw = parseDouble(columns[7].trim());

                    String brand = columns[11].trim();
                    String category = columns[12].trim();

                    if (category.isEmpty()) {
                        skippedRows++;
                        continue;
                    }

                    // Normalizasyon ve Encoding işlemleri (Yönerge Madde 3.3 Min-Max)
                    double encodedBrand = Math.abs(brand.hashCode() % 100);
                    
                    double normalizedAge = preProcessor.normalize(price, 0, 50000);
                    double normalizedCity = preProcessor.normalize(encodedBrand, 0, 100);
                    double normalizedGender = preProcessor.normalize(amount, 0, 1000);
                    double normalizedSpending = preProcessor.normalize(totalRaw, 0, 1000000);

                    userList.add(new UserRecord(normalizedAge, normalizedCity, normalizedGender, normalizedSpending, category));
                } catch (Exception e) {
                    skippedRows++;
                    continue;
                }
            }
        } catch (IOException e) {
            throw new DataLoadException(
                "CSV dosyası okunurken hata oluştu!\n" +
                "Dosya: " + file.getName() + "\n" +
                "Detay: " + e.getMessage()
            );
        }

        if (userList.isEmpty()) {
            throw new DataLoadException(
                "CSV dosyası okundu ancak geçerli kayıt bulunamadı!\n" +
                "Toplam satır: " + lineNumber + ", Atlanan: " + skippedRows + "\n" +
                "CSV ayracını (virgül/noktalı virgül) ve sütun yapısını kontrol edin."
            );
        }

        System.out.println("[DataLoader] CSV yüklendi: " + userList.size() +
                " kayıt başarılı, " + skippedRows + " satır atlandı.");
        return userList;
    }

    /**
     * String → double dönüşümü. Türkçe ondalık ayırıcısını (virgül) da destekler.
     */
    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        // Türkçe ondalık ayırıcısı: virgül → nokta
        return Double.parseDouble(value.replace(",", "."));
    }

    // ==================== ÖZEL HATA SINIFI ====================
    /**
     * Veri yükleme hatalarını temsil eden özel checked exception.
     * Bu sayede hatalar System.err'ye düşmek yerine çağırana (GUI'ye) iletilir.
     */
    public static class DataLoadException extends Exception {
        public DataLoadException(String message) {
            super(message);
        }

        public DataLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}