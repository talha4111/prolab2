package prolab.salespredict.core;

import prolab.salespredict.model.UserRecord;
import java.util.List;

public abstract class BaseAlgorithm implements IClassifier {
    
    /**
     * Test veri seti üzerinden algoritmanın doğruluk (accuracy) oranını hesaplar.
     */
    public double calculateAccuracy(List<UserRecord> testData) {
        int correctPredictions = 0;
        
        try {
            for (UserRecord record : testData) {
                String predictedCategory = predict(record);
                if (predictedCategory.equals(record.getCategory())) {
                    correctPredictions++;
                }
            }
            if (testData.isEmpty()) return 0.0;
            return ((double) correctPredictions / testData.size()) * 100.0;
            
        } catch (Exception e) {
            System.err.println("Doğruluk oranı hesaplanırken hata: " + e.getMessage());
            return 0.0;
        }
    }
}