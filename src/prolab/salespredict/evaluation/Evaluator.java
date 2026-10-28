package prolab.salespredict.evaluation;

import prolab.salespredict.core.IClassifier;
import prolab.salespredict.model.UserRecord;
import java.util.*;

public class Evaluator {

    // Kategori bazlı performans detaylarını tutan veri yapısı
    public static class CategoryStat {
        public String name;
        public int support; // Toplam o kategorideki örnek sayısı
        public int correct; // Doğru tahmin sayısı
        public int incorrect; // Yanlış tahmin sayısı
        
        public double getAccuracy() { 
            return support == 0 ? 0 : (double) correct / support * 100.0; 
        }
    }

    // Arayüze aktarılacak tüm analiz sonuçları
    public static class EvaluationResult {
        public String modelName;
        public double overallAccuracy;
        public long timeMs;
        public long memoryKb;
        public String reportText;
        public List<CategoryStat> categoryStats = new ArrayList<>();
    }

    public EvaluationResult evaluateModel(IClassifier model, List<UserRecord> trainingData, List<UserRecord> testData, String modelName) {
        EvaluationResult result = new EvaluationResult();
        result.modelName = modelName;
        StringBuilder report = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        
        // Bellek ölçümü öncesi çöp toplayıcıyı tetikle
        // hocalar genelde bellek ölçerken gc() istiyor diye ekledim
        runtime.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();


        long startTime = System.currentTimeMillis();
        model.train(trainingData); // Modeli eğit
        
        int correct = 0;
        
        // normal 2 boyutlu array yerine map ile tutmak daha esnek geldi
        Map<String, Map<String, Integer>> matrix = new HashMap<>();


        // Test verisini tahmin et ve matrisi doldur
        for (UserRecord record : testData) {
            String actual = record.getCategory();
            String predicted = model.predict(record);
            if (actual.equals(predicted)) correct++;

            matrix.putIfAbsent(actual, new HashMap<>());
            matrix.get(actual).put(predicted, matrix.get(actual).getOrDefault(predicted, 0) + 1);
        }
        
        long endTime = System.currentTimeMillis();
        long endMemory = runtime.totalMemory() - runtime.freeMemory();

        result.overallAccuracy = testData.isEmpty() ? 0 : (double) correct / testData.size() * 100;
        result.timeMs = endTime - startTime;
        result.memoryKb = (endMemory - startMemory) / 1024;
        
        report.append("=== ").append(modelName).append(" Analizi ===\n");
        report.append("Eğitim: ").append(trainingData.size()).append(" | Test: ").append(testData.size()).append("\n");
        report.append(String.format("Doğruluk: %%%.2f\n", result.overallAccuracy));
        report.append("Süre: ").append(result.timeMs).append(" ms\n");
        report.append("Bellek Kullanımı: ").append(result.memoryKb).append(" KB\n");
        report.append("\n--- Hata Matrisi ---\n");
        
        // Hata matrisi ve Kategori istatistiklerini oluştur
        for (Map.Entry<String, Map<String, Integer>> entry : matrix.entrySet()) {
            String real = entry.getKey();
            Map<String, Integer> predicts = entry.getValue();
            report.append(real).append(" -> ").append(predicts).append("\n");
            
            CategoryStat stat = new CategoryStat();
            stat.name = real;
            stat.correct = predicts.getOrDefault(real, 0);
            stat.support = predicts.values().stream().mapToInt(Integer::intValue).sum();
            stat.incorrect = stat.support - stat.correct;
            result.categoryStats.add(stat);
        }
        
        // Tablo için harf sırasına diz
        result.categoryStats.sort(Comparator.comparing(c -> c.name));
        
        result.reportText = report.toString();
        return result;
    }
}