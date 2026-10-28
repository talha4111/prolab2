package prolab.salespredict.core;

import prolab.salespredict.model.UserRecord;
import java.util.List;

public interface IClassifier {
    // Algoritmanın öğrenme aşaması
    void train(List<UserRecord> trainingData);
    
    // Tek bir kullanıcı için kategori tahmini
    String predict(UserRecord user);
}