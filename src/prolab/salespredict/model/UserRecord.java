package prolab.salespredict.model;

public class UserRecord {
    // Tüm özellikler private olarak tanımlandı (Encapsulation)
    private double age; 
    private double city; 
    private double gender; 
    private double spendingScore;
    private String category; // Tahmin edilecek hedef değişken

    // Yapıcı Metot (Constructor)
    public UserRecord(double age, double city, double gender, double spendingScore, String category) {
        this.age = age;
        this.city = city;
        this.gender = gender;
        this.spendingScore = spendingScore;
        this.category = category;
    }

    // Sadece okuma izni veren public Getter metotları
    public double getAge() { return age; }
    public double getCity() { return city; }
    public double getGender() { return gender; }
    public double getSpendingScore() { return spendingScore; }
    public String getCategory() { return category; }
}