## ProLab 2-2: Satış Tahmin Sistemi Teknik Analiz Raporu

  Bu rapor, Kocaeli Üniversitesi Bilgisayar Mühendisliği Bölümü Programlama Laboratuvarı II dersi
  kapsamında geliştirilen projenin kod mimarisini incelemektedir.

  ---

  1. Veri Modelleme: UserRecord.java
  Sistemin kalbi, verinin nasıl temsil edildiğidir. Bu sınıf, projenin Kapsülleme (Encapsulation)
  isterini doğrudan karşılar.

* Hangi İsteri Karşılıyor?: Veri Yönetimi ve Encapsulation (Yönerge Madde 2.1).
* Kod Bloğu Analizi:
  * Private Alanlar: age, city, gender, spendingScore ve category değişkenleri private tutularak
         verinin dışarıdan kontrolsüzce değiştirilmesi engellenmiştir.
  * Constructor (Yapıcı Metot): Veriler nesne oluşturulurken bir kez atanır, bu da verinin
         "immutability" (değişmezlik) prensibine yakın durmasını sağlar.
  * Public Getter Metotları: Algoritmalar bu verilere sadece okuma amaçlı erişebilir.

  ---

  1. Veri Girişi: DataLoader.java
  Verinin dış dünyadan (Excel/CSV) sisteme aktarıldığı ilk duraktır.

* Hangi İsteri Karşılıyor?: Veri Yönetimi ve Exception Handling (Yönerge Madde 2.5 ve 2.6).
* Kod Bloğu Analizi:
  * loadData Metodu: Polimorfik bir yaklaşımla dosya uzantısına bakarak loadFromExcel veya
         loadFromCSV metotlarını çağırır.
  * Apache POI Entegrasyonu: Excel dosyalarını okumak için kullanılan XSSFWorkbook yapısı,
         kurumsal Java uygulamalarındaki standart dosya işleme yeteneğini yansıtır.
  * Hata Yönetimi (DataLoadException): Projenin en güçlü yanlarından biri; boş satırları atlamak
         veya hatalı formatları kullanıcıya GUI üzerinden anlamlı mesajlarla iletmektir.
  * Akış Mantığı: Veriyi okuduğu anda PreProcessor nesnesini kullanarak normalizasyon işlemini
         başlatır.

  ---

  1. Veri Ön İşleme: PreProcessor.java
  Ham veriyi matematiksel bir modele dönüştüren "rafineri" merkezidir.

* Hangi İsteri Karşılıyor?: Veri Ön İşleme, Sayısal Dönüştürme ve Normalizasyon (Yönerge Madde 3).
* Kod Bloğu Analizi:
  * normalize (Min-Max): KNN gibi mesafe tabanlı algoritmaların en büyük düşmanı olan "ölçek
         farkını" ortadan kaldırır. Formül: (değer - min) / (max - min). Tüm değerleri [0, 1]
         aralığına hapseder.
  * encodeGender ve encodeLabel: Makineler metinlerden anlamaz. "Male/Female" bilgisini "1/0"
         gibi ikilik sisteme, şehir isimlerini ise hashCode() kullanarak sayısal ID'lere dönüştürür.

  ---

  1. Mimari Temeller: IClassifier.java ve BaseAlgorithm.java
  Projenin Soyutlama (Abstraction) ve Kalıtım (Inheritance) prensiplerinin uygulandığı yerdir.

* Hangi İsteri Karşılıyor?: Nesneye Yönelik Tasarım (Yönerge Madde 2.2, 2.3, 2.4).
* Kod Bloğu Analizi:
  * IClassifier Arayüzü: Tüm algoritmalar için train() ve predict() standartlarını belirler. Bu
         sayede GUI, hangi algoritmayı çalıştırdığını bilmeden aynı metotları çağırabilir
         (Polimorfizm).
  * BaseAlgorithm Abstract Sınıfı: calculateAccuracy() gibi her iki algoritma için de ortak olan
         mantığı barındırır. Bu, "Don't Repeat Yourself" (DRY) prensibinin bir gereğidir.

  ---

  1. Algoritma 1: KNNClassifier.java
  "Benzer insanlar benzer ürünleri alır" mantığının kodlanmış halidir.

* Hangi İsteri Karşılıyor?: K-En Yakın Komşu Algoritması (Yönerge Madde 4.1).
* Kod Bloğu Analizi:
  * train Metodu: KNN "Lazy Learner" olduğu için eğitim aşamasında sadece veriyi hafızaya
         kaydeder.
  * calculateEuclideanDistance: Öklid mesafesi formülü ile iki nokta arasındaki kuş uçuşu
         mesafeyi hesaplar.
  * PriorityQueue Kullanımı: 5000+ satırlık veride tüm mesafeleri hesaplayıp en küçük K tanesini
         seçmek için en verimli yoldur.
  * Majority Vote: En yakın K komşunun hangi kategoriye en çok oy verdiğini bir HashMap üzerinden
         sayarak nihai kararı verir.

  ---

  1. Algoritma 2: DecisionTreeClassifier.java
  Veriden otomatik kurallar çıkaran, mantıksal bir karar ağacı yapısıdır.

* Hangi İsteri Karşılıyor?: Karar Ağacı Algoritması (Yönerge Madde 4.2).
* Kod Bloğu Analizi:
  * Node İç Sınıfı: Ağacın her bir dalını (soru düğümü) veya yaprağını (sonuç düğümü) temsil
         eder.
  * buildTree (Recursive): Özyinelemeli bir yapıda, veriyi en iyi bölen özelliği bulana kadar
         dallanır.
  * Gini Impurity (Safsızlık): Veriyi bölerken karmaşıklığı en çok azaltan özelliği seçmek için
         kullanılan matematiksel kriterdir.
  * maxDepth Kontrolü: Ağacın sonsuza kadar büyümesini (ezberlemesini/overfitting) engelleyen bir
         "derinlik freni" mekanizmasıdır.

  ---

  1. Performans Ölçümü: Evaluator.java
  Bir mühendisin en önemli görevi ölçmektir; bu sınıf projenin bilimsel tarafını temsil eder.

* Hangi İsteri Karşılıyor?: Performans Ölçümü ve Doğruluk Testi (Yönerge Madde 5).
* Kod Bloğu Analizi:
  * Zaman ve Bellek Ölçümü: System.currentTimeMillis() ile ms hassasiyetinde hız, Runtime nesnesi
         ile KB cinsinden bellek kullanımı ölçülür.
  * Hata Matrisi (Confusion Matrix): Modelin "Gıda" alacak birini "Elektronik" olarak tahmin edip
         etmediğini detaylıca raporlar.
  * CategoryStat: Başarıyı sadece genel yüzde olarak değil, kategori bazlı olarak analiz eder.

  ---

  1. Kullanıcı Arayüzü: MainFrame.java
  Kullanıcının sistemle etkileşime girdiği, sonuçların görselleştiği merkezdir.

* Hangi İsteri Karşılıyor?: Model Çıktılarının Görüntülenmesi (Yönerge Madde 6).
* Kod Bloğu Analizi:
  * SwingWorker Kullanımı: Uzun süren eğitim ve tahmin işlemleri sırasında ekranın donmasını
         engeller, arka planda hesaplama yaparken UI'ı canlı tutar.
  * GraphPanel (Custom Bar Chart): Hazır kütüphane kullanmadan, Graphics2D ile barların ve
         yazıların koordinatlarını hesaplayarak dinamik başarı grafiklerini çizer.
  * JSplitPane Tasarımı: Tablo verisi ile grafiğin yan yana senkronize bir şekilde sunulmasını
         sağlar.

  ---

  1. Giriş Noktası: Main.java
  Uygulamanın hayat bulduğu ana sınıftır.

* Kod Bloğu Analizi:
  * SwingUtilities.invokeLater: Arayüzün thread-safe (iş parçacığı güvenli) bir şekilde
         başlatılmasını sağlar. Bu, Java Swing uygulamalarında profesyonel bir standarttır.

  ---
