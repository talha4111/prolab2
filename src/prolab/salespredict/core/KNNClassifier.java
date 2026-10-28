package prolab.salespredict.core;

import prolab.salespredict.model.UserRecord;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class KNNClassifier extends BaseAlgorithm {
    
    private int k; // Bakılacak komşu sayısı (Örn: 5)
    private List<UserRecord> trainingData; // Eğitilmiş (kaydedilmiş) geçmiş veriler

    public KNNClassifier(int k) {
        this.k = k;
    }

    @Override
    public void train(List<UserRecord> trainingData) {
        // KNN "Tembel Öğrenen" (Lazy Learner) bir algoritmadır. 
        // Gerçek bir matematiksel model kurmaz, sadece veriyi hafızaya alır.
        this.trainingData = trainingData;
    }

    @Override
    public String predict(UserRecord user) {
        try {
            if (trainingData == null || trainingData.isEmpty()) {
                throw new IllegalStateException("Eğitim verisi bulunamadı! Önce train() metodunu çağırın.");
            }

            // Mesafeleri küçükten büyüğe sıralayacak Öncelikli Kuyruk (PriorityQueue)
            PriorityQueue<Neighbor> pq = new PriorityQueue<>(Comparator.comparingDouble(Neighbor::getDistance));

            // Yeni kullanıcının, geçmişteki TÜM kullanıcılarla arasındaki mesafeyi hesapla
            for (UserRecord trainRecord : trainingData) {
                double distance = calculateEuclideanDistance(user, trainRecord);
                pq.offer(new Neighbor(trainRecord, distance));
            }

            // En yakın K adet komşunun kategorilerini saymak için bir sözlük (Map)
            Map<String, Integer> categoryCounts = new HashMap<>();
            for (int i = 0; i < k && !pq.isEmpty(); i++) {
                Neighbor nearest = pq.poll(); // En yakın komşuyu kuyruktan çıkar
                String category = nearest.getRecord().getCategory();
                // Kategori daha önce eklendiyse sayısını 1 artır, eklenmediyse 1 yap
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            }

            // En çok tekrar eden (oy alan) kategoriyi bul
            String bestCategory = "Unknown";
            int maxCount = -1;
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    bestCategory = entry.getKey();
                }
            }

            return bestCategory;

        } catch (Exception e) {
            System.err.println("KNN Tahmin hatası: " + e.getMessage());
            return "Hata";
        }
    }

    /**
     * İki kullanıcı arasındaki Öklid mesafesini hesaplar.
     */
    private double calculateEuclideanDistance(UserRecord u1, UserRecord u2) {
        double sum = 0;
        try {
            sum += Math.pow(u1.getAge() - u2.getAge(), 2);
            sum += Math.pow(u1.getCity() - u2.getCity(), 2);
            sum += Math.pow(u1.getGender() - u2.getGender(), 2);
            sum += Math.pow(u1.getSpendingScore() - u2.getSpendingScore(), 2);
        } catch (Exception e) {
            System.err.println("Mesafe hesaplama hatası: " + e.getMessage());
        }
        return Math.sqrt(sum);
    }

    /**
     * Kuyrukta sıralama yapabilmek için geçici bir iç sınıf (Inner Class).
     * Sadece mesafeyi ve o mesafeye sahip kullanıcıyı tutar.
     */
    private static class Neighbor {
        private UserRecord record;
        private double distance;

        public Neighbor(UserRecord record, double distance) {
            this.record = record;
            this.distance = distance;
        }

        public UserRecord getRecord() { return record; }
        public double getDistance() { return distance; }
    }
}