package prolab.salespredict.core;

import prolab.salespredict.model.UserRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionTreeClassifier extends BaseAlgorithm {

    private int maxDepth; // Aşırı öğrenmeyi (Overfitting) engellemek için maksimum derinlik
    private Node root;    // Ağacın başlangıç (kök) düğümü

    public DecisionTreeClassifier(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public void train(List<UserRecord> trainingData) {
        // Ağacı kökten başlayarak recursive (özyinelemeli) şekilde inşa et
        this.root = buildTree(trainingData, 0);
    }

    @Override
    public String predict(UserRecord user) {
        if (root == null) {
            throw new IllegalStateException("Model henüz eğitilmedi!");
        }
        return traverseTree(user, root);
    }

    /**
     * Ağacı oluşturmak için kullanılan özyinelemeli (recursive) metot.
     */
    private Node buildTree(List<UserRecord> data, int currentDepth) {
        // Veri boşsa veya maksimum derinliğe ulaşıldıysa yaprak düğüm (leaf node) oluştur
        if (data.isEmpty()) return null;
        
        String majorityCategory = getMajorityCategory(data);

        // Eğer tüm veriler aynı kategorideyse veya max derinliğe ulaştıysak dallanmayı durdur
        if (currentDepth >= maxDepth || isPure(data)) {
            return new Node(majorityCategory); // Yaprak düğüm
        }

        // Veriyi en iyi bölen özelliği ve eşik değerini (threshold) bul
        SplitResult bestSplit = findBestSplit(data);

        // Eğer veriyi bölecek anlamlı bir kural bulunamadıysa yaprak düğüm yap
        if (bestSplit == null || bestSplit.leftData.isEmpty() || bestSplit.rightData.isEmpty()) {
            return new Node(majorityCategory);
        }

        // Sol ve sağ dalları özyinelemeli olarak oluştur
        Node leftChild = buildTree(bestSplit.leftData, currentDepth + 1);
        Node rightChild = buildTree(bestSplit.rightData, currentDepth + 1);

        // Karar düğümünü (dalı) döndür
        return new Node(bestSplit.featureIndex, bestSplit.threshold, leftChild, rightChild);
    }

    /**
     * Yeni bir kullanıcının özelliklerini ağaçtaki kurallara göre test ederek yaprak düğüme ulaşır.
     */
    private String traverseTree(UserRecord user, Node node) {
        if (node.isLeaf()) {
            return node.category; // Yaprak düğüme ulaştıysak kategoriyi döndür
        }

        double featureValue = getFeatureValue(user, node.featureIndex);
        if (featureValue <= node.threshold) {
            return traverseTree(user, node.left);
        } else {
            return traverseTree(user, node.right);
        }
    }

    // --- YARDIMCI METOTLAR VE İÇ SINIFLAR ---

    /**
     * Veriyi en iyi ayıran özelliği Gini Impurity (Safsızlık) mantığına göre bulur.
     */
    private SplitResult findBestSplit(List<UserRecord> data) {
        SplitResult bestSplit = null;
        double bestGini = Double.MAX_VALUE;

        // 4 farklı özellik var: 0: Yaş, 1: Şehir, 2: Cinsiyet, 3: Harcama Skoru
        for (int featureIndex = 0; featureIndex < 4; featureIndex++) {
            for (UserRecord record : data) {
                double threshold = getFeatureValue(record, featureIndex);
                
                List<UserRecord> left = new ArrayList<>();
                List<UserRecord> right = new ArrayList<>();

                for (UserRecord r : data) {
                    if (getFeatureValue(r, featureIndex) <= threshold) left.add(r);
                    else right.add(r);
                }

                if (left.isEmpty() || right.isEmpty()) continue;

                // Bölünmenin kalitesini ölç
                double gini = calculateGini(left, right);
                if (gini < bestGini) {
                    bestGini = gini;
                    bestSplit = new SplitResult(featureIndex, threshold, left, right);
                }
            }
        }
        return bestSplit;
    }

    private double calculateGini(List<UserRecord> left, List<UserRecord> right) {
        // Basitleştirilmiş bir hata hesaplama fonksiyonu (Gini safsızlığı yaklaşımı)
        int total = left.size() + right.size();
        return ((double) left.size() / total) * getGiniForGroup(left) +
               ((double) right.size() / total) * getGiniForGroup(right);
    }

    private double getGiniForGroup(List<UserRecord> group) {
        Map<String, Integer> counts = new HashMap<>();
        for (UserRecord r : group) {
            counts.put(r.getCategory(), counts.getOrDefault(r.getCategory(), 0) + 1);
        }
        double impurity = 1.0;
        for (int count : counts.values()) {
            double prob = (double) count / group.size();
            impurity -= prob * prob;
        }
        return impurity;
    }

    private boolean isPure(List<UserRecord> data) {
        String firstCategory = data.get(0).getCategory();
        for (UserRecord record : data) {
            if (!record.getCategory().equals(firstCategory)) return false;
        }
        return true;
    }

    private String getMajorityCategory(List<UserRecord> data) {
        Map<String, Integer> counts = new HashMap<>();
        String majority = null;
        int max = 0;
        for (UserRecord r : data) {
            int count = counts.getOrDefault(r.getCategory(), 0) + 1;
            counts.put(r.getCategory(), count);
            if (count > max) {
                max = count;
                majority = r.getCategory();
            }
        }
        return majority;
    }

    private double getFeatureValue(UserRecord record, int featureIndex) {
        switch (featureIndex) {
            case 0: return record.getAge();
            case 1: return record.getCity();
            case 2: return record.getGender();
            case 3: return record.getSpendingScore();
            default: return 0.0;
        }
    }

    /**
     * Ağacın Düğüm (Node) Yapısı 
     */
    private static class Node {
        int featureIndex; // Hangi özelliğe göre bölündü (Örn: 0 ise Yaş)
        double threshold; // Bölünme eşik değeri (Örn: Yaş < 25)
        Node left;        // Eşik değerinden küçük olanlar (Sol dal)
        Node right;       // Eşik değerinden büyük olanlar (Sağ dal)
        String category;  // Eğer bu bir yaprak düğümse tahmin edilecek kategori

        // Karar (Dal) Düğümü Constructor'ı
        public Node(int featureIndex, double threshold, Node left, Node right) {
            this.featureIndex = featureIndex;
            this.threshold = threshold;
            this.left = left;
            this.right = right;
        }

        // Yaprak Düğüm (Leaf) Constructor'ı
        public Node(String category) {
            this.category = category;
        }

        public boolean isLeaf() {
            return category != null;
        }
    }

    /**
     * Bölünme sonucunu tutmak için geçici veri sınıfı
     */
    private static class SplitResult {
        int featureIndex;
        double threshold;
        List<UserRecord> leftData;
        List<UserRecord> rightData;

        public SplitResult(int featureIndex, double threshold, List<UserRecord> leftData, List<UserRecord> rightData) {
            this.featureIndex = featureIndex;
            this.threshold = threshold;
            this.leftData = leftData;
            this.rightData = rightData;
        }
    }
}